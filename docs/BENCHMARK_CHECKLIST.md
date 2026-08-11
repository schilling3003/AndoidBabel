# Relay on-device benchmark checklist

Use this checklist to run the real English ↔ Spanish vertical slice on your own
Android device and send back the evidence we need to complete the Round 2 gate.

## What you will produce

1. The `app-release.apk` from this branch installed on a physical phone.
2. A pulled `relay_benchmark_latest.json` file after several conversation turns.
3. A `logcat` capture during the test run.
4. A short note with device model, SoC, Android version, and the `.litertlm` file
   you imported.
5. (Optional) screenshots or a screen recording.

## Device and host requirements

- Android phone or tablet, ARM64, Android 8.0+ (API 26+). 8 GB RAM strongly
  recommended for Gemma 4 E2B.
- USB debugging enabled and `adb` available on your computer.
- A `.litertlm` Gemma 4 E2B translation model file. Relay does not bundle the
  model; you must obtain a compatible file separately and import it through the
  first-run Storage Access Framework flow.
- Network access for the very first run so `moonshine-voice` can download the
  STT/TTS model files. After that, translation is fully offline.

## Build the release APK

From the repo root:

```bash
export ANDROID_HOME=/path/to/Android/Sdk
./gradlew :app:assembleRelease
```

The output is:

```
app/build/outputs/apk/release/app-release.apk
```

This APK is built for `arm64-v8a` only and is unsigned. You can install it with
`adb` for development testing.

## Install and prepare

1. Connect the device and verify `adb` sees it:

   ```bash
   adb devices
   ```

2. Install the APK:

   ```bash
   adb install -r app/build/outputs/apk/release/app-release.apk
   ```

3. Grant microphone permission:

   ```bash
   adb shell pm grant com.schilling3003.relay android.permission.RECORD_AUDIO
   ```

4. Clear any stale benchmark data:

   ```bash
   adb shell rm -f /sdcard/Android/data/com.schilling3003.relay/files/relay_benchmark_*.json
   adb shell rm -f /sdcard/Android/data/com.schilling3003.relay/files/relay_benchmark_latest.json
   ```

## Import the Gemma model

1. Put the `.litertlm` file on the device storage (e.g. `Download/`).
2. Open the **Relay** app.
3. On **Set up Relay**, tap **Import .litertlm model** and select the file.
4. Wait for the model to validate and warm. The status changes to ready when
   the translation engine is loaded.

## Run the English ↔ Spanish vertical slice

1. Leave the source language as **English** and target as **Spanish**.
2. Long-press **Hold to speak**, say a short English phrase (5–15 words), and
   release.
3. Verify the UI shows:
   - `Listening…` while your finger is down.
   - `Transcribing…` after release.
   - `Translating…`.
   - `Speaking translation…`.
   - Final transcript and translation text appear.
   - You hear the translated Spanish audio.
4. Tap the swap button to make **Spanish** the source and **English** the target.
5. Long-press again and speak in Spanish.
6. Repeat at least **10 warm turns** in each direction for meaningful p50/p95
   numbers.

## Offline validation

1. After the first successful turn, put the device in **Airplane mode**.
2. Perform another English → Spanish turn.
3. Confirm it still completes without network access.
4. Capture `logcat` and check that there are no network-related errors.

## Capture benchmark report and logs

Every completed or cancelled turn writes a JSON report to the app's external
files directory and logs the path under the `RelayBenchmark` tag.

1. Run the test.
2. Pull the latest benchmark JSON:

   ```bash
   adb pull /sdcard/Android/data/com.schilling3003.relay/files/relay_benchmark_latest.json ./relay_benchmark_latest.json
   ```

   If that path is not accessible on your device, use:

   ```bash
   adb shell run-as com.schilling3003.relay cat files/relay_benchmark_latest.json > relay_benchmark_latest.json
   ```

3. Pull logcat:

   ```bash
   adb logcat -d -s RelayPerf:D RelayBenchmark:D RelayApplication:D AndroidRuntime:E '*:F' > relay_logcat.txt
   ```

4. Convert the JSON report to a readable summary:

   ```bash
   python tools/extract_benchmark.py relay_benchmark_latest.json > relay_benchmark_report.md
   ```

## Send back results

Open a PR comment or issue with:

- `relay_benchmark_latest.json`
- `relay_logcat.txt`
- `relay_benchmark_report.md`
- Device model, SoC (`adb shell getprop ro.hardware`), Android version.
- Gemma `.litertlm` file name / source.
- Any crashes, wrong translations, missing audio, or UI problems.

We will use this evidence to update `docs/GAUNTLET_STATE.md`, score Round 2, and
plan the next track.
