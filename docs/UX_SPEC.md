# UX and interface specification

## Design principles

1. **One obvious next action.** Recording controls dominate; settings do not.
2. **Direction is unmistakable.** Color, label, position, and language name all
   communicate who is speaking and where the translation goes. Never rely on
   color alone.
3. **Speed is visible.** Immediate feedback and progressive results matter as
   much as raw inference time.
4. **Shared-device dignity.** Both participants receive equal visual weight.
5. **Plain language.** Say “Preparing translator,” not “Loading E2B executor.”
6. **No deceptive readiness.** A recording control is enabled only when the
   required engines and assets are usable.

## Primary screen

Use a single conversation surface rather than separate pages per speaker.

- A compact top region shows source and target language selectors, a swap
  control, readiness, and access to settings.
- The center shows the current source transcript and translated result with
  strong typographic separation. Previous turns recede rather than compete.
- The bottom provides two large, labeled push-to-talk targets—one per language.
  Each target must be reachable with one thumb and at least 64 dp on its
  shortest dimension; aim substantially larger on normal phones.
- Stop and replay controls appear contextually without moving the primary
  recording targets.
- Settings, diagnostics, and model management never obscure an active recording.

Do not use a tiny floating microphone button, hidden keyboard shortcuts, an
unlabeled waveform as status, or a chat bubble layout that makes translation
direction ambiguous.

## Tabletop mode

In landscape or when explicitly selected, split the screen into two mirrored
speaker zones facing opposite edges. Each side has:

- its own language name and push-to-talk control;
- readable transcript/translation orientation for that participant;
- the same semantic state and accessibility labels;
- a center swap/exit affordance protected from accidental taps.

Do not force automatic tabletop rotation solely from device orientation; retain
the user's explicit mode choice.

## Required states

Create deterministic previews and screenshot tests for all of these:

1. first run, no model;
2. importing and validating model;
3. installing language assets;
4. warming engines;
5. ready;
6. recording with live level and elapsed time;
7. transcribing with optional partial text;
8. translating;
9. speaking;
10. completed turn;
11. microphone permission denied;
12. missing language asset;
13. model incompatible/corrupt;
14. operation cancelled/interrupted;
15. recoverable inference failure;
16. no storage/memory warning;
17. offline-ready confirmation.

Processing states may morph within the same surface; they must not cause layout
jumps that move the user's controls.

## Feedback requirements

- Touch-down acknowledgment: visual and haptic response in the same rendered
  frame. Optional start tone must respect system audio/accessibility settings.
- Active recording: persistent high-contrast label, timer, and level indicator.
- Release: immediate transition to processing; never leave a stale recording
  indicator while inference starts.
- Partial transcript: update stably and distinguish provisional text.
- Translation: reveal progressively if the API streams; do not animate every
  character in a way that slows reading.
- TTS: show Speaking, expose Stop, and permit Replay afterward.
- Errors: name the failed stage and provide one primary recovery action.

## Accessibility and internationalization

- Meet WCAG AA contrast for text and essential controls.
- Every interactive element has a concise content description and role.
- TalkBack announcements occur on meaningful state transitions without reading
  every partial token.
- Support font scale 2.0 without clipping, overlap, or unreachable actions.
- Touch targets are at least 48 dp; primary speech targets are at least 64 dp.
- Respect reduced motion, haptic, and audio preferences.
- Test Arabic with true RTL layout and mixed-direction numbers/punctuation.
- Do not flag, abbreviate, or represent a language solely by a country.
- Keep recognized and translated text selectable; protect push-to-talk gestures
  from text-selection conflicts.

## Original visual-quality bar

Reference leading translation apps for hierarchy, state clarity, ergonomics,
and error recovery. Compare screenshots side by side at equivalent states, but
judge this product against these questions:

- Can a first-time user identify how each person speaks in under five seconds?
- Does the screen look intentional in every state, not just Ready?
- Is the current stage obvious from two meters away?
- Are both languages readable at large text and in RTL?
- Do controls remain stable while results arrive?
- Is the product recognizable as its own design?

The critic fails the round if visual approval is based only on source-code
inspection. It must inspect rendered screenshots or the running app.

