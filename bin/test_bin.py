"""python3 -m unittest bin/test_bin.py · tsts 4 th bin/ scripts (no android, no LLM, no network: urlopen is mocked)"""
import contextlib
import importlib.util
import io
import json
import pathlib
import re
import shutil
import subprocess
import sys
import tempfile
import unittest
from unittest import mock

BIN = pathlib.Path(__file__).resolve().parent


def load(name):
    spec = importlib.util.spec_from_file_location(name, BIN / f"{name}.py")
    m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
    return m


check_langs, tf = load("check_langs"), load("translate_full")


def fake_tree(menu, files):
    """tmp repo root w I18n.kt (Lang.menu) + content files {name: json}"""
    d = tempfile.TemporaryDirectory()
    root = pathlib.Path(d.name)
    kt = root / check_langs.I18N_KT; kt.parent.mkdir(parents=True)
    kt.write_text(f"companion object {{ val menu = listOf({', '.join(m.upper() for m in menu)}) }}")
    c = root / check_langs.CONTENT; c.mkdir(parents=True)
    for n, j in files.items():
        (c / n).write_text(json.dumps(j))
    return d, root


class ShipGuard(unittest.TestCase):
    def test_complete_langs_pass(self):
        d, root = fake_tree(["en", "de", "it"], {"i18n_it.json": {"ui": {"a": "b"}}, "full_it.json": {"v": {"1": "x"}, "fn": {}}})
        self.assertEqual(check_langs.problems(root), []); d.cleanup()

    def test_menu_lang_without_book_is_refused(self):
        # the 0.9.0 case: ur listed + UI strings there, book file missing
        d, root = fake_tree(["en", "de", "ur", "hi"], {"i18n_ur.json": {"ui": {"a": "b"}}, "i18n_hi.json": {"ui": {"a": "b"}},
                                                      "full_hi.json": {"v": {}, "fn": {}}})
        p = check_langs.problems(root)
        self.assertEqual(len(p), 2); self.assertIn("full_ur.json", p[0]); self.assertIn("full_hi.json", p[1])
        d.cleanup()

    def test_missing_ui_file_is_refused(self):
        d, root = fake_tree(["en", "bn"], {"full_bn.json": {"v": {"1": "x"}}})
        self.assertEqual(len(check_langs.problems(root)), 1); d.cleanup()

    def test_cli_exit_code(self):
        d, root = fake_tree(["en", "de", "ur"], {})
        r = subprocess.run([sys.executable, str(BIN / "check_langs.py"), str(root)], capture_output=True, text=True)
        self.assertEqual(r.returncode, 1); self.assertIn("ur", r.stdout); d.cleanup()

    def test_untranslated_ui_string_is_refused(self):
        # qa_1116 #11: a new tr() label w/o its i18n "ui" entry = stays EN i that lang
        d, root = fake_tree(["en", "it"], {"i18n_it.json": {"ui": {"Back": "Indietro"}}, "full_it.json": {"v": {"1": "x"}}})
        kt = root / check_langs.SRC / "X.kt"; kt.parent.mkdir(parents=True, exist_ok=True)
        kt.write_text('Text(tr("Zurück", "Back")); Text(tr("Später", "Later"))\n// tr("Kommentar", "Comment") = ignored\n'
                      'Text(tr("Hallo $n", "Hi $n"))')
        p = check_langs.problems(root)
        self.assertEqual(len(p), 1); self.assertIn("1 UI strings", p[0]); self.assertIn("Later", p[0]); d.cleanup()

    def test_untranslated_plan_day_is_refused(self):
        # @1116: Ludditen "Und heute?" bridges (xd) were EN i all 11 langs · ur missed 1 Andacht
        plans = [{"id": "lud", "days": [{"ad": "A0", "xd": "X0"}, {"ad": "A1"}, {}]}]
        d, root = fake_tree(["en", "it"], {"plans.json": plans, "full_it.json": {"v": {"1": "x"}},
                                           "i18n_it.json": {"ui": {}, "a": {"lud:0": "a"}}})
        p = check_langs.problems(root)
        self.assertEqual(len(p), 2)
        self.assertIn("1 Andachten", p[0]); self.assertIn("lud:1", p[0]); self.assertIn("--only a", p[0])
        self.assertIn("1 bridges", p[1]); self.assertIn("lud:0", p[1]); self.assertIn("--only x", p[1]); d.cleanup()

    def test_translate_py_yields_bridges_w_andacht_keys(self):
        tr = load("translate")
        items = list(tr.src_items({"a", "x"}))
        xs = {k for kind, k, *_ in items if kind == "x"}
        self.assertTrue(xs, "plans.json hs no xd bridges anymore?")
        self.assertEqual(xs, check_langs.day_keys(BIN.parent)["x"])

    def test_real_repo_passes(self):
        self.assertEqual(check_langs.problems(BIN.parent), [])

    def test_ship_sh_calls_guard_b4_bump(self):
        s = (BIN / "ship.sh").read_text()
        self.assertIn("bin/check_langs.py", s)
        self.assertLess(s.index("bin/check_langs.py"), s.index('sed -i -E "s/^val appVersion'))


class Reply(io.BytesIO):
    """urlopen() result: json.load() reads it"""
    def __enter__(self): return self
    def __exit__(self, *a): pass


def llm(response, evals=100):
    return Reply(json.dumps({"response": response, "eval_count": evals}).encode())


class Meter(unittest.TestCase):
    def setUp(self):
        tf.METER.reset()

    def ask(self, reply):
        with mock.patch.object(tf.urllib.request, "urlopen", side_effect=reply if isinstance(reply, Exception) else None,
                               return_value=None if isinstance(reply, Exception) else reply), \
                contextlib.redirect_stdout(io.StringIO()) as out:
            try:
                return tf.ask("m", "http://x", "it", {"a": ("Das Kapital.", "")}), out.getvalue()
            except tf.CallFailed as e:
                return e, out.getvalue()

    def test_ok_call_logs_secs_and_toks(self):
        (res, n), out = self.ask(llm(json.dumps({"k0": " Il capitale. "}), evals=42))
        self.assertEqual((res, n), ({"a": "Il capitale."}, 42))
        self.assertIn("⏱", out); self.assertIn("42 tok", out); self.assertRegex(out, r"\d+\.\d+s")
        self.assertEqual((tf.METER.calls, tf.METER.fails, tf.METER.toks, tf.METER.wasted), (1, 0, 42, 0))

    def test_bad_json_counts_its_toks_as_wasted(self):
        # model ran on + never closed th string ➡ 18 min o GPU, 0 usable ➡ must show up
        e, out = self.ask(llm('{"k0": "Il capi', evals=16000))
        self.assertIsInstance(e, tf.CallFailed); self.assertEqual(e.toks, 16000)
        self.assertIn("WASTED", out); self.assertIn("16000", out)
        self.assertEqual((tf.METER.fails, tf.METER.wasted, tf.METER.toks), (1, 16000, 0))
        self.assertIn("1/1 calls failed = 16000 tok", tf.METER.line())

    def test_timeout_counts_secs_only(self):
        e, out = self.ask(TimeoutError("timed out"))
        self.assertIsInstance(e, tf.CallFailed); self.assertEqual(e.toks, 0)
        self.assertEqual((tf.METER.fails, tf.METER.wasted), (1, 0)); self.assertIn("TimeoutError", out)

    def test_meter_line_empty_when_all_ok(self):
        tf.METER.call(1.0, 10, ok=True)
        self.assertEqual(tf.METER.line(), "")

    def test_run_shows_waste_i_progress_line(self):
        with tempfile.TemporaryDirectory() as t, mock.patch.object(tf, "OUT", pathlib.Path(t)), \
                mock.patch.object(tf, "items", return_value=[("v", "1", "Das Kapital.", "")]), \
                mock.patch.object(tf.urllib.request, "urlopen", side_effect=lambda *a, **k: llm('{"k0": "Il cap', 500)), \
                contextlib.redirect_stdout(io.StringIO()) as out:
            tf.run("it", ["v"], "m", "http://x", 0)
        s = out.getvalue()
        self.assertIn("calls failed", s); self.assertIn("wasted", s)
        self.assertEqual(tf.METER.wasted, 1000)   # batch of 1: try 1 + try 2 (no split, no single retry)


align, build = load("align"), load("build")


class AlignEn(unittest.TestCase):
    """big round #1/#4: EN reader gaps · DE section titles · ch.1 TOC as body txt"""
    def test_split_en_spreads_sentences_by_de_length(self):
        en = "One short. Two is a bit longer here. Three closes it all off nicely."
        parts = align.split_en(en, [10, 60])
        self.assertEqual(len(parts), 2); self.assertTrue(all(parts))
        self.assertEqual(" ".join(parts), en)
        self.assertEqual(parts[0], "One short.")

    def test_split_en_prefers_paragraph_breaks_and_keeps_1_sentence_each(self):
        parts = align.split_en("A first para. ¶ B second para.", [5, 5])
        self.assertEqual(parts, ["A first para.", "B second para."])
        self.assertEqual(align.split_en("Only one.", [5, 5]), ["Only one.", ""])

    # qa_1116 #12 (7 EN gaps ➡ 0)
    def test_sentence_ends_aftr_closing_quote_not_aftr_mr(self):
        s = align.sentences("He sums up: “long hours.” The report trusts “a manufacture”. Says Mr. Redgrave, fine.", align.SENT)
        self.assertEqual(s, ["He sums up:", "“long hours.”", "The report trusts “a manufacture”.", "Says Mr. Redgrave, fine."])

    def test_too_few_sentences_cut_at_semicolon_then_longest_absatz_wins(self):
        # 23,215/216: short DE heading + body ⬄ 1 EN sentence ➡ body gets it, not th heading
        self.assertEqual(align.split_en("Not so excessive yet here", [19, 121]), ["", "Not so excessive yet here"])
        # 13,107/109: 2 quotes joined by `;” and` ➡ 1 each
        self.assertEqual(align.split_en("“Co-operation of workpeople;” and on the other hand, as “a vast automaton.”", [248, 297]),
                         ["“Co-operation of workpeople;”", "and on the other hand, as “a vast automaton.”"])
        # tables: old rule (all on th 1st)
        self.assertEqual(align.split_en("A | b | c", [5, 50], first=True), ["A | b | c", ""])

    def test_pin_forces_bead_start(self):
        de = ["Kurz.", "Grotesk erscheint dies in Druckereien.", "Jedes Kind muss vorher die Schule besuchen, sagt das Gesetz."]
        en = ["Short, and also a sentence about grotesque things.", "Horribly grotesque in print works.", "Every child must attend school, says the act."]
        pinned = align.align(de, en, pins={1: "Horribly grotesque"})
        self.assertTrue(any(d and d[0] == 1 and e and e[0] == 1 for d, e in pinned), pinned)   # DE 1 starts a bead at EN 1
        self.assertFalse(any(1 in d and d[0] != 1 for d, _ in pinned), pinned)                 # nvr glued 2 DE 0

    def test_toc_dropped(self):
        body = '<h3>Chapter One</h3><p class="toc">Contents</p><p class="index">A. Elementary</p><h3>SECTION 1</h3><p>The wealth'
        out = align.drop_toc(body)
        self.assertNotIn("Elementary", out); self.assertIn("SECTION 1", out)

    def test_heads_build_de_style_titles(self):
        h = align.Heads()
        h.push(3, "Chapter One: Commodities"); h.push(3, "SECTION 3"); h.push(4, "THE FORM OF VALUE OR EXCHANGE VALUE")
        self.assertEqual(h.cur(), ["Commodities", "3. The Form of Value or Exchange Value"])   # chapter = lvl 2 (DE Kap 4/24)
        h.push(5, "A. Elementary Form"); h.push(7, "(a.) The nature [12] of this form")
        self.assertEqual(h.cur()[-1], "a. The nature of this form")
        self.assertEqual(align.title_case("DAY AND NIGHT WORK. THE RELAY SYSTEM"), "Day and Night Work. The Relay System")

    def test_en_sec_titles_innermost_label_match(self):
        V = [{"kap": "1", "sec": "1.3.A.3", "sec_title": "3. Die Äquivalentform",
              "en_head": ["3. The Form of Value", "A. Elementary", "3. The Equivalent form"]},
             {"kap": "15", "sec": "15.IV", "sec_title": "IV. Gleichzeitige Variationen",
              "en_head": ["4. Simultaneous Variations", "B. Increasing intensity"]},
             {"kap": "24", "sec": "24.1", "sec_title": "1. Das Geheimnis", "en_head": ["The Secret of Primitive Accumulation"]},
             {"kap": "2", "sec": "", "sec_title": "", "en_head": []}]
        t = build.en_sec_titles(V)
        self.assertEqual(t[("1", "1.3.A.3")], "3. The Equivalent form")
        self.assertEqual(t[("15", "15.IV")], "IV. Simultaneous Variations")
        self.assertEqual(t[("24", "24.1")], "1. The Secret of Primitive Accumulation")
        self.assertNotIn(("2", ""), t)


ROOT = BIN.parent


def vkey(v):
    return tuple(int(x) for x in v.split("."))


class Notes(unittest.TestCase):
    """notes.py ⬄ ui/Changelog.kt: release body (ship.sh) + page "What's new" both read th in-app changelog"""
    notes = load("notes")

    def test_versions_newest_first_and_semver(self):
        vs = self.notes.versions()
        self.assertGreater(len(vs), 3)
        for v in vs:
            self.assertRegex(v, r"^\d+\.\d+\.\d+$")
        self.assertEqual(vs, sorted(vs, key=vkey, reverse=True))

    def test_evy_release_has_notes(self):
        for v in self.notes.versions():
            self.assertTrue(self.notes.notes(v), f"Release(\"{v}\") parses 2 0 notes ➡ T(\"de\", \"en\") pairs broken?")

    def test_dev_version_reads_base_and_unknown_is_empty(self):
        v = self.notes.versions()[0]
        self.assertEqual(self.notes.notes(f"{v}-dev.1105.1200"), self.notes.notes(v))
        self.assertEqual(self.notes.notes("0.0.0"), [])

    def test_app_version_has_notes(self):
        ver = subprocess.run(["bash", "-c", "source bin/lib.sh; app_version"], cwd=ROOT, capture_output=True, text=True).stdout.strip()
        self.assertIn(ver.split("-")[0], self.notes.versions(), "appVersion wo Release(…) i Changelog.kt ➡ ship.sh refuses")

    def test_unicode_escapes_decode(self):
        for v in self.notes.versions():
            for n in self.notes.notes(v):
                self.assertNotRegex(n, r"\\u[0-9a-fA-F]{4}|\\n", f"{v}: raw escape left i {n!r}")

    def test_cli_html_plain(self):
        v = self.notes.versions()[0]
        r = subprocess.run([sys.executable, str(BIN / "notes.py"), v, "--html", "--plain"], capture_output=True, text=True)
        self.assertEqual(r.returncode, 0)
        items = re.findall(r"<li>(.*?)</li>", r.stdout)
        self.assertEqual(len(items), len(self.notes.notes(v)))
        for i in items:
            self.assertRegex(i, r"^[\w„“\"(&]", f"--plain left a leading emoji: {i!r}")
            self.assertNotRegex(i, r"<(?!/?\w)", "unescaped <")


@unittest.skipUnless(shutil.which("qrencode"), "page() needs qrencode")
class Page(unittest.TestCase):
    """lib.sh page() dry-run: fake apk ➡ th 3 files th app + Obtainium + agents read (no build, no upload)"""

    def render(self, chan="stable"):
        d = tempfile.TemporaryDirectory(); self.addCleanup(d.cleanup)
        out = pathlib.Path(d.name); ver = "0.10.0"; apk = f"OhMyMarx-{ver}.apk"
        (out / apk).write_bytes(b"not really an apk")
        r = subprocess.run(["bash", "-c", 'source bin/lib.sh; page "$@"', "_", str(out), ver, apk, chan],
                           cwd=ROOT, capture_output=True, text=True)
        self.assertEqual(r.returncode, 0, r.stderr)
        return out, ver, apk

    def test_version_json_is_what_update_kt_parses(self):
        out, ver, apk = self.render()
        j = json.loads((out / "version.json").read_text())
        self.assertEqual((j["version"], j["apk"]), (ver, apk))
        self.assertIsInstance(j["code"], int)
        self.assertRegex(j["sha256"], r"^[0-9a-f]{64}$")

    def test_stable_page(self):
        out, ver, apk = self.render()
        h = (out / "index.html").read_text()
        self.assertIn(f'href="{apk}"', h)                      # Obtainium's HTML source finds th apk link
        self.assertIn("mailto:omm@scagent.de", h)
        self.assertIn(f"What’s new in {ver}", h)
        self.assertGreaterEqual(h.count("<h2>"), 4)
        self.assertNotIn("$(", h)                               # unexpanded shell
        ld = json.loads(re.search(r'<script type="application/ld\+json">(.*?)</script>', h, re.S).group(1))
        self.assertEqual((ld["softwareVersion"], ld["downloadUrl"].rsplit("/", 1)[1]), (ver, apk))
        self.assertIn("<svg", h)                                # QR
        self.assertIn(f"Version: {ver}", (out / "llms.txt").read_text())

    def test_dev_page(self):
        out, ver, apk = self.render("dev")
        h = (out / "index.html").read_text()
        self.assertIn(f'href="{apk}"', h)
        self.assertIn('name="robots" content="noindex"', h)
        self.assertFalse((out / "llms.txt").exists())


if __name__ == "__main__":
    unittest.main()
