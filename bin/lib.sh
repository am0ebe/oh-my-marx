# shared bits 4 bin/deploy.sh + bin/ship.sh (sourced, not run)
# shellcheck shell=bash

APP="Oh My Marx"
FILE="OhMyMarx"                       # apk name: OhMyMarx-<version>.apk ➡ Obtainium reads th version fr it
export JAVA_HOME=~/.local/opt/jdk21 ANDROID_HOME=~/Android/Sdk
ADB=$ANDROID_HOME/platform-tools/adb

# 🌐 where th download pages live (@1103: /bible dropped, too new 2 need a redirect)
# short link 4 ppl: scagent.de/omm = 302 by ops' Caddy · Obtainium keeps th full URL
WEB=https://scagent.de/oh-my-marx
DIRS=/srv/www/oh-my-marx
MAIL=omm@scagent.de                                 # feedback addr (Migadu alias ➡ am0ebe's inbox)
SRC=https://github.com/am0ebe/oh-my-marx
FGJ=https://git.scagent.de/am0/oh-my-marx           # Forgejo mirror (self-hosted, no public sign-up ➡ issues stay on GitHub)

# th 1 version number lives i android/app/build.gradle.kts: val appVersion = "x.y.z"
app_version() { sed -nE 's/^val appVersion = "([^"]+)".*/\1/p' android/app/build.gradle.kts; }

# all tools i bin/, 1 line each (shown by evy -h)
tools() {
    cat <<'EOF'
tools i bin/ (run fr th repo root, -h on any for help):
  deploy.sh    🧪 dev build (app "Oh My Marx DEV", own icon slot) ➡ <web>/dev/ (Obtainium) · -a = + wireless adb
  ship.sh      📦 release ➡ <web>/ + GitHub release (clean + pushed tree, new version) · -p = page only
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

# download page = 1 real page (stable) + <web>/dev/ = bare Obtainium source 4 th DEV app (strix @1106: no dev webpage, but th URL)
#   1 apk link (version i th file name) ➡ Obtainium's HTML source finds it
#   page OUTDIR VERSION APKNAME [dev]
page() {
    local out=$1 ver=$2 apk=$3 chan=${4:-stable}
    local mb sha code li news=""
    mb=$(du -m "$out/$apk" | cut -f1); sha=$(sha256sum "$out/$apk" | cut -d' ' -f1)
    code=$("$ANDROID_HOME"/build-tools/*/aapt2 dump badging "$out/$apk" 2>/dev/null | sed -nE "1s/.*versionCode='([0-9]+)'.*/\1/p")
    # 🆕 th app's update check (data/Update.kt) reads version + code (DEV app ➡ dev/version.json)
    printf '{"version":"%s","code":%s,"apk":"%s","sha256":"%s"}\n' "$ver" "${code:-0}" "$apk" "$sha" > "$out/version.json"
    if [ "$chan" = dev ]; then
        cat > "$out/index.html" <<EOF
<!doctype html><html lang="en"><meta charset="utf-8"><meta name="robots" content="noindex"><title>$APP DEV</title>
<p>🧪 Dev build $ver (separate app "Oh My Marx DEV") = Obtainium source only · normal install: <a href="../">$WEB/</a></p>
<p><a href="$apk">$apk</a></p>
EOF
        return
    fi
    li=$(bin/notes.py "$ver" --html --plain)
    [ -n "$li" ] && news="<section class=\"w\"><h2>What’s new in ${ver%%-*}</h2><ul>$li</ul></section>"
    # order (strix @1105/06): Obtainium 1st · APK 2nd · build-it-urself 3rd (GitHub | Forgejo tabs) · what's new · footer = feedback + share
    # favicon = th app icon (Saint Marx w halo on red, bin/favicon.png) · obtainium://add/URL = Obtainium's deep link
    # tabs = CSS-only (radio + :checked), no JS · #feedback = anchor th app's Über can link 2
    cat > "$out/index.html" <<EOF
<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>$APP ☭</title>
<link rel="icon" type="image/png" href="favicon.png"><link rel="apple-touch-icon" href="favicon.png">
<style>body{font:17px/1.5 system-ui,sans-serif;max-width:34em;margin:2em auto;padding:0 16px;background:#1d1230;color:#f4ead8}
a.b,a.o,a.s{display:block;text-align:center;padding:.9em;border-radius:12px;text-decoration:none;font-weight:700;margin:1em 0}
a.b{background:#b3261e;color:#fff}a.o{background:#e8b931;color:#1d1230}a.s{border:1.5px solid #e8b931;color:#e8b931}
h2{font-size:1.05em;margin:1.6em 0 .3em}code{background:#0004;padding:.1em .3em;border-radius:4px;word-break:break-all}small{opacity:.7}
a{color:#e8b931}ul,ol{padding-left:1.3em}
.w{background:#0003;border-radius:12px;padding:.4em 1em .6em;margin-top:1.8em;font-size:.88em}.w h2{margin:.5em 0 .2em}.w ul{margin:.3em 0}
.tb{margin-top:.6em}.tb input{display:none}.tb label{display:inline-block;padding:.35em 1em;border-radius:10px 10px 0 0;cursor:pointer;opacity:.6;background:#0003}
#tg:checked+label,#tf:checked+label{opacity:1;background:#f4ead81a;font-weight:700}
.tp{display:none;background:#f4ead81a;border-radius:0 12px 12px 12px;padding:.2em 1em .8em}#tg:checked~#pg,#tf:checked~#pf{display:block}
footer{margin-top:2.4em;padding-top:1.2em;border-top:1px solid #f4ead822;text-align:center}
.r{display:grid;grid-template-columns:repeat(2,1fr);gap:.5em;max-width:13em;margin:0 auto}
.p{display:flex;flex-direction:column;align-items:center;gap:.3em;padding:.7em .2em .55em;border:1px solid #e8b93155;border-radius:14px;background:none;
color:#e8b931;font:inherit;font-size:.8em;text-decoration:none;cursor:pointer;white-space:nowrap}.p:hover{background:#e8b93122}.p svg{width:1.5em;height:1.5em}</style>
<h1>☭ $APP</h1>
<p>Read Marx like the Bible: a daily verse from <i>Das Kapital</i>, the whole book, reading plans, streaks —
and a Hooked mode that confesses every habit trick the app plays on you. The app speaks German first (plus EN · IT · ES · PT · 中文 · عربي).</p>
<h2>📲 Install with Obtainium <small>(recommended: updates itself)</small></h2>
<ol><li>Get <a href="https://github.com/ImranR98/Obtainium/releases/latest">Obtainium</a> (free, open source app updater)</li>
<li>Tap <b>➕ Add to Obtainium</b> below ➡ <b>Add</b> ➡ <b>Install</b></li></ol>
<a class="o" href="obtainium://add/$WEB/">➕ Add to Obtainium</a>
<p><small>Button does nothing? In Obtainium tap ＋ and paste <code>$WEB/</code></small></p>
<h2>Or download the APK</h2>
<a class="b" href="$apk">⬇️ Download $APP $ver · ${mb} MB</a>
<ol><li>Tap the download ➡ allow installing from this source (once) ➡ <b>Install</b></li>
<li>Play Protect may warn about an "unknown app" — normal outside the Play Store ➡ <b>Install anyway</b></li>
<li>The app tells you when a new version is out ➡ tap ➡ install over it, your data stays</li></ol>
<h2>Or build it yourself</h2>
<p>Open source (GPL-3.0): read the code and build the APK on your own machine instead of trusting ours.
Step-by-step guide in the README, no Android experience needed.</p>
<div class="tb">
<input type="radio" name="t" id="tg" checked><label for="tg">GitHub</label><input type="radio" name="t" id="tf"><label for="tf">Forgejo</label>
<div class="tp" id="pg"><a class="s" href="$SRC">Source on GitHub</a>
<p><code>git clone $SRC</code></p>
<p><small>Bug or idea and a GitHub account? <a href="$SRC/issues/new/choose">Open an issue</a> · all: <a href="$SRC/issues">issues</a> · <a href="$SRC/releases">releases</a></small></p></div>
<div class="tp" id="pf"><a class="s" href="$FGJ">Source on Forgejo</a>
<p><code>git clone $FGJ.git</code></p>
<p><small>Same code, mirrored on our own server in Germany (self-hosted <a href="https://forgejo.org">Forgejo</a>, no Microsoft). No sign-up there: issues go to GitHub or by mail.</small></p></div>
</div>
$news
<p><small>Older versions + all release notes: <a href="$SRC/releases">GitHub releases</a> · v$ver · $(date '+%F %H:%M') · sha256 ${sha:0:16}…</small></p>
<footer id="feedback">
<div class="r">
<a class="p" href="mailto:$MAIL?subject=Oh%20My%20Marx%20$ver&amp;body=What%20happened%20or%20what%20would%20you%20like%3F%0A%0A%0A(a%20screenshot%20helps%20%E2%80%94%20version%20$ver)" title="$MAIL"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><path d="M22 6l-10 7L2 6"/></svg>Feedback</a>
<button class="p" id="sh" title="scagent.de/omm"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="M8.6 13.5l6.8 4M15.4 6.5l-6.8 4"/></svg><span>Share</span></button>
</div>
</footer>
<script>
// share: phone ➡ native share sheet · desktop ➡ copy th short link
document.getElementById('sh').onclick = async function () {
  var u = 'https://scagent.de/omm', l = this.querySelector('span');
  try {
    if (navigator.share) { await navigator.share({ title: 'Oh My Marx', text: 'Read Marx like the Bible ☭', url: u }); return; }
    await navigator.clipboard.writeText(u); l.textContent = 'Link copied ✔';
  } catch (e) {}
};
</script>
EOF
}

# upload 2 evy page dir (+ SUBDIR): apk 1st (if any), page 2nd (page nvr points at a missing file) · keep newest 3 apks
#   publish OUTDIR [dev]
publish() {
    local out=$1 sub=${2:-} d
    for d in $DIRS; do
        d="$d${sub:+/$sub}"
        ssh vps "mkdir -p $d"
        ls "$out"/*.apk >/dev/null 2>&1 && rsync -t --chmod=F644 "$out"/*.apk "vps:$d/"
        rsync -t --chmod=F644 "$out/index.html" "$out/version.json" bin/favicon.png "vps:$d/"
        ssh vps "cd $d && ls -t *.apk | tail -n +4 | xargs -r rm --"
    done
    # 👥 Kreis invite landing page (bin/kreis.html) ➡ <web>/k/ · refreshed on evy publish
    for d in $DIRS; do
        ssh vps "mkdir -p $d/k"
        rsync -t --chmod=F644 bin/kreis.html "vps:$d/k/index.html"
    done
}
