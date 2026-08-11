---
name: relay-emulator-e2e
description: How to run end-to-end Android emulator tests for the Relay offline translator app.
---

# Relay end-to-end emulator testing

Use this skill when asked to verify the Relay Android UI in an emulator, especially for UI-only/fake-engine gauntlet rounds.

## Environment assumptions

- Repo root is the local checkout (e.g. `/home/ubuntu/repos/AndoidBabel`).
- `ANDROID_HOME=/home/ubuntu/Android/Sdk` is exported.
- Android SDK command-line tools, `platform-tools`, `emulator`, `system-images;android-34;google_apis_playstore;x86_64`, and `platforms;android-34` are installed.

## Pre-flight checklist

1. Ensure the test user can access KVM:
   ```bash
   sudo chmod 666 /dev/kvm   # or add user to the kvm group and re-login
   ```
2. Check/resize the emulator window so the full phone UI is visible:
   ```bash
   wmctrl -i -r <emulator-window-id> -e 0,0,0,600,1170
   ```
3. Use `adb -s emulator-5554` for commands if the emulator is on the default port.

## Build and install

```bash
./gradlew :app:assembleDebug
adb -s emulator-5554 install -t -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 shell am start -n com.schilling3003.relay/.ui.MainActivity
```

## Inspecting the running UI

- Dump the accessibility tree:
  ```bash
  adb -s emulator-5554 shell uiautomator dump /sdcard/window_dump.xml
  adb -s emulator-5554 pull /sdcard/window_dump.xml ./window_dump.xml
  ```
- Screenshot:
  ```bash
  adb -s emulator-5554 exec-out screencap -p > screenshot.png
  ```

## Fake-engine UI-only rounds

For a UI-only round the fake `ModelManager` starts in `ModelState.Ready` and the
app opens directly to the conversation screen. Long-press the primary speak
control to start recording, release to run the staged pipeline, and verify the
state labels and fake transcript/translation appear.

## Core journey checks

1. Cold launch lands on `ConversationScreen` with source `English` and target `Spanish`.
2. Long-press speak → status `Listening…`, release → `Transcribing…` → `Translating…` → `Speaking translation…` → back to `Hold to speak`.
3. Swap languages exchanges source/target and the next turn reflects the new pair.
4. Tabletop toggle splits the screen into two mirrored speaker zones and toggles back.

## Known emulator gotchas

- The nested-virtualization warning dialog may block interactions; dismiss it with the OK button or set the emulator config flag `hw.gpu.enabled=yes` and use `-gpu swiftshader_indirect` if GPU issues arise.
- Play Store system images can display extra dialogs (Chrome Web Store, Google setup). Close unrelated windows (`wmctrl -c`) before the test.
