# Gauntlet state ledger

This file survives long loops and context compaction. Update it after every
accepted round. Preserve failed attempts; do not rewrite history to look clean.

## Current status

- Phase: Round 2 in progress — real engine adapters wired, build/lint/unit tests green
- Commit under evaluation: latest on `devin/round1-scaffold-ui`
- Overall evidence-backed score: not rated / 100 (real engines compile but no
  physical-device or model-asset measurements yet)
- Release gates: Build gate passes debug with real engine artifacts; remaining
  gates blocked by missing model assets and physical reference device
- Physical reference device: not selected
- Current highest-impact gap: end-to-end English ↔ Spanish vertical slice with real
  STT/Gemma/TTS on a physical device, requiring `.litertlm` and Moonshine model files
- Next action: obtain/confirm model assets, run on a physical reference device, or
  mark the gate `BLOCKED — PHYSICAL DEVICE + MODEL ASSETS REQUIRED`
- Environment: Android SDK command-line tools installed at `/home/ubuntu/Android/Sdk`
  (build-tools 34.0.0, platform 34, emulator, x86_64 system image). AVD and physical
  device tests are later steps.

## Product decisions

| Date | Decision | Evidence/reason | Revisit trigger |
| --- | --- | --- | --- |
| Initial | Native Kotlin + Compose; no web/Python production path | Usability, latency, lifecycle, and Android integration goals | Only if a native library is impossible to integrate |
| Initial | Staged Moonshine STT → Gemma text → Moonshine TTS is the first pipeline | Lowest-risk path and explicit transcript | Native Gemma audio wins the controlled benchmark |
| Initial | Gemma model imported separately | Multi-gigabyte asset should not live in base APK/Git | Product distribution requirements change |
| Initial | Working title “Relay” is provisional | Avoid blocking implementation on branding | Human naming checkpoint |
| 2026-08-11 | Application ID `com.schilling3003.relay`; minSdk 26; target/compileSdk 34; ARM64 primary | Broad modern-device coverage; aligns with Moonshine/LiteRT-LM requirements | Library version constraints change |
| 2026-08-11 | LiteRT-LM Android `0.15.0`; Moonshine Voice `0.1.1` | Latest stable releases in Google/Maven repos as of build date | New stable releases verified and benchmarked |
| 2026-08-11 | First vertical slice uses deterministic fake engines; real engines behind interfaces | UX/state-machine can be judged before model integration | Interfaces must remain stable for real engine swap |
| 2026-08-11 | Kotlin 2.3.0 and Compose compiler 2.3.0 | Resolves LiteRT-LM 0.15.0 binary Kotlin metadata 2.3.0 incompatibility with the earlier Kotlin 2.0.21 compiler | Re-pin only if runtime/AGP issues surface on the reference device |
| 2026-08-11 | Real `litertlm-android` and `moonshine-voice` artifacts wired | Engine adapters (`GemmaTranslationEngine`, `MoonshineSpeechRecognizer`, `MoonshineSpeechSynthesizer`, `AudioTrackAudioPlayer`, `RealAudioRecorder`) compile behind stable interfaces | Replace only if runtime failures or unacceptable latency on reference device |
| 2026-08-11 | Moonshine STT/TTS model files downloaded on first use with explicit user action | Avoids bundling large assets in the base APK; network is used only for model acquisition, never for inference | Replace with bundled or SAF-imported models if offline-first setup is required from first launch |
| 2026-08-11 | Runtime `RECORD_AUDIO` permission requested at startup | Required for real `AudioRecord` capture; can be pre-granted via adb for emulator testing | Add a dedicated permission rationale screen if UX review requires it |

## Gate status

| Gate | Status | Evidence | Blocker/next step |
| --- | --- | --- | --- |
| Build | Passes debug with real engine artifacts | `./gradlew :app:compileDebugKotlin :app:lintDebug :app:testDebugUnitTest :app:assembleDebug` all green with `litertlm-android` and `moonshine-voice` enabled | Release build and APK signing config; R8/ProGuard rules for native engines |
| Offline | Not run | — | Real engines + model import on a device |
| Privacy | Reviewed | Manifest has `RECORD_AUDIO`; `moonshine-voice` AAR declares `INTERNET`/`ACCESS_NETWORK_STATE` for explicit on-demand model downloads. No telemetry, analytics, or transcript logging. Translation is on-device. | Verify no network path is used during inference; document downloader behavior |
| Core journey | Real engine adapters wired and compile; UI journey verified with fake engines on android-34 x86_64 emulator | `GemmaTranslationEngine`, `MoonshineSpeechRecognizer`, `MoonshineSpeechSynthesizer`, `AudioTrackAudioPlayer`, `RealAudioRecorder` integrated behind stable interfaces; state machine unchanged | Real `.litertlm` + Moonshine STT/TTS model files on a physical device for English ↔ Spanish two-turn test |
| Stability | Partial | Unit tests cover state transitions, cancellation, swap-while-recording, turn accumulation | Soak, lifecycle, rotation, process-death tests on device |
| Accessibility | Partial | Compose semantics on speak button, role/Button, content descriptions, large-text-safe scrollable layouts | TalkBack script, contrast checker, RTL/device screenshot suite |
| Correctness | Partial | `LanguageTest` covers direction/script/defaults; `ConversationViewModelTest` covers pipeline | Real translation corpus, parser tests, malformed-output recovery |
| Evidence | Partial | `PerformanceRecorder` interface and fake implementation; `BenchmarkReport` model | Real device traces and exported JSON |
| Independent review | Not run | — | Critic pass after this PR |

## Score history

| Round | Commit | Focus | Before | After | Evidence | Critic verdict |
| ---: | --- | --- | ---: | ---: | --- | --- |
| 1 | (this PR) | Reproducible build, fake-engine UI vertical slice, state-machine tests, emulator e2e fixes | 0 | not rated | Build/lint/unit tests green; debug APK produced; end-to-end emulator run verified default conversation, English→Spanish turn, swap, and tabletop mode after fixing BigSpeakButton press/release and setup routing | passed testing agent; pending independent critic review |
| 2 | (this PR) | Real LiteRT-LM/Moonshine engine integration | not rated | not rated | Kotlin 2.3.0/Compose compiler 2.3.0 resolve metadata mismatch; `GemmaTranslationEngine`, `MoonshineSpeechRecognizer`, `MoonshineSpeechSynthesizer`, `AudioTrackAudioPlayer`, `RealAudioRecorder` wired; build/lint/unit tests green | compiled; no model-asset or device evidence yet |

## Current benchmark summary

No measurements yet. Do not populate targets as if they were measurements.

## Open blockers/questions

- Select physical reference Android device.
- Confirm final application ID, product name, icon, and signing approach before
  external release.
- Verify current stable LiteRT-LM and Moonshine Android versions during setup.

## Failed attempts and lessons

1. `android.useAndroidX=true` was missing from `gradle.properties`, causing the
   build to fail with `Configuration :app:debugRuntimeClasspath contains AndroidX
   dependencies, but the android.useAndroidX property is not enabled`.
   **Fix:** added `gradle.properties` with `android.useAndroidX=true`.
2. `ic_launcher_round` was missing in `mipmap-anydpi-v26` while the manifest
   referenced it. **Fix:** added an adaptive-icon alias.
3. Vector drawables used `?attr/colorOnSurface` before the theme defined it.
   **Fix:** switched to opaque `#FF000000` fill; Compose `Icon` applies the correct
   `LocalContentColor` at runtime.
4. LiteRT-LM `0.15.0` ships Kotlin metadata compiled for Kotlin 2.3.0, which the
   initial Kotlin 2.0.21 compiler could not consume, causing a binary-metadata
   version error. **Fix:** bumped Kotlin and the Compose compiler plugin to 2.3.0
   and enabled `litertlm-android` and `moonshine-voice`; the build, lint, and
   unit tests now pass with the real engine artifacts.
5. End-to-end emulator test revealed the fake-engine UI was blocked by the setup
   screen (`FakeModelManager` defaulted to `Missing`) and the speak button
   long-press/release did not reliably fire `stopRecording` because `Button` +
   `pointerInput` captured stale `isRecording`/`enabled` snapshots.
   **Fix:** `FakeModelManager` now defaults to `Ready`; `SetupViewModel` routes to
   setup only when the model is not `Ready`; `BigSpeakButton` uses `Surface` +
   `rememberUpdatedState` + `tryAwaitRelease()` so current callbacks fire.

## Round template

Copy this section for each accepted round:

```markdown
### Round N — concise focus

- Baseline commit and score:
- Failed gate or highest-impact gap:
- Builder change:
- Commands/tests run:
- Rendered/device evidence:
- Fresh critic findings:
- Measured before/after:
- Regressions checked:
- Decision: accept / reject / blocked
- New score and gate status:
- Next highest-impact gap:
```

