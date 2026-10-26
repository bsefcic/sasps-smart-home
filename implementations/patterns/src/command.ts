import type { Light } from './light.ts';

export interface Command {
  execute(): void;
}

export class SetLightPowerCommand implements Command {
  readonly #light: Light;
  readonly #power: boolean;

  constructor(light: Light, power: boolean) {
    this.#light = light;
    this.#power = power;
  }

  execute(): void {
    this.#light.setPower(this.#power);
  }
}
