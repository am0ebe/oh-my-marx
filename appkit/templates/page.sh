# 📄 appkit page template — th default download page (apptool sources it when th app's bin/lib.sh hs no page())
#   own look? copy this page() into bin/lib.sh + change it (it must keep `version_json` + 1 apk link per page)
#   order (🦠 #327 @1122): 1. APK + in-app update (main) · 2. Obtainium "also works" · build it yourself · what's new · feedback
#   uses: APP FILE PKG WEB SRC · optional: TAGLINE · FGJ (2nd git host) · MAIL · NOTES · COLORS (BG FG ACCENT BTN)
#   page OUTDIR VERSION APKNAME [dev]
# shellcheck shell=bash

page() {
    local out=$1 ver=$2 apk=$3 chan=${4:-stable}
    local mb sha news="" li="" bg=${BG:-#1d1230} fg=${FG:-#f4ead8} ac=${ACCENT:-#e8b931} bt=${BTN:-#b3261e}
    version_json "$out" "$ver" "$apk"   # 🆕 th in-app updater reads this · nvr drop it
    mb=$(command du -m "$out/$apk" | cut -f1); sha=$(sha256sum "$out/$apk" | cut -d' ' -f1)
    local css="body{font:17px/1.5 system-ui,sans-serif;max-width:34em;margin:2em auto;padding:0 16px;background:$bg;color:$fg}
a{color:$ac}a.b,a.o{display:block;text-align:center;padding:.9em;border-radius:12px;text-decoration:none;font-weight:700;margin:1em 0}
a.b{background:$bt;color:#fff}a.o{background:$ac;color:$bg}h2{font-size:1.05em;margin:1.6em 0 .3em}
code{background:#0004;padding:.1em .3em;border-radius:4px;word-break:break-all}small{opacity:.7}"
    local ob_tip="<p><small>Button does nothing? In Obtainium tap ＋ and paste the page URL, and under <i>Version string extraction regular expression</i> enter <code>$FILE-(.+)\\.apk</code></small></p>"
    if [ "$chan" = dev ]; then
        cat > "$out/index.html" <<EOF
<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<meta name="robots" content="noindex"><title>$APP DEV 🧪</title><link rel="icon" type="image/png" href="../favicon.png">
<style>$css</style>
<h1>🧪 $APP DEV</h1>
<p>Test build of what's coming next. It installs as a <b>separate app</b> ("$APP DEV") next to the normal one, with its own data. Can break. Normal install: <a href="../">$WEB/</a></p>
<a class="b" href="$apk">⬇️ Download DEV $ver · ${mb} MB</a>
<p><small>The DEV app tells you about newer dev builds itself.</small></p>
<a class="o" href="$(oblink "$PKG.dev" "$WEB/dev/" "$APP DEV")">➕ Also works: follow dev builds in Obtainium</a>
<p><small>$(date '+%F %H:%M') · sha256 ${sha:0:16}…</small></p>
EOF
        return
    fi
    # what's new = th app's release notes (md "- line" ➡ <li>)
    [ -n "${NOTES:-}" ] && li=$($NOTES "$ver" 2>/dev/null | sed -nE 's/^- +(.*)/<li>\1<\/li>/p') || true
    [ -n "$li" ] && news="<h2>What’s new in ${ver%%-*}</h2><ul>$li</ul>"
    cat > "$out/index.html" <<EOF
<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>$APP</title><meta name="description" content="${TAGLINE:-$APP for Android}">
<link rel="icon" type="image/png" href="favicon.png"><style>$css</style>
<h1>$APP</h1>
<p>${TAGLINE:-}</p>
<h2>1. Download the APK <small>(recommended: the app updates itself)</small></h2>
<a class="b" href="$apk">⬇️ Download $APP $ver · ${mb} MB</a>
<ol><li>Tap the download ➡ allow installing from this source (once) ➡ <b>Install</b></li>
<li>Play Protect may warn about an "unknown app" — normal outside the Play Store ➡ <b>Install anyway</b></li>
<li>Updates: the app tells you when a new version is out ➡ <b>Install</b> ➡ confirm, your data stays.
⚙️ <b>Update automatically</b> = new versions install without asking (Android 12+)</li></ol>
<h2>2. Also works: Obtainium <small>(if you already use it)</small></h2>
<ol><li>Get <a href="https://github.com/ImranR98/Obtainium/releases/latest">Obtainium</a> (free, open source app updater)</li>
<li>Tap <b>➕ Add to Obtainium</b> below ➡ <b>Add</b> ➡ <b>Install</b></li></ol>
<a class="o" href="$(oblink "$PKG" "$WEB/" "$APP")">➕ Add to Obtainium</a>
$ob_tip
<p><small>🧪 Test builds: <a href="dev/">$WEB/dev/</a></small></p>
<h2>Or build it yourself</h2>
<p>Open source: <a href="$SRC">$SRC</a>${FGJ:+ · mirror: <a href='$FGJ'>$FGJ</a>}</p>
$news
<p><small>All versions + notes: <a href="$SRC/releases">releases</a> · v$ver · $(date '+%F %H:%M') · sha256 ${sha:0:16}…${MAIL:+ · 💬 <a href='mailto:$MAIL'>$MAIL</a>}</small></p>
EOF
}
