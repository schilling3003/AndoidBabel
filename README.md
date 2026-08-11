# Relay — Native Android Offline Voice Translator

Work-in-progress native Android app for two-person offline voice translation.
Built with Kotlin, Jetpack Compose, and coroutines/Flow. The architecture keeps
speech recognition, translation, and speech synthesis behind narrow interfaces
so real engines can be swapped in without changing the UI.

> **Current status:** Round 1 complete. The project builds, passes unit tests,
> and runs a UI-only vertical slice with deterministic fake engines. Real
> LiteRT-LM (Gemma) and Moonshine Voice integration is the next gauntlet round.

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
  fake implementations
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

## Offline and privacy

- Only `RECORD_AUDIO` is declared in the manifest. No `INTERNET` permission, no
  analytics, no cloud inference, and no transcript logging in the current
  codebase.
- All inference runs on-device after the model is imported.

## Known limitations (Round 1)

- Real LiteRT-LM and Moonshine Voice artifacts are pinned in the version catalog
  but not yet wired to the UI; the first vertical slice uses deterministic fakes.
- No on-device STT/TTS/translation yet.
- Release signing config and arm64 release APK are future gauntlet steps.
- Screenshot, RTL, large-text, and accessibility tests are partially implemented
  but not yet executed on a physical device.

## Next steps

1. Resolve Kotlin/Compose/LiteRT-LM version compatibility on a reference
   device.
2. Wire real `LocalModelManager` → LiteRT-LM → Moonshine pipeline for
   English ↔ Spanish.
3. Add remaining languages with RTL and script-specific validation.
4. Add performance instrumentation and produce a signed arm64 release APK.

## License

Copyright (C) 2026 Brandon Schilling. All rights reserved unless otherwise noted.
