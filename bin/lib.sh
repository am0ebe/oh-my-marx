# shared bits 4 bin/deploy.sh + bin/ship.sh (sourced, not run)
# shellcheck shell=bash

APP="Oh My Marx"
FILE="OhMyMarx"                       # apk name: OhMyMarx-<version>.apk ➡ Obtainium reads th version fr it
export JAVA_HOME=~/.local/opt/jdk21 ANDROID_HOME=~/Android/Sdk
ADB=$ANDROID_HOME/platform-tools/adb

# 🌐 where th download pages live (@1103: /bible dropped, too new 2 need a redirect)
# short link 4 ppl: scagent.de/omm (+ /omm/dev) = 302 by ops' Caddy · Obtainium keeps th full URL
WEB=https://scagent.de/oh-my-marx
DIRS=/srv/www/oh-my-marx
MAIL=omm@scagent.de                                 # feedback addr (Migadu alias ➡ am0ebe's inbox)
SRC=https://github.com/am0ebe/oh-my-marx

# th 1 version number lives i android/app/build.gradle.kts: val appVersion = "x.y.z"
app_version() { sed -nE 's/^val appVersion = "([^"]+)".*/\1/p' android/app/build.gradle.kts; }

# all tools i bin/, 1 line each (shown by evy -h)
tools() {
    cat <<'EOF'
tools i bin/ (run fr th repo root, -h on any for help):
  deploy.sh    🧪 dev build (app "Oh My Marx DEV", own icon slot) ➡ <web>/dev (Obtainium) · -a = wireless adb
  ship.sh      📦 release ➡ <web>/ + GitHub release (clean + pushed tree, new version)
  fetch.py     1️⃣ download Kapital html (DE + EN)
  split.py     2️⃣ html ➡ verses + footnotes
  align.py     3️⃣ DE ⬄ EN paragraph alignment
  score.py     4️⃣ LLM: quote · juice · difficulty · tags (~35 min)
  rank.py      5️⃣ LLM: relative ⭐ + 🔥 picks per section
  build.py     6️⃣ plans + export ➡ android assets
  andacht.py   7️⃣ LLM devotionals per plan day ➡ build.py again
  translate.py 8️⃣ quotes/UI/… ➡ it es pt zh ar (--only ui = just UI strings)
  server/      👥 community counter API (server/README.md: run, test, VPS install)
EOF
}

# download page: 1 apk link (version i th file name) ➡ Obtainium's HTML source finds it
#   page OUTDIR VERSION APKNAME CHANNEL-NOTE OBTAINIUM-URL
page() {
    local out=$1 ver=$2 apk=$3 note=$4 url=$5
    local mb sha code news
    mb=$(du -m "$out/$apk" | cut -f1); sha=$(sha256sum "$out/$apk" | cut -d' ' -f1)
    code=$("$ANDROID_HOME"/build-tools/*/aapt2 dump badging "$out/$apk" 2>/dev/null | sed -nE "1s/.*versionCode='([0-9]+)'.*/\1/p")
    news=$(bin/notes.py "$ver" --html)
    [ -n "$news" ] && news="<h2>📜 What’s new in ${ver%%-*}</h2><ul>$news</ul>"
    # same structure as README.md (what · install · updates · what's new · feedback · source) · English
    cat > "$out/index.html" <<EOF
<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>$APP ☭</title>
<style>body{font:17px/1.5 system-ui,sans-serif;max-width:34em;margin:2em auto;padding:0 16px;background:#1d1230;color:#f4ead8}
a.b{display:block;text-align:center;background:#b3261e;color:#fff;padding:.9em;border-radius:12px;text-decoration:none;font-weight:700;margin:1.2em 0}
h2{font-size:1.05em;margin:1.6em 0 .3em}code{background:#0004;padding:.1em .3em;border-radius:4px}small{opacity:.7}
.n{background:#e8b93133;padding:.5em .8em;border-radius:8px}a{color:#e8b931}ul,ol{padding-left:1.3em}</style>
<h1>☭ $APP</h1>
<p>Read Marx like the Bible: a daily verse from <i>Das Kapital</i>, the whole book, reading plans, streaks —
and a Hooked mode that confesses every habit trick the app plays on you. The app speaks German first (plus EN · IT · ES · PT · 中文 · عربي).</p>
$note
<a class="b" href="$apk">⬇️ Download $APP $ver · ${mb} MB</a>
<ol><li>Tap the download ➡ allow installing from this source (once) ➡ <b>Install</b></li>
<li>Play Protect may warn about an "unknown app" — normal outside the Play Store ➡ <b>Install anyway</b></li></ol>
<h2>🆕 Updates</h2>
<p>The app tells you when a new version is out ➡ tap ➡ install over it, your data stays.
Prefer fully automatic? <a href="https://github.com/ImranR98/Obtainium/releases">Obtainium</a> ➡ ＋ ➡ <code>$url</code></p>
$news
<p><small>Older versions + all release notes: <a href="$SRC/releases">GitHub releases</a></small></p>
<h2>💬 Feedback</h2>
<p><a href="mailto:$MAIL?subject=Oh%20My%20Marx%20$ver">$MAIL</a> — a screenshot + one sentence is plenty 🙏 · share: <a href="https://scagent.de/omm">scagent.de/omm</a></p>
<h2>🔓 Source</h2>
<p><a href="$SRC">$SRC</a> · GPL-3.0 · build it yourself if you'd rather not trust an APK</p>
<p><small>v$ver · $(date '+%F %H:%M') · sha256 ${sha:0:16}…</small></p>
EOF
    # 🆕 th app's update check (data/Update.kt) reads this: code = versionCode, compared w th installed one
    printf '{"version":"%s","code":%s,"apk":"%s","sha256":"%s"}\n' "$ver" "${code:-0}" "$apk" "$sha" > "$out/version.json"
}

# upload 2 evy page dir (+ SUBDIR): apk 1st, page 2nd (page nvr points at a missing file) · keep newest 3 apks
#   publish OUTDIR [SUBDIR]
publish() {
    local out=$1 sub=${2:-} d
    for d in $DIRS; do
        d="$d${sub:+/$sub}"
        ssh vps "mkdir -p $d"
        rsync -t --chmod=F644 "$out"/*.apk "vps:$d/"
        rsync -t --chmod=F644 "$out/index.html" "$out/version.json" "vps:$d/"
        ssh vps "cd $d && ls -t *.apk | tail -n +4 | xargs -r rm --"
    done
    # 👥 Kreis invite landing page (bin/kreis.html) ➡ <web>/k/ · same 4 stable + dev, so always refreshed
    for d in $DIRS; do
        ssh vps "mkdir -p $d/k"
        rsync -t --chmod=F644 bin/kreis.html "vps:$d/k/index.html"
    done
}
