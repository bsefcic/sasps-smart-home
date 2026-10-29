package ro.upb.sasps.smarthome.patterns;

import java.util.Objects;

public final class SetLightPowerCommand implements Command {
  private final Light light;
  private final boolean power;

  public SetLightPowerCommand(Light light, boolean power) {
    this.light = Objects.requireNonNull(light, "light");
    this.power = power;
  }

  @Override
  public void execute() {
    light.setPower(power);
  }
}
