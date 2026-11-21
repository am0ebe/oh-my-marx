#!/usr/bin/env bash
# font_subset — shrink the bundled Literata fonts (D-058) to what the app really uses
#
# why: the two variable Literata TTFs added 1.86 MB to the APK. The app uses only 5 fixed styles
#      (ui/ds/Type.kt BookSerif) and only Latin, Greek and Cyrillic (zh · ar · hi · bn · ur fall back
#      to the system font glyph by glyph anyway).
# what: takes the ORIGINAL variable files from git (commit e813452), cuts 5 static instances
#       (regular · medium · bold at opsz 12, italic · bold italic at opsz 18), keeps every OpenType
#       feature, drops all other glyphs, writes res/font/literata_*.ttf. fonttools runs via uvx.
#
# examples:
#   bin/font_subset.sh            # rewrite res/font/literata_*.ttf from the originals
#   bin/font_subset.sh -c, --check    # only print the sizes, write nothing
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then sed -n '2,13p' "$0" | sed 's/^# \{0,1\}//'; exit 0; fi
case "${1:-}" in -c|--check) set -- --check ;; esac   # -c = short o --check (vibe++ D-030)

SRC_REV=e813452
FONT_DIR=android/app/src/main/res/font
# Latin (incl. extended + Vietnamese) · combining marks · Greek · Cyrillic · punctuation · currency · letterlike · arrows · ligatures
UNICODES="U+0000-024F,U+0259,U+02B0-02FF,U+0300-036F,U+0370-03FF,U+0400-04FF,U+1E00-1EFF,U+2000-206F,U+20A0-20CF,U+2100-214F,U+2190-21FF,U+2212,U+FB00-FB06"
ft() { uvx --quiet --from fonttools fonttools "$@"; }
tmp=$(mktemp -d); trap 'rm -rf "$tmp"' EXIT
git show "$SRC_REV:$FONT_DIR/literata.ttf" > "$tmp/roman.ttf"
git show "$SRC_REV:$FONT_DIR/literata_italic.ttf" > "$tmp/italic.ttf"

[[ "${1:-}" == "--check" ]] || rm -f "$FONT_DIR/literata.ttf" "$FONT_DIR/literata_italic.ttf"   # old variable files (originals stay i git)
total=0
# name · source · weight · optical size (same values Type.kt used on the variable font)
for spec in "regular roman 400 12" "medium roman 500 12" "bold roman 700 12" "italic italic 400 18" "bold_italic italic 700 18"; do
  read -r name src w opsz <<< "$spec"
  ft varLib.instancer "$tmp/$src.ttf" "wght=$w" "opsz=$opsz" -q -o "$tmp/inst_$name.ttf"
  ft subset "$tmp/inst_$name.ttf" --unicodes="$UNICODES" --layout-features='*' --notdef-outline --output-file="$tmp/sub_$name.ttf"
  b=$(stat -c %s "$tmp/sub_$name.ttf"); total=$((total + b))
  echo "literata_$name.ttf: $((b / 1024)) KB"
  [[ "${1:-}" == "--check" ]] || cp "$tmp/sub_$name.ttf" "$FONT_DIR/literata_$name.ttf"
done
echo "total $((total / 1024)) KB (was 1813 KB as 2 variable files)"
