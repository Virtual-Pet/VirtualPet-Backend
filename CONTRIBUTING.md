# Contributing

Thanks for taking the time to contribute.

## Branching model — Gitflow

This repository follows **[Gitflow](https://nvie.com/posts/a-successful-git-branching-model/)**. There are two long-lived branches and
three categories of short-lived branches.

### Long-lived branches

| Branch    | Purpose                                                        |
| --------- | -------------------------------------------------------------- |
| `main`    | Production. Every commit is releasable and tagged with semver. |
| `develop` | Integration branch for the next release. Always green in CI.   |

Direct pushes to `main` and `develop` are not allowed — both are protected
and only receive merges via PR.

### Short-lived branches

| Type    | Branches from | Merges back into         | Naming                 |
| ------- | ------------- | ------------------------ | ---------------------- |
| Feature | `develop`     | `develop`                | `feature/<short-desc>` |
| Release | `develop`     | `main` **and** `develop` | `release/<x.y.z>`      |
| Hotfix  | `main`        | `main` **and** `develop` | `hotfix/<x.y.z>`       |
| Bugfix  | `develop`     | `develop`                | `bugfix/<short-desc>`  |

Rules:

- **Features** never branch from `main`. Cut from `develop`, PR back to `develop`.
- **Releases** stabilise what's on `develop`: only bugfixes, version bumps and
  changelog updates. When merged to `main` they are tagged `vX.Y.Z`.
- **Hotfixes** are the only branches cut from `main`. They patch production
  and must be merged into **both** `main` and `develop` to avoid regressions.
- Keep branches short-lived (< 1 week ideally). Rebase on the source branch
  before opening a PR to keep history linear.

### Typical flow

```sh
# Start a feature
git checkout develop && git pull
git checkout -b feature/customer-pagination

# ... commits ...

git push -u origin feature/customer-pagination
# Open PR → develop
```

```sh
# Cut a release
git checkout develop && git pull
git checkout -b release/1.4.0
# bump version, update CHANGELOG, last-minute fixes
# PR → main, then back-merge main → develop
git tag -a v1.4.0 -m "Release 1.4.0"
git push origin v1.4.0
```

```sh
# Patch production
git checkout main && git pull
git checkout -b hotfix/1.4.1
# fix + bump patch version
# PR → main (release), then PR → develop (forward-port)
```

## Commit style

- One concern per commit. Keep diffs focused.
- **Conventional Commits** strongly encouraged:
  `feat:`, `fix:`, `refactor:`, `chore:`, `docs:`, `test:`, `ci:`, `build:`,
  `perf:`. Breaking changes use `!`: `feat!: drop legacy auth endpoint`.

## Before opening a PR

```sh
./mvnw spotless:apply
./mvnw verify
```

Then push and open a PR using the template. Link the related issue.

## Code style

- Java formatting is enforced by **Spotless + google-java-format**.
- CI fails on `spotless:check`. Run `./mvnw spotless:apply` before pushing.
- Indentation, line endings and charset are governed by `.editorconfig`.

## Database changes

- Schema changes go through **Flyway** under
  `src/main/resources/db/migration/`.
- Versioned migrations (`V*`) are **immutable** once merged to `develop`.
  Need to change one? Add a new `V*` on top.
- Repeatable migrations (`R__*`) may be edited but must remain idempotent.

## Tests

- Unit tests live under `src/test/java`.
- Tests must pass against a real Postgres (Testcontainers) once that wiring
  lands; do not rely on H2-only behaviour.

## Reporting issues

Use the issue templates. Security issues: see [`SECURITY.md`](SECURITY.md).
