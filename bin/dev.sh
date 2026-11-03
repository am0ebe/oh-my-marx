#!/usr/bin/env bash
# 📱 dev loop: build ➡ install ➡ launch on th fon over wifi · no cable, no taps on th fon
#
# once on th fon (same wifi as tow):
#   Settings ➡ Developer options ➡ Wireless debugging ON ➡ "Pair device with pairing code"
#   tow: adb pair IP:PORT CODE      (IP:PORT + 6-digit code shown on th fon)
# aftw: just `bin/dev.sh` — paired fons announce themselves via mDNS ➡ auto-connect
set -euo pipefail
cd "$(dirname "$0")/../android"
export JAVA_HOME=~/.local/opt/jdk21 ANDROID_HOME=~/Android/Sdk
adb=$ANDROID_HOME/platform-tools/adb

have_fon() { "$adb" devices | grep -qE '\sdevice$'; }

if ! have_fon; then
    # _adb-tls-connect = a paired fon w Wireless debugging on · last col = IP:PORT (port changes evy toggle)
    "$adb" mdns services | awk '/_adb-tls-connect/ {print $NF}' | while read -r hp; do
        "$adb" connect "$hp" >/dev/null 2>&1 || true
    done
fi
have_fon || { echo "📱 no fon: Wireless debugging ON + same wifi? · 1st time: $adb pair IP:PORT CODE"; exit 1; }

./gradlew --offline -q installDebug
"$adb" shell am start -n de.strix.marxversion/.MainActivity >/dev/null
echo "✔️ installed + launched"
