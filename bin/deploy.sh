#!/usr/bin/env bash
# 🧪 deploy = a dev build 4 ur own fon: ➡ <web>/dev/ (Obtainium "DEV" entry) · -a = also straight over wireless adb
#   dev build = own app "Oh My Marx DEV" (de.strix.marxversion.dev) ➡ sits next 2 th stable one, own data
#   <web>/dev/ = bare Obtainium source (apk link + version.json 4 th DEV app's update banner), no real page (@1106)
set -euo pipefail
cd "$(dirname "$0")/.."
. bin/lib.sh
PKG=de.strix.marxversion.dev

usage() {
    cat <<EOF
usage: bin/deploy.sh [-a|--adb] [-q|--quiet] [-h|--help|-?]

  🧪 dev build "Oh My Marx DEV", version <next release>-dev.MMDD.HHMM (newer evy minute), no commit needed
     next release = appVersion if not shipped yet, else th next minor (0.8.0 shipped ➡ 0.9.0-dev…) · nvr bump by hand

  (default)   ➡ $WEB/dev/  ➡ Obtainium on th fon: pull 2 refresh ➡ Update
              (once: Obtainium ➡ ＋ ➡ URL $WEB/dev/ ➡ a 2nd app next 2 th stable one)
  -a --adb    + install th same apk over wireless adb right now (home wifi, opens th app)
  -q --quiet  w -a: install only, don't open th app (u r using th fon)

  wireless adb once: fon ➡ Developer options ➡ Wireless debugging ➡ "Pair w code"
                     ➡ adb pair IP:PORT  (then it reconnects by itself via mDNS)
  release 4 friends: bin/ship.sh

EOF
    tools
}

adb=0; launch=1
for a in "$@"; do
    case "$a" in
        -h|--help|-\?) usage; exit 0 ;;
        -a|--adb) adb=1 ;;
        -q|--quiet) launch=0 ;;
        *) usage; exit 1 ;;
    esac
done

if [ "$adb" = 1 ]; then   # check th fon 1st ➡ fail b4 a 1-min build
    have_fon() { "$ADB" devices | grep -qE '\sdevice$'; }
    if ! have_fon; then
        # _adb-tls-connect = a paired fon w Wireless debugging on · last col = IP:PORT (port changes evy toggle)
        "$ADB" mdns services | awk '/_adb-tls-connect/ {print $NF}' | while read -r hp; do
            "$ADB" connect "$hp" >/dev/null 2>&1 || true
        done
    fi
    have_fon || { echo "📱 no fon: Wireless debugging ON + same wifi? · 1st time: adb pair IP:PORT"; exit 1; }
fi

# -Pdev ➡ versionName gets -dev.MMDD.HHMM ➡ Obtainium sees a new version
# dev = test build o th NEXT release: appVersion alr shipped (tag exists) ➡ label as next minor (0.8.0 ➡ 0.9.0-dev…)
base=$(app_version)
if git rev-parse -q --verify "refs/tags/v$base" >/dev/null; then
    IFS=. read -r ma mi _ <<<"$base"; base="$ma.$((mi + 1)).0"
fi
(cd android && ./gradlew --offline -q assembleDebug -Pdev -PdevBase="$base")
ver=$(sed -nE 's/.*"versionName": "([^"]+)".*/\1/p' android/app/build/outputs/apk/debug/output-metadata.json | head -1)
out=$(mktemp -d); trap 'rm -rf "$out"' EXIT
apk="$FILE-$ver.apk"
cp android/app/build/outputs/apk/debug/app-debug.apk "$out/$apk"
page "$out" "$ver" "$apk" dev
publish "$out" dev
echo "✔️ $ver ➡ $WEB/dev/ · fon: Obtainium ➡ pull 2 refresh"

if [ "$adb" = 1 ]; then
    "$ADB" install -r "$out/$apk" >/dev/null
    [ "$launch" = 1 ] && "$ADB" shell am start -n "$PKG/de.strix.marxversion.MainActivity" >/dev/null
    echo "✔️ + installed over adb$([ "$launch" = 1 ] && echo ' + launched')"
fi
