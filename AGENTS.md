# Instructions for contributors and coding agents

## Purpose and boundaries

This repository compares two functionally equivalent TypeScript smart-home implementations. Read `docs/scope.md` and the issue's acceptance criteria before changing code. `docs/architecture.md` explains the comparison boundaries.

- Node.js 24, npm, strict TypeScript, ESM. Import local TypeScript files with `.ts` extensions.
- Node runs erasable TypeScript directly. Do not use enums, constructor parameter properties, or other syntax requiring transformation.
- Public contracts and neutral vendor simulators live in `shared/`. Do not put commands, scene execution, automation policies, or implementation-specific logic there.
- Never import one implementation from the other. Only the test runner and demo/evaluation entry points may load both.
- Do not intentionally break or slow down baseline. Do not introduce patterns just to increase a count.
- Preserve the same semantics, fixtures, errors, and workloads across variants.

## Workflow

1. Read `CONTRIBUTING.md`, applicable nested `AGENTS.md`, and the issue. Inspect relevant existing files.
2. State a brief plan. Resolve ambiguity using scope/contracts; do not silently invent requirements.
3. Make the smallest coherent change. Avoid unrelated refactors and new dependencies without justification.
4. Add meaningful tests for new behavior. Update shared acceptance tests for public behavior; do not weaken tests to get a pass.
5. Run `npm run check` and the affected demos. Report exact commands and any failures honestly.
6. Update affected docs and summarize behavior, tradeoffs, and validation in the PR.

`npm ci` installs dependencies; `npm run format` formats; `npm run check` runs typecheck, lint, tests, and format checks. `npm run demo -- baseline` and `npm run demo -- patterns` run the starter.

Do not commit credentials, node_modules, generated workspaces, or fabricated measurements. Do not merge PRs or modify repository settings as part of a coding issue. The separate, controlled AI experiment uses an isolated snapshot and neutral instructions; see `docs/evaluation.md`.
