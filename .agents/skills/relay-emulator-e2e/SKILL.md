---
name: relay-emulator-e2e
description: How to run end-to-end Android emulator tests for the Relay offline translator app.
---

# Relay end-to-end emulator testing

Use this skill when asked to verify the Relay Android UI in an emulator, for UI-only/fake-engine gauntlet rounds or real-engine smoke tests.

## Environment assumptions

- Repo root is the local checkout (e.g. `/home/ubuntu/repos/AndoidBabel`).
- `ANDROID_HOME=/home/ubuntu/Android/Sdk` is exported.
- Android SDK command-line tools, `platform-tools`, `emulator`, `system-images;android-34;google_apis_playstore;x86_64`, and `platforms/android-34` are installed.

## Pre-flight checklist

1. Ensure the test user can access KVM:
   ```bash
   sudo chmod 666 /dev/kvm   # or add user to the kvm group and re-login
   ```
2. Boot the AVD (skip if already running):
   ```bash
   $ANDROID_HOME/emulator/emulator -avd android-34 -no-snapshot-load -gpu swiftshader_indirect -no-boot-anim -writable-system -port 5554
   ```
3. Check/resize the emulator window so the full phone UI is visible:
   ```bash
   wmctrl -i -r <emulator-window-id> -e 0,0,0,600,1170
   ```
4. Dismiss the nested-virtualization warning dialog by clicking its OK button, or set `hw.gpu.enabled=yes` and use `-gpu swiftshader_indirect`.
5. Use `adb -s emulator-5554` for commands if the emulator is on the default port.

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

## Real-engine smoke test

For rounds where `RelayApplication.useFakeEngines = false` and no `.litertlm` or Moonshine model files are installed:

1. Build/install the APK.
2. Launch the app; `MainActivity` requests `RECORD_AUDIO` on first start.
3. Expect a system permission dialog (`Allow Relay to record audio?`).
4. After any permission response, the app should land on the **Set up Relay** screen with the status card:
   > "A Gemma translation model is needed before you can translate offline."
5. To skip the dialog on subsequent runs, pre-grant the permission:
   ```bash
   adb -s emulator-5554 shell pm grant com.schilling3003.relay android.permission.RECORD_AUDIO
   adb -s emulator-5554 shell am force-stop com.schilling3003.relay
   adb -s emulator-5554 shell am start -n com.schilling3003.relay/.ui.MainActivity
   ```
6. Check `adb logcat` for `FATAL EXCEPTION`, `AndroidRuntime`, or `com.schilling3003.relay` crashes.

## Fake-engine UI-only rounds

For a UI-only round, the fake `ModelManager` is configured to start in `ModelState.Ready` and the app opens directly to the conversation screen. Long-press the primary speak control to start recording, release to run the staged pipeline, and verify the state labels and fake transcript/translation appear.

## Core journey checks (fake/UI-only)

1. Cold launch lands on `ConversationScreen` with source `English` and target `Spanish`.
2. Long-press speak → status `Listening…`, release → `Transcribing…` → `Translating…` → `Speaking translation…` → back to `Hold to speak`.
3. Swap languages exchanges source/target and the next turn reflects the new pair.
4. Tabletop toggle splits the screen into two mirrored speaker zones and toggles back.

## Known emulator gotchas

- The nested-virtualization warning dialog may block interactions; dismiss it with the OK button.
- Play Store system images can display extra dialogs (Chrome Web Store, Google setup). Close unrelated windows (`wmctrl -c`) before the test.
- If the real `litertlm` or `moonshine` artifacts fail to resolve, verify the Kotlin version in `gradle/libs.versions.toml` matches the native library metadata.
