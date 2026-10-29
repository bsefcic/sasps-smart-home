# Instructions for contributors and coding agents

Java 17+, Maven Wrapper 3.9.11. Spring Boot is used in `server` ONLY. Read `docs/contracts-v1.md`, `docs/tasks-sprint-1.md`, the assigned issue, and applicable nested instructions before editing. Contract v1 is decided; do not redesign fields/endpoints/inventory or ask colleagues to choose them.

## Boundaries

- Plain Java modules: shared/contracts (immutable DTOs/interfaces), shared/simulators (neutral vendor APIs), implementations/baseline and implementations/patterns (independent control engines).
- Neither implementation depends on the other, Spring, Jackson or the web server. No compared orchestration in shared modules.
- server owns HTTP parsing, transport error mapping, Spring DI and one synchronized session per process. Both engines use the exact same web infrastructure.
- app is the optional legacy CLI demo; acceptance-tests runs the same parameterized suite on both engines. A React client may call the API later; do not add frontend now.
- Use Java 17 standard features, explicit dependencies and no Lombok/global mutable registries. Do not deliberately break or slow baseline, force extra patterns, or implement features reserved for AI evaluation.

## Workflow

1. Inspect the code and the fixed contract. State a short plan and keep the change within the issue.
2. Implement requested behavior in both variants when applicable. Public validation shape/ranges are fixed; engine orchestration stays separate.
3. Add meaningful JUnit tests. Do not weaken acceptance tests to hide differences.
4. Format: `./mvnw spotless:apply`. Verify: `./mvnw clean verify`. Windows: `.\mvnw.cmd` with the same goals.
5. Run the relevant HTTP examples from `docs/http/smart-home.http`. Start the packaged server with `java -jar server/target/smart-home-server.jar`. A second process can use `--smart-home.variant=baseline --server.port=8081`.
6. Update affected docs and report exact checks in the PR. At least two colleagues other than the author must approve; Java 17 and 21 CI checks must pass. Do not merge or change repository settings as part of a coding issue.

Never commit target directories, secrets, generated experiment workspaces or fabricated results. Controlled AI evaluation uses neutral isolated workspaces, not these development instructions. See docs/evaluation.md. Draft code must clearly distinguish implemented behavior from contract targets; do not silently claim unimplemented features work.
