#!/usr/bin/env bash
# 📦 ship = a release 4 friends: APK ➡ scagent.de/oh-my-marx/ (download page) + GitHub release
set -euo pipefail
cd "$(dirname "$0")/.."
. bin/lib.sh

usage() {
    cat <<EOF
usage: bin/ship.sh [patch|minor|major] [-p|--page] [-h|--help|-?]

  📦 release: build release APK ➡ $WEB/ (page, Obtainium follows it) + GitHub release v<version>
  minor       0.7.0 ➡ 0.8.0 = new features  ┐ bumps fr th last shipped tag ➡ writes appVersion ➡
  patch       0.8.0 ➡ 0.8.1 = only fixes    ├ commits "vX.Y.Z" + pushes ➡ ships
  major       0.8.0 ➡ 1.0.0                 ┘
  (none)      ship appVersion as it is (must not be shipped yet)
  -p --page   only re-render + upload th page (lib.sh page() changed) 4 th last shipped version · no build, no tag

  before:
    1. Changelog.kt: a Release("X.Y.Z", …) entry 4 th NEW version on top (= in-app + GitHub notes)
    2. commit + push evythg else (th tag = exactly th shipped code; script refuses otherwise)
  signed w ~/.android/debug.keystore ➡ ⚠️ lose it = friends must uninstall
  dev builds (no commit needed, version = last ship + "-dev.MMDD.HHMM"): bin/deploy.sh

EOF
    tools
}
level=""
case "${1:-}" in -h|--help|-\?) usage; exit 0 ;; -p|--page) only_page=1 ;; patch|minor|major) level=$1 ;; "") ;; *) usage; exit 1 ;; esac

if [ -n "${only_page:-}" ]; then
    # last shipped = newest v* tag · apk comes fr th server (size + sha + versionCode = exactly what ppl download)
    ver=$(git tag -l 'v*' --sort=-v:refname | head -1); ver=${ver#v}
    [ -n "$ver" ] || { echo "❌ no v* tag ➡ nothing shipped yet"; exit 1; }
    out=$(mktemp -d); trap 'rm -rf "$out"' EXIT
    apk="$FILE-$ver.apk"
    curl -fsSo "$out/$apk" "$WEB/$apk" || { echo "❌ $WEB/$apk not on th server"; exit 1; }
    page "$out" "$ver" "$apk"
    rm "$out/$apk"                     # page only ➡ publish skips th apk upload
    publish "$out"
    echo "✔️ page re-rendered 4 v$ver ➡ $WEB/"
    exit 0
fi

ver=$(app_version)
[ -n "$ver" ] || { echo "❌ no appVersion i build.gradle.kts"; exit 1; }
[ -z "$(git status --porcelain)" ] || { echo "❌ uncommitted changes ➡ commit 1st"; exit 1; }
git fetch -q origin && [ "$(git rev-parse HEAD)" = "$(git rev-parse origin/main)" ] || { echo "❌ HEAD ≠ origin/main ➡ push 1st"; exit 1; }

# 🔢 bump: fr th last shipped tag (not fr appVersion ➡ "minor" twice = still 1 step)
if [ -n "$level" ]; then
    last=$(git tag -l 'v*' --sort=-v:refname | head -1); last=${last#v}
    IFS=. read -r ma mi pa <<<"${last:-0.0.0}"
    case "$level" in major) next="$((ma + 1)).0.0" ;; minor) next="$ma.$((mi + 1)).0" ;; patch) next="$ma.$mi.$((pa + 1))" ;; esac
    if [ "$next" != "$ver" ]; then
        [ -n "$(bin/notes.py "$next")" ] || { echo "❌ no Release(\"$next\", …) i ui/Changelog.kt ➡ write th notes 1st"; exit 1; }
        sed -i -E "s/^val appVersion = \"[^\"]+\"/val appVersion = \"$next\"/" android/app/build.gradle.kts
        git commit -qm "v$next" android/app/build.gradle.kts && git push -q origin main
        echo "🔢 $ver ➡ $next (committed + pushed)"
        ver=$next
    fi
fi
git rev-parse -q --verify "refs/tags/v$ver" >/dev/null && { echo "❌ v$ver alr shipped ➡ bin/ship.sh patch|minor|major"; exit 1; }
[ -n "$(bin/notes.py "$ver")" ] || { echo "❌ no Release(\"$ver\", …) i ui/Changelog.kt ➡ write th notes 1st"; exit 1; }

(cd android && ./gradlew --offline -q assembleRelease)
out=$(mktemp -d); trap 'rm -rf "$out"' EXIT
apk="$FILE-$ver.apk"
cp android/app/build/outputs/apk/release/app-release.apk "$out/$apk"
page "$out" "$ver" "$apk"
publish "$out"

# 🏷️ annotated tag on th shipped commit (notes inside ➡ `git show v0.8.0`) · local + pushed
# remotes @1107 (go/pr/ops/repos.md): origin = Forgejo (only push target) · gh = GitHub, fetch-only
#   GitHub gets code + tags via th Forgejo push-mirror (on evy push) ➡ wait til th tag lands there, `--verify-tag` needs it
git tag -a "v$ver" -m "$APP $ver"$'\n\n'"$(bin/notes.py "$ver")"
git push -q origin "v$ver"
echo "⏳ waiting 4 th Forgejo ➡ GitHub mirror 2 carry v$ver (≤120 s)"
deadline=$((SECONDS + 120))
until git ls-remote --exit-code gh "refs/tags/v$ver" >/dev/null 2>&1; do
    [ $SECONDS -lt $deadline ] || { echo "❌ v$ver not on GitHub aftr 120 s ➡ mirror down? (ops: repo mirror oh-my-marx) · page + Forgejo tag alr live, rerun only th gh release step"; exit 1; }
    sleep 5
done
# GitHub release = th archive: evy version stays downloadable (bug i 0.8? grab 0.7) · notes = th in-app changelog
gh release create "v$ver" "$out/$apk" --repo am0ebe/oh-my-marx --verify-tag --title "$APP $ver" \
    --notes "$(bin/notes.py "$ver")"$'\n\n'"Install: open $WEB/ on the phone, or download the APK below. Older versions: see all releases."
git fetch -q --tags origin
echo "✔️ shipped v$ver ➡ $WEB/"
