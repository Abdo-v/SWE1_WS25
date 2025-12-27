# Tests (src/test/java)

This folder mirrors the production package layout to keep responsibility boundaries clear.

## Structure
- `client.controller`: service-level behavior (status transitions, exception mapping, network/AI interactions)
- `client.controller.network`: network-side helpers and converters
- `client.exception`: exception semantics (fail-fast, context fields)
- `client.main`: startup argument parsing/validation
- `client.model`: domain invariants and core game state behavior
- `client.model.ai`: AI/pathfinding correctness + regressions
- `client.model.mapper`: map containers/generation/validation rules
- `client.view`: CLI output contracts (stdout vs stderr, ANSI-free, icon rendering)

## Conventions used
- Tests target one component per class (SRP) and keep fixtures minimal.
- Preconditions/invariants are asserted explicitly when they matter (fail-fast).
- Parameterized tests are used for equivalence partitions (e.g., corners/edges).
- Mockito is used where collaboration behavior matters (controller/network boundaries), while pure mapping/logic prefers real objects.

## Capturing CLI output
- View tests use `client.view.testsupport.StdIoCapture` to safely capture `System.out` and `System.err` as UTF-8.
- Stream routing is part of the contract: user-facing output should go to stdout; technical/errors should go to stderr.

## Notes
- Prefer assertions on stable contracts (e.g., rendered grid lines) over brittle full-output string matching.
