/** Public boundary only: no implementations or automation policies here. */
export interface SetLightPower {
  type: 'set-light-power';
  deviceId: string;
  power: boolean;
}

export type HomeCommand = SetLightPower;

export type CommandResult =
  | { ok: true }
  | { ok: false; code: 'UNKNOWN_DEVICE'; message: string };

export interface LightSnapshot {
  id: string;
  kind: 'light';
  power: boolean;
}

export interface HomeSnapshot {
  devices: LightSnapshot[];
}

export interface SmartHome {
  execute(command: HomeCommand): CommandResult;
  getSnapshot(): HomeSnapshot;
}
