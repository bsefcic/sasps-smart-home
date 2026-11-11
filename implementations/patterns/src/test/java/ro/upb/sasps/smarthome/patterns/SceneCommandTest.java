package ro.upb.sasps.smarthome.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import ro.upb.sasps.smarthome.simulators.VendorALight;

class SceneCommandTest {
  @Test
  void constructionHasNoEffectsAndExecutionPreservesOrderAndRepetitions() {
    List<Integer> effects = new ArrayList<>();
    Command first = () -> effects.add(1);
    Command second = () -> effects.add(2);
    SceneCommand scene = new SceneCommand(List.of(first, second, first));

    assertEquals(List.of(), effects);
    scene.execute();
    assertEquals(List.of(1, 2, 1), effects);
  }

  @Test
  void lastSettingOfTheSameDeviceWins() {
    VendorALight vendor = new VendorALight("living-light");
    Light light = new VendorALightAdapter(vendor);
    SceneCommand scene =
        new SceneCommand(
            List.of(new SetLightPowerCommand(light, true), new SetLightPowerCommand(light, false)));

    scene.execute();

    assertEquals(VendorALight.Power.DISABLED, vendor.readPower());
  }

  @Test
  void copiesTheSuppliedListBeforeExecution() {
    List<Integer> effects = new ArrayList<>();
    List<Command> commands = new ArrayList<>();
    commands.add(() -> effects.add(1));
    SceneCommand scene = new SceneCommand(commands);
    commands.clear();
    commands.add(() -> effects.add(2));

    scene.execute();

    assertEquals(List.of(1), effects);
  }

  @Test
  void rejectsNullListAndNullElementsWithoutExecutingAnything() {
    List<Integer> effects = new ArrayList<>();
    Command command = () -> effects.add(1);

    assertThrows(NullPointerException.class, () -> new SceneCommand(null));
    assertThrows(NullPointerException.class, () -> new SceneCommand(Arrays.asList(command, null)));
    assertEquals(List.of(), effects);
  }
}
