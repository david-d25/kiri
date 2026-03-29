# Kiri — Claude Code Guide

## Project Overview
Kiri is an autonomous agent system with Telegram integration. Full-stack application:
- **Backend:** Kotlin 2.1.10 + Spring Boot 3.5.4
- **Frontend:** TypeScript + React 19 + Next.js 15 (Turbopack)
- **Database:** PostgreSQL with Flyway migrations + pgvector support
- **Version:** 1.4.0

## Commands

### Backend
```bash
./gradlew build          # Build backend JAR
./gradlew test           # Run tests (JUnit Platform)
./gradlew bootRun        # Run Spring Boot locally (port 8080)
```

### Frontend (admin-ui)
```bash
cd admin-ui
npm install              # Install dependencies
npm run dev              # Dev server (Next.js Turbopack)
npm run build            # Production build
```

### Deployment
```bash
./gradlew prepareDeployment   # Copies artifacts into deployment/
./gradlew packageDeployment   # Creates versioned zip (docker-compose-<version>.zip)
```

## Project Structure

```
kiri/
├── src/main/kotlin/space/davids_digital/kiri/
│   ├── agent/           # Core agent engine
│   │   ├── engine/      # Agent execution engine & event bus
│   │   ├── frame/       # Frame buffer & data abstractions
│   │   ├── tool/        # Tool registry & execution
│   │   ├── memory/      # Memory management
│   │   └── app/         # App implementations (Telegram, Image, Scratchpad)
│   ├── bot/telegram/    # Telegram bot commands & handlers
│   ├── integration/     # LLM provider integrations (Anthropic, OpenAI, Google)
│   ├── llm/             # Chat completion models & DSL
│   ├── model/           # Data models (User, Settings, MemoryPoint, Telegram types)
│   └── aop/             # Aspect-oriented programming (cache eviction)
├── src/test/kotlin/...  # Tests
├── admin-ui/            # Next.js frontend
│   ├── components/      # React components (SCSS modules)
│   ├── pages/           # Next.js pages
│   ├── services/        # API services
│   ├── hooks/           # Custom React hooks
│   └── styles/          # Global styles
├── deployment/          # Docker Compose + Dockerfiles + nginx config
├── local/               # Local dev config (gitignored overrides)
└── build.gradle.kts     # Root Gradle build file
```

## Local Development Setup
Create `local/application-local.yml` (gitignored) to override defaults:
```yaml
app:
  security:
    encryptionKeyBase64: your-base64-key
  integration:
    telegram:
      botId: your-bot-id
      apiKey: your-telegram-api-key
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/kiri
    username: your-username
    password: your-password
```

## Key Technical Details
- **LLM integrations:** Anthropic Claude (anthropic-java 2.11.1), OpenAI (openai-java 4.18.0), Google GenAI (google-genai 1.38.0)
- **Frontend base path:** `/kiri` (configured in next.config.js)
- **Coroutines:** Spring controllers support suspend functions via kotlinx-coroutines-reactor
- **MapStruct:** Used for entity/DTO mapping; component model set to `spring` via kapt
- **Caching:** Caffeine cache with Spring Cache abstraction
- **Build property expansion:** `processResources` expands Gradle project properties into resource files

## Architecture Notes
- Agent execution uses a frame-buffer model for managing conversation context
- Tools are registered via a tool registry and executed through a tool call executor
- Telegram bot acts as the primary user-facing interface
- Admin UI provides configuration and monitoring (served under `/kiri` path)
