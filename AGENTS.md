# Repository instructions

Build a native Android, offline-first voice translator where usability and
perceived speed outrank feature count. Read these sources before changing code:

1. `docs/PRODUCT_SPEC.md`
2. `docs/UX_SPEC.md`
3. `docs/ARCHITECTURE.md`
4. `docs/QUALITY_GATES.md`
5. `docs/TEST_AND_BENCHMARK_PLAN.md`
6. `docs/GAUNTLET_STATE.md`

Use `.agents/skills/translation-gauntlet/SKILL.md` for iterative improvement.

## Skill precedence

`translation-gauntlet` is the controlling workflow for implementation,
testing, UI/UX, architecture, performance, and release decisions.

The external `google-gemma/gemma-skills` repository's `gemma-dev` skill may be
installed locally and used only for:

- Gemma model capabilities and selection;
- Gemma 4 prompt formatting and thinking-mode configuration;
- quantization and Multi-Token Prediction research;
- locating current official Gemma documentation.

Follow `docs/GEMMA_DEV_SKILL.md` when using it. For Android deployment,
LiteRT-LM integration, audio architecture, dependency selection, and
performance decisions, this repository's specifications and measured
physical-device evidence override generic `gemma-dev` recommendations. Do not
switch to Transformers, Transformers.js, Gradio, Vertex AI, MLX, or cloud
inference unless the product specifications are explicitly changed.

Non-negotiable constraints:

- Native Kotlin and Jetpack Compose; no Python server, localhost HTTP bridge,
  React WebView, or cloud inference in the production path.
- All speech, translation, and synthesis run on-device after assets are
  installed.
- The Gemma `.litertlm` model is imported or downloaded separately; do not
  place multi-gigabyte model weights in Git or the base APK.
- Never claim a performance result that was not measured. Record device, build
  type, corpus, sample count, p50, and p95.
- Keep UI work on the main thread and model/audio work off it.
- Prefer a small, understandable dependency graph. Pin versions and explain
  non-obvious dependencies.
- Do not copy another translator's branding or exact visual design. References
  establish a usability bar, not an asset source.

Definition of done is the release gate in `docs/QUALITY_GATES.md`, not merely a
successful build.
