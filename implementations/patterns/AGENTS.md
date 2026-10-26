# Patterns implementation

Follow the root instructions. Use patterns where justified: Command for actions, Adapter for vendor APIs, Composite for scenes, Observer for sensor events, Strategy for temperature policies. Only Command, Adapter, and one light exist in the bootstrap.

Prefer small Java interfaces, explicit constructors, and immutable request/snapshot records. The public HomeCommand record hierarchy is a request format; executable Command objects are separate. Avoid global registries, singleton state, frameworks, and speculative abstraction. Do not import or depend on smart-home-baseline. Preserve public semantics and run the common acceptance tests from the repository root.
