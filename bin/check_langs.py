#!/usr/bin/env python3
"""check_langs — ship guard: evy lang i th app's menu (Lang.menu i I18n.kt) must hv its translation files.

  EN (source text) + DE (Marx's original) need none · evy oth menu lang needs
    assets/content/i18n_<xx>.json (UI + quotes) AND full_<xx>.json (th book, "v" non-empty)
  else exit 1 + list ➡ bin/ship.sh refuses (0.9.0 changelog claimed ur/hi/bn book b4 full_xx.json existed)

  bin/check_langs.py [ROOT]      ROOT = repo root (dflt: th one above bin/) · tsts pass a fake tree
"""
import json
import pathlib
import re
import sys

BUILTIN = {"en", "de"}
I18N_KT = "android/app/src/main/java/de/strix/marxversion/data/I18n.kt"
CONTENT = "android/app/src/main/assets/content"


def menu(root):
    """Lang.menu codes (lowercase) fr I18n.kt"""
    m = re.search(r"val menu\s*=\s*listOf\(([^)]*)\)", (root / I18N_KT).read_text(encoding="utf-8"))
    if not m:
        raise SystemExit(f"❌ no `val menu = listOf(…)` i {I18N_KT} (check_langs.py out o sync?)")
    return [c.strip().lower() for c in m.group(1).split(",") if c.strip()]


def _json(f):
    try:
        return json.loads(f.read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return None


def problems(root):
    root = pathlib.Path(root)
    out = []
    for lang in menu(root):
        if lang in BUILTIN:
            continue
        d = root / CONTENT
        ui, full = _json(d / f"i18n_{lang}.json"), _json(d / f"full_{lang}.json")
        if not ui:
            out.append(f"{lang}: i18n_{lang}.json missing/empty")
        if not (isinstance(full, dict) and full.get("v")):
            out.append(f"{lang}: full_{lang}.json missing/empty (no book text)")
    return out


def main(argv):
    root = pathlib.Path(argv[0]) if argv else pathlib.Path(__file__).resolve().parent.parent
    bad = problems(root)
    for b in bad:
        print(f"  ❌ {b}")
    if bad:
        print("❌ Lang.menu lists langs w/o translation files ➡ translate (bin/translate.py · translate_full.py --export) "
              "o drop them fr Lang.menu, then ship")
        return 1
    print(f"✔️ langs: {len([c for c in menu(root) if c not in BUILTIN])} menu langs, all hv i18n + full_*.json")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
