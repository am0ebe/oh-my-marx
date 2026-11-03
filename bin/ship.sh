#!/usr/bin/env bash
# 📦 ship = a release 4 friends: APK ➡ scagent.de/bible/ (download page) + GitHub release
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
page "$out" "$ver" "$apk" ""
publish "$out" /srv/www/bible

# GitHub release (private repo 4 now ➡ friends use th VPS page · public later ➡ Obtainium can follow GitHub directly)
gh release create "v$ver" "$out/$apk" --title "$APP $ver" --notes "$(git log -1 --format=%s)"
git fetch -q --tags origin
echo "✔️ shipped v$ver ➡ $WEB/"
