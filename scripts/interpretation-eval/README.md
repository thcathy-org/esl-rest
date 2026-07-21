# Interpretation model eval

Benchmark Replicate / LocalAI models against the production `gpt-5-mini` baseline for vocab interpretation (`GET /interpretation`).

Prompts match `ReplicateAIService.buildInterpretationPrompt()` in this repo.

## Prerequisites

- Python 3.10+
- Baseline CSV with columns `id`, `text`, `lang`, `interpretation` (production outputs)
- For Replicate eval: `REPLICATE_API_TOKEN`
- For LocalAI eval: `LOCALAI_URL`, `LOCALAI_API_KEY`

Copy `.env.example` to `.env` and fill in tokens (do not commit `.env`).

## Scripts location

These scripts live in the **esl-all workspace** (monorepo root, not committed here):

```
esl-all/scripts/interpretation-eval/
  compare_replicate_models.py   # Replicate candidates vs baseline
  compare_interpretation_models.py  # LocalAI models vs baseline
  .env.example
  results/                      # eval output CSV + summary JSON
```

Run from workspace root or `scripts/interpretation-eval/`.

## Quick start

```bash
cd /path/to/esl-all/scripts/interpretation-eval
cp .env.example .env   # edit tokens

# Replicate: compare nano + 4o-mini vs gpt-5-mini baseline (50 rows)
export REPLICATE_API_TOKEN=...
python3 compare_replicate_models.py --limit 50

# LocalAI: smoke test qwen3-4b (10 rows)
export LOCALAI_URL=https://homeserver.funfunspell.com/local-ai
export LOCALAI_API_KEY=...
python3 compare_interpretation_models.py --limit 10 --models qwen3-4b
```

Baseline CSV default path in scripts: `esl-all/tmp/esl_VOCAB_INTERPRETATION_2.csv`. Override with `--input /path/to.csv`.

## Output

Each run writes to `results/`:

- `{timestamp}-{model}.csv` — per-row candidate vs baseline
- `{timestamp}-{model}.summary.json` — quality metrics, latency, estimated cost

Key quality fields: `exact_match`, `normalized_match`, `word_leakage_en`, `wrong_chinese_script`, `english_in_zh_output`.

## Decision record

See [INTERPRETATION_MODEL_DECISION.md](../../INTERPRETATION_MODEL_DECISION.md) at repo root.
