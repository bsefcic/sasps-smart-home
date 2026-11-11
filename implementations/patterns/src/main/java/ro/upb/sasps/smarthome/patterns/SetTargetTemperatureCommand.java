package ro.upb.sasps.smarthome.patterns;

import java.util.Objects;
import ro.upb.sasps.smarthome.contracts.ContractChecks;

public final class SetTargetTemperatureCommand implements Command {
  private final Thermostat thermostat;
  private final double temperature;

  public SetTargetTemperatureCommand(Thermostat thermostat, double temperature) {
    this.thermostat = Objects.requireNonNull(thermostat, "thermostat");
    ContractChecks.requireTemperature(temperature);
    this.temperature = temperature;
  }

  @Override
  public void execute() {
    thermostat.setTargetTemperature(temperature);
  }
}
