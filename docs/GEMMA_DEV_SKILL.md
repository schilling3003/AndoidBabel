# Using the external `gemma-dev` skill

## Decision

Use `google-gemma/gemma-skills`'s `gemma-dev` as a supporting Gemma-domain
skill. It does not replace `translation-gauntlet`, the Android architecture, or
the repository's measurable quality gates.

Source: <https://github.com/google-gemma/gemma-skills/tree/main/skills/gemma-dev>

The external repository says it is not an officially supported Google product.
No root `LICENSE` file was present when this project checked it, so its
`SKILL.md` is not vendored here. Review its current contents and terms before
committing a copied version.

## Local installation

From the repository root, use the skills CLI without `--global`:

```bash
npx skills add google-gemma/gemma-skills --skill gemma-dev
```

Confirm where the installer placed the skill. Devin's recommended project path
is `.agents/skills/gemma-dev/SKILL.md`. Do not overwrite
`.agents/skills/translation-gauntlet/`.

If policy or licensing review does not permit committing the installed skill,
keep it as a developer-local aid and leave this repository's committed skills
unchanged.

## Allowed scope

Invoke `gemma-dev` for:

- Gemma 4 E2B/E4B capability and model-selection questions;
- official prompt formatting and structured-output research;
- thinking-mode configuration;
- MTP and quantization investigation;
- finding the latest official Gemma documentation.

Example:

```text
/gemma-dev Determine the correct Gemma 4 E2B prompt structure for strict
translation JSON through LiteRT-LM. Do not change the Android architecture.
```

## Explicit precedence

When instructions disagree, use this order:

1. product/privacy constraints in `docs/PRODUCT_SPEC.md`;
2. measured release gates in `docs/QUALITY_GATES.md`;
3. Android decisions in `docs/ARCHITECTURE.md`;
4. iterative procedure in `translation-gauntlet`;
5. generic recommendations from `gemma-dev`.

`gemma-dev` must not cause the project to switch to Transformers,
Transformers.js, Gradio, Vertex AI, MLX, cloud inference, or a Python runtime.
MTP or quantized-model recommendations are candidates only after compatibility
with the pinned LiteRT-LM Android runtime is verified and physical-device
benchmarks show a net benefit.

## Update discipline

Record the external skill commit/version used in `docs/GAUNTLET_STATE.md`. On an
upgrade, review the diff for changes to model names, deployment tooling,
documentation sources, or instructions that conflict with this repository.
