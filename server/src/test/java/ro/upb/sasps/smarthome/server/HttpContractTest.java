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
  void resetLight() throws Exception {
    assertEquals(200, post("/commands", light(false)).statusCode());
  }

  @Test
  void exposesHealthAndAnImmutableSnapshot() throws Exception {
    assertEquals("UP", json(get("/health")).get("status").textValue());
    JsonNode devices = json(get("/home")).get("devices");
    assertEquals("living-light", devices.get(0).get("id").textValue());
    assertEquals("light", devices.get(0).get("kind").textValue());
    assertFalse(devices.get(0).get("power").booleanValue());
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
  @ValueSource(strings = {"100", "4.9", "22.25"})
  void invalidTemperatureIs422(String temperature) throws Exception {
    assertError(
        post(
            "/commands",
            "{\"type\":\"set-target-temperature\",\"deviceId\":\"living-thermostat\",\"temperature\":"
                + temperature
                + "}"),
        422,
        "INVALID_TEMPERATURE");
  }

  @Test
  void validFutureFeaturesAreExplicitlyNotImplementedInStarter() throws Exception {
    assertError(
        post(
            "/commands",
            "{\"type\":\"set-target-temperature\",\"deviceId\":\"living-thermostat\",\"temperature\":22}"),
        501,
        "NOT_IMPLEMENTED");
    assertError(
        post("/scenes/execute", "{\"name\":\"leave-home\",\"commands\":[" + light(false) + "]}"),
        501,
        "NOT_IMPLEMENTED");
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
