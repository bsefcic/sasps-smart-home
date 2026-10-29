package ro.upb.sasps.smarthome.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ContractV1Test {
  @ParameterizedTest
  @ValueSource(doubles = {5.0, 20.0, 22.1, 35.0})
  void acceptsExactTemperatureBoundsAndTenths(double temperature) {
    assertEquals(
        temperature, new SetTargetTemperature("living-thermostat", temperature).temperature());
  }

  @ParameterizedTest
  @ValueSource(
      doubles = {4.9, 35.1, 22.25, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  void rejectsInvalidTemperature(double temperature) {
    assertThrows(
        IllegalArgumentException.class,
        () -> new SetTargetTemperature("living-thermostat", temperature));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "Living-Light", "bad id", "../light"})
  void rejectsInvalidIds(String id) {
    assertThrows(IllegalArgumentException.class, () -> new SetLightPower(id, true));
  }

  @Test
  void scenesDefensivelyCopyAndHaveFixedBounds() {
    List<HomeCommand> commands = new ArrayList<>();
    commands.add(new SetLightPower("living-light", false));
    SceneRequest scene = new SceneRequest("leave-home", commands);
    commands.clear();
    assertEquals(1, scene.commands().size());
    assertThrows(UnsupportedOperationException.class, () -> scene.commands().clear());
    assertThrows(IllegalArgumentException.class, () -> new SceneRequest("empty", List.of()));
    assertThrows(IllegalArgumentException.class, () -> new SceneRequest(" ", scene.commands()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new SceneRequest("large", Collections.nCopies(51, scene.commands().get(0))));
    assertEquals(
        50,
        new SceneRequest("maximum", Collections.nCopies(50, scene.commands().get(0)))
            .commands()
            .size());
  }
}
