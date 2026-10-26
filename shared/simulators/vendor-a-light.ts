/** Neutral vendor simulator; API deliberately differs from the public contract. */
export class VendorALight {
  readonly id: string;
  #enabled = false;

  constructor(id: string) {
    this.id = id;
  }

  setPower(value: 'enabled' | 'disabled'): void {
    this.#enabled = value === 'enabled';
  }

  readPower(): 'enabled' | 'disabled' {
    return this.#enabled ? 'enabled' : 'disabled';
  }
}
