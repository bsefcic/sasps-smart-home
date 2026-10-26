import type { SmartHome } from '../../../shared/contracts/index.ts';
import { VendorALight } from '../../../shared/simulators/vendor-a-light.ts';
import { SetLightPowerCommand } from './command.ts';
import type { Light } from './light.ts';
import { VendorALightAdapter } from './vendor-a-light-adapter.ts';

export function createHome(): SmartHome {
  const light = new VendorALightAdapter(new VendorALight('living-light'));
  const lights = new Map<string, Light>([[light.id, light]]);

  return {
    execute(request) {
      const receiver = lights.get(request.deviceId);
      if (!receiver) {
        return {
          ok: false,
          code: 'UNKNOWN_DEVICE',
          message: `Unknown device: ${request.deviceId}`,
        };
      }
      const command = new SetLightPowerCommand(receiver, request.power);
      command.execute();
      return { ok: true };
    },
    getSnapshot() {
      return {
        devices: [...lights.values()].map((device) => ({
          id: device.id,
          kind: 'light' as const,
          power: device.isOn(),
        })),
      };
    },
  };
}
