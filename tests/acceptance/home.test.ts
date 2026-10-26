import assert from 'node:assert/strict';
import { describe, it } from 'node:test';
import { createHome as createBaseline } from '../../implementations/baseline/src/index.ts';
import { createHome as createPatterns } from '../../implementations/patterns/src/index.ts';

for (const [variant, createHome] of [
  ['baseline', createBaseline],
  ['patterns', createPatterns],
] as const) {
  describe(variant, () => {
    it('starts with an off light and applies on/off commands', () => {
      const home = createHome();
      assert.equal(home.getSnapshot().devices[0]?.power, false);
      assert.deepEqual(
        home.execute({
          type: 'set-light-power',
          deviceId: 'living-light',
          power: true,
        }),
        { ok: true },
      );
      assert.equal(home.getSnapshot().devices[0]?.power, true);
      home.execute({
        type: 'set-light-power',
        deviceId: 'living-light',
        power: false,
      });
      assert.equal(home.getSnapshot().devices[0]?.power, false);
    });

    it('rejects an unknown device without changing the home', () => {
      const home = createHome();
      const before = home.getSnapshot();
      assert.deepEqual(
        home.execute({
          type: 'set-light-power',
          deviceId: 'missing',
          power: true,
        }),
        {
          ok: false,
          code: 'UNKNOWN_DEVICE',
          message: 'Unknown device: missing',
        },
      );
      assert.deepEqual(home.getSnapshot(), before);
    });

    it('does not leak mutable state through snapshots or between instances', () => {
      const home = createHome();
      const snapshot = home.getSnapshot();
      snapshot.devices[0]!.power = true;
      assert.equal(home.getSnapshot().devices[0]?.power, false);
      home.execute({
        type: 'set-light-power',
        deviceId: 'living-light',
        power: true,
      });
      assert.equal(createHome().getSnapshot().devices[0]?.power, false);
    });
  });
}
