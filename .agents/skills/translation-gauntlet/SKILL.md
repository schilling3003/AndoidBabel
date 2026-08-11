---
name: translation-gauntlet
description: Run one evidence-based builder/critic improvement round on the native Android offline translator.
argument-hint: "[optional focus area]"
triggers:
  - user
  - model
---

# Translation gauntlet — one round

Execute exactly one complete improvement round. `$ARGUMENTS` may suggest a
focus, but objective failed gates and measured impact take priority.

## 1. Recover state

Read, in order:

1. `AGENTS.md`
2. `docs/PRODUCT_SPEC.md`
3. `docs/UX_SPEC.md`
4. `docs/ARCHITECTURE.md`
5. `docs/QUALITY_GATES.md`
6. `docs/TEST_AND_BENCHMARK_PLAN.md`
7. `docs/GAUNTLET_STATE.md`
8. `docs/GEMMA_DEV_SKILL.md`

Inspect Git status, recent commits, current diff, CI/test state, and existing
evidence. Do not discard unrelated user changes. If the worktree is not clean,
identify whether the changes belong to the current round before proceeding.

## 2. Choose one target

Select the highest-impact unresolved release gate or scorecard gap that can be
improved and verified in this environment. Prefer, in order:

1. P0/P1 correctness, privacy, crash, or data-loss risk;
2. broken build/core journey/offline behavior;
3. measured end-to-end latency and UI jank;
4. conversation usability and state clarity;
5. accessibility, RTL, and recovery;
6. lower-severity polish or maintainability.

State the baseline, acceptance evidence, and regression surface before editing.
Do not choose a subjective redesign when a harder failed gate exists.

## 3. Separate builder and critic

Use a specialist builder to implement the smallest coherent fix. If managed
delegation is available, use a separate fresh-context critic. Otherwise perform
a distinct adversarial critic pass after implementation and do not let builder
rationale count as evidence.

The critic receives the relevant quality gate, baseline artifact/evidence, new
artifact/evidence, and reference bar—but not a request to agree with the
builder. For visual work it must inspect rendered screenshots or the running
app. For performance it must inspect real reports/traces. For behavior it must
inspect tests and exercise the path.

## 4. Implement and verify

- Make the narrowest change that can materially improve the target.
- Run focused tests continuously.
- Run the relevant regression suite before acceptance.
- Capture before/after evidence using the same device, build, corpus, and
  conditions whenever comparing performance.
- Never label emulator latency as physical-device evidence.
- Never weaken a test, quality gate, budget, permission, privacy constraint, or
  accessibility requirement merely to pass.
- Do not add model weights, signing secrets, personal audio, or huge traces.

## 5. Critic verdict

The fresh critic must return:

- PASS, FAIL, or BLOCKED;
- concrete evidence inspected;
- regression risks;
- score/gate movement justified by evidence;
- the single highest-impact remaining gap.

If FAIL, either repair within this round and re-run the critic or record the
failed attempt without committing it as an improvement. If two consecutive
approaches fail on the same metric, stop and escalate the architecture decision.

## 6. Persist and finish

Update `docs/GAUNTLET_STATE.md` with:

- baseline and resulting commit/diff;
- commands and tests actually run;
- evidence paths and whether emulator/physical device;
- before/after measurements;
- critic verdict;
- score and gate changes;
- failed attempts and next target.

Commit only a coherent, verified improvement. Review the final diff for secrets,
generated weights, accidental large files, weakened tests, and scope creep.

If all release gates genuinely pass and the evidence-backed score is at least
90/100, run the complete release verification once, update the ledger, and
report `GAUNTLET COMPLETE`. If hardware or human evidence is required, report
`BLOCKED` with an exact handoff procedure. Never fabricate completion.
