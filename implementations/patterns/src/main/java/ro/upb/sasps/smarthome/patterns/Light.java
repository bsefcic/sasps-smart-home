package ro.upb.sasps.smarthome.patterns;

public interface Light {
  String id();

  void setPower(boolean power);

  boolean isOn();
}
