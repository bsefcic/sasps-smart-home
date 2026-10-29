package ro.upb.sasps.smarthome.server;

import ro.upb.sasps.smarthome.contracts.HomeSnapshot;

public record ExecutionResponse(boolean ok, int executedCommands, HomeSnapshot snapshot) {}
