# Android Offline Translator — Devin Gauntlet Pack

This package is a source-of-truth bundle for building a native Android, fully
offline, two-person voice translator. It is intentionally written for an
agentic build loop: the goal and quality bar are precise, while implementation
details remain flexible enough for Devin to respond to measured evidence.

## Files

- `DEVIN_GAUNTLET_PROMPT.md` — master prompt to start the project.
- `AGENTS.md` — short always-on repository guidance.
- `docs/PRODUCT_SPEC.md` — product scope and user journeys.
- `docs/UX_SPEC.md` — interaction and visual requirements.
- `docs/ARCHITECTURE.md` — technical boundaries and decisions.
- `docs/QUALITY_GATES.md` — scorecard and non-negotiable release gates.
- `docs/TEST_AND_BENCHMARK_PLAN.md` — evidence Devin must collect.
- `docs/GAUNTLET_STATE.md` — persistent loop ledger template.
- `docs/RESEARCH_NOTES.md` — primary-source grounding and rationale.
- `docs/GEMMA_DEV_SKILL.md` — safe scope and installation guidance for the
  external Gemma domain skill.
- `.agents/skills/translation-gauntlet/SKILL.md` — one rigorous improvement round.

## How to use this in Devin

1. Copy `AGENTS.md`, `docs/`, and `.agents/` into the repository root. Keep
   `DEVIN_GAUNTLET_PROMPT.md` outside the committed repository if the prompt is
   being supplied privately to Devin.
2. Commit the specification package before starting. Devin CLI's built-in
   `/loop` requires a clean Git state.
3. Optionally install the `gemma-dev` domain skill locally as described in
   `docs/GEMMA_DEV_SKILL.md`. Do not install it globally for this project.
4. Start Devin with the separately retained master prompt:

   ```bash
   devin --prompt-file DEVIN_GAUNTLET_PROMPT.md
   ```

5. After the first coherent vertical slice is committed, run repeated
   evidence-based improvement rounds with Devin CLI's built-in loop:

   ```text
   /loop Read and follow .agents/skills/translation-gauntlet/SKILL.md. Execute exactly one complete gauntlet round against docs/QUALITY_GATES.md, update docs/GAUNTLET_STATE.md with real evidence, and commit an improvement only when verification passes. If every release gate is genuinely satisfied, perform the final verification procedure and stop with GAUNTLET COMPLETE; never invent unavailable device evidence.
   ```

You may also invoke `/translation-gauntlet` for one manual round. Do **not**
create a custom skill named `loop`; Devin CLI already owns that command.

## Human checkpoints

The loop may proceed autonomously through scaffolding, tests, UI iterations,
and emulator verification. Human review is required before:

- choosing the final product name and icon;
- publishing an APK or Play Store artifact;
- accepting performance gates on a physical reference device;
- changing the supported-language list or privacy promise;
- adding telemetry, analytics, accounts, cloud APIs, or network-dependent
  behavior.
