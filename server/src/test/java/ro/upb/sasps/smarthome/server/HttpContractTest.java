package ro.upb.sasps.smarthome.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

abstract class HttpContractTest {
  @LocalServerPort int port;
  @Autowired ObjectMapper mapper;
  private final HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

  @BeforeEach
  void resetDevices() throws Exception {
    assertEquals(200, post("/commands", light(false)).statusCode());
    assertEquals(200, post("/commands", temperature("living-thermostat", 20)).statusCode());
  }

  @Test
  void exposesHealthAndAnImmutableSnapshot() throws Exception {
    assertEquals("UP", json(get("/health")).get("status").textValue());
    JsonNode devices = json(get("/home")).get("devices");
    assertEquals("living-light", devices.get(0).get("id").textValue());
    assertEquals("light", devices.get(0).get("kind").textValue());
    assertFalse(devices.get(0).get("power").booleanValue());
    assertEquals(2, devices.size());
    assertEquals("living-thermostat", devices.get(1).get("id").textValue());
    assertEquals("thermostat", devices.get(1).get("kind").textValue());
    assertEquals(20.0, devices.get(1).get("targetTemperature").doubleValue());
    assertFalse(devices.get(0).has("targetTemperature"));
    assertFalse(devices.get(1).has("power"));
  }

  @Test
  void keepsStateAcrossRequestsAndCapturesCommandSnapshot() throws Exception {
    HttpResponse<String> response = post("/commands", light(true));
    assertEquals(200, response.statusCode());
    JsonNode result = json(response);
    assertTrue(result.get("ok").booleanValue());
    assertEquals(1, result.get("executedCommands").intValue());
    assertTrue(result.get("snapshot").get("devices").get(0).get("power").booleanValue());
    assertTrue(json(get("/home")).get("devices").get(0).get("power").booleanValue());
  }

  @Test
  void unknownDeviceIs404AndDoesNotMutate() throws Exception {
    String before = get("/home").body();
    HttpResponse<String> response =
        post("/commands", "{\"type\":\"set-light-power\",\"deviceId\":\"missing\",\"power\":true}");
    assertError(response, 404, "UNKNOWN_DEVICE");
    assertEquals(before, get("/home").body());
    assertNull(json(response).get("error").get("commandIndex"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "null",
        "[]",
        "{}",
        "{",
        "{\"type\":\"set-light-power\",\"deviceId\":\"living-light\"}",
        "{\"type\":\"set-light-power\",\"deviceId\":\"living-light\",\"power\":\"true\"}",
        "{\"type\":\"set-light-power\",\"deviceId\":\"living-light\",\"power\":null}",
        "{\"type\":\"set-light-power\",\"deviceId\":\"living-light\",\"power\":true,\"extra\":1}",
        "{\"type\":\"set-light-power\",\"deviceId\":\"living-light\",\"power\":true,\"power\":false}",
        "{\"type\":\"set-light-power\",\"deviceId\":\"living-light\",\"power\":true} {}"
      })
  void rejectsMalformedOrWrongShapeWithoutMutating(String payload) throws Exception {
    String before = get("/home").body();
    assertError(post("/commands", payload), 400, "INVALID_REQUEST");
    assertEquals(before, get("/home").body());
  }

  @Test
  void rejectsUnknownCommandType() throws Exception {
    assertError(
        post("/commands", "{\"type\":\"fly\",\"deviceId\":\"living-light\"}"),
        400,
        "INVALID_COMMAND");
  }

  @ParameterizedTest
  @ValueSource(strings = {"100", "4.9", "35.1", "22.25", "1e309", "-1e309"})
  void invalidTemperatureIs422(String temperature) throws Exception {
    String before = get("/home").body();
    assertError(
        post(
            "/commands",
            "{\"type\":\"set-target-temperature\",\"deviceId\":\"living-thermostat\",\"temperature\":"
                + temperature
                + "}"),
        422,
        "INVALID_TEMPERATURE");
    assertEquals(before, get("/home").body());
  }

  @ParameterizedTest
  @ValueSource(strings = {"NaN", "Infinity", "-Infinity"})
  void rejectsNonJsonNumbersAndNumericStrings(String token) throws Exception {
    String before = get("/home").body();
    for (String value : List.of(token, "\"" + token + "\"")) {
      String payload =
          "{\"type\":\"set-target-temperature\",\"deviceId\":\"living-thermostat\",\"temperature\":"
              + value
              + "}";
      assertError(post("/commands", payload), 400, "INVALID_REQUEST");
      assertEquals(before, get("/home").body());
    }
  }

  @ParameterizedTest
  @ValueSource(doubles = {5.0, 35.0, 22.1})
  void temperaturePersistsAcrossRequests(double value) throws Exception {
    for (int repeat = 0; repeat < 2; repeat++) {
      HttpResponse<String> response = post("/commands", temperature("living-thermostat", value));
      assertEquals(200, response.statusCode());
      assertTrue(json(response).get("ok").booleanValue());
      assertEquals(1, json(response).get("executedCommands").intValue());
      assertEquals(
          value,
          json(response)
              .get("snapshot")
              .get("devices")
              .get(1)
              .get("targetTemperature")
              .doubleValue());
      assertEquals(
          value, json(get("/home")).get("devices").get(1).get("targetTemperature").doubleValue());
    }
  }

  @Test
  void inventoryErrorsDoNotMutateState() throws Exception {
    post("/commands", light(true));
    post("/commands", temperature("living-thermostat", 22.1));
    String before = get("/home").body();
    assertError(post("/commands", temperature("missing", 22)), 404, "UNKNOWN_DEVICE");
    assertEquals(before, get("/home").body());
    assertError(post("/commands", temperature("living-light", 22)), 422, "INCOMPATIBLE_DEVICE");
    assertEquals(before, get("/home").body());
    assertError(
        post("/commands", light(true).replace("living-light", "living-thermostat")),
        422,
        "INCOMPATIBLE_DEVICE");
    assertEquals(before, get("/home").body());
  }

  @Test
  void executesMixedScenesInOrderAndReturnsFinalSnapshot() throws Exception {
    String payload =
        "{\"name\":\"leave-home\",\"commands\":["
            + light(true)
            + ","
            + temperature("living-thermostat", 22.1)
            + ","
            + light(false)
            + ","
            + temperature("living-thermostat", 18)
            + "]}";
    HttpResponse<String> response = post("/scenes/execute", payload);
    assertEquals(200, response.statusCode());
    assertTrue(json(response).get("ok").booleanValue());
    assertEquals(4, json(response).get("executedCommands").intValue());
    assertEquals(json(get("/home")), json(response).get("snapshot"));
    assertFalse(json(response).get("snapshot").get("devices").get(0).get("power").booleanValue());
    assertEquals(
        18,
        json(response)
            .get("snapshot")
            .get("devices")
            .get(1)
            .get("targetTemperature")
            .doubleValue());
  }

  @Test
  void sceneInventoryErrorsAreIndexedAndHaveNoEffects() throws Exception {
    String[] invalid = {
      light(true).replace("living-light", "missing"),
      temperature("missing", 18),
      light(true).replace("living-light", "living-thermostat"),
      temperature("living-light", 18)
    };
    for (int kind = 0; kind < invalid.length; kind++) {
      for (int index = 0; index < 3; index++) {
        List<String> commands =
            new java.util.ArrayList<>(List.of(light(true), temperature("living-thermostat", 18)));
        commands.add(index, invalid[kind]);
        String before = get("/home").body();
        HttpResponse<String> response =
            post(
                "/scenes/execute",
                "{\"name\":\"invalid\",\"commands\":[" + String.join(",", commands) + "]}");
        assertError(
            response, kind < 2 ? 404 : 422, kind < 2 ? "UNKNOWN_DEVICE" : "INCOMPATIBLE_DEVICE");
        assertEquals(index, json(response).get("error").get("commandIndex").intValue());
        assertFalse(json(response).has("snapshot"));
        assertFalse(json(response).has("executedCommands"));
        assertEquals(before, get("/home").body());
      }
    }
  }

  @Test
  void acceptsOneAndFiftyCommandsButRejectsFiftyOne() throws Exception {
    for (int count : new int[] {1, 50, 51}) {
      String before = get("/home").body();
      String commands = String.join(",", java.util.Collections.nCopies(count, light(true)));
      HttpResponse<String> response =
          post("/scenes/execute", "{\"name\":\"boundary\",\"commands\":[" + commands + "]}");
      if (count <= 50) {
        assertEquals(200, response.statusCode());
        assertEquals(count, json(response).get("executedCommands").intValue());
      } else {
        assertError(response, 400, "INVALID_SCENE");
        assertEquals(before, get("/home").body());
      }
    }
  }

  @Test
  void validatesEverySceneInputBeforeCallingEngine() throws Exception {
    String before = get("/home").body();
    String scene =
        "{\"name\":\"leave-home\",\"commands\":["
            + light(true)
            + ",{\"type\":\"set-target-temperature\",\"deviceId\":\"living-thermostat\",\"temperature\":100}]}";
    HttpResponse<String> response = post("/scenes/execute", scene);
    assertError(response, 422, "INVALID_TEMPERATURE");
    assertEquals(1, json(response).get("error").get("commandIndex").intValue());
    assertEquals(before, get("/home").body());
    assertError(
        post("/scenes/execute", "{\"name\":\"empty\",\"commands\":[]}"), 400, "INVALID_SCENE");
  }

  @Test
  void serializesConcurrentCommandsWithTheirOwnResultSnapshot() {
    List<CompletableFuture<Void>> futures =
        IntStream.range(0, 24)
            .mapToObj(
                index -> {
                  boolean power = index % 2 == 0;
                  return client
                      .sendAsync(
                          request("/commands", light(power)), HttpResponse.BodyHandlers.ofString())
                      .thenAccept(
                          response -> {
                            try {
                              assertEquals(200, response.statusCode());
                              assertEquals(
                                  power,
                                  json(response)
                                      .get("snapshot")
                                      .get("devices")
                                      .get(0)
                                      .get("power")
                                      .booleanValue());
                            } catch (Exception exception) {
                              throw new IllegalStateException(exception);
                            }
                          });
                })
            .toList();
    CompletableFuture.allOf(futures.toArray(CompletableFuture<?>[]::new)).join();
  }

  @Test
  void allowsReactDevelopmentOriginOnlyWhenConfigured() throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(uri("/commands"))
            .header("Origin", "http://localhost:5173")
            .header("Access-Control-Request-Method", "POST")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
            .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    assertEquals(
        "http://localhost:5173",
        response.headers().firstValue("Access-Control-Allow-Origin").orElseThrow());
  }

  private static String temperature(String id, double value) {
    return "{\"type\":\"set-target-temperature\",\"deviceId\":\""
        + id
        + "\",\"temperature\":"
        + value
        + "}";
  }

  private static String light(boolean power) {
    return "{\"type\":\"set-light-power\",\"deviceId\":\"living-light\",\"power\":" + power + "}";
  }

  private URI uri(String path) {
    return URI.create("http://127.0.0.1:" + port + "/api/v1" + path);
  }

  private HttpRequest request(String path, String body) {
    return HttpRequest.newBuilder(uri(path))
        .timeout(Duration.ofSeconds(10))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
  }

  private HttpResponse<String> post(String path, String body) throws Exception {
    return client.send(request(path, body), HttpResponse.BodyHandlers.ofString());
  }

  private HttpResponse<String> get(String path) throws Exception {
    return client.send(
        HttpRequest.newBuilder(uri(path)).timeout(Duration.ofSeconds(10)).GET().build(),
        HttpResponse.BodyHandlers.ofString());
  }

  private JsonNode json(HttpResponse<String> response) throws Exception {
    return mapper.readTree(response.body());
  }

  private void assertError(HttpResponse<String> response, int status, String code)
      throws Exception {
    assertEquals(status, response.statusCode(), response.body());
    JsonNode body = json(response);
    assertFalse(body.get("ok").booleanValue());
    assertEquals(code, body.get("error").get("code").textValue());
  }
}
