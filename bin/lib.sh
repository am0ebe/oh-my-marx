# shared bits 4 bin/deploy.sh + bin/ship.sh (sourced, not run)
# shellcheck shell=bash

APP="Oh My Marx"
FILE="OhMyMarx"                       # apk name: OhMyMarx-<version>.apk ➡ Obtainium reads th version fr it
WEB=https://scagent.de/bible          # stable channel · dev channel = $WEB/dev
export JAVA_HOME=~/.local/opt/jdk21 ANDROID_HOME=~/Android/Sdk
ADB=$ANDROID_HOME/platform-tools/adb

# th 1 version number lives i android/app/build.gradle.kts: val appVersion = "x.y.z"
app_version() { sed -nE 's/^val appVersion = "([^"]+)".*/\1/p' android/app/build.gradle.kts; }

# all tools i bin/, 1 line each (shown by evy -h)
tools() {
    cat <<'EOF'
tools i bin/ (run fr th repo root, -h on any for help):
  deploy.sh   🧪 dev build ➡ scagent.de/bible/dev (Obtainium) · -a = wireless adb instead
  ship.sh     📦 release ➡ scagent.de/bible + GitHub release (clean + pushed tree, new version)
  fetch.py    1️⃣ download Kapital html (DE + EN)
  split.py    2️⃣ html ➡ verses + footnotes
  align.py    3️⃣ DE ⬄ EN paragraph alignment
  score.py    4️⃣ LLM: quote · juice · difficulty · tags (~35 min)
  rank.py     5️⃣ LLM: relative ⭐ + 🔥 picks per section
  build.py    6️⃣ plans + export ➡ android assets
  andacht.py  7️⃣ LLM devotionals per plan day ➡ build.py again
  translate.py 8️⃣ quotes/UI/… ➡ it es pt zh ar (--only ui = just UI strings)
EOF
}

# download page: 1 apk link (version i th file name) ➡ Obtainium's HTML source finds it
#   page OUTDIR VERSION APKNAME CHANNEL-NOTE
page() {
    local out=$1 ver=$2 apk=$3 note=$4
    local mb sha; mb=$(du -m "$out/$apk" | cut -f1); sha=$(sha256sum "$out/$apk" | cut -c1-16)
    cat > "$out/index.html" <<EOF
<!doctype html><html lang="de"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>$APP ☭</title>
<style>body{font:17px/1.5 system-ui,sans-serif;max-width:34em;margin:2em auto;padding:0 16px;background:#1d1230;color:#f4ead8}
a.b{display:block;text-align:center;background:#b3261e;color:#fff;padding:.9em;border-radius:12px;text-decoration:none;font-weight:700;margin:1.2em 0}
code{background:#0004;padding:.1em .3em;border-radius:4px}small{opacity:.7}.n{background:#e8b93133;padding:.5em .8em;border-radius:8px}</style>
<h1>☭ $APP</h1>
<p>Marx lesen wie die Bibel: jeden Tag ein Häppchen Kapital. <small>Read Marx like the Bible.</small></p>
$note
<a class="b" href="$apk">⬇️ $APP $ver herunterladen · ${mb} MB</a>
<ol><li>APK antippen ➡ „Aus dieser Quelle zulassen“ (einmalig) ➡ Installieren</li>
<li>Play Protect meckert evtl. („unbekannte App“) ➡ „Trotzdem installieren“</li>
<li>Updates: <b>Obtainium</b> ➡ App hinzufügen ➡ URL <code>$WEB/</code> ➡ meldet neue Versionen
(Versions-Regex: <code>(\d+\.\d+\.\d+[-.\w]*)\.apk</code>)</li></ol>
<p><small>v$ver · $(date '+%F %H:%M') · sha256 $sha…</small></p>
EOF
}

# upload apk 1st, page 2nd (page nvr points at a missing file) · keep newest 3 apks
#   publish OUTDIR REMOTE-DIR
publish() {
    local out=$1 dir=$2
    ssh vps "mkdir -p $dir"
    rsync -t --chmod=F644 "$out"/*.apk "vps:$dir/"
    rsync -t --chmod=F644 "$out/index.html" "vps:$dir/"
    ssh vps "cd $dir && ls -t *.apk | tail -n +4 | xargs -r rm --"
}
