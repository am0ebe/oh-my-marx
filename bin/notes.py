#!/usr/bin/env python3
"""notes — release notes of one version, read from the in-app changelog (ui/Changelog.kt = the single source).

  bin/notes.py 0.7.0           # markdown bullets (EN) ➡ GitHub release body (bin/ship.sh)
  bin/notes.py 0.7.0 --html    # <li> items ➡ "What's new" on the download page (bin/lib.sh)
  bin/notes.py 0.7.0 --plain   # w/o th leading emoji (page uses --html --plain)
  bin/notes.py 0.7.0-dev.1105  # dev versions ➡ notes of their base version (0.7.0)
  bin/notes.py --versions      # all versions in the changelog, newest first (page: "Older versions")
Unknown version ➡ prints nothing (exit 0).
"""
import html, pathlib, re, sys

KT = pathlib.Path(__file__).resolve().parent.parent / "android/app/src/main/java/de/strix/marxversion/ui/Changelog.kt"
PAIR = re.compile(r'T\(\s*"((?:[^"\\]|\\.)*)"\s*,\s*"((?:[^"\\]|\\.)*)"\s*\)')

def versions():
    """all versions i th changelog, newest 1st"""
    return re.findall(r'\bRelease\(\s*"([^"]+)"', KT.read_text(encoding="utf-8"))

def notes(version):
    base = version.split("-")[0]
    src = KT.read_text(encoding="utf-8")
    blocks = re.split(r'\bRelease\(', src)[1:]
    for b in blocks:
        m = re.match(r'\s*"([^"]+)"', b)
        if m and m.group(1) == base:
            return [en.encode().decode("unicode_escape").encode("latin-1").decode("utf-8") for _, en in PAIR.findall(b)]
    return []

if __name__ == "__main__":
    if len(sys.argv) < 2 or sys.argv[1] in ("-h", "--help"):
        print(__doc__); sys.exit(0)
    if sys.argv[1] == "--versions":
        print("\n".join(versions())); sys.exit(0)
    items = notes(sys.argv[1])
    if "--plain" in sys.argv:  # download page (strix @1106: "drop th icons, 2 much") ➡ cut leading emoji
        items = [re.sub(r"^[^\w„“\"(]+", "", i) for i in items]
    if "--html" in sys.argv:
        print("".join(f"<li>{html.escape(i)}</li>" for i in items))
    else:
        print("\n".join(f"- {i}" for i in items))
