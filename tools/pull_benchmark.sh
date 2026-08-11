#!/usr/bin/env bash
set -euo pipefail

PKG="com.schilling3003.relay"
DEST="${1:-./relay_benchmark_latest.json}"

# Try external files path first; fall back to run-as for devices that restrict it.
if adb shell "test -f /sdcard/Android/data/${PKG}/files/relay_benchmark_latest.json"; then
  adb pull "/sdcard/Android/data/${PKG}/files/relay_benchmark_latest.json" "${DEST}"
else
  echo "External path not accessible, trying run-as..."
  adb shell "run-as ${PKG} cat files/relay_benchmark_latest.json" > "${DEST}"
fi

echo "Benchmark report pulled to ${DEST}"
