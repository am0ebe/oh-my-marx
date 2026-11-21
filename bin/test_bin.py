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

    def test_half_latin_word_is_refused(self):
        # #246: "تrier" i th AR Marx birthday card · zh "的App" = normal (no spaces i zh)
        d, root = fake_tree(["en", "ar", "zh"], {"i18n_ar.json": {"ui": {"a": "ماركس (*1818 في تrier)! Claude Code، MEW"}},
                                                 "full_ar.json": {"v": {"1": "x"}},
                                                 "i18n_zh.json": {"ui": {"a": "的App"}}, "full_zh.json": {"v": {"1": "x"}}})
        p = check_langs.problems(root)
        self.assertEqual(len(p), 1); self.assertIn("ar: 1 half-Latin", p[0]); self.assertIn("تrier", p[0]); d.cleanup()

    def test_quote_left_in_german_is_refused(self):
        # #248: id echoed Marx's German for 12 quotes · Dante (Italian i th DE src) = fine
        de = "Vor diesen 500 Kubikfuß Luft geht daher der Fabrikgesetzgebung der Atem aus und ist zu Ende."
        dante = "Assai bene è trascorsa d'esta moneta già la lega e'l peso, ma dimmi se tu l'hai nella tua borsa."
        verses = [{"id": "13,257", "qd": de}, {"id": "3,21", "qd": dante}]
        d, root = fake_tree(["en", "id"], {"verses.json": verses, "full_id.json": {"v": {"1": "x"}},
                                           "i18n_id.json": {"ui": {}, "q": {"13,257": de + " ", "3,21": dante}}})
        p = check_langs.problems(root)
        self.assertEqual(len(p), 1); self.assertIn("id: 1 quotes still German (e.g. 13,257)", p[0]); d.cleanup()

    def test_hand_fix_and_dup_tail(self):
        tr = load("translate")
        fx = {"*": [["Atschaft: ", ""]], "ar": [["تrier", "ترير"]]}
        self.assertEqual(tr.hand_fix("ar", "في تrier", fx), "في ترير")
        self.assertEqual(tr.hand_fix("it", "Atschaft: Marx ci mostra.", fx), "Marx ci mostra.")
        self.assertEqual(tr.hand_fix("en", "Body. What now, really?\n\nWhat now, really?", fx), "Body. What now, really?")
        self.assertEqual(tr.hand_fix("en", "Short? Short?", fx), "Short? Short?")   # < 16 chars = maybe on purpose

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


class Placeholders(unittest.TestCase):
    """qa_1120 (ar shot): "HERE BEGINS CHAPTER %s" ➡ ar "… ٪s" ➡ shown "s٪" · 26 such UI strings i 8 langs"""
    def test_check_langs_flags_lost_placeholder(self):
        d, root = fake_tree(["en", "ar"], {"i18n_ar.json": {"ui": {"Day %d": "اليوم ٪d", "Hi": "مرحبا"}},
                                           "full_ar.json": {"v": {"1,1": "x"}}})
        with d:
            bad = check_langs.problems(root)
        self.assertTrue(any("broken %s/%d" in b for b in bad), bad)

    def test_translate_normalizes_arabic_percent_and_rejects_lost_ones(self):
        tr = load("translate")
        self.assertEqual("الفصل %s", tr.fix_ph("CHAPTER %s", "الفصل ٪s"))
        self.assertEqual("%d / %d", tr.fix_ph("%d of %d", "%d / %d"))
        self.assertIsNone(tr.fix_ph("Day %d", "Tag"))
        self.assertIsNone(tr.fix_ph("(full text only)", "Capítulo %d"))


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


class ShowSamples(unittest.TestCase):
    """🐛 @1121 🦠: `--show 5 --langs it es` looked like DE + EN only ➡ evy lang gets its own flagged row"""

    def show(self, langs, n=5):
        de1, de2 = "Die Ware ist ein Ding.", "Das Geld ist Ware."
        with tempfile.TemporaryDirectory() as t, mock.patch.object(tf, "OUT", pathlib.Path(t)), \
                mock.patch.object(tf, "items", return_value=[("v", "1,1", de1, "A commodity is a thing."),
                                                             ("v", "1,2", de2, "Money is a commodity.")]), \
                contextlib.redirect_stdout(io.StringIO()) as out:
            rows = {"it": [{"kind": "v", "k": "1,1", "src": tf.sha(de1), "t": "La merce è una cosa."},
                           {"kind": "v", "k": "1,2", "src": "old", "t": "Il denaro è merce."}],
                    "es": [{"kind": "v", "k": "1,1", "src": tf.sha(de1), "t": "La mercancía es una cosa."}]}
            for lang, xs in rows.items():
                (pathlib.Path(t) / f"{lang}.jsonl").write_text("".join(json.dumps(x) + "\n" for x in xs))
            tf.show(langs, n, seed=1)
        return out.getvalue()

    def test_every_lang_has_own_row_per_sample(self):
        s = self.show(["it", "es"])
        self.assertEqual(s.count("━━"), 2)                 # same 2 samples 4 both langs, not 1 block per lang
        self.assertEqual(s.count("🇩🇪 de:"), 2)
        self.assertIn("🇮🇹 it: La merce è una cosa.", s)
        self.assertIn("🇪🇸 es: La mercancía es una cosa.", s)
        self.assertIn("🇪🇸 es: ❌ not translated", s)       # es lacks 1,2 ➡ says so, not silently skipped

    def test_stale_src_is_marked(self):
        self.assertIn("🇮🇹 it: ⚠️ stale Il denaro", self.show(["it"]))

    def test_no_translations_says_so(self):
        self.assertIn("no translations yet 4 fr", self.show(["fr"]))

    def test_cut_stops_at_a_word(self):
        self.assertEqual(tf.cut("eins zwei drei", 8), "eins …")


align, build = load("align"), load("build")


class PlanVersions(unittest.TestCase):
    """🔢 D-042: build.py versions() ➡ any cut change = new ver · old cuts ride along frozen · same cut = no bump"""
    def setUp(self):
        self.d = tempfile.TemporaryDirectory(); self.f = pathlib.Path(self.d.name) / "pv.json"
        self.b = load("build")

    def tearDown(self): self.d.cleanup()

    def plan(self, *days): return [{"id": "p", "days": [{"v": list(d)} for d in days]}]

    def test_first_build_is_v1_no_old(self):
        P = self.b.versions(self.plan(["1,1"], ["1,2"]), self.f)
        self.assertEqual(1, P[0]["ver"]); self.assertNotIn("old", P[0])

    def test_same_cut_no_bump(self):
        self.b.versions(self.plan(["1,1"], ["1,2"]), self.f)
        with contextlib.redirect_stdout(io.StringIO()):
            P = self.b.versions(self.plan(["1,1"], ["1,2"]), self.f)
        self.assertEqual(1, P[0]["ver"])

    def test_recut_bumps_and_keeps_old_frozen(self):
        self.b.versions(self.plan(["1,1"], ["1,2"]), self.f)
        with contextlib.redirect_stdout(io.StringIO()) as out:
            P = self.b.versions(self.plan(["1,1", "1,2"], ["1,3"]), self.f)
        self.assertEqual(2, P[0]["ver"])
        self.assertEqual([["1,1"], ["1,2"]], [d["v"] for d in P[0]["old"]["1"]])
        self.assertIn("v2", out.getvalue())

    def test_real_archive_matches_plans(self):
        """committed archive = th shipped plans.json (forgot 2 commit plan_versions.json ➡ fails)"""
        arch = json.loads((BIN.parent / "data/plan_versions.json").read_text(encoding="utf-8"))
        for p in json.loads((BIN.parent / "data/plans.json").read_text(encoding="utf-8")):
            self.assertEqual(p.get("ver"), len(arch[p["id"]]), p["id"])
            self.assertEqual([d["v"] for d in p["days"]], [d["v"] for d in arch[p["id"]][-1]["days"]], p["id"])


class PlanContent(unittest.TestCase):
    """📚 D-056 (R5 re-eval @1119): bibel · 30tage · montag fixes stay fixed · Andachten found by verse ids · hand ones win"""
    @classmethod
    def setUpClass(cls):
        root = BIN.parent
        cls.P = {p["id"]: p for p in json.loads((root / "data/plans.json").read_text(encoding="utf-8"))}
        cls.V = {v["id"]: v for v in json.loads((root / "android/app/src/main/assets/content/verses.json").read_text(encoding="utf-8"))}

    def test_every_plan_day_hs_andacht_or_bridge(self):
        for p in self.P.values():
            if p["id"] == "jahr": continue   # pure reading, by design (andacht.py SKIP)
            bare = [i for i, d in enumerate(p["days"]) if not d.get("ad") and not d.get("xd")]
            self.assertFalse(bare, f"{p['id']}: days w/o Andacht + w/o bridge {bare}")

    def test_evy_plan_day_hs_a_teaser_q(self):
        """❓ C7 (D-048 c · bin/tease.py): Stempeluhr 'Tomorrow: …?' = a question, nvr a cut quote"""
        for p in self.P.values():
            if p["id"] == "jahr": continue
            for i, d in enumerate(p["days"]):
                for k in ("qd", "qe"):
                    self.assertTrue(d.get(k, "").endswith("?"), f"{p['id']} day {i + 1}: {k} = {d.get(k)!r}")

    def test_bibel_evy_day_hs_a_bible_ref(self):
        """v1 = 7/10 days w no Bible at all (score.py 'bib' flag)"""
        bib = re.compile(r"Moses|prophet|Bible|Lamb of God|Christian|Revelation|Gospel|Paradise|Eden|Jehovah|original sin|Holy Ghost")
        for i, d in enumerate(self.P["bibel"]["days"]):
            self.assertRegex(" ".join(self.V[v]["en"] for v in d["v"]), bib, f"bibel day {i + 1}")

    def test_30tage_follows_marx_ladder(self):
        kaps = [self.V[d["v"][0]]["kap"] for d in self.P["30tage"]["days"]]
        self.assertLess(kaps.index("12"), kaps.index("13"))   # coop ➡ manufacture ➡ machines
        self.assertLess(kaps.index("22"), kaps.index("23"))   # surplus value ➡ capital, then its general law

    def test_montag_stays_on_th_working_day(self):
        self.assertFalse([d["v"] for d in self.P["montag"]["days"] if self.V[d["v"][0]]["kap"] in ("1", "2", "3")])

    def test_hand_andachten_beat_llm_cache(self):
        hand = [json.loads(l) for l in (BIN.parent / "data/andachten_hand.jsonl").read_text(encoding="utf-8").splitlines() if l.strip()]
        for a in hand:
            d = next((d for d in self.P[a["plan"]]["days"] if d["v"] == a["v"]), None)
            self.assertIsNotNone(d, f"hand Andacht for {a['plan']} {a['v']} matches no day (re-cut?)")
            self.assertEqual(a["de"], d.get("ad"), f"{a['plan']} {a['v']}: LLM Andacht won over th hand one")


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


class Retag(unittest.TestCase):
    """retag.py: LLM answer cleaned · chip pools at weight ≥ 2 · its NERVT = th app's (logic/Daily.kt)"""
    rt = load("retag")

    def test_clean(self):
        c = self.rt.clean
        self.assertEqual(c({"topics": [{"tag": "lohn", "weight": 3}, {"tag": "geld", "weight": 1}, {"tag": "wert", "weight": 2}]}),
                         {"lohn": 3, "geld": 1}, "max 2")
        self.assertEqual(c({"topics": [{"tag": "nope", "weight": 3}, {"tag": "lohn", "weight": 9}]}), {}, "unknown tag / bad weight dropped")
        self.assertEqual(c({"topics": [{"tag": "lohn", "weight": 1}, {"tag": "lohn", "weight": 2}]}), {"lohn": 2})
        self.assertEqual(c({}), {})

    def test_pools(self):
        rows = [{"de_sha": "a", "tw": {"ausbeutung": 2}}, {"de_sha": "b", "tw": {"kapitalist": 1}}, {"de_sha": "c", "tw": {"arbeitszeit": 3}}]
        p = self.rt.pools(rows)
        self.assertEqual((p["chef"], p["ueberstunden"], p["lohn"]), (1, 1, 0))
        self.assertEqual(self.rt.pools(rows, quoted={"c"})["chef"], 0)

    def test_nervt_matches_app(self):
        kt = (BIN.parent / "android/app/src/main/java/de/strix/marxversion/logic/Daily.kt").read_text()
        block = kt.split("val NERVT = linkedMapOf(", 1)[1].split("\n)", 1)[0]
        app = {k: re.findall(r'"(\w+)"', v) for k, v in re.findall(r'"(\w+)" to listOf\(([^)]*)\)', block)}
        self.assertEqual(app, self.rt.NERVT)
        self.assertTrue(all(t in self.rt.TAGS for ts in app.values() for t in ts))


class MatrixCleanup(unittest.TestCase):
    """matrix_e2e --cleanup: both state shapes (API run · --app runs) · own leftovers redacted · MATCH filter · no network"""
    def setUp(self):
        self.mx = load("matrix_e2e")
        self.d = tempfile.TemporaryDirectory(); self.addCleanup(self.d.cleanup)
        self.mx.STATE = pathlib.Path(self.d.name) / "matrix_e2e.json"
        u = lambda n: {"user_id": f"@{n}:scagent.de", "pw": "pw", "token": f"tok-{n}"}
        self.mx.STATE.write_text(json.dumps({"users": {"a": u("a"), "b": u("b")}, "rooms": {"kreis": "!k"}, "kreis_alias": "#omm-kreis-aaa:scagent.de"}))
        self.app = self.mx.STATE.parent / "matrix_e2e_app_1121-1427.json"
        self.app.write_text(json.dumps({"users": {"app": u("app"), "b": u("b2")}, "rooms": {"kap": "!kap8"},
                                        "aliases": [["#omm-kreis-x:scagent.de", "app"], ["#omm-kreis-y:scagent.de", "b"]], "rows": []}))
        self.calls = []

        def call(method, path, body=None, token=None, base=None):
            self.calls.append((method, path, token))
            if "/messages" in path:   # 1 own leftover + 1 o someone else's i evy room
                return 200, {"chunk": [{"event_id": "$mine", "sender": {"tok-app": "@app:scagent.de"}.get(token, "?"), "type": "m.room.message", "content": {"body": "x"}},
                                       {"event_id": "$other", "sender": "@real:scagent.de", "type": "m.room.message", "content": {"body": "y"}}]}
            return 200, {}
        self.mx.call = call

    def run_quiet(self, only=""):
        with contextlib.redirect_stdout(io.StringIO()):
            return self.mx.cleanup(only)

    def test_match_filter_keeps_other_runs(self):
        self.assertEqual(self.run_quiet("1427"), 0)
        self.assertFalse(self.app.exists())
        self.assertTrue(self.mx.STATE.exists(), "API run state untouched")
        self.assertFalse(any(t in ("tok-a", "tok-b") for _, _, t in self.calls))

    def test_aliases_by_creator_and_only_own_msgs_redacted(self):
        self.run_quiet()
        drops = {(p, t) for m, p, t in self.calls if m == "DELETE"}
        self.assertEqual(drops, {("/directory/room/%23omm-kreis-x%3Ascagent.de", "tok-app"), ("/directory/room/%23omm-kreis-y%3Ascagent.de", "tok-b2"),
                                 ("/directory/room/%23omm-kreis-aaa%3Ascagent.de", "tok-a")})
        redacts = [(p, t) for m, p, t in self.calls if "/redact/" in p]
        self.assertEqual(redacts, [("/rooms/%21kap8/redact/%24mine/" + p.rsplit("/", 1)[1], "tok-app") for p, _ in redacts])
        self.assertEqual(len(redacts), 1, "nvr redacts a real user's msg")
        self.assertEqual(sum(p == "/account/deactivate" for _, p, _ in self.calls), 4)
        self.assertEqual(self.mx.state_files(), [], "all green ➡ state files gone")

    def test_api_runs_never_overwrite_each_other(self):
        """2 API runs ➡ 2 stamped files (+ legacy + app) ➡ an older run keeps its --cleanup handle (#305)"""
        self.mx.save({"stamp": "1121-1300", "users": {}, "rooms": {}})
        self.mx.save({"stamp": "1121-1400", "users": {}, "rooms": {}})
        names = [f.name for f in self.mx.state_files()]
        self.assertEqual(names, ["matrix_e2e.json", "matrix_e2e_1121-1300.json", "matrix_e2e_1121-1400.json", "matrix_e2e_app_1121-1427.json"])


if __name__ == "__main__":
    unittest.main()
