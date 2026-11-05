#!/usr/bin/env bash
# 🧪 deploy = a dev build onto ur own fon: via Obtainium (dev channel) o directly via wireless adb
#   dev build = own app "Oh My Marx DEV" (de.strix.marxversion.dev) ➡ sits next 2 th stable one,
#   own data, own Obtainium entry (no "app alr added" clash)
set -euo pipefail
cd "$(dirname "$0")/.."
. bin/lib.sh
PKG=de.strix.marxversion.dev

usage() {
    cat <<EOF
usage: bin/deploy.sh [-a|--adb] [-q|--quiet] [-h|--help|-?]

  🧪 dev build "Oh My Marx DEV", version <appVersion>-dev.MMDD.HHMM (newer evy minute), no commit needed

  (default)   ➡ $WEB/dev/  ➡ Obtainium on th fon: pull 2 refresh ➡ Update
              (once: Obtainium ➡ ＋ ➡ URL $WEB/dev/ ➡ a 2nd app next 2 th stable one)
  -a --adb    install straight over wireless adb instead (home wifi, ~30 s, opens th app)
  -q --quiet  w -a: install only, don't open th app (u r using th fon)

  wireless adb once: fon ➡ Developer options ➡ Wireless debugging ➡ "Pair w code"
                     ➡ adb pair IP:PORT  (then it reconnects by itself via mDNS)
  release 4 friends: bin/ship.sh

EOF
    tools
}

mode=web; launch=1
for a in "$@"; do
    case "$a" in
        -h|--help|-\?) usage; exit 0 ;;
        -a|--adb) mode=adb ;;
        -q|--quiet) launch=0 ;;
        *) usage; exit 1 ;;
    esac
done

if [ "$mode" = adb ]; then
    have_fon() { "$ADB" devices | grep -qE '\sdevice$'; }
    if ! have_fon; then
        # _adb-tls-connect = a paired fon w Wireless debugging on · last col = IP:PORT (port changes evy toggle)
        "$ADB" mdns services | awk '/_adb-tls-connect/ {print $NF}' | while read -r hp; do
            "$ADB" connect "$hp" >/dev/null 2>&1 || true
        done
    fi
    have_fon || { echo "📱 no fon: Wireless debugging ON + same wifi? · 1st time: adb pair IP:PORT"; exit 1; }
    (cd android && ./gradlew --offline -q installDebug -Pdev)
    [ "$launch" = 1 ] && "$ADB" shell am start -n "$PKG/de.strix.marxversion.MainActivity" >/dev/null
    echo "✔️ installed over adb$([ "$launch" = 1 ] && echo ' + launched')"
    exit 0
fi

# 🌐 dev channel: build w -Pdev ➡ versionName gets -dev.MMDD.HHMM ➡ Obtainium sees a new version
(cd android && ./gradlew --offline -q assembleDebug -Pdev)
ver=$(sed -nE 's/.*"versionName": "([^"]+)".*/\1/p' android/app/build/outputs/apk/debug/output-metadata.json | head -1)
out=$(mktemp -d); trap 'rm -rf "$out"' EXIT
apk="$FILE-$ver.apk"
cp android/app/build/outputs/apk/debug/app-debug.apk "$out/$apk"
page "$out" "$ver" "$apk" dev
publish "$out" dev
echo "✔️ deployed $ver ➡ $WEB/dev/ (= Dev tab on $WEB/#dev) · fon: Obtainium ➡ pull 2 refresh"
