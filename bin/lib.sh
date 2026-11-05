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
  deploy.sh    🧪 dev build (app "Oh My Marx DEV", own icon slot) ➡ <web>/dev + page's Dev tab · -a = wireless adb
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

# download page = 1 page, 2 tabs (strix @1105): Stable (static HTML) | 🧪 Dev (JS loads dev/version.json)
#   ⚠️ only th stable apk is a real <a href> i th page ➡ Obtainium (no JS) on <web>/ nvr sees th dev apk
#   <web>/dev/ = stub: dev apk link (Obtainium DEV entry) + browsers redirect ➡ <web>/#dev
#   page OUTDIR VERSION APKNAME stable|dev
page() {
    local out=$1 ver=$2 apk=$3 chan=$4
    local mb sha code li news="" js
    mb=$(du -m "$out/$apk" | cut -f1); sha=$(sha256sum "$out/$apk" | cut -d' ' -f1)
    code=$("$ANDROID_HOME"/build-tools/*/aapt2 dump badging "$out/$apk" 2>/dev/null | sed -nE "1s/.*versionCode='([0-9]+)'.*/\1/p")
    li=$(bin/notes.py "$ver" --html)
    # 🆕 th app's update check (data/Update.kt) reads version + code · mb + notes = 4 th page's Dev tab
    VER=$ver CODE=${code:-0} APK=$apk SHA=$sha MB=$mb LI=$li python3 -c 'import json, os; e = os.environ
print(json.dumps({"version": e["VER"], "code": int(e["CODE"]), "apk": e["APK"], "sha256": e["SHA"],
                  "mb": int(e["MB"]), "notes": e["LI"]}, ensure_ascii=False))' > "$out/version.json"
    if [ "$chan" = dev ]; then
        cat > "$out/index.html" <<EOF
<!doctype html><html lang="en"><meta charset="utf-8"><title>$APP DEV</title>
<meta http-equiv="refresh" content="0;url=../#dev"><a href="$apk">$apk</a> · <a href="../#dev">$WEB/#dev</a>
EOF
        return
    fi
    [ -n "$li" ] && news="<h2>📜 What’s new in ${ver%%-*}</h2><ul>$li</ul>"
    # Dev tab: quoted heredoc ➡ JS \${…} + backticks stay literal (nvr put them i th unquoted one below)
    js=$(cat <<'JS'
<script>
const W=location.pathname.replace(/[^/]*$/,''),T=document.querySelectorAll('.t a'),S=document.getElementById('s'),D=document.getElementById('d');
function show(dev){S.hidden=dev;D.hidden=!dev;T[0].classList.toggle('on',!dev);T[1].classList.toggle('on',dev);if(dev&&!D.dataset.ok)load()}
async function load(){D.dataset.ok=1;try{
const j=await(await fetch('dev/version.json',{cache:'no-store'})).json(),u=location.origin+W+'dev/';
D.innerHTML=`<p class=n>🧪 <b>Dev build</b>: newest work in progress, may break. Installs as a separate app “Oh My Marx DEV” next to the stable one, with its own data.</p>
<a class=o href="obtainium://add/${u}">➕ Add DEV to Obtainium</a>
<p><small>or in Obtainium tap ＋ and paste <code>${u}</code></small></p>
<a class=b href="dev/${j.apk}">⬇️ Download DEV ${j.version}${j.mb?' · '+j.mb+' MB':''}</a>
${j.notes?`<h2>📜 Coming in ${j.version.split('-')[0]}</h2><ul>${j.notes}</ul>`:''}
<p><small>sha256 ${j.sha256.slice(0,16)}…</small></p>`}catch(e){D.innerHTML='<p>No dev build right now.</p>'}}
T.forEach((a,i)=>a.onclick=e=>{e.preventDefault();history.replaceState(null,'',i?'#dev':W);show(!!i)});
show(location.hash=='#dev');
</script>
JS
)
    # order (strix @1105): tabs · Obtainium 1st · APK 2nd · what's new 3rd · then feedback + source · English
    # favicon = ☭ gold on red, inline SVG (no extra file) · obtainium://add/URL = Obtainium's own deep link
    cat > "$out/index.html" <<EOF
<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>$APP ☭</title>
<link rel="icon" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'><rect width='100' height='100' rx='22' fill='%23b3261e'/><text x='50' y='76' font-size='72' text-anchor='middle' fill='%23e8b931'>%E2%98%AD</text></svg>">
<style>body{font:17px/1.5 system-ui,sans-serif;max-width:34em;margin:2em auto;padding:0 16px;background:#1d1230;color:#f4ead8}
a.b,a.o{display:block;text-align:center;padding:.9em;border-radius:12px;text-decoration:none;font-weight:700;margin:1em 0}
a.b{background:#b3261e;color:#fff}a.o{background:#e8b931;color:#1d1230}
.t{display:flex;gap:.4em;margin:1.4em 0 .2em}.t a{padding:.4em 1em;border-radius:10px;text-decoration:none;color:#f4ead8;background:#0003;opacity:.65}
.t a:first-child{flex:1;text-align:center}.t a.on{background:#f4ead822;opacity:1;font-weight:700}
h2{font-size:1.05em;margin:1.6em 0 .3em}code{background:#0004;padding:.1em .3em;border-radius:4px;word-break:break-all}small{opacity:.7}
.n{background:#e8b93133;padding:.5em .8em;border-radius:8px}a{color:#e8b931}ul,ol{padding-left:1.3em}</style>
<h1>☭ $APP</h1>
<p>Read Marx like the Bible: a daily verse from <i>Das Kapital</i>, the whole book, reading plans, streaks —
and a Hooked mode that confesses every habit trick the app plays on you. The app speaks German first (plus EN · IT · ES · PT · 中文 · عربي).</p>
<nav class="t"><a href="./" class="on">Stable $ver</a><a href="#dev">🧪 Dev</a></nav>
<div id="s">
<h2>📲 Install with Obtainium <small>(recommended: updates itself)</small></h2>
<ol><li>Get <a href="https://github.com/ImranR98/Obtainium/releases/latest">Obtainium</a> (free, open source app updater)</li>
<li>Tap <b>➕ Add to Obtainium</b> below ➡ <b>Add</b> ➡ <b>Install</b></li></ol>
<a class="o" href="obtainium://add/$WEB/">➕ Add to Obtainium</a>
<p><small>Button does nothing? In Obtainium tap ＋ and paste <code>$WEB/</code></small></p>
<h2>⬇️ Or download the APK</h2>
<a class="b" href="$apk">⬇️ Download $APP $ver · ${mb} MB</a>
<ol><li>Tap the download ➡ allow installing from this source (once) ➡ <b>Install</b></li>
<li>Play Protect may warn about an "unknown app" — normal outside the Play Store ➡ <b>Install anyway</b></li>
<li>The app tells you when a new version is out ➡ tap ➡ install over it, your data stays</li></ol>
$news
<p><small>Older versions + all release notes: <a href="$SRC/releases">GitHub releases</a> · v$ver · $(date '+%F %H:%M') · sha256 ${sha:0:16}…</small></p>
</div>
<div id="d" hidden></div>
<h2>💬 Feedback</h2>
<p><a href="mailto:$MAIL?subject=Oh%20My%20Marx%20$ver">$MAIL</a> — a screenshot + one sentence is plenty 🙏 · share: <a href="https://scagent.de/omm">scagent.de/omm</a></p>
<h2>🔓 Source</h2>
<p><a href="$SRC">$SRC</a> · GPL-3.0 · build it yourself if you'd rather not trust an APK</p>
$js
EOF
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
