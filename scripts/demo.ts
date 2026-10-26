import { createHome as createBaseline } from '../implementations/baseline/src/index.ts';
import { createHome as createPatterns } from '../implementations/patterns/src/index.ts';

const variant = process.argv[2] ?? 'baseline';
if (variant !== 'baseline' && variant !== 'patterns') {
  console.error('Usage: npm run demo -- baseline|patterns');
  process.exitCode = 1;
} else {
  const home = variant === 'baseline' ? createBaseline() : createPatterns();
  const result = home.execute({
    type: 'set-light-power',
    deviceId: 'living-light',
    power: true,
  });
  console.log(
    JSON.stringify({ result, snapshot: home.getSnapshot() }, null, 2),
  );
}
