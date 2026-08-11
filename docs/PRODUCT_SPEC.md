# Product specification

## Product promise

An immediate, private conversation across a language barrier. Once language
assets and the Gemma model are installed, voice and text never leave the device.

Working title: **Relay**. This is provisional and must not block engineering.

## Target user and context

- Two people who do not share a language.
- Noisy or connectivity-poor travel, service, workplace, and community settings.
- One shared Android phone, used in portrait, landscape, or flat on a table.
- Users may be stressed, unfamiliar with the app, visually impaired, or using
  scripts with different reading directions.

## Supported languages for v1

| Code | Language | Direction considerations |
| --- | --- | --- |
| `en` | English | LTR |
| `ar` | Modern Standard Arabic | RTL |
| `es` | Spanish | LTR |
| `ja` | Japanese | LTR, mixed scripts |
| `zh` | Mandarin Chinese, Simplified | LTR, Han script |
| `ko` | Korean | LTR, Hangul |

Every supported source/target combination must work. Language packs may be
installed independently, and missing packs must never produce a dead end.

## Core journeys

### First run

1. App opens into a useful setup screen within one second, even though inference
   is not ready.
2. User sees plain-language storage and device requirements.
3. User imports a `.litertlm` file through the system file picker.
4. App validates compatibility without duplicating the model unnecessarily.
5. User installs or imports speech assets for selected languages.
6. App warms engines with visible, cancellable progress and reaches Ready.

### Conversation

1. User sees two languages and a clear direction.
2. User holds the control associated with their language and receives immediate
   visual, haptic, and accessible recording feedback.
3. Releasing ends capture. The UI progresses through Listening/Transcribing,
   Translating, and Speaking without freezing.
4. Source transcript appears first; translation appears as soon as available.
5. Spoken output begins promptly and can be stopped or replayed.
6. The other person can respond without navigating to another page.

### Recovery

- Permission denied: explain why the microphone is needed and provide a direct
  path to retry or settings.
- Missing/corrupt/incompatible model: preserve conversation settings, identify
  the exact issue, and allow replacement.
- Interrupted audio: stop safely and retain completed text.
- Engine out of memory or initialization failure: release resources, explain
  recovery, and never crash-loop.
- Background/rotation/process recreation: do not leak microphone, player, or
  model handles; restore stable UI state.

## Required features

- Push-to-talk as the default; tap-to-toggle is an accessibility option.
- Swap languages, replay output, stop speech, edit recognized source text, and
  retry translation.
- Portrait conversation mode and mirrored tabletop mode.
- Visible engine readiness and language-pack status.
- Import/remove/replace model without clearing preferences.
- Local-only recent conversation history, off by default, with one-action erase.
- Dark and light themes, system dynamic color where legible, large text, and
  TalkBack semantics.
- A privacy screen that accurately describes stored assets and text.

## Non-goals for v1

- Accounts, cloud sync, analytics, advertising, or remote inference.
- Camera/image translation, document translation, group conversations, or
  background listening.
- Voice cloning.
- Automatic language detection unless evidence shows it is fast and reliable
  for every supported language.
- A chatbot or general assistant experience.
- Play Store publication during the autonomous build.

## Success outcomes

- A new user completes a two-turn conversation without documentation.
- The app remains useful in airplane mode after setup.
- Users always know whether the app is recording, processing, or speaking.
- Perceived waiting is minimized through immediate feedback, partial results,
  prewarming, and cancellable operations.
- The release candidate meets `QUALITY_GATES.md` with reproducible evidence.

