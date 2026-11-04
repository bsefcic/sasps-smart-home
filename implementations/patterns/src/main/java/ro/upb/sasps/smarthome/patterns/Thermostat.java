package ro.upb.sasps.smarthome.patterns;

/** Application-facing thermostat API, with temperatures expressed in degrees Celsius. */
public interface Thermostat {
  String id();

  void setTargetTemperature(double value);

  double targetTemperature();
}
