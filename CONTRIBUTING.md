# Contributing

Kiri is a personal project that happens to be open source, so the bar here is "does it fit the
existing design" rather than a formal process. Issues and pull requests are welcome.

## Before you start

For anything larger than a bug fix, open an issue first. The agent's design has a fairly specific
shape — frames in, tool calls out — and a change that doesn't fit it is easier to discuss before
it's written than after.

## Setting up

See [Getting started](README.md#getting-started). In short: PostgreSQL with pgvector, a Telegram bot
token in `local/application-local.yml`, `./gradlew bootRun`, `npm run dev` in `admin-ui`.

Gradle is pinned to JDK 21 through `gradle/gradle-daemon-jvm.properties` and downloads it if needed,
so `./gradlew` should work regardless of your default JDK.

## Checks

```bash
./gradlew :build                 # compile + test the backend
cd admin-ui && npx tsc --noEmit  # type-check the admin UI
cd admin-ui && npm run build     # production build
```

CI runs the same three on every pull request.

## Code style

`.editorconfig` covers indentation and line endings; any editor that reads it will do the right thing.
Beyond that:

- **Kotlin** — official Kotlin style, 4 spaces, 120 columns. Constructor injection, no field injection.
  Prefer immutable models and expression bodies where they read well.
- **TypeScript/React** — 4 spaces, function components, SCSS modules co-located with the component
  in `components/<Name>/<Name>.module.scss`.
- **Comments** — explain why, not what. Skip anything a competent reader gets from the code itself;
  do document non-obvious invariants, races and workarounds.
- **Naming** — no abbreviations that aren't already used in the codebase.

## Adding an agent app

An app is a namespace of tools the agent can open and close. Extend `AgentApp`, annotate with
`@AgentToolNamespace`, mark tools with `@AgentToolMethod`, and register the app in `AppManager`.
Parameter schemas are derived from the Kotlin signature — don't hand-write JSON schema. Apps are
prototype-scoped: one instance per open, so per-session state can live in fields.

## Database changes

Migrations are Flyway SQL in `src/main/resources/db/migration`, named `V<n>__description.sql`.
Never edit a migration that has already been applied anywhere — add a new one.

Entity-to-model mapping goes through MapStruct via kapt. After changing a mapper interface, run a
clean build so generated sources are regenerated.

## Commits and pull requests

- Present tense, imperative subject: `Add calendar recurrence support`.
- One logical change per pull request; keep unrelated reformatting out of it.
- Say what you tested. There is no broad automated test suite, so manual verification notes matter.

## Security

Don't open a public issue for a security problem — email the address on the maintainer's GitHub
profile instead.
