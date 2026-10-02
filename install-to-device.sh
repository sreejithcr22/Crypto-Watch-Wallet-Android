#!/usr/bin/env bash
#
# Install the signed release APK on a physical device connected over adb TCP/IP.
#
# Usage (run this on the Mac that has the device paired/connected):
#   ./install-to-device.sh                 # the only/authorized device
#   ./install-to-device.sh 192.168.0.7:5555
#
# Remote/one-off usage from another machine that can see the device:
#   adb connect <ip>:5555                  # device must have `adb tcpip 5555` enabled
#   ./install-to-device.sh <ip>:5555
#
# For Android 11+ Wireless debugging instead of tcpip 5555:
#   adb pair <ip>:<pairing-port> <6-digit-code>     # code shown in
#                                                 # Settings > Developer options >
#                                                 # Wireless debugging > Pair device
#   adb connect <ip>:<connect-port>      # port shown on the Wireless debugging screen
#   ./install-to-device.sh <ip>:<connect-port>
#
set -euo pipefail

APK="${APK:-app/build/outputs/apk/release/app-release.apk}"
PKG="com.codit.cryptowatchwallet"
MAIN_ACTIVITY="$PKG/.activity.MainActivity"
BUILD_TOOLS="${BUILD_TOOLS:-$(ls -d "$HOME/Library/Android/sdk/build-tools/"* 2>/dev/null | sort -V | tail -1 || true)}"

# --- locate adb (macOS SDK, Homebrew, PATH) --------------------------------
if ! command -v adb >/dev/null 2>&1; then
  for candidate in "$HOME/Library/Android/sdk/platform-tools/adb" \
                   "$HOME/Android/Sdk/platform-tools/adb" \
                   /usr/local/bin/adb /opt/homebrew/bin/adb; do
    [ -x "$candidate" ] && export PATH="$(dirname "$candidate"):$PATH" && break
  done
fi
command -v adb >/dev/null 2>&1 || { echo "adb not found - set ANDROID_HOME/PATH" >&2; exit 1; }

# --- pick the device -------------------------------------------------------
if [ $# -ge 1 ]; then
  case "$1" in
    *:*) adb connect "$1" >/dev/null 2>&1 || true ;;
  esac
  SERIAL="$1"
else
  DEVICES="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"
  if [ -z "$DEVICES" ]; then echo "no authorized device found (run 'adb devices')" >&2; exit 1; fi
  COUNT="$(printf '%s\n' "$DEVICES" | wc -l | tr -d ' ')"
  if [ "$COUNT" -gt 1 ]; then
    echo "multiple devices:"; printf '  %s\n' "$DEVICES"; echo "usage: $0 <serial>" >&2; exit 1
  fi
  SERIAL="$DEVICES"
fi
A=(adb -s "$SERIAL")
"${A[@]}" wait-for-device

echo "device : $SERIAL  ($("${A[@]}" shell getprop ro.product.model | tr -d '\r') / Android $("${A[@]}" shell getprop ro.build.version.release | tr -d '\r'))"
[ -f "$APK" ] || { echo "APK not found - run: ./gradlew assembleRelease" >&2; exit 1; }
if [ -n "$BUILD_TOOLS" ] && [ -x "$BUILD_TOOLS/aapt2" ]; then
  BADGING="$("$BUILD_TOOLS/aapt2" dump badging "$APK" 2>/dev/null || true)"
  CERTS="$("$BUILD_TOOLS/apksigner" verify --print-certs "$APK" 2>/dev/null || true)"
  printf '%s\n' "$BADGING" | sed -n '1p'
  printf '%s\n' "$CERTS" | sed -n '1,2p'
fi

# --- install ---------------------------------------------------------------
# -r keeps app data (upgrade). If the installed app was signed by the Play
# app-signing key the signature differs and the update is rejected with
# INSTALL_FAILED_UPDATE_INCOMPATIBLE -> uninstall first (loses local data).
if ! "${A[@]}" install -r "$APK"; then
  echo "retrying after uninstall (installed build is signed with a different key)" >&2
  "${A[@]}" uninstall "$PKG" >/dev/null
  "${A[@]}" install "$APK"
fi

"${A[@]}" shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS 2>/dev/null || true

# --- launch and show the crash buffer --------------------------------------
"${A[@]}" logcat -c
"${A[@]}" shell am force-stop "$PKG"
"${A[@]}" shell am start -n "$MAIN_ACTIVITY" >/dev/null
sleep 6
echo "running: $("${A[@]}" shell pidof "$PKG" | tr -d '\r')"
echo "--- crash buffer ---"
CRASH="$("${A[@]}" logcat -d -b crash 2>/dev/null || true)"
printf '%s\n' "$CRASH" | tail -40