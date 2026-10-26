# Patterns implementation

Follow the root instructions. Use patterns only where the issue and problem justify them: Command (actions), Adapter (vendor APIs), Composite (scenes), Observer (sensor events), Strategy (temperature policies). The bootstrap implements only one light, Adapter, and Command; the others are planned.

Prefer explicit dependencies supplied through constructors/functions. Keep abstractions small. Do not add singleton registries, a framework, a database, or speculative patterns. No imports from `implementations/baseline`. Preserve the common contract and semantics.
