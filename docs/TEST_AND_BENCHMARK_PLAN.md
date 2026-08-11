# Test and benchmark plan

## Evidence hierarchy

1. Physical-device release-build measurement.
2. Instrumented Android test on emulator/device.
3. JVM/unit test.
4. Static review.
5. Unsupported assertion — not evidence.

Store small JSON/CSV reports and representative screenshots in a versioned
`evidence/` directory. Do not commit audio containing personal speech, model
weights, huge traces, or device identifiers beyond model/OS/SoC.

## Automated test layers

### Unit

- Conversation state-machine transitions and stale-turn rejection.
- Language-pair and RTL direction logic.
- Structured translation parsing and malformed-output recovery.
- Model/asset manifest validation, integrity, and storage calculations.
- Text chunking, cancellation, timing aggregation, and percentile calculation.
- View-model behavior with deterministic fake engines and virtual time.

### Compose UI and screenshots

- Every state listed in `UX_SPEC.md`.
- Compact/medium/expanded widths; portrait and landscape/tabletop.
- Light, dark, and high-contrast-friendly themes.
- Font scales 1.0, 1.3, and 2.0.
- English and Arabic RTL plus representative Japanese, Chinese, and Korean text.
- Stable controls while partial and final results change.
- Semantics tree assertions for labels, roles, enabled state, and announcements.

### Instrumented integration

- Microphone permission grant/deny/revoke.
- File picker model import, corrupt file, incompatible model, insufficient
  storage, replacement, and removal.
- Process recreation, rotation, background/foreground, phone call/audio focus,
  wired/Bluetooth route changes, and rapid cancellation.
- Airplane-mode two-turn conversation.
- Engine initialization failure and memory-pressure recovery.
- 100-turn scripted soak with alternating speakers and language swaps.

Use fake audio/model engines in CI where real model execution is impractical,
but maintain an explicitly labeled physical-device suite for release evidence.

## Translation corpus

Create a small, original, versioned corpus rather than scraping copyrighted
examples. Cover at least:

- greetings and short travel questions;
- numbers, times, currency, dates, names, and addresses;
- negation and safety-critical contrasts;
- polite versus direct register;
- ambiguous pronouns and context-light phrases;
- punctuation, code switching, and mixed-direction text;
- noisy 3 s, 5 s, and 10 s recordings from consenting test speakers or
  appropriately licensed synthetic speech.

For each supported language, maintain at least 30 utterances. Do not demand one
exact translation when multiple natural renderings are correct. Judge meaning
preservation, omissions/additions, names/numbers, fluency, and script.

## Performance instrumentation

Use monotonic timestamps around these events:

- press received;
- recording feedback rendered;
- recorder started/stopped;
- audio available;
- STT started/partial/final;
- translation queued/first token/final;
- TTS queued/first audio/playback complete;
- cancellation requested/completed;
- model and language-engine load start/ready;
- memory/thermal snapshot where platform APIs permit.

Export a machine-readable report containing app commit, build type, device
profile, model versions, pipeline implementation, corpus ID, warm/cold flag,
sample count, failures, p50, p95, max, and per-stage durations. Never log the
spoken content in production performance logs.

Use Android Macrobenchmark, Baseline Profiles, frame metrics/JankStats, and
Perfetto or Android Studio profiling for bottleneck diagnosis. Run benchmarks
with stable thermal conditions and identify throttled runs rather than silently
averaging them.

## Usability gauntlet

Run these tasks with at least three people unfamiliar with the implementation
before calling the UI complete; five is preferred:

1. Import a supplied model and make the app Ready.
2. Set English ↔ another language.
3. Speak and obtain one translation.
4. Let the other person reply.
5. Correct a transcription and retry.
6. Stop speech and replay it.
7. Recover from a missing language pack.
8. Switch into and out of tabletop mode.
9. Erase local history.

Record completion, time, wrong taps, hesitation, help requested, and comments.
Do not coach unless the user is blocked; record the blockage first.

## Visual critic procedure

For each major state:

1. Capture the app at matched dimensions.
2. Assemble a comparison with one or more lawful public reference screenshots
   showing an analogous state.
3. Hide product names when practical and randomize left/right order.
4. Ask a fresh critic to rank action clarity, hierarchy, readability, status
   clarity, ergonomic confidence, and perceived polish.
5. Require the critic to cite visible evidence and the single largest gap.
6. Reject feedback that merely prefers a brand or asks for visual copying.

## Release evidence report

The final report must list:

- commit and APK checksum;
- dependency/model/asset versions and licenses;
- passed, failed, and blocked gates;
- performance table with raw-report link/path;
- screenshots and accessibility results;
- test commands and outcomes;
- known limitations and reproduction steps;
- whether evidence came from emulator or named physical reference hardware.

