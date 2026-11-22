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
app = {"id": pkg, "url": url, "author": author, "name": name, "preferredApkIndex": 0,
       "additionalSettings": json.dumps(settings), "overrideSource": None}
print("obtainium://app/" + urllib.parse.quote(json.dumps(app), safe=""))
PY
}

# ⬆️ upload 2 evy page dir (+ SUBDIR): apk 1st (if any), page 2nd (page nvr points at a missing file) · keep newest 3 apks
#   hooks (optional, th app's): publish_dir OUTDIR SUBDIR DIR (extra files per dir) · publish_done OUTDIR SUBDIR
#   publish OUTDIR [dev]
publish() {
    local out=$1 sub=${2:-} d
    for d in $DIRS; do
        d="$d${sub:+/$sub}"
        ssh vps "mkdir -p $d"
        ls "$out"/*.apk >/dev/null 2>&1 && rsync -t --chmod=F644 "$out"/*.apk "vps:$d/"
        rsync -t --chmod=F644 "$out/index.html" "$out/version.json" "${FAVICON:-bin/favicon.png}" "vps:$d/"
        declare -F publish_dir >/dev/null && publish_dir "$out" "$sub" "$d"
        ssh vps "cd $d && ls -t *.apk | tail -n +4 | xargs -r rm --"
    done
    if declare -F publish_done >/dev/null; then publish_done "$out" "$sub"; fi
}
