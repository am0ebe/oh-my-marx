# 🧰 appkit apptool lib — sourced (by apptool + by th app's bin/lib.sh), defines functions only, no side effects
#   besides 2 env defaults · th app's config + hooks live i th app's bin/lib.sh (README.md § tool)
# shellcheck shell=bash

APPKIT_TOOL=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
APPKIT=$(dirname "$APPKIT_TOOL")
# 🔧 build env (our boxes) · override: APPKIT_JAVA_HOME / APPKIT_ANDROID_HOME
export JAVA_HOME=${APPKIT_JAVA_HOME:-$HOME/.local/opt/jdk21} ANDROID_HOME=${APPKIT_ANDROID_HOME:-$HOME/Android/Sdk}
ADB=$ANDROID_HOME/platform-tools/adb

# th 1 version number: `val appVersion = "x.y.z"` i th app module's build.gradle.kts (tool/app-version.gradle.kts reads it)
app_version() { sed -nE 's/^val appVersion = "([^"]+)".*/\1/p' "${GRADLE_APP:-android/app/build.gradle.kts}"; }

enc() { python3 -c 'import sys, urllib.parse; print(urllib.parse.quote(sys.argv[1], safe=""))' "$1"; }

# 🆕 version.json = th updater's contract (core/selfupdate Update.parse): version · code · apk · sha256 · mb
#   version_json OUTDIR VERSION APKNAME   (apk must sit i OUTDIR)
version_json() {
    local out=$1 ver=$2 apk=$3 mb sha code
    mb=$(command du -m "$out/$apk" | cut -f1); sha=$(sha256sum "$out/$apk" | cut -d' ' -f1)
    code=$("$ANDROID_HOME"/build-tools/*/aapt2 dump badging "$out/$apk" 2>/dev/null | sed -nE "1s/.*versionCode='([0-9]+)'.*/\1/p") || true
    printf '{"version":"%s","code":%s,"apk":"%s","sha256":"%s","mb":%s}\n' "$ver" "${code:-0}" "$apk" "$sha" "$mb" > "$out/version.json"
}

# ➕ Obtainium "add app" link WITH settings: plain obtainium://add/URL ➡ HTML source w/o a version regex =
#   no version detection, only an APK hash ➡ breaks once th app was installed by hand/adb ("shows new version, can't
#   install"). th regex reads th version fr th file name (<FILE>-0.9.0-dev.1107.0415.apk) = exactly th installed
#   versionName ➡ Obtainium compares real versions. Format = Obtainium's own export (lib/pages/apps.dart).
#   oblink PKG URL NAME   (uses $FILE · $AUTHOR)
oblink() {
    python3 - "$@" "$FILE" "${AUTHOR:-}" <<'PY'
import json, re, sys, urllib.parse
pkg, url, name, file, author = sys.argv[1:6]
# fallback if th regex ever misses: hash th LINK (has th version i it), not th 1st bytes o th APK (identical across
# builds ➡ Obtainium's dflt "partial APK hash" nvr changes = "no update")
settings = {"versionExtractionRegEx": re.escape(file) + r"-(.+)\.apk", "matchGroupToUse": "$1", "versionDetection": True,
            "defaultPseudoVersioningMethod": "APKLinkHash"}
if url.startswith("http://"):          # a LOCAL_WEB page on th tailnet = plain http ➡ Obtainium wants this set
    settings["allowInsecure"] = True
app = {"id": pkg, "url": url, "author": author, "name": name, "preferredApkIndex": 0,
       "additionalSettings": json.dumps(settings), "overrideSource": None}
print("obtainium://app/" + urllib.parse.quote(json.dumps(app), safe=""))
PY
}

# ⬆️ upload 2 evy page dir (+ SUBDIR): apk 1st (if any), page 2nd (page nvr points at a missing file) · keep newest 3 apks
#   hooks (optional, th app's): publish_dir OUTDIR SUBDIR DIR (extra files per dir) · publish_done OUTDIR SUBDIR
#   publish OUTDIR [dev]
#   config: PUBLISH_HOST (dflt vps · "" = DIRS r folders on this box, 4 tsts) · PUBLIC_GATE=1 ➡ a web dir th does not
#   exist yet is only created w YES_PUBLIC=1 (apptool -y): a file there = public th same second
publish() {
    local out=$1 sub=${2:-} d fav=${FAVICON:-bin/favicon.png}
    for d in $DIRS; do
        d="$d${sub:+/$sub}"
        gate_check "$sub"
        far "mkdir -p $d" || { echo "🚧 can't create $d on ${PUBLISH_HOST-vps} (parent owned by root? once: sudo install -d -o \$USER $d)" >&2; exit 1; }
        ls "$out"/*.apk >/dev/null 2>&1 && rsync -t --chmod=F644 "$out"/*.apk "$(far_path "$d")/"
        rsync -t --chmod=F644 "$out/index.html" "$out/version.json" "$(far_path "$d")/"
        [ -f "$fav" ] && rsync -t --chmod=F644 "$fav" "$(far_path "$d")/"   # an app w an inline favicon hs no file
        declare -F publish_dir >/dev/null && publish_dir "$out" "$sub" "$d"
        far "cd $d && ls -t *.apk | tail -n +4 | xargs -r rm --"
    done
    if declare -F publish_done >/dev/null; then publish_done "$out" "$sub"; fi
}
# 🛑 gate_check [SUBDIR]: PUBLIC_GATE=1 + a web dir th is not there + no -y ➡ stop (exit 2) · apptool calls it b4 th build too
gate_check() {
    [ "${PUBLIC_GATE:-}" = 1 ] && [ "${YES_PUBLIC:-}" != 1 ] || return 0
    local d
    for d in $DIRS; do
        d="$d${1:+/$1}"
        far "test -d $d" || { echo "🛑 $d does not exist yet: th 1st upload makes a public page ➡ owner's ok 1st, then rerun w -y · look 1st: -n" >&2; exit 2; }
    done
}
# where th pages live: PUBLISH_HOST unset = vps (as alw) · set empty = this box (tsts, dry setups)
far() { if [ -n "${PUBLISH_HOST-vps}" ]; then ssh -o BatchMode=yes -o ConnectTimeout=10 "${PUBLISH_HOST-vps}" "$1"; else sh -c "$1"; fi; }
far_path() { if [ -n "${PUBLISH_HOST-vps}" ]; then echo "${PUBLISH_HOST-vps}:$1"; else echo "$1"; fi; }

# 🔑 release key (optional): RELEASE_KEY = a .properties file outside th repo (storeFile · storePassword · keyAlias ·
#   keyPassword) th th app's gradle signingConfig reads · unset = th app signs releases its own way (debug key)
#   key_make = create keystore + properties if missing · key_check APK = th apk must carry that key, nvr th debug key
key_make() {
    [ -n "${RELEASE_KEY:-}" ] || { echo "ℹ️ no RELEASE_KEY i bin/lib.sh ➡ releases use th debug key (~/.android/debug.keystore) · lose it = evy install must uninstall"; return 0; }
    if [ -f "$RELEASE_KEY" ]; then echo "🔑 release key: $(sed -nE 's/^storeFile=//p' "$RELEASE_KEY") · settings $RELEASE_KEY · ⚠️ bup both (lost = evy install must uninstall)"; return 0; fi
    local store="${RELEASE_KEY%.properties}.jks" pass alias=${FILE,,}
    pass=$(openssl rand -hex 24)
    mkdir -p "$(dirname "$RELEASE_KEY")"
    KIT_PASS="$pass" "$JAVA_HOME/bin/keytool" -genkeypair -keystore "$store" -storetype PKCS12 -alias "$alias" -keyalg RSA -keysize 4096 \
        -validity 10950 -dname "CN=$FILE, O=${AUTHOR:-appkit}" -storepass:env KIT_PASS -keypass:env KIT_PASS >/dev/null 2>&1
    ( umask 077; printf 'storeFile=%s\nstorePassword=%s\nkeyAlias=%s\nkeyPassword=%s\n' "$store" "$pass" "$alias" "$pass" > "$RELEASE_KEY" )
    chmod 600 "$store" "$RELEASE_KEY"
    echo "🔑 new release key: $store · settings (password) $RELEASE_KEY · ⚠️ not i any bup yet ➡ copy both 2 th password safe"
}
apk_signer() {
    local s; s=$(find "$ANDROID_HOME/build-tools" -name apksigner | sort -V | tail -1)
    PATH="$JAVA_HOME/bin:$PATH" "$s" verify --print-certs "$1" 2>/dev/null | sed -nE 's/^Signer #1 certificate DN: //p' | head -1
}
key_check() {
    [ -n "${RELEASE_KEY:-}" ] || return 0
    local who; who=$(apk_signer "$1")
    case "$who" in ""|*"Android Debug"*) echo "❌ release apk signed by '${who:-nobody}', not th release key ($RELEASE_KEY) ➡ apptool ship -k · gradle signingConfig" >&2; return 1 ;; esac
    echo "🔑 signer: $who"
}
