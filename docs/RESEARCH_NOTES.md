# Research notes and source grounding

Checked on 2026-08-10. Re-verify current dependency versions when implementation
starts; capabilities and APIs can change.

## Gauntlet Loop

Matt Shumer's published Gauntlet Loop prompt establishes an ambitious external
quality bar, asks a lead agent to split the artifact into independently
judgeable components, assigns separate builders and harsh critics, compares
real outputs against references, and repeats when the generated result loses.
The useful mechanism is builder/critic separation plus evidence-based iteration,
not the hyperbolic use of words such as “perfect.”

- Original explainer: <https://x.com/mattshumer_/status/2081857631254372509>
- Original example prompt: <https://x.com/mattshumer_/status/2081830214384886228>

This package adds explicit correctness, privacy, accessibility, hardware,
budget, and stop gates because an unbounded subjective loop can consume time
without producing measurable improvement.

## Devin loop and skills

Devin CLI already provides `/loop <prompt>`, which runs a prompt and then
auto-reviews the diff repeatedly. It requires a clean Git state. Therefore this
package does not define a conflicting custom skill named `loop`.

- CLI commands: <https://docs.devin.ai/cli/essential-commands>

Devin discovers repository skills in `.agents/skills/<skill-name>/SKILL.md`
(recommended) and permits slash invocation by skill name. The included
`translation-gauntlet` skill defines one bounded round; the built-in `/loop`
can repeat it.

- Skills overview: <https://docs.devin.ai/product-guides/skills>
- Skill format: <https://docs.devin.ai/cli/extensibility/skills/creating-skills>

## Source project

The Raspberry Pi reference project is a React UI plus Python API server. It uses
Moonshine Voice for language-specific STT, Gemma 4 E2B through a localhost
LiteRT-LM OpenAI-compatible endpoint for text translation, and Moonshine Voice
TTS backed by Kokoro/Piper. Its model download script uses the official generic
CPU `gemma-4-E2B-it.litertlm` build and budgets about 6 GB while downloading and
importing duplicate copies.

- Repository: <https://github.com/google-gemma/gemma-translator>
- Backend pipeline: <https://github.com/google-gemma/gemma-translator/blob/main/backend/server.py>
- Model script: <https://github.com/google-gemma/gemma-translator/blob/main/download_model.sh>

## Android feasibility

LiteRT-LM has a stable Kotlin API intended for Android/JVM, supports multimodal
inputs, and exposes hardware acceleration. Gemma 4 E2B supports audio input and
text output, including speech recognition and direct speech translation.

- LiteRT-LM Android guide: <https://ai.google.dev/edge/litert-lm/android>
- LiteRT-LM overview: <https://ai.google.dev/edge/litert-lm/overview>
- Gemma audio capabilities: <https://ai.google.dev/gemma/docs/capabilities/audio>

Moonshine Voice publishes Android Java/JNI bindings and an Android artifact
covering microphone transcription and text-to-speech. Its current README shows
`ai.moonshine:moonshine-voice:0.1.1`; implementation must verify and pin the
current stable version rather than blindly using “latest.”

- Moonshine Voice Android quickstart: <https://github.com/moonshine-ai/moonshine#android>
- Android microphone binding: <https://github.com/moonshine-ai/moonshine/blob/main/language-bindings/android/java/main/java/ai/moonshine/voice/MicTranscriber.java>
- Android TTS binding: <https://github.com/moonshine-ai/moonshine/blob/main/language-bindings/android/java/main/java/ai/moonshine/voice/TextToSpeech.java>

## Important inference

No source-project issue, PR, or comment was found that explicitly explains why
Moonshine STT was chosen over Gemma's native audio input. The staged pipeline is
therefore treated as a low-risk baseline, not a permanent mandate. The native
audio path must earn adoption through controlled physical-device comparison.

## Gemma development skill

The `google-gemma/gemma-skills` repository provides a relevant `gemma-dev`
skill covering Gemma 4 model selection, prompt formatting, MTP, quantization,
and documentation lookup. It does not provide an Android/LiteRT-LM application
workflow and its generic deployment options include stacks excluded by this
project. Use it as a supporting domain skill under the precedence rules in
`AGENTS.md` and `docs/GEMMA_DEV_SKILL.md`.

- Repository: <https://github.com/google-gemma/gemma-skills>
- Skill source: <https://github.com/google-gemma/gemma-skills/blob/main/skills/gemma-dev/SKILL.md>

The repository's README states that it is not an officially supported Google
product, and no root `LICENSE` file was present when checked. For that reason,
this package links to and documents installation of the external skill rather
than copying its full contents into this repository.
