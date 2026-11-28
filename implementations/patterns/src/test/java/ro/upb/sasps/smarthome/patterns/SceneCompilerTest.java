package ro.upb.sasps.smarthome.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import ro.upb.sasps.smarthome.contracts.CommandResult;
import ro.upb.sasps.smarthome.contracts.HomeCommand;
import ro.upb.sasps.smarthome.contracts.SceneEntry;
import ro.upb.sasps.smarthome.contracts.SceneReference;
import ro.upb.sasps.smarthome.contracts.SetTargetTemperature;

class SceneCompilerTest {
  private final List<Double> effects = new ArrayList<>();

  private SceneCompiler compiler(Map<String, List<SceneEntry>> scenes) {
    return new SceneCompiler(
        scenes,
        command ->
            ((SetTargetTemperature) command).deviceId().equals("missing")
                ? CommandResult.unknownDevice("missing")
                : CommandResult.success(),
        command -> () -> effects.add(((SetTargetTemperature) command).temperature()));
  }

  private static HomeCommand target(String id, double temperature) {
    return new SetTargetTemperature(id, temperature);
  }

  @Test
  void compilesAReusedSubsceneOnceForEveryReferenceWithoutEffects() {
    SceneCompiler compiler = compiler(Map.of("warm", List.of(target("t", 21), target("t", 22))));

    CommandResult result =
        compiler.compile(
            List.of(new SceneReference("warm"), target("t", 18), new SceneReference("warm")));

    assertEquals(CommandResult.success(), result);
    assertEquals(5, compiler.commandCount());
    assertEquals(List.of(), effects);
    compiler.scene().execute();
    assertEquals(List.of(21.0, 22.0, 18.0, 21.0, 22.0), effects);
  }

  @Test
  void keepsTheFirstInventoryErrorUntilTraversalSucceeds() {
    SceneCompiler compiler = compiler(Map.of("warm", List.of(target("missing", 21))));

    CommandResult result =
        compiler.compile(
            List.of(target("t", 18), new SceneReference("warm"), target("missing", 5)));

    assertEquals(CommandResult.unknownDevice("missing").atPath(List.of(1, 0)), result);
    assertThrows(IllegalStateException.class, compiler::scene);
    assertEquals(List.of(), effects);
  }

  @Test
  void handlesOneRequest() {
    SceneCompiler compiler = compiler(Map.of());
    compiler.compile(List.of(target("t", 18)));

    assertThrows(IllegalStateException.class, () -> compiler.compile(List.of(target("t", 18))));
  }
}
