package ro.upb.sasps.smarthome.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ro.upb.sasps.smarthome.contracts.CommandResult;
import ro.upb.sasps.smarthome.contracts.ErrorCode;
import ro.upb.sasps.smarthome.contracts.LightSnapshot;
import ro.upb.sasps.smarthome.contracts.SetLightPower;

class MotionLightTest {
  private final PatternsHome home = new PatternsHome();

  @Test
  void motionTurnsOnTheLightOfTheRoom() {
    assertEquals(CommandResult.success(), home.reportMotion("living-motion", true));

    assertTrue(lightIsOn(home));
  }

  @Test
  void repeatedMotionDoesNotTriggerTheRuleAgain() {
    home.reportMotion("living-motion", true);
    home.execute(new SetLightPower("living-light", false));

    home.reportMotion("living-motion", true);

    assertFalse(lightIsOn(home));
  }

  @Test
  void noMotionLeavesTheLightOff() {
    home.reportMotion("living-motion", false);

    assertFalse(lightIsOn(home));
  }

  @Test
  void anUnknownSensorIsRejectedWithoutChanges() {
    CommandResult result = home.reportMotion("missing", true);

    assertEquals(ErrorCode.UNKNOWN_DEVICE, result.code());
    assertFalse(lightIsOn(home));
  }

  @Test
  void separateHomesDoNotShareEvents() {
    PatternsHome other = new PatternsHome();

    home.reportMotion("living-motion", true);

    assertFalse(lightIsOn(other));
  }

  private static boolean lightIsOn(PatternsHome home) {
    return home.getSnapshot().devices().stream()
        .filter(LightSnapshot.class::isInstance)
        .map(LightSnapshot.class::cast)
        .findFirst()
        .orElseThrow()
        .power();
  }
}
