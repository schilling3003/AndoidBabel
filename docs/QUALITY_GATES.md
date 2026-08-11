# Gauntlet quality gates

These gates prevent the project from stopping at “works on my machine.” A score
cannot override a failed release gate.

## Reference hardware and evidence

Select at least one real ARM64 Android phone with 8 GB RAM or more as the
reference device. Record model, SoC, Android version, thermal state, power mode,
free memory, model file/version, app commit, and release build configuration.
Emulator results are valid for correctness and screenshots, not final latency,
memory, audio-routing, thermal, or microphone gates.

## Non-negotiable release gates

| Gate | Pass condition |
| --- | --- |
| Build | Clean checkout builds, tests, lints, and produces an installable arm64 release APK. |
| Offline | After assets are installed, the complete two-turn journey passes in airplane mode with no attempted network dependency. |
| Privacy | No transcript/audio telemetry, production transcript logging, account, or cloud inference. |
| Core journey | Every supported language can be source and target; the two-person reply flow needs no navigation. |
| Stability | Zero crashes/ANRs in the scripted 100-turn soak and lifecycle/interruption suite. |
| Accessibility | TalkBack, font scale 2.0, contrast, touch targets, and RTL checks pass with evidence. |
| Correctness | No P0/P1 defect; structured translation failures and missing assets recover without app restart. |
| Evidence | Every claimed measurement includes device, build, corpus, sample count, p50, and p95. |
| Independent review | Fresh critic finds no unresolved blocker and total score is at least 90/100. |

## Performance budgets

Measure release builds on the reference phone. Exclude human speaking duration.
Use at least 30 warm samples and report cold behavior separately.

| Metric | Target | Hard ceiling |
| --- | ---: | ---: |
| Press-to-visible/haptic recording feedback | ≤50 ms p95 | 100 ms p95 |
| App launch to usable setup/ready UI | ≤1,000 ms p95 | 1,500 ms p95 |
| Warm stop-to-final-transcript, 5 s utterance | ≤800 ms p50 / 1,400 ms p95 | 2,000 ms p95 |
| Warm transcript-to-complete translation, ≤15 source words | ≤1,500 ms p50 / 2,500 ms p95 | 4,000 ms p95 |
| Warm stop-to-visible translation | ≤2,500 ms p50 / 3,500 ms p95 | 5,000 ms p95 |
| Translation-visible to audible TTS start | ≤400 ms p50 / 700 ms p95 | 1,200 ms p95 |
| UI jank during capture/processing | <3% slow frames | <5% slow frames |

If current on-device libraries make a target infeasible, retain the measurement,
profile the bottleneck, optimize the highest contributor, and document the
remaining limitation. Never alter a budget merely to make the score pass; a
human may revise a budget after reviewing evidence.

## Quality scorecard — 100 points

| Category | Weight | Critic evidence |
| --- | ---: | --- |
| Conversation usability | 25 | Moderated or proxy task walkthroughs, mis-tap count, time to first successful turn, rendered-state review. |
| Perceived and measured speed | 25 | Physical-device traces and exported stage timings; immediate-feedback inspection. |
| Visual/state quality | 15 | Screenshots of every state across portrait, tabletop, dark/light, large text, and RTL; blind/reference comparison. |
| Voice interaction quality | 10 | STT/TTS corpus results, interruption, replay, audio-route, and noisy-room checks. |
| Reliability and recovery | 10 | Soak, lifecycle, permission, storage, memory, corruption, and cancellation tests. |
| Accessibility/i18n | 10 | TalkBack script, semantics checks, contrast, font scale, RTL, and script rendering. |
| Engineering/release quality | 5 | CI, static analysis, dependency audit, reproducibility, privacy review, documentation. |

Scoring rules:

- 0–59: prototype; not credible.
- 60–79: functional but clearly below the reference bar.
- 80–89: strong beta; continue the gauntlet.
- 90–100: release candidate only if every non-negotiable gate also passes.
- A score without evidence is zero for that item.
- The critic must name the single highest-impact gap and the evidence that would
  prove it fixed.

## Defect severity

- **P0:** privacy breach, data loss, unsafe deletion, unusable core app, or
  persistent crash loop.
- **P1:** crash/ANR in a core journey, wrong translation direction, microphone
  continues unexpectedly, offline promise broken, inaccessible primary action,
  or supported language cannot complete a turn.
- **P2:** significant recovery, performance, layout, or audio problem with a
  workaround.
- **P3:** polish issue that does not obscure state or action.

## Loop budget and stopping

A single unattended `/loop` run may perform at most 12 accepted improvement
rounds or 8 hours, whichever comes first. This is a safety/cost checkpoint, not
a declaration that the product is finished. At the checkpoint, summarize score
movement and continue only with a new explicit run.

Stop the gauntlet when:

1. all gates pass and the score is ≥90 with independent evidence; or
2. the next gate requires a physical device, human usability test, unavailable
   asset, credential, licensing decision, or material product choice; or
3. two consecutive attempts fail to improve the same measured metric, in which
   case record the attempts and escalate the architectural decision.

