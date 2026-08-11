# Android architecture

## Platform baseline

- Kotlin and Jetpack Compose.
- ARM64 production target. Choose and document min/target SDK after verifying
  current library requirements; prefer broad modern-device coverage over legacy
  workarounds.
- Pin stable versions of Android Gradle Plugin, Kotlin, Compose BOM, LiteRT-LM,
  and Moonshine Voice. Depend on official artifacts whenever available.
- Use coroutines and `StateFlow` for asynchronous work and observable state.
- Prefer manual constructor injection until dependency complexity justifies a
  framework. Avoid startup-heavy infrastructure by default.

## Production pipeline

Initial default:

```text
AudioRecord / Moonshine MicTranscriber
              ↓
        source transcript
              ↓
LiteRT-LM persistent Gemma conversation/request
              ↓
      translated text
              ↓
Moonshine TextToSpeech → AudioTrack
```

Gemma outputs strict structured data. Validate and recover from fenced,
malformed, or extra text without silently displaying JSON. Do not reuse
conversation history across independent translation turns unless tests prove it
improves consistency without contaminating meaning.

## Core interfaces

Exact names may evolve, but preserve these boundaries:

```kotlin
interface SpeechRecognizer {
    val readiness: StateFlow<EngineReadiness>
    fun partials(): Flow<PartialTranscript>
    suspend fun transcribe(audio: RecordedAudio, language: Language): Transcript
    suspend fun cancel()
}

interface TranslationEngine {
    val readiness: StateFlow<EngineReadiness>
    suspend fun translate(text: String, source: Language, target: Language): Translation
    suspend fun cancel()
}

interface SpeechSynthesizer {
    val readiness: StateFlow<EngineReadiness>
    suspend fun speak(text: String, language: Language)
    suspend fun stop()
}

interface ModelManager {
    val state: StateFlow<ModelState>
    suspend fun import(uri: Uri): ImportResult
    suspend fun validate(): ValidationResult
    suspend fun remove()
}

interface PerformanceRecorder {
    fun mark(event: PipelineEvent)
    fun export(): BenchmarkReport
}
```

Use fake deterministic implementations for Compose previews, UI tests, and the
first UI-only vertical slice.

## State model

Represent the conversation as an explicit finite state machine rather than a
loose collection of booleans:

```text
Setup → Warming → Ready → Recording → Transcribing → Translating → Speaking
                        ↘ Cancelled/Error/Ready recovery ↗
```

Events are serialized. A new recording stops TTS. Swapping languages is blocked
or queued while recording. Cancellation propagates to the current operation.
Stale async results carry a turn ID and may not overwrite a newer turn.

## Threading and lifecycle

- Main thread: Compose state publication and lightweight UI events only.
- Dedicated coroutine contexts/executors: model load/inference, audio encoding,
  file operations, hashing, and synthesis.
- Initialize heavyweight engines once and reuse them. Prewarm after required
  assets are ready, not before the user understands the cost.
- Release microphone and playback promptly on pause, interruption, or route
  change. Preserve safe model state where memory allows.
- Handle configuration and process recreation through saved lightweight state;
  never serialize native engine handles.
- Monitor trim-memory callbacks and implement a documented eviction policy for
  inactive language engines.

## Model and asset management

- Import Gemma with Android Storage Access Framework. Persist URI permission
  when safe or copy atomically into app storage when direct access is unsuitable.
  Before copying, display required storage and check free space.
- Validate extension, readable size, model metadata/API compatibility where
  available, and optional SHA-256 from a user-supplied manifest.
- Use atomic temporary filenames and rename only after validation.
- Never delete the user's original file.
- Track language-specific STT/TTS assets, versions, sizes, and integrity.
- No model weights, generated binaries, keystores, or downloaded voice assets
  belong in Git.

## Native Gemma audio experiment

Gemma audio is a candidate optimization, not an assumed upgrade. The spike must
use the same utterances, device, release build, warm/cold conditions, and output
quality rubric as the staged pipeline. Compare:

- stop-to-transcript and stop-to-translation p50/p95;
- peak proportional set size and memory pressure;
- engine warmup and encoder load time;
- energy/thermal behavior across 20 consecutive turns;
- transcript and translation quality across all supported languages;
- cancellation and crash behavior.

Adopt it only with a written decision record and a meaningful overall win. If
it wins, keep separate TTS because Gemma produces text, not audio.

## Security and privacy

- No telemetry or analytics SDKs.
- Do not request Internet permission unless an explicit optional downloader is
  implemented; imported-model operation should not need it.
- Keep local history off by default. Encrypt sensitive persisted content using
  platform facilities if history is enabled.
- Redact transcripts from production logs and crash messages.
- Use scoped storage and least-privilege permissions.

## Build quality

- Reproducible Gradle wrapper and version catalog.
- Formatting, lint, static analysis, unit tests, instrumented tests, screenshot
  tests, and release build in CI.
- Baseline profile and Macrobenchmark modules when the vertical slice is stable.
- No secrets or signing keys in the repository.

