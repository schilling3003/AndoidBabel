# Relay — Native Android Offline Voice Translator

Work-in-progress native Android app for two-person offline voice translation.
Built with Kotlin, Jetpack Compose, and coroutines/Flow. The architecture keeps
speech recognition, translation, and speech synthesis behind narrow interfaces
so real engines can be swapped in without changing the UI.

> **Current status:** Round 2 in progress. The project builds, passes unit tests,
> and wires real LiteRT-LM (Gemma) and Moonshine Voice STT/TTS engine adapters.
> The UI-only vertical slice with deterministic fakes is complete; the next step
> is end-to-end English ↔ Spanish on a physical device with real model assets.

## Supported languages

English, Arabic, Spanish, Japanese, Mandarin Chinese, Korean.

## Build

```bash
export ANDROID_HOME=/path/to/Android/Sdk
./gradlew :app:compileDebugKotlin :app:lintDebug :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Project structure

- `app/src/main/java/com/schilling3003/relay/domain/` — models and state
- `app/src/main/java/com/schilling3003/relay/engines/` — engine interfaces and
  fake and real implementations (`litert/`, `moonshine/`)
- `app/src/main/java/com/schilling3003/relay/viewmodel/` — `ConversationViewModel`
- `app/src/main/java/com/schilling3003/relay/ui/` — Jetpack Compose screens
- `app/src/main/java/com/schilling3003/relay/audio/` — audio recorder/player
  abstractions
- `app/src/main/java/com/schilling3003/relay/storage/` — `.litertlm` model
  import/persistence
- `app/src/test/` — unit tests

## First-run model setup

The app does not bundle the Gemma model. On first launch it prompts for a
`.litertlm` file and imports it via the Storage Access Framework. The model is
copied into app-private storage and validated before the conversation UI is
shown.

Moonshine STT/TTS language models are downloaded on first use (explicit,
background, one-time) to avoid bundling large assets. Once downloaded, all
inference is on-device.

## Offline and privacy

- `RECORD_AUDIO` is requested at runtime for microphone capture.
- The `moonshine-voice` dependency declares `INTERNET`/`ACCESS_NETWORK_STATE`
  so it can download language models on demand. No network is used during
  translation or synthesis, and the app does not send transcripts or audio
  to a server.
- No analytics or transcript logging in the current codebase.
- All inference runs on-device after models are present.

## Known limitations (Round 2)

- Real engine adapters compile but have not been exercised end-to-end with
  a `.litertlm` Gemma model and Moonshine STT/TTS model files on a device.
- Translation output parsing uses `Message.toString()`; it may need a structured
  response format once the model is available.
- Runtime `RECORD_AUDIO` permission is requested at startup; a dedicated
  permission-rationale screen may be added later.
- Release signing config, R8/ProGuard rules, and arm64 release APK are future
  gauntlet steps.

## Next steps

1. Obtain/confirm a compatible `.litertlm` Gemma 4 E2B model and Moonshine
   STT/TTS model files for English and Spanish.
2. Run the English ↔ Spanish vertical slice on a physical reference device.
3. Add remaining languages with RTL and script-specific validation.
4. Add performance instrumentation and produce a signed arm64 release APK.

## License

Copyright (C) 2026 Brandon Schilling. All rights reserved unless otherwise noted.
