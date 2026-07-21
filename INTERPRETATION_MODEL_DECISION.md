# Interpretation model decision

**Date:** 2026-07-21  
**Chosen:** Replicate `openai/gpt-5-mini` (keep — already in production)

## Context

Vocab interpretation API: `GET /interpretation?text=&lang=` in `esl-rest`, backed by `ReplicateAIService`. Client: `esl-ionic` `InterpretationService`.

Config: `ReplicateAIService.InterpretationModel=openai/gpt-5-mini` in `application.properties`.

## Tested (Jul 2026)

Eval scripts: `scripts/interpretation-eval/` (see README). Baseline CSV: `esl_VOCAB_INTERPRETATION_2.csv` (production `gpt-5-mini` outputs). Sample: 50 rows stratified by lang.

| Model | Provider | Exact match / 50 | Notes |
|-------|----------|------------------|-------|
| `openai/gpt-5-mini` | Replicate | **Baseline** | Current production — keep |
| `openai/gpt-4o-mini` | Replicate | 17 | Faster avg latency; 1 English word leak in EN defs |
| `openai/gpt-5-nano` | Replicate | 14 | Cheapest (~$0.00035/sample); 1 English leak in zh-Hant |
| `qwen3-4b` | LocalAI | 10-row smoke only | ~8s p50 latency; not competitive for production |
| `qwen3.5-4b-dflash` | LocalAI | Failed | GPU OOM on shared homeserver |

Eval data (workspace monorepo):

- `scripts/interpretation-eval/results/20260719-030226-replicate-openai_gpt-4o-mini.summary.json`
- `scripts/interpretation-eval/results/20260719-030226-replicate-openai_gpt-5-nano.summary.json`
- `scripts/interpretation-eval/results/20260719-025311-qwen3-4b.summary.json`

## Decision

**Keep `openai/gpt-5-mini`.** Candidates did not beat the baseline on quality; nano is cheaper but lower exact-match rate; LocalAI 4B models are too slow or unreliable on current hardware.

Revisit if Replicate pricing changes materially or a new mini-class model scores higher on the same eval set.

## Config

```properties
ReplicateAIService.InterpretationModel=openai/gpt-5-mini
```

Prompt builder: `ReplicateAIService.buildInterpretationPrompt()` — do not change without re-running eval.

## Re-eval checklist

1. Run `scripts/interpretation-eval/compare_replicate_models.py` (see README)
2. Compare `exact_match`, `word_leakage_en`, `english_in_zh_output`, latency, cost
3. Update this doc and `application.properties` if switching models
