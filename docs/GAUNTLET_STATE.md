# Gauntlet state ledger

This file survives long loops and context compaction. Update it after every
accepted round. Preserve failed attempts; do not rewrite history to look clean.

## Current status

- Phase: specification/bootstrap
- Commit under evaluation: not yet created
- Overall evidence-backed score: 0/100
- Release gates: not evaluated
- Physical reference device: not selected
- Current highest-impact gap: no Android implementation
- Next action: validate specs, scaffold reproducible project, and build the
  deterministic fake-engine UI vertical slice

## Product decisions

| Date | Decision | Evidence/reason | Revisit trigger |
| --- | --- | --- | --- |
| Initial | Native Kotlin + Compose; no web/Python production path | Usability, latency, lifecycle, and Android integration goals | Only if a native library is impossible to integrate |
| Initial | Staged Moonshine STT → Gemma text → Moonshine TTS is the first pipeline | Lowest-risk path and explicit transcript | Native Gemma audio wins the controlled benchmark |
| Initial | Gemma model imported separately | Multi-gigabyte asset should not live in base APK/Git | Product distribution requirements change |
| Initial | Working title “Relay” is provisional | Avoid blocking implementation on branding | Human naming checkpoint |

## Gate status

| Gate | Status | Evidence | Blocker/next step |
| --- | --- | --- | --- |
| Build | Not run | — | Scaffold project |
| Offline | Not run | — | Implement vertical slice |
| Privacy | Not run | — | Add manifest/logging review |
| Core journey | Not run | — | Implement fake then real engines |
| Stability | Not run | — | Add lifecycle and soak tests |
| Accessibility | Not run | — | Add semantics/screenshots/manual script |
| Correctness | Not run | — | Add state/parser/corpus tests |
| Evidence | Not run | — | Add performance recorder/export |
| Independent review | Not run | — | Run critic after rendered artifact exists |

## Score history

| Round | Commit | Focus | Before | After | Evidence | Critic verdict |
| ---: | --- | --- | ---: | ---: | --- | --- |

## Current benchmark summary

No measurements yet. Do not populate targets as if they were measurements.

## Open blockers/questions

- Select physical reference Android device.
- Confirm final application ID, product name, icon, and signing approach before
  external release.
- Verify current stable LiteRT-LM and Moonshine Android versions during setup.

## Failed attempts and lessons

None yet.

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

