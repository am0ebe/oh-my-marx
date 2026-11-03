#!/usr/bin/env bash
# 📦 ship a build 2 friends: APK ➡ scagent.de/bible/ (download page) + GitHub release
#   friends: open https://scagent.de/bible/ ➡ download ➡ install · o Obtainium on that URL = update notifications
#
# rules:
#   - bump versionName (+ versionCode) i android/app/build.gradle.kts first ➡ Obtainium only sees a NEW name
#   - tree must be clean + pushed ➡ th tag = exactly th shipped code
#   - signed w ~/.android/debug.keystore (release buildType uses it) ➡ ⚠️ lose that file = friends must uninstall
# one-time VPS: sudo install -d -o am0 -g am0 /srv/www/bible
set -euo pipefail
cd "$(dirname "$0")/.."
export JAVA_HOME=~/.local/opt/jdk21 ANDROID_HOME=~/Android/Sdk

ver=$(sed -nE 's/.*versionName = "([^"]+)".*/\1/p' android/app/build.gradle.kts)
[ -n "$ver" ] || { echo "❌ no versionName"; exit 1; }
[ -z "$(git status --porcelain)" ] || { echo "❌ uncommitted changes ➡ commit 1st"; exit 1; }
git fetch -q origin && [ "$(git rev-parse HEAD)" = "$(git rev-parse origin/main)" ] || { echo "❌ HEAD ≠ origin/main ➡ push 1st"; exit 1; }
git rev-parse -q --verify "refs/tags/v$ver" >/dev/null && { echo "❌ v$ver alr shipped ➡ bump versionName"; exit 1; }

(cd android && ./gradlew --offline -q assembleRelease)
out=$(mktemp -d); trap 'rm -rf "$out"' EXIT
apk="$out/bibleApp-$ver.apk"
cp android/app/build/outputs/apk/release/app-release.apk "$apk"
sha=$(sha256sum "$apk" | cut -c1-16)
mb=$(du -m "$apk" | cut -f1)

# download page: version i th file name ➡ Obtainium (HTML source) spots new builds
cat > "$out/index.html" <<EOF
<!doctype html><html lang="de"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>bibleApp ☭</title>
<style>body{font:17px/1.5 system-ui,sans-serif;max-width:34em;margin:2em auto;padding:0 16px;background:#1d1230;color:#f4ead8}
a.b{display:block;text-align:center;background:#b3261e;color:#fff;padding:.9em;border-radius:12px;text-decoration:none;font-weight:700;margin:1.2em 0}
code{background:#0004;padding:.1em .3em;border-radius:4px}small{opacity:.7}</style>
<h1>☭ bibleApp</h1>
<p>Marx lesen wie die Bibel: jeden Tag ein Häppchen Kapital. <small>Read Marx like the Bible.</small></p>
<a class="b" href="bibleApp-$ver.apk">⬇️ bibleApp $ver herunterladen · ${mb} MB</a>
<ol><li>APK antippen ➡ „Aus dieser Quelle zulassen“ (einmalig) ➡ Installieren</li>
<li>Play Protect meckert evtl. („unbekannte App“) ➡ „Trotzdem installieren“</li>
<li>Updates: diese Seite wieder öffnen — oder <b>Obtainium</b> ➡ App hinzufügen ➡ URL <code>https://scagent.de/bible/</code> ➡ meldet neue Versionen</li></ol>
<p><small>v$ver · $(date +%F) · sha256 $sha…</small></p>
EOF

# VPS: new apk 1st, page 2nd (page nvr points at a missing file) · keep last 3 apks
rsync -t --chmod=F644 "$apk" vps:/srv/www/bible/
rsync -t --chmod=F644 "$out/index.html" vps:/srv/www/bible/
ssh vps 'cd /srv/www/bible && ls -t bibleApp-*.apk | tail -n +4 | xargs -r rm --'

# GitHub release (private repo 4 now ➡ friends use th VPS page · public later ➡ Obtainium can follow GitHub directly)
gh release create "v$ver" "$apk" --title "bibleApp $ver" --notes "$(git log -1 --format=%s)"
git fetch -q --tags origin
echo "✔️ shipped v$ver ➡ https://scagent.de/bible/"
