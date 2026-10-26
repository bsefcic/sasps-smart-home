import type { SmartHome } from '../../../shared/contracts/index.ts';
import { VendorALight } from '../../../shared/simulators/vendor-a-light.ts';

/** Direct, centralized implementation. Correctness is not intentionally weakened. */
export function createHome(): SmartHome {
  const lights = new Map([['living-light', new VendorALight('living-light')]]);

  return {
    execute(command) {
      switch (command.type) {
        case 'set-light-power': {
          const light = lights.get(command.deviceId);
          if (!light) {
            return {
              ok: false,
              code: 'UNKNOWN_DEVICE',
              message: `Unknown device: ${command.deviceId}`,
            };
          }
          light.setPower(command.power ? 'enabled' : 'disabled');
          return { ok: true };
        }
      }
    },
    getSnapshot() {
      return {
        devices: [...lights.values()].map((light) => ({
          id: light.id,
          kind: 'light' as const,
          power: light.readPower() === 'enabled',
        })),
      };
    },
  };
}
