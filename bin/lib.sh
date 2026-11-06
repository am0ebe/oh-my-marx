# shared bits 4 bin/deploy.sh + bin/ship.sh (sourced, not run)
# shellcheck shell=bash

APP="Oh My Marx"
FILE="OhMyMarx"                       # apk name: OhMyMarx-<version>.apk ➡ Obtainium reads th version fr it
export JAVA_HOME=~/.local/opt/jdk21 ANDROID_HOME=~/Android/Sdk
ADB=$ANDROID_HOME/platform-tools/adb

# 🌐 where th download pages live (@1103: /bible dropped, too new 2 need a redirect)
# short link 4 ppl: scagent.de/omm = 302 by ops' Caddy · Obtainium keeps th full URL
WEB=https://scagent.de/oh-my-marx
SHORT=https://scagent.de/omm                         # th link ppl share (QR · share btns)
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
  ship.sh      📦 release ➡ <web>/ + GitHub release · patch|minor|major = bump 1st · -p = page only
  fetch.py     1️⃣ download Kapital html (DE + EN)
  split.py     2️⃣ html ➡ verses + footnotes
  align.py     3️⃣ DE ⬄ EN paragraph alignment
  score.py     4️⃣ LLM: quote · juice · difficulty · tags (~35 min)
  rank.py      5️⃣ LLM: relative ⭐ + 🔥 picks per section
  build.py     6️⃣ plans + export ➡ android assets
  andacht.py   7️⃣ LLM devotionals per plan day ➡ build.py again
  translate.py 8️⃣ quotes/UI/… ➡ it es pt zh ar (--only ui = just UI strings)
  translate_full.py 9️⃣ whole book ➡ it es pt zh ar (hours) · --export ➡ android assets
  server/      👥 community counter API (server/README.md: run, test, VPS install)
EOF
}

# brand icon (bin/icons = Simple Icons, CC0) inline ➡ no CDN = no 3rd-party request fr th page
ico() { sed -e 's/<title>[^<]*<\/title>//' -e 's/<svg /<svg class="ic" aria-hidden="true" /' "bin/icons/$1.svg"; }
enc() { python3 -c 'import sys, urllib.parse; print(urllib.parse.quote(sys.argv[1], safe=""))' "$1"; }

# shared look (stable page + /dev/) · tabs = CSS-only (radio + :checked), no JS
CSS='body{font:17px/1.5 system-ui,sans-serif;max-width:34em;margin:2em auto;padding:0 16px;background:#1d1230;color:#f4ead8}
a.b,a.o,a.s{display:block;text-align:center;padding:.9em;border-radius:12px;text-decoration:none;font-weight:700;margin:1em 0}
a.b{background:#b3261e;color:#fff}a.o{background:#e8b931;color:#1d1230}a.s{border:1.5px solid #e8b931;color:#e8b931}
h2{font-size:1.05em;margin:1.6em 0 .3em}code{background:#0004;padding:.1em .3em;border-radius:4px;word-break:break-all}small{opacity:.7}
a{color:#e8b931}ul,ol{padding-left:1.3em}
.w{background:#0003;border-radius:12px;padding:.4em 1em .6em;margin-top:1.8em;font-size:.88em}.w h2{margin:.5em 0 .2em}.w ul{margin:.3em 0}
.ic{width:1.1em;height:1.1em;fill:currentColor;vertical-align:-.15em}
.tb{margin-top:.6em}.tb input{display:none}.tb label{display:inline-block;padding:.35em 1em;border-radius:10px 10px 0 0;cursor:pointer;opacity:.6;background:#0003}
.tb label .ic{margin-right:.35em}.tb input:checked+label{opacity:1;background:#f4ead81a;font-weight:700}
.tp{display:none;background:#f4ead81a;border-radius:0 12px 12px 12px;padding:.2em 1em .8em}
#as:checked~#ps,#ad:checked~#pd,#tg:checked~#pg,#tf:checked~#pf{display:block}
footer{margin-top:2.4em;padding-top:1.2em;border-top:1px solid #f4ead822;text-align:center}
footer h2{margin:0 0 .8em}
.r{display:grid;grid-template-columns:repeat(3,1fr);gap:.5em;max-width:20em;margin:0 auto}
.p{display:flex;flex-direction:column;align-items:center;gap:.3em;padding:.7em .2em .55em;border:1px solid #e8b93155;border-radius:14px;background:none;
color:#e8b931;font:inherit;font-size:.8em;text-decoration:none;cursor:pointer;white-space:nowrap}.p:hover{background:#e8b93122}.p svg{width:1.5em;height:1.5em}
.w h3{font-size:1em;margin:.7em 0 .1em}.w summary{cursor:pointer;color:#e8b931;margin:.5em 0 .2em}
.sb{display:inline-flex;align-items:center;gap:.5em;padding:.8em 1.6em;border-radius:12px;border:1.5px solid #e8b931;background:none;color:#e8b931;font:inherit;font-weight:700;cursor:pointer}
.sb:hover{background:#e8b93122}
dialog{background:#2a1d42;color:#f4ead8;border:1px solid #e8b93155;border-radius:16px;padding:1.2em 1em 1em;max-width:min(24em,calc(100vw - 32px));text-align:center}
dialog::backdrop{background:#000a}dialog h2{margin:0 0 .4em}
.x{position:absolute;top:.4em;right:.6em;background:none;border:0;color:#f4ead8;font-size:1.2em;cursor:pointer;opacity:.7}
.so{display:grid;grid-template-columns:repeat(3,1fr);gap:.6em;margin:1em 0}
.so a{display:flex;flex-direction:column;align-items:center;gap:.35em;padding:.7em .2em;border-radius:14px;background:#f4ead814;color:#f4ead8;text-decoration:none;font-size:.78em}
.so a:hover{background:#e8b93133;color:#e8b931}.so .ic{width:1.6em;height:1.6em}
.r2{display:flex;gap:.5em;justify-content:center}.r2 .p{flex:1;flex-direction:row;justify-content:center;font-size:.85em}
svg.qr{width:10em;height:10em;margin:1em auto .3em;display:block;border-radius:10px}
.fb{margin-top:1.4em;font-size:.9em}'

# download page = 1 real page (stable) + <web>/dev/ = small page that's also th Obtainium source 4 th DEV app
#   1 apk link per page (version i th file name) ➡ Obtainium's HTML source finds it
#   page OUTDIR VERSION APKNAME [dev]
page() {
    local out=$1 ver=$2 apk=$3 chan=${4:-stable}
    local mb sha code li news="" md=""
    mb=$(command du -m "$out/$apk" | cut -f1); sha=$(sha256sum "$out/$apk" | cut -d' ' -f1)
    code=$("$ANDROID_HOME"/build-tools/*/aapt2 dump badging "$out/$apk" 2>/dev/null | sed -nE "1s/.*versionCode='([0-9]+)'.*/\1/p")
    # 🆕 th app's update check (data/Update.kt) + th Dev tab on th stable page read this
    printf '{"version":"%s","code":%s,"apk":"%s","sha256":"%s","mb":%s}\n' "$ver" "${code:-0}" "$apk" "$sha" "$mb" > "$out/version.json"
    if [ "$chan" = dev ]; then
        cat > "$out/index.html" <<EOF
<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="robots" content="noindex"><title>$APP DEV 🧪</title><link rel="icon" type="image/png" href="../favicon.png">
<style>$CSS</style>
<h1>🧪 $APP DEV</h1>
<p>Test build of what's coming next, for testers and contributors. It installs as a <b>separate app</b> ("Oh My Marx DEV")
next to the normal one, with its own data. Can break. Normal install: <a href="../">$WEB/</a></p>
<a class="b" href="$apk">⬇️ Download DEV $ver · ${mb} MB</a>
<a class="o" href="obtainium://add/$WEB/dev/">➕ Follow dev builds in Obtainium</a>
<p><small>$(date '+%F %H:%M') · sha256 ${sha:0:16}… · bugs: <a href="$SRC/issues/new/choose">issues</a></small></p>
EOF
        return
    fi
    # what's new = same as th app (⚙️): no emoji · this version open · older ones folded
    local v old=""
    li=$(bin/notes.py "$ver" --html --plain)
    for v in $(bin/notes.py --versions); do
        [ "$v" = "${ver%%-*}" ] && continue
        old+="<h3>$v</h3><ul>$(bin/notes.py "$v" --html --plain)</ul>"
    done
    [ -n "$old" ] && old="<details><summary>Older versions</summary>$old</details>"
    [ -n "$li" ] && news="<section class=\"w\"><h2>What’s new in ${ver%%-*}</h2><ul>$li</ul>$old</section>"
    # 🤖 agent-friendly: same facts as plain markdown (llmstxt.org convention) + JSON-LD below
    cat > "$out/llms.txt" <<EOF
# $APP

> Android app that serves Karl Marx's *Das Kapital* vol. 1 as a daily verse, with bible-app mechanics (verse cards,
> the whole book, reading plans, streaks) and a "Hooked mode" that labels every habit trick it uses.
> Free, open source (GPL-3.0), works offline, no ads, no analytics, no Google services.

- Version: $ver · ${mb} MB · Android 8.0+
- APK: $WEB/$apk (sha256 $sha)
- Latest version as JSON: $WEB/version.json
- Install + auto-update with Obtainium: add the URL $WEB/
- Languages: English (Moore/Aveling 1887, default), German (original), Italian, Spanish, Portuguese, Chinese, Arabic (whole book translated from German by a local LLM)
- Source: $SRC · mirror: $FGJ
- Release notes + older versions: $SRC/releases
- Contact: $MAIL

## What's new in $ver

$(bin/notes.py "$ver" | sed 's/^- [^[:alnum:]„“"(]* */- /')
EOF
    # 📣 share: native sheet 1st (phone) · then 1-tap links · prefilled txt = th hook · OG card = th preview evy messenger shows
    local u t
    u=$(enc "$SHORT"); t=$(enc "Read Das Kapital like the Bible ☭ a daily verse, the whole book, free & open source: $SHORT")
    # order (strix @1105/06): Obtainium 1st · APK 2nd (Stable | 🧪 Dev) · build-it-urself 3rd (GitHub | Forgejo) · what's new · footer = share + feedback
    cat > "$out/index.html" <<EOF
<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>$APP ☭</title>
<meta name="description" content="Read Das Kapital like the Bible: a daily verse from Marx, the whole book in 7 languages, reading plans, streaks. Free, open source Android app.">
<meta property="og:type" content="website"><meta property="og:site_name" content="$APP"><meta property="og:url" content="$WEB/">
<meta property="og:title" content="$APP ☭ Read Das Kapital like the Bible">
<meta property="og:description" content="A daily verse from Marx, the whole book in 7 languages, reading plans, streaks. Free and open source, for Android.">
<meta property="og:image" content="$WEB/og.png"><meta property="og:image:width" content="1200"><meta property="og:image:height" content="630">
<meta property="og:image:alt" content="Karl Marx with a halo: Oh My Marx, read Das Kapital like the Bible"><meta name="twitter:card" content="summary_large_image">
<link rel="icon" type="image/png" href="favicon.png"><link rel="apple-touch-icon" href="favicon.png">
<link rel="alternate" type="text/markdown" href="llms.txt" title="$APP in plain text (for AI agents)">
<script type="application/ld+json">{"@context":"https://schema.org","@type":"MobileApplication","name":"$APP",
"description":"Karl Marx's Das Kapital vol. 1 as a daily verse, bible-app style","operatingSystem":"Android 8.0+",
"applicationCategory":"BookApplication","softwareVersion":"$ver","fileSize":"${mb} MB","downloadUrl":"$WEB/$apk","installUrl":"$WEB/",
"inLanguage":["de","en","it","es","pt","zh","ar"],"license":"https://www.gnu.org/licenses/gpl-3.0.html","isAccessibleForFree":true,
"offers":{"@type":"Offer","price":"0","priceCurrency":"EUR"},"author":{"@type":"Person","name":"am0ebe"},
"sameAs":["$SRC","$FGJ"],"image":"$WEB/og.png"}</script>
<style>$CSS</style>
<h1>☭ $APP</h1>
<p>Read Marx like the Bible: a daily verse from <i>Das Kapital</i>, the whole book, reading plans, streaks —
and a Hooked mode that confesses every habit trick the app plays on you. In English, German (Marx's original), Italiano · Español · Português · 中文 · عربي.</p>
<h2>📲 Install with Obtainium <small>(recommended: updates itself)</small></h2>
<ol><li>Get <a href="https://github.com/ImranR98/Obtainium/releases/latest">Obtainium</a> (free, open source app updater)</li>
<li>Tap <b>➕ Add to Obtainium</b> below ➡ <b>Add</b> ➡ <b>Install</b></li></ol>
<a class="o" href="obtainium://add/$WEB/">➕ Add to Obtainium</a>
<p><small>Button does nothing? In Obtainium tap ＋ and paste <code>$WEB/</code></small></p>
<h2>Or download the APK</h2>
<div class="tb">
<input type="radio" name="a" id="as" checked><label for="as">Stable $ver</label><input type="radio" name="a" id="ad"><label for="ad">🧪 Dev</label>
<div class="tp" id="ps"><a class="b" href="$apk">⬇️ Download $APP $ver · ${mb} MB</a>
<ol><li>Tap the download ➡ allow installing from this source (once) ➡ <b>Install</b></li>
<li>Play Protect may warn about an "unknown app" — normal outside the Play Store ➡ <b>Install anyway</b></li>
<li>The app tells you when a new version is out ➡ tap ➡ install over it, your data stays</li></ol></div>
<div class="tp" id="pd"><p>Test builds of what's coming next, for testers and contributors. A <b>separate app</b> ("Oh My Marx DEV")
next to the normal one, with its own data. Can break.</p>
<a class="s" id="dv" href="dev/">⬇️ Latest dev build</a>
<a class="s" href="obtainium://add/$WEB/dev/">➕ Follow dev builds in Obtainium</a>
<p><small>By hand: Obtainium ➡ ＋ ➡ <code>$WEB/dev/</code></small></p></div>
</div>
<h2>Or build it yourself</h2>
<p>Open source (GPL-3.0): read the code and build the APK on your own machine instead of trusting ours.
Step-by-step guide in the README, no Android experience needed.</p>
<div class="tb">
<input type="radio" name="t" id="tg" checked><label for="tg">$(ico github)GitHub</label><input type="radio" name="t" id="tf"><label for="tf">$(ico forgejo)Forgejo</label>
<div class="tp" id="pg"><a class="s" href="$SRC">$(ico github) Source on GitHub</a>
<p><code>git clone $SRC</code></p>
<p><small>Bug or idea and a GitHub account? <a href="$SRC/issues/new/choose">Open an issue</a> · all: <a href="$SRC/issues">issues</a> · <a href="$SRC/releases">releases</a></small></p></div>
<div class="tp" id="pf"><a class="s" href="$FGJ">$(ico forgejo) Source on Forgejo</a>
<p><code>git clone $FGJ.git</code></p>
<p><small>Same code, mirrored on our own server in Germany (self-hosted <a href="https://forgejo.org">Forgejo</a>, no Microsoft). No sign-up there: issues go to GitHub or by mail.</small></p></div>
</div>
$news
<p><small>Older versions + all release notes: <a href="$SRC/releases">GitHub releases</a> · v$ver · $(date '+%F %H:%M') · sha256 ${sha:0:16}…</small></p>
<footer>
<h2>Know someone who'd read Marx on the bus? ☭</h2>
<button class="sb" id="sh"><svg class="ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="fill:none"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><path d="M8.6 13.5l6.8 4M15.4 6.5l-6.8 4"/></svg> Share $APP</button>
<p class="fb" id="feedback">💬 Bug, idea, praise? <a href="mailto:$MAIL?subject=Oh%20My%20Marx%20$ver&amp;body=What%20happened%20or%20what%20would%20you%20like%3F%0A%0A%0A(a%20screenshot%20helps%20%E2%80%94%20version%20$ver)">$MAIL</a>
· <a href="$SRC/issues/new/choose">GitHub issue</a></p>
</footer>
<!-- 📣 share panel: same on fon + PC · "More apps…" = th fon's own share sheet (Signal, Element/Matrix, …) where it exists -->
<dialog id="sd" aria-label="Share">
<button class="x" id="sx" aria-label="Close">✕</button>
<h2>Share $APP</h2>
<nav class="so">
<a href="https://s2f.kytta.dev/?text=$t" title="Fediverse: Mastodon, Lemmy, Misskey …" rel="noopener" target="_blank">$(ico mastodon)<span>Fediverse</span></a>
<a href="https://bsky.app/intent/compose?text=$t" title="Bluesky" rel="noopener" target="_blank">$(ico bluesky)<span>Bluesky</span></a>
<a href="https://t.me/share/url?url=$u&amp;text=$(enc "Read Das Kapital like the Bible ☭")" title="Telegram" rel="noopener" target="_blank">$(ico telegram)<span>Telegram</span></a>
<a href="https://www.reddit.com/submit?url=$u&amp;title=$(enc "Oh My Marx: read Das Kapital like the Bible (free, open source Android app)")" title="Reddit" rel="noopener" target="_blank">$(ico reddit)<span>Reddit</span></a>
<a href="mailto:?subject=$(enc "Oh My Marx ☭")&amp;body=$t" title="Mail"><svg class="ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="fill:none"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><path d="M22 6l-10 7L2 6"/></svg><span>Mail</span></a>
<a href="https://wa.me/?text=$t" title="WhatsApp" rel="noopener" target="_blank">$(ico whatsapp)<span>WhatsApp</span></a>
</nav>
<div class="r2"><button class="p" id="cp"><span>📋 Copy link</span></button><button class="p" id="mo" hidden>More apps…</button></div>
$(qrencode -t SVG --inline --svg-path -m 2 --foreground=1d1230 --background=f4ead8 "$SHORT" | sed 's/<svg /<svg class="qr" role="img" aria-label="QR code: scagent.de\/omm" /')
<small>Scan with a phone camera · scagent.de/omm</small>
</dialog>
<script>
var U = '$SHORT', TXT = 'Read Das Kapital like the Bible ☭ a daily verse, the whole book, free & open source';
var d = document.getElementById('sd'), mo = document.getElementById('mo');
document.getElementById('sh').onclick = function () { d.showModal(); };
document.getElementById('sx').onclick = function () { d.close(); };
d.onclick = function (e) { if (e.target === d) d.close(); };   // tap outside th panel = close
document.getElementById('cp').onclick = function () {
  var s = this.querySelector('span');
  navigator.clipboard.writeText(U).then(function () { s.textContent = '✔ Copied'; }, function () { prompt('Copy this link:', U); });
};
// desktop Firefox/Chrome on Linux expose navigator.share but open nothing ➡ touch devices only
if (navigator.share && matchMedia('(pointer: coarse)').matches) { mo.hidden = false; mo.onclick = function () { navigator.share({ title: '$APP', text: TXT, url: U }).catch(function () {}); }; }
// 🧪 Dev tab: newest dev build straight fr dev/version.json (dev deploys don't re-render this page)
fetch('dev/version.json', { cache: 'no-cache' }).then(function (r) { return r.json(); }).then(function (j) {
  var a = document.getElementById('dv'); a.href = 'dev/' + j.apk; a.textContent = '⬇️ Download DEV ' + j.version + (j.mb ? ' · ' + j.mb + ' MB' : '');
}).catch(function () {});
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
        [ -z "$sub" ] && rsync -t --chmod=F644 bin/og.png "$out/llms.txt" "vps:$d/"   # stable only: preview card + agent txt
        ssh vps "cd $d && ls -t *.apk | tail -n +4 | xargs -r rm --"
    done
    # 👥 Kreis invite landing page (bin/kreis.html) ➡ <web>/k/ · refreshed on evy publish
    for d in $DIRS; do
        ssh vps "mkdir -p $d/k"
        rsync -t --chmod=F644 bin/kreis.html "vps:$d/k/index.html"
    done
}
