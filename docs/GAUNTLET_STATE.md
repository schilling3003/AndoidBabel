# Gauntlet state ledger

This file survives long loops and context compaction. Update it after every
accepted round. Preserve failed attempts; do not rewrite history to look clean.

## Current status

- Phase: specification/bootstrap complete; Round 1 accepted
- Commit under evaluation: pending first PR
- Overall evidence-backed score: not rated / 100 (UI-only vertical slice; no
  physical-device measurements yet)
- Release gates: Build gate in progress; remaining gates require real engines or
  physical device evidence
- Physical reference device: not selected
- Current highest-impact gap: real LiteRT-LM and Moonshine engine integration
  blocked by Kotlin/compiler version coupling in this environment; engines are
  behind stable interfaces and will be wired in a later round
- Next action: land this scaffold/UI round, then run a critic pass and begin real
  engine integration on the reference device
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
| 2026-08-11 | Kotlin 2.0.21 and Compose BOM 2024.11.00 for first build | Stable, widely-supported combination; LiteRT-LM 0.15.0 compiled against newer Kotlin metadata (2.2/2.3) cannot be consumed by this compiler | Verify a matching Kotlin/Compose/LiteRT-LM triple before enabling real engine artifacts |
| 2026-08-11 | Real `litertlm-android` and `moonshine-voice` artifacts not included in first vertical slice | Keeps the build, tests, and screenshots runnable without resolving metadata incompatibility or downloading model assets | Re-enabled once a compatible compiler triple and the model import flow are verified |

## Gate status

| Gate | Status | Evidence | Blocker/next step |
| --- | --- | --- | --- |
| Build | In progress / passes debug | `./gradlew :app:compileDebugKotlin :app:lintDebug :app:testDebugUnitTest :app:assembleDebug` all green on this session | Release build and APK signing config; emulator/physical device runs |
| Offline | Not run | — | Real engines + model import on a device |
| Privacy | Reviewed | Manifest has only `RECORD_AUDIO`; no Internet permission, telemetry, analytics, or transcript logging in code | Formal privacy review after real persistence added |
| Core journey | UI-only with fake engines, e2e verified on android-34 x86_64 emulator | All supported languages in `Language`; state machine covers Recording→Transcribing→Translating→Speaking→Ready; default English→Spanish turn, language swap, and tabletop toggle exercised | Real STT/Gemma/TTS integration and physical-device two-turn test |
| Stability | Partial | Unit tests cover state transitions, cancellation, swap-while-recording, turn accumulation | Soak, lifecycle, rotation, process-death tests on device |
| Accessibility | Partial | Compose semantics on speak button, role/Button, content descriptions, large-text-safe scrollable layouts | TalkBack script, contrast checker, RTL/device screenshot suite |
| Correctness | Partial | `LanguageTest` covers direction/script/defaults; `ConversationViewModelTest` covers pipeline | Real translation corpus, parser tests, malformed-output recovery |
| Evidence | Partial | `PerformanceRecorder` interface and fake implementation; `BenchmarkReport` model | Real device traces and exported JSON |
| Independent review | Not run | — | Critic pass after this PR |

## Score history

| Round | Commit | Focus | Before | After | Evidence | Critic verdict |
| ---: | --- | --- | ---: | ---: | --- | --- |
| 1 | (this PR) | Reproducible build, fake-engine UI vertical slice, state-machine tests, emulator e2e fixes | 0 | not rated | Build/lint/unit tests green; debug APK produced; end-to-end emulator run verified default conversation, English→Spanish turn, swap, and tabletop mode after fixing BigSpeakButton press/release and setup routing | passed testing agent; pending independent critic review |

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
4. LiteRT-LM `0.15.0` ships Kotlin metadata compiled for a newer Kotlin
   (2.2/2.3) than the initial Kotlin 2.0.21 compiler could consume, causing a
   binary-metadata version error. **Fix for this round:** disabled the real engine
   dependencies and left them commented in `app/build.gradle.kts` behind stable
   interfaces so the build, UI, and tests can progress. Revisit with a verified
   Kotlin/Compose/LiteRT-LM triple before enabling real inference.
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

