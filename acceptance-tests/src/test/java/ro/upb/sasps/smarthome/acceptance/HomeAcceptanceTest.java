package ro.upb.sasps.smarthome.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ro.upb.sasps.smarthome.baseline.BaselineHome;
import ro.upb.sasps.smarthome.contracts.CommandResult;
import ro.upb.sasps.smarthome.contracts.HomeSnapshot;
import ro.upb.sasps.smarthome.contracts.SetLightPower;
import ro.upb.sasps.smarthome.contracts.SmartHome;
import ro.upb.sasps.smarthome.patterns.PatternsHome;

class HomeAcceptanceTest {
  static Stream<Arguments> implementations() {
    return Stream.of(
        Arguments.of("baseline", (Supplier<SmartHome>) BaselineHome::new),
        Arguments.of("patterns", (Supplier<SmartHome>) PatternsHome::new));
  }

  @ParameterizedTest(name = "{0}: on/off")
  @MethodSource("implementations")
  void appliesPowerCommands(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    assertFalse(home.getSnapshot().devices().get(0).power());
    assertEquals(CommandResult.success(), home.execute(new SetLightPower("living-light", true)));
    assertTrue(home.getSnapshot().devices().get(0).power());
    home.execute(new SetLightPower("living-light", false));
    assertFalse(home.getSnapshot().devices().get(0).power());
  }

  @ParameterizedTest(name = "{0}: unknown device")
  @MethodSource("implementations")
  void rejectsUnknownDeviceWithoutSideEffects(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    HomeSnapshot before = home.getSnapshot();
    assertEquals(
        CommandResult.unknownDevice("missing"), home.execute(new SetLightPower("missing", true)));
    assertEquals(before, home.getSnapshot());
  }

  @ParameterizedTest(name = "{0}: isolated state")
  @MethodSource("implementations")
  void protectsSnapshotsAndInstances(String variant, Supplier<SmartHome> factory) {
    SmartHome home = factory.get();
    HomeSnapshot before = home.getSnapshot();
    assertThrows(UnsupportedOperationException.class, () -> before.devices().clear());
    home.execute(new SetLightPower("living-light", true));
    assertFalse(before.devices().get(0).power());
    assertFalse(factory.get().getSnapshot().devices().get(0).power());
  }
}
