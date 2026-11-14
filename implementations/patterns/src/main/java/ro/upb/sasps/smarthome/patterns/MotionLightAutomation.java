package ro.upb.sasps.smarthome.patterns;

import java.util.Objects;

public final class MotionLightAutomation implements MotionListener {
  private final Light light;

  public MotionLightAutomation(Light light) {
    this.light = Objects.requireNonNull(light, "light");
  }

  @Override
  public void onMotionDetected(String sensorId) {
    new SetLightPowerCommand(light, true).execute();
  }
}
