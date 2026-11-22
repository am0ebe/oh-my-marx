#!/usr/bin/env bash
# 🧪 test_apptool — apptool against a throw-away app: fake gradle, local folders, no network, no fon (~5 s)
set -uo pipefail
VERSION=1.0
KIT=$(cd "$(dirname "$(readlink -f "$0")")/.." && pwd)
TOOL=$KIT/tool/apptool

usage() {
    cat <<EOF
test_apptool $VERSION — tst apptool (deploy · ship · page · gate · guards) wo a build, a server o a fon

y:   apptool ships evy app ➡ a bug here = no app can deploy · it died silently on th 1st clean-tree deploy
     (page hook ended 1 under set -e) · until now only tstd by hand
wh:  makes a tmp app (git repo + bin/lib.sh + a fake gradlew th writes a fake apk) ➡ runs th real apptool
     against local folders (PUBLISH_HOST="" · WEB=file://…) ➡ exit 0 = all green

usage: tool/test_apptool.sh [-v|--verbose] [-k|--keep] [-o|--only WORD] | -h | -V
  -v, --verbose    print apptool's output 4 evy case
  -k, --keep       keep th tmp app (path printed) 2 look around
  -o, --only WORD  run only cases whose name contains WORD
  -h, --help       this help
  -V, --version    print version

run:
  tool/test_apptool.sh             # b4 evy kit commit th touches tool/
  tool/test_apptool.sh -o ship -v  # only th ship cases, w output
EOF
}

verbose=0; keep=0; only=""
while [ $# -gt 0 ]; do
    case "$1" in
        -h | --help) usage; exit 0 ;;
        -V | --version) echo "test_apptool $VERSION"; exit 0 ;;
        -v | --verbose) verbose=1 ;;
        -k | --keep) keep=1 ;;
        -o | --only) only=${2:-}; shift ;;
        *) usage >&2; exit 1 ;;
    esac
    shift
done

T=$(mktemp -d)
cleanup() { if [ "$keep" = 1 ]; then echo "tmp app kept: $T"; else rm -rf "$T"; fi; }
trap cleanup EXIT
export STUB_LOG=$T/gradle.log TMPDIR=$T/tmp; mkdir "$TMPDIR"   # apptool's mktemp lands here ➡ a leaked build dir shows
# hermetic: no real SDK / JDK (no adb daemon, no aapt2) · git wo th user's config
export APPKIT_ANDROID_HOME=$T/sdk APPKIT_JAVA_HOME=$T/jdk
export GIT_CONFIG_GLOBAL=/dev/null GIT_CONFIG_SYSTEM=/dev/null
export GIT_AUTHOR_NAME=t GIT_AUTHOR_EMAIL=t@t GIT_COMMITTER_NAME=t GIT_COMMITTER_EMAIL=t@t

pass=0; fail=0; OUT=""; RC=0; name=""; skip=0
# new_app [page-hook-file]: fresh app repo (clean tree, pushed 2 a bare origin) + empty web folder
new_app() {
    rm -rf "$T/app" "$T/origin.git" "$T/www" "$T/local"; : > "$STUB_LOG"
    mkdir -p "$T/app/bin" "$T/app/android/app" "$T/www"
    cd "$T/app" || exit 1
    echo 'val appVersion = "0.1.0"' > android/app/build.gradle.kts
    cat > android/gradlew <<'EOF'
#!/usr/bin/env bash
# fake gradle: writes th apk + metadata apptool reads
echo "$*" >> "$STUB_LOG"
base=0.0.0; kind=""
for a in "$@"; do
    case "$a" in -PdevBase=*) base=${a#-PdevBase=} ;; *assembleDebug) kind=debug ;; *assembleRelease) kind=release ;; esac
done
d=app/build/outputs/apk/$kind; mkdir -p "$d"
ver="$base-dev.${STUB_STAMP:-0101.0000}"
[ "$kind" = debug ] && printf '{"elements":[{"versionName": "%s"}]}\n' "$ver" > "$d/output-metadata.json"
{ [ -n "${STUB_SAME:-}" ] || echo "$ver $kind"; head -c 2048 /dev/zero; } > "$d/app-$kind.apk"
EOF
    chmod +x android/gradlew
    cat > bin/lib.sh <<EOF
APP="demo"; FILE="demo"; PKG=org.example.demo; AUTHOR=me
WEB="file://$T/www"; DIRS="$T/www"; PUBLISH_HOST=""
SRC=https://example.org/me/demo
NOTES=demo_notes
demo_notes() { echo "- a note 4 \$1"; }
EOF
    [ -n "${1:-}" ] && cat "$1" >> bin/lib.sh
    printf 'android/app/build/\n' > .gitignore
    git init -q -b main . && git add -A && git commit -qm init
    git init -q --bare "$T/origin.git" && git remote add origin "$T/origin.git" && git push -q origin main
}
# run NAME WANT_RC cmd… ➡ $OUT · $RC
run() {
    name=$1; local want=$2; shift 2
    case "$name" in *"$only"*) skip=0 ;; *) skip=1; return 0 ;; esac
    OUT=$("$@" 2>&1); RC=$?
    [ "$verbose" = 1 ] && printf '── %s (exit %s)\n%s\n' "$name" "$RC" "$OUT"
    if [ "$RC" = "$want" ]; then ok "exit $want"; else no "exit $RC, want $want"; fi
}
ok() { pass=$((pass + 1)); [ "$verbose" = 1 ] && echo "  ✔️ $name · $1"; return 0; }
no() { fail=$((fail + 1)); echo "  ❌ $name · $1"; [ "$verbose" = 1 ] || printf '%s\n' "$OUT" | tail -5 | sed 's/^/       /'; return 0; }
has() { [ "$skip" = 1 ] && return 0; case "$OUT" in *"$1"*) ok "says '$1'" ;; *) no "does not say '$1'" ;; esac; }
hasnt() { [ "$skip" = 1 ] && return 0; case "$OUT" in *"$1"*) no "says '$1'" ;; *) ok "silent on '$1'" ;; esac; }
is() { [ "$skip" = 1 ] && return 0; if eval "$2"; then ok "$1"; else no "$1"; fi; }

# ── basics ──
new_app
run "help" 0 "$TOOL" -h;                 has "usage: apptool"
run "version flag" 0 "$TOOL" -V;         has "apptool "
run "unknown cmd" 1 "$TOOL" nope
run "deploy help" 0 "$TOOL" deploy -h;   has "--dry-run"
run "ship help" 0 "$TOOL" ship -h;       has "--key"
run "unknown deploy flag" 1 "$TOOL" deploy -z
run "app version" 0 "$TOOL" version;     is "prints 0.1.0" '[ "$OUT" = 0.1.0 ]'
run "oblink" 0 "$TOOL" oblink;           has "obtainium://app/"; has "demo-"; hasnt "allowInsecure"
run "oblink dev" 0 "$TOOL" oblink dev;   has "org.example.demo.dev"
(cd "$T" && run "no lib.sh" 1 "$TOOL" version && has "no bin/lib.sh")
run "missing config" 1 env -u X bash -c 'sed -i "s/^SRC=.*//" bin/lib.sh; exec "$0" version' "$TOOL"; has "SRC not set"

# ── deploy ──
new_app
run "deploy dry, clean tree, kit page" 0 "$TOOL" deploy -n
has "dry run"; has "demo-0.1.0-dev.0101.0000.apk"; has '"version":"0.1.0-dev.0101.0000"'
is "nothing uploaded" '[ -z "$(ls "$T/www")" ]'
is "tmp build dir gone" '[ -z "$(ls "$TMPDIR")" ]'

run "deploy 2 a folder" 0 "$TOOL" deploy
is "apk there" '[ -f "$T/www/dev/demo-0.1.0-dev.0101.0000.apk" ]'
is "page there" 'grep -q "Download DEV 0.1.0-dev.0101.0000" "$T/www/dev/index.html"'
is "version.json = th updater contract" 'grep -qE "^\{\"version\":\"0.1.0-dev.0101.0000\",\"code\":[0-9]+,\"apk\":\"demo-0.1.0-dev.0101.0000.apk\",\"sha256\":\"[0-9a-f]{64}\",\"mb\":[0-9]+\}$" "$T/www/dev/version.json"'
is "sha256 = th apk's" '[ "$(sha256sum "$T/www/dev/demo-0.1.0-dev.0101.0000.apk" | cut -d" " -f1)" = "$(sed -E "s/.*\"sha256\":\"([0-9a-f]+)\".*/\1/" "$T/www/dev/version.json")" ]'
is "stable folder untouched" '[ ! -f "$T/www/index.html" ]'

run "status reads th live page" 0 "$TOOL" status; has "web dev: {"; has "0.1.0-dev.0101.0000"

for s in 0102.0000 0103.0000 0104.0000 0105.0000; do STUB_STAMP=$s "$TOOL" deploy >/dev/null 2>&1; sleep 0.05; done
run "keeps th newest 3 apks" 0 env STUB_STAMP=0106.0000 "$TOOL" deploy
is "3 apks left" '[ "$(ls "$T/www/dev"/*.apk | wc -l)" = 3 ]'
is "newest kept" '[ -f "$T/www/dev/demo-0.1.0-dev.0106.0000.apk" ]'

sleep 1.1   # rsync skips a file w th same size + same second (version.json) · real deploys r ≥ 1 min apart
STUB_SAME=1 STUB_STAMP=0107.0000 "$TOOL" deploy >/dev/null 2>&1   # live apk now starts w th same bytes as th nxt build
run "1 KiB guard: same 1st bytes as live" 1 env STUB_SAME=1 STUB_STAMP=0108.0000 "$TOOL" deploy
has "1st 1 KiB"; is "not uploaded" '[ ! -f "$T/www/dev/demo-0.1.0-dev.0108.0000.apk" ]'

git -c tag.gpgSign=false tag v0.1.0
run "dev base = nxt minor once shipped" 0 env STUB_STAMP=0109.0000 "$TOOL" deploy -n
has "demo-0.2.0-dev.0109.0000.apk"; is "gradle got -PdevBase=0.2.0" 'tail -1 "$STUB_LOG" | grep -q -- "-Pdev -PdevBase=0.2.0"'

run "local page needs config" 1 "$TOOL" deploy -p; has "LOCAL_DIR"
new_app; printf 'LOCAL_DIR="%s"; LOCAL_WEB="http://box:1/demo"\n' "$T/local" >> bin/lib.sh; git commit -qam local
run "local page only" 0 "$TOOL" deploy -p
is "local apk + page" '[ -f "$T/local/demo-0.1.0-dev.0101.0000.apk" ] && [ -f "$T/local/index.html" ] && [ -f "$T/local/version.json" ]'
is "web untouched" '[ -z "$(ls "$T/www")" ]'
run "qr link o a http page" 0 bash -c 'qrencode() { :; }; export -f qrencode; exec "$0" deploy -r -p' "$TOOL"; has "allowInsecure"

# ── public gate ──
new_app; echo 'PUBLIC_GATE=1' >> bin/lib.sh; git commit -qam gate; rmdir "$T/www"
run "gate: new web dir refused" 2 "$TOOL" deploy
has "does not exist yet"; is "nothing created" '[ ! -e "$T/www" ]'; is "refused b4 th build" '[ ! -s "$STUB_LOG" ]'
run "gate: dry run still allowed" 0 "$TOOL" deploy -n; is "nothing created" '[ ! -e "$T/www" ]'
run "gate: -y creates it" 0 "$TOOL" deploy -y; is "apk there" '[ -f "$T/www/dev/demo-0.1.0-dev.0101.0000.apk" ]'
run "gate: 2nd upload needs no -y" 0 env STUB_STAMP=0102.0000 "$TOOL" deploy

# ── page hooks ──
cat > "$T/hook_ok" <<'EOF'
page() {   # app's own page · th stamp line is th one th killed a clean-tree deploy when written as $([ … ] && echo)
    local stamp; version_json "$1" "$2" "$3"
    stamp="commit $(git rev-parse --short HEAD)$([ -z "$(git status --porcelain)" ] || echo ' + uncommitted')"
    echo "<p>$APP $2 · $PAGE_URL · $stamp</p>" > "$1/index.html"
}
EOF
new_app "$T/hook_ok"
run "own page hook, clean tree" 0 "$TOOL" deploy
is "hook's page, w PAGE_URL" 'grep -q "demo 0.1.0-dev.0101.0000 · file://.*/www/dev/ · commit" "$T/www/dev/index.html"'
is "no dirty mark" '! grep -q uncommitted "$T/www/dev/index.html"'
echo x > dirty.txt
run "own page hook, dirty tree" 0 env STUB_STAMP=0102.0000 "$TOOL" deploy; is "dirty mark" 'grep -q uncommitted "$T/www/dev/index.html"'

cat > "$T/hook_dies" <<'EOF'
page() { local stamp; version_json "$1" "$2" "$3"; stamp="x$([ -n "$(git status --porcelain)" ] && echo ' + uncommitted')"; echo "$stamp" > "$1/index.html"; }
EOF
new_app "$T/hook_dies"
run "a hook th dies says where" 1 "$TOOL" deploy -n; has "🐛"; has "page"; has "stamp="
is "nothing uploaded" '[ -z "$(ls "$T/www")" ]'

cat > "$T/hook_forgets" <<'EOF'
page() { echo hi > "$1/index.html"; }
EOF
new_app "$T/hook_forgets"
run "a hook wo version.json is refused" 1 "$TOOL" deploy; has "version.json"; is "nothing uploaded" '[ -z "$(ls "$T/www")" ]'

# ── ship ──
new_app
run "ship dry" 0 "$TOOL" ship 0.3.0 -n
has "wd bump 0.1.0 ➡ 0.3.0"; has "no bump · no tag"; is "nothing uploaded" '[ -z "$(ls "$T/www")" ]'
is "no bump" 'grep -q "\"0.1.0\"" android/app/build.gradle.kts'; is "no tag" '[ -z "$(git tag)" ]'

echo x > dirty.txt
run "ship refuses a dirty tree" 1 "$TOOL" ship; has "uncommitted"
rm dirty.txt; git commit -q --allow-empty -m local-only
run "ship refuses unpushed commits" 1 "$TOOL" ship; has "push 1st"
git push -q origin main

run "ship as it stands" 0 "$TOOL" ship
has "shipped v0.1.0"; has "no GitHub release"
is "apk + page + version.json" '[ -f "$T/www/demo-0.1.0.apk" ] && [ -f "$T/www/index.html" ] && grep -q "\"version\":\"0.1.0\"" "$T/www/version.json"'
is "tag pushed" 'git ls-remote --exit-code origin refs/tags/v0.1.0 >/dev/null'
is "notes i th tag" 'git tag -l -n9 v0.1.0 | grep -q "a note 4 0.1.0"'
is "what's new on th page" 'grep -q "<li>a note 4 0.1.0</li>" "$T/www/index.html"'

run "same version twice" 1 "$TOOL" ship; has "alr shipped"
run "older version" 1 "$TOOL" ship 0.0.9; has "≤ last shipped"
run "not a version" 1 "$TOOL" ship 1.2
run "ship patch" 0 "$TOOL" ship patch
has "0.1.0 ➡ 0.1.1"; has "shipped v0.1.1"
is "bump committed + pushed" 'grep -q "\"0.1.1\"" android/app/build.gradle.kts && [ "$(git log -1 --format=%s)" = v0.1.1 ] && [ "$(git rev-parse HEAD)" = "$(git rev-parse origin/main)" ]'
is "tree clean aftr ship" '[ -z "$(git status --porcelain)" ]'
run "0.10.0 > 0.9.0 (not string order)" 0 bash -c '"$0" ship 0.9.0 >/dev/null 2>&1 && exec "$0" ship 0.10.0' "$TOOL"; has "shipped v0.10.0"

rm -f "$T/www/index.html"
run "page only" 0 "$TOOL" page
has "page re-rendered 4 v0.10.0"; is "page back" 'grep -q "Download demo 0.10.0" "$T/www/index.html"'
is "apk not touched" '[ -f "$T/www/demo-0.10.0.apk" ]'

new_app; echo 'SHIP_GUARD=demo_guard; demo_guard() { echo "tsts red"; return 1; }' >> bin/lib.sh; git commit -qam guard; git push -q origin main
run "ship guard stops b4 th bump" 1 "$TOOL" ship minor
has "tsts red"; is "no bump · no build" 'grep -q "\"0.1.0\"" android/app/build.gradle.kts && [ ! -s "$STUB_LOG" ]'

new_app; printf 'NOTES=no_notes; no_notes() { :; }\n' >> bin/lib.sh; git commit -qam nonotes; git push -q origin main
run "no release notes ➡ no ship" 1 "$TOOL" ship minor; has "no release notes"; is "no bump" 'grep -q "\"0.1.0\"" android/app/build.gradle.kts'

new_app; echo "RELEASE_KEY=$T/none.properties" >> bin/lib.sh; git commit -qam key; git push -q origin main
run "release key missing" 1 "$TOOL" ship -n; has "no release key"
touch "$T/none.properties"
run "apk not signed by th release key" 1 "$TOOL" ship -n; has "not th release key"; is "nothing uploaded" '[ -z "$(ls "$T/www")" ]'

name="all cases"; skip=0; OUT=$(ls "$TMPDIR")
is "no tmp dir leaked by any run (good · refused · died)" '[ -z "$OUT" ]'

echo
if [ "$fail" = 0 ]; then echo "✔️ apptool: $pass checks green"; else echo "❌ apptool: $fail red · $pass green"; exit 1; fi
