#!/usr/bin/env python3
"""check_langs — ship guard: evy lang i th app's menu (Lang.menu i I18n.kt) must hv its translation files.

  EN (source text) + DE (Marx's original) need none · evy oth menu lang needs
    assets/content/i18n_<xx>.json (UI + quotes) AND full_<xx>.json (th book, "v" non-empty)
  else exit 1 + list ➡ bin/ship.sh refuses (0.9.0 changelog claimed ur/hi/bn book b4 full_xx.json existed)
  + evy tr("de", "en") / T(…) UI string i th Kotlin src needs its "ui" entry i evy i18n_<xx>.json (qa_1116 #11: 51
    new btn labels stayed EN i all 11 langs ➡ "not all button txts switch") ➡ fix = bin/translate.py --only ui
  + evy plan day's Andacht ("a") + "Und heute?" bridge ("x", Ludditen) i plans.json needs its entry too
    (@1116: bridges were EN i all 11 langs · ur missed 1 Andacht) ➡ bin/translate.py --only a x

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


SRC = "android/app/src/main/java"
# same extraction as translate.py ui_strings() (key = EN string, unescaped · templates w $ can't be looked up ➡ skipped)
PAIR = re.compile(r'\b(?:T|tr)\(\s*"((?:[^"\\]|\\.)*)"\s*,\s*"((?:[^"\\]|\\.)*)"\s*,?\s*\)', re.S)
COMMENT = re.compile(r'^\s*(//|\*|/\*\*).*$', re.M)


def ui_keys(root):
    keys = set()
    for f in sorted((root / SRC).rglob("*.kt")):
        for m in PAIR.finditer(COMMENT.sub("", f.read_text(encoding="utf-8"))):
            en = bytes(m.group(2), "utf-8").decode("unicode_escape").encode("latin-1").decode("utf-8")
            if "$" not in en:
                keys.add(en)
    return keys


def _json(f):
    try:
        return json.loads(f.read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return None


def day_keys(root):
    """{"a": {"plan:dayIdx", …}, "x": {…}} fr plans.json (same keys as translate.py · no plans.json = none)"""
    out = {"a": set(), "x": set()}
    for p in _json(root / CONTENT / "plans.json") or []:
        for i, d in enumerate(p.get("days", [])):
            for kind, field in (("a", "ad"), ("x", "xd")):
                if d.get(field):
                    out[kind].add(f"{p['id']}:{i}")
    return out


def problems(root):
    root = pathlib.Path(root)
    out = []
    keys, days = ui_keys(root), day_keys(root)
    for lang in menu(root):
        if lang in BUILTIN:
            continue
        d = root / CONTENT
        ui, full = _json(d / f"i18n_{lang}.json"), _json(d / f"full_{lang}.json")
        if not ui:
            out.append(f"{lang}: i18n_{lang}.json missing/empty")
        else:
            miss = sorted(keys - set(ui.get("ui", {})))
            if miss:
                out.append(f"{lang}: {len(miss)} UI strings untranslated (e.g. {', '.join(repr(m[:30]) for m in miss[:3])}) "
                           "➡ bin/translate.py --only ui")
            for kind, name in (("a", "Andachten"), ("x", "bridges")):
                miss = sorted(days[kind] - set(ui.get(kind, {})))
                if miss:
                    out.append(f"{lang}: {len(miss)} {name} untranslated (e.g. {', '.join(miss[:3])}) "
                               f"➡ bin/translate.py --only {kind}")
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
