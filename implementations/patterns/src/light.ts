export interface Light {
  readonly id: string;
  setPower(power: boolean): void;
  isOn(): boolean;
}
