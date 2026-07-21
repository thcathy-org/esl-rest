# AGENTS.md — esl-rest

## Scope

Spring Boot REST API for [funfunspell.com](https://www.funfunspell.com). Owns dictation/vocab/member APIs, Auth0 JWT validation, TTS publish queue (LocalAI Kokoro + Azure), interpretation, and orchestration of image generation.

## Commands

```bash
./gradlew build          # build
./gradlew test           # all tests
./gradlew bootJar        # executable JAR
```

Single test: `./gradlew test --tests "com.esl.service.tts.TtsPublisherServiceTest"`

## Related repos

| Repo | Relationship |
|------|--------------|
| `esl-ionic` | Primary client — calls this API with Auth0 JWT |
| `image-generation-server` | Vocab image generation — called via `ImageGenerationService` |
| ~~`esl-speech-worker`~~ | **Deprecated** — not used; TTS is LocalAI Kokoro + Azure |

## Cross-repo changes

- **TTS provider / voice / timeout** → `TtsPublisherService`, `LocalAiService`; verify LocalAI ingress/timeouts
- **Interpretation model** → `ReplicateAIService`; see [INTERPRETATION_MODEL_DECISION.md](./INTERPRETATION_MODEL_DECISION.md); eval: [scripts/interpretation-eval/README.md](./scripts/interpretation-eval/README.md)
- **Image generation** → `ImageGenerationService` + `image-generation-server` deploy/config
- **Auth0 / JWT** → `SecurityConfig`, `JWTAuthorizationFilter`; also update `esl-ionic` auth client if contract changes

## Do not

- Bump `TtsPublisherService.Version` for Kokoro-only engine swaps (see CLAUDE.md TTS decisions)
- Add or extend `esl-speech-worker` integration — deprecated
- Include Claude attribution in commit messages

## Deep context

See [CLAUDE.md](./CLAUDE.md) for architecture, TTS pipeline, security, external services, and testing patterns.
