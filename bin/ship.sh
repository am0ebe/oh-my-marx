#!/usr/bin/env bash
# 📦 ship = a release 4 friends: APK ➡ scagent.de/oh-my-marx/ (download page) + GitHub release
set -euo pipefail
cd "$(dirname "$0")/.."
. bin/lib.sh

usage() {
    cat <<EOF
usage: bin/ship.sh [-h|--help|-?]

  📦 release: build release APK ➡ $WEB/ (page, Obtainium follows it) + GitHub release v<version>

  before:
    1. bump  val appVersion = "x.y.z"  i android/app/build.gradle.kts  (Obtainium only sees a NEW version)
    2. commit + push  (th tag = exactly th shipped code; script refuses otherwise)
  signed w ~/.android/debug.keystore ➡ ⚠️ lose it = friends must uninstall
  dev builds (no commit needed): bin/deploy.sh

EOF
    tools
}
case "${1:-}" in -h|--help|-\?) usage; exit 0 ;; "") ;; *) usage; exit 1 ;; esac

ver=$(app_version)
[ -n "$ver" ] || { echo "❌ no appVersion i build.gradle.kts"; exit 1; }
[ -z "$(git status --porcelain)" ] || { echo "❌ uncommitted changes ➡ commit 1st"; exit 1; }
git fetch -q origin && [ "$(git rev-parse HEAD)" = "$(git rev-parse origin/main)" ] || { echo "❌ HEAD ≠ origin/main ➡ push 1st"; exit 1; }
git rev-parse -q --verify "refs/tags/v$ver" >/dev/null && { echo "❌ v$ver alr shipped ➡ bump appVersion"; exit 1; }

(cd android && ./gradlew --offline -q assembleRelease)
out=$(mktemp -d); trap 'rm -rf "$out"' EXIT
apk="$FILE-$ver.apk"
cp android/app/build/outputs/apk/release/app-release.apk "$out/$apk"
page "$out" "$ver" "$apk" stable
publish "$out"

# GitHub release = th archive: evy version stays downloadable (bug i 0.8? grab 0.7) · notes = th in-app changelog
gh release create "v$ver" "$out/$apk" --title "$APP $ver" \
    --notes "$(bin/notes.py "$ver")"$'\n\n'"Install: open $WEB/ on the phone, or download the APK below. Older versions: see all releases."
git fetch -q --tags origin
echo "✔️ shipped v$ver ➡ $WEB/"
