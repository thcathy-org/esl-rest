# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## About

Spring Boot 3.2 REST API backend for [funfunspell.com](https://www.funfunspell.com) — a free English vocabulary learning app. Java 21. Built with Gradle.

## Commands

```bash
# Build
./gradlew build

# Run tests (all)
./gradlew test

# Run a single test class
./gradlew test --tests "com.esl.service.tts.TtsPublisherServiceTest"

# Run a single test method
./gradlew test --tests "com.esl.service.tts.TtsPublisherServiceTest.publishNext_shouldSkipWhenR2NotConfigured"

# Build executable JAR
./gradlew bootJar
```

Tests use both JUnit 5 (Java) and Spock (Groovy). The test database is H2 in-memory; schema is populated via `src/test/resources/data-h2.sql`.

## Git

Do not include `Co-Authored-By: Claude` or any Claude attribution in commit messages.

## Architecture

### Package layout

- `com.esl.controller` — REST controllers (Spring MVC `@RestController`)
- `com.esl.service` — Business logic services
  - `service/tts` — TTS pipeline (queue, publish, audio key building)
  - `service/rest` — External HTTP integrations (R2, SpeechWorker, Cloudflare AI, Replicate, image gen)
  - `service/memberword` — Member vocabulary management
- `com.esl.dao` — Data access (legacy DAO pattern + Spring Data repositories under `dao/repository`)
- `com.esl.entity` — JPA entities mapped to MySQL
- `com.esl.config` — Spring configuration (Security, Cache)
- `com.esl.security` — JWT filter
- `com.esl.util` — Stateless utility classes

### Security

JWT-based, stateless. Auth0 issues JWTs validated via an X.509 public key (path configured via `auth0.cert`). The `JWTAuthorizationFilter` reads the Bearer token and populates the `SecurityContext`. Routes under `/member/**` require authentication; `/admin/**` requires role `admin`; everything else is public.

### TTS pipeline

The TTS pipeline asynchronously generates and stores audio for vocabulary content:

1. **`TtsQueueService`** — Enqueues content (vocab words or article sentence chunks) into the `TTS_PUBLISH_QUEUE` DB table.
2. **`TtsPublisherService`** — Scheduled poller (configurable interval) that picks up pending/failed queue items and publishes audio to Cloudflare R2.
   - Each item generates two audio files: one for the plain text, one for a punctuation-spelled variant (e.g., "." → "full stop").
   - Audio keys follow the pattern: `tts/{version}/{shard}/{slug}/{sha256hash}.mp3`
   - Supports two TTS providers, selected via `TtsPublisherService.Provider`: `esl_speech_worker` (default) or `cloudflare_aura2`.
   - Failed items are retried with backoff up to `MaxAttempts` (default 288 ≈ 1 day at 5-min intervals).
   - Items that trigger a non-retryable "invalid input" error from the speech worker are deleted immediately.

### External service integrations (`service/rest`)

- **`R2StorageService`** — Cloudflare R2 (S3-compatible) for audio file storage. Gracefully disables itself if credentials are missing.
- **`SpeechWorkerService`** — Internal ESL speech worker API for TTS generation.
- **`CloudflareAIService`** — Cloudflare Aura2 TTS as an alternative provider.
- **`ReplicateAIService`** / **`ImageGenerationService`** — Image generation for vocab.
- **`WebParserRestService`** — Web scraping via jsoup.

### Runtime configuration

All external credentials are injected via environment variables:

| Variable | Purpose |
|---|---|
| `MYSQL_HOST`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD` | MySQL connection |
| `R2_ENDPOINT`, `R2_BUCKET`, `R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY` | Cloudflare R2 |
| `ESL_SPEECH_WORKER_HOST`, `ESL_SPEECH_WORKER_APIKEY` | TTS speech worker |
| `CLOUDFLARE_ACCOUNT_ID`, `CLOUDFLARE_API_TOKEN` | Cloudflare AI TTS |

### Caching

Caffeine in-memory cache configured in `CacheConfig`. `CachedVocabService` wraps vocab question creation with `@Cacheable`.

### Testing patterns

Unit tests for services use plain Mockito (no Spring context). `ReflectionTestUtils.setField` is used to inject `@Value`-annotated fields in unit tests. Integration/controller tests use `@SpringBootTest` with H2. The `testing=true` property flag in `SecurityConfig` allows bypassing JWT validation in tests.