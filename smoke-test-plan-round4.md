# Relay Android Round 4 end-to-end smoke test plan

Branch under test: `devin/round1-scaffold-ui` (Round 4)  
Target: `android-34` x86_64 emulator (`emulator-5554`), debug APK.

## Goal

Verify the Round 4 build compiles, passes unit tests and lint, installs, launches, imports a fake `.litertlm` model, reaches the conversation screen, opens Settings from both setup and conversation, supports language selection, lists all-language voice model downloads, toggles tabletop mode, and does not crash.

## Preconditions

- Emulator `android-34` booted and reachable as `emulator-5554`.
- `/dev/kvm` writable by the test user.
- Debug APK built at `app/build/outputs/apk/debug/app-debug.apk`.

## TC0: Build, unit tests, and lint

1. Run `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`.
   **Expected:** `BUILD SUCCESSFUL` with no failing unit tests or lint errors.

## TC1: Fresh install and setup screen

1. Uninstall any existing Relay package:
   `adb -s emulator-5554 uninstall com.schilling3003.relay`
2. Install the Round 4 debug APK:
   `adb -s emulator-5554 install -t -r app/build/outputs/apk/debug/app-debug.apk`
3. Pre-grant `RECORD_AUDIO` to avoid the permission dialog interrupting the flow:
   `adb -s emulator-5554 shell pm grant com.schilling3003.relay android.permission.RECORD_AUDIO`
4. Clear logcat: `adb -s emulator-5554 logcat -c`
5. Launch the app: `adb -s emulator-5554 shell am start -n com.schilling3003.relay/.ui.MainActivity`
6. **Expected:** The app reaches **Set up Relay** showing:
   - Title `Set up Relay`
   - Card `A Gemma translation model is needed before you can translate offline.`
   - Body `Requires ~6 GB free storage. The model file is not included with the app.`
   - Button `Import .litertlm model`
   - Text link `More languages & voices`
7. Capture a screenshot.

## TC2: Import a fake `.litertlm` and reach the conversation screen

### Option A (preferred): UI file-picker import
1. Push a tiny fake `.litertlm` to the emulator's shared Downloads folder:
   `adb -s emulator-5554 push /tmp/fake.litertlm /sdcard/Download/fake.litertlm`
2. Tap **Import .litertlm model**.
3. In the system file picker, navigate to Downloads and select `fake.litertlm`.
4. **Expected:** The setup screen updates to `Translation model ready.` and shows the `Voice models` card with four pending items (English/Spanish STT + TTS).

### Option B (fallback): pre-stage the file to simulate a successful import
If the file picker cannot be driven reliably:
1. Force-stop the app.
2. Create a 1-byte file named `gemma.litertlm` under the app's private model directory:
   ```
   adb -s emulator-5554 shell run-as com.schilling3003.relay mkdir -p files/models
   adb -s emulator-5554 shell run-as com.schilling3003.relay cp /data/local/tmp/fake.litertlm files/models/gemma.litertlm
   ```
3. Relaunch the app.
4. **Expected:** The setup screen shows `Translation model ready.` and the `Voice models` card appears with English/Spanish STT + TTS items.

### Reach conversation
1. Tap **Skip — download later** (or **Ready to translate** if enabled).
2. **Expected:** The app transitions to the conversation screen with `English · English` ↔ `Spanish · Español` in the top bar and a `Hold to speak` button.
3. Check `adb logcat` for `FATAL EXCEPTION` / `AndroidRuntime` / `com.schilling3003.relay` crashes.

## TC3: Settings from setup and conversation

1. From setup (before reaching conversation), tap **More languages & voices**.
   **Expected:** The **Settings** screen opens with:
   - Title `Settings`
   - Back arrow
   - `I speak` and `They speak` language dropdowns
   - `Tabletop mode` switch
   - `Voice models` card with all-language download rows
2. Go back (arrow) to setup.
3. Reach the conversation screen (TC2).
4. Tap the **Settings** icon in the top bar.
   **Expected:** The same **Settings** screen opens.

## TC4: Language dropdowns and all-language voice download list

1. In **Settings**, tap the `I speak` dropdown and select a different language (e.g. `Arabic · العربية`).
   **Expected:** The dropdown closes and `I speak` now shows `Arabic · العربية`.
2. Tap the `They speak` dropdown and select a different language (e.g. `English · English`).
   **Expected:** `They speak` updates; the source and target are not the same.
3. Scroll to the **Voice models** card.
   **Expected:** The card title is `Voice models` and subtitle is `Download speech-to-text and text-to-speech data for every supported language.`
4. **Expected:** The list is grouped by language and contains 12 rows total: for each of the six supported languages (`English`, `Arabic`, `Spanish`, `Japanese`, `Mandarin`, `Korean`) there is one `speech-to-text` row and one `text-to-speech` row.
5. Capture a screenshot of the full list (scroll if needed).

## TC5: Tabletop mode toggle

1. In **Settings**, toggle **Tabletop mode** on.
2. Go back to the conversation screen.
   **Expected:** The screen splits into two side-by-side speaker zones (left = source, right = target). Each zone shows its language label, a `Hold to speak` button, and there is **no central menu column**.
3. Open **Settings** again and toggle **Tabletop mode** off.
4. Go back.
   **Expected:** Conversation returns to portrait single-column layout.

## TC6: Logcat crash check

1. After all interactions, run `adb -s emulator-5554 logcat -d`.
2. **Expected:** No `FATAL EXCEPTION`, no `AndroidRuntime` crash targeting `com.schilling3003.relay`, and no `Process: com.schilling3003.relay` death unrelated to intentional `am force-stop`.

## Evidence

- Recording of the full emulator sequence.
- Screenshots:
  - `/tmp/relay_round4_setup_missing.png` — initial setup screen
  - `/tmp/relay_round4_setup_ready.png` — setup after fake model import/ready
  - `/tmp/relay_round4_conversation.png` — conversation screen
  - `/tmp/relay_round4_settings.png` — Settings screen with language dropdowns and voice models
  - `/tmp/relay_round4_tabletop.png` — tabletop mode with two speaker zones
  - `/tmp/relay_round4_logcat.txt` — logcat dump
- UIAutomator dumps captured at key screens.

## Pass/fail criteria

- `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` passes.
- App launches and reaches **Set up Relay** without crash.
- Fake `.litertlm` import (or pre-staged model) makes setup show `Translation model ready.` and the conversation screen is reachable.
- Settings opens from both setup (`More languages & voices`) and conversation (top bar settings icon).
- Language dropdowns update source/target.
- All-language voice models list shows 12 rows (6 languages × STT + TTS).
- Tabletop mode splits the screen into two side-by-side zones with no central menu.
- No `FATAL EXCEPTION` / `AndroidRuntime` / `com.schilling3003.relay` crash in `adb logcat`.
