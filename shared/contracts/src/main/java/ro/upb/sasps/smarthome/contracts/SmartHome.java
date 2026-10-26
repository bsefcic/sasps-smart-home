package ro.upb.sasps.smarthome.contracts;

public interface SmartHome {
  CommandResult execute(HomeCommand command);

  HomeSnapshot getSnapshot();
}
