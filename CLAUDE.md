# Kiri — Claude Code Guide

## Project Overview
Kiri is a self-driving LLM agent with a Telegram front end. Full-stack application:
- **Backend:** Kotlin 2.3.21 + Spring Boot 3.5.4 (Gradle 8.14.3, JDK 21 toolchain)
- **Frontend:** TypeScript + React 19 + Next.js 15 (Turbopack)
- **Database:** PostgreSQL with Flyway migrations + pgvector
- **Version:** see `version` in `build.gradle.kts`

## Commands

### Backend
```bash
./gradlew :build         # Compile + test the backend only
./gradlew test           # Run tests (JUnit Platform)
./gradlew bootRun        # Run Spring Boot locally (port 8080)
```
`./gradlew build` (without the leading colon) also builds `admin-ui` and the deployment zip, which is
slow — use `:build` while working on the backend.

The Gradle daemon JVM is pinned to 21 in `gradle/gradle-daemon-jvm.properties` and is downloaded
automatically, so no `JAVA_HOME` override is needed.

### Frontend (admin-ui)
```bash
cd admin-ui
npm ci                   # Install from the lockfile
npm run dev              # Dev server (Next.js Turbopack, port 3000, base path /kiri)
npx tsc --noEmit         # Type check
npm run build            # Production build
```

### Deployment
```bash
./gradlew prepareDeployment   # Copies artifacts into deployment/build/staging
./gradlew packageDeployment   # Creates docker-compose-<version>.zip
```

### Local database
```bash
docker compose -f docker-compose.dev.yml up -d
```

## Project Structure

```
kiri/
├── src/main/kotlin/space/davids_digital/kiri/
│   ├── agent/
│   │   ├── engine/       # Tick loop, event bus, lifecycle hooks
│   │   ├── frame/        # Frame buffer, frame types, rendering to LLM requests
│   │   ├── tool/         # Tool registry, reflective scanner, parameter mapper, executor
│   │   ├── memory/       # Memory tools (memorize/query/forget)
│   │   ├── notification/ # Wake-up sources
│   │   └── app/          # Agent apps: telegram, calendar, files, image, svg, scratchpad
│   ├── bot/telegram/     # Telegram bot commands & handlers
│   ├── integration/      # Anthropic, OpenAI, Google GenAI, Telegram clients
│   ├── llm/              # Provider-agnostic chat completion model + request DSL
│   ├── model/            # Domain models
│   ├── orm/              # Entities, repositories, MapStruct mappers, ORM services
│   ├── rest/             # Controllers, DTOs, SSE, auth
│   ├── security/         # Role annotations and checks
│   ├── service/          # Application services
│   └── aop/              # Cache eviction aspect
├── src/main/resources/
│   └── db/migration/     # Flyway SQL (V<n>__name.sql)
├── admin-ui/             # Next.js frontend (components/, pages/, hooks/, lib/, services/)
├── deployment/           # Docker Compose overlay + Dockerfiles + nginx config
├── local/                # Local dev config (gitignored overrides)
└── build.gradle.kts      # Root Gradle build file
```

## Local Development Setup
Create `local/application-local.yml` (gitignored) to override defaults:
```yaml
app:
  security:
    encryptionKeyBase64: your-base64-key   # openssl rand -base64 32
  auth:
    telegram:
      botUsername: your_bot_username
  integration:
    telegram:
      botId: 123456789
      apiKey: your-telegram-api-key
```
LLM provider API keys are **not** configured here — they are stored encrypted in the database and
edited from the admin UI under Integrations.

Every API endpoint requires the `ADMIN` role, and nothing seeds the first user. Insert one manually:
`insert into main.users (id, role) values (<telegram-user-id>, 'OWNER');`

## Key Technical Details
- **LLM integrations:** Anthropic (anthropic-java 2.24.0), OpenAI (openai-java 4.18.0),
  Google GenAI (google-genai 1.38.0)
- **Frontend base path:** `/kiri` (configured in `next.config.js`)
- **Coroutines:** Spring controllers support suspend functions via kotlinx-coroutines-reactor
- **MapStruct:** entity/DTO mapping, component model `spring` via kapt — clean build after changing a mapper
- **Caching:** Caffeine; two managers — the `@Primary` default (60s) and `oneHour`
- **Settings:** two tiers — YAML (`AppProperties`, startup-only) and database-backed runtime settings
  declared via `SettingOrmService.declare*`, editable from the admin UI without a restart
- **Build property expansion:** `processResources` expands Gradle project properties into resources

## Architecture Notes
- The engine renders the frame buffer into a chat completion request each tick, executes the returned
  tool calls, appends result frames, and repeats until the agent sleeps
- Frames are bounded; the oldest are dropped past `FrameBuffer.hardLimit`
- Apps are prototype-scoped and contribute tools only while open, keeping the tool list small
- Tool JSON schemas are derived from Kotlin signatures — never hand-written
- Telegram bot is the primary user-facing interface; the admin UI (served under `/kiri`) handles
  configuration and monitoring

## Conventions
- Comments explain why, not what — skip anything obvious from the code itself
- Kotlin: 4 spaces, 120 columns, constructor injection
- Admin UI: 4 spaces, function components, SCSS modules co-located with components
