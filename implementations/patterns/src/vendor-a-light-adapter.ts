import type { VendorALight } from '../../../shared/simulators/vendor-a-light.ts';
import type { Light } from './light.ts';

export class VendorALightAdapter implements Light {
  readonly #vendor: VendorALight;

  constructor(vendor: VendorALight) {
    this.#vendor = vendor;
  }

  get id(): string {
    return this.#vendor.id;
  }

  setPower(power: boolean): void {
    this.#vendor.setPower(power ? 'enabled' : 'disabled');
  }

  isOn(): boolean {
    return this.#vendor.readPower() === 'enabled';
  }
}
