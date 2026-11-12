"""python3 -m unittest bin/test_bin.py · tsts 4 th bin/ scripts (no android, no LLM, no network: urlopen is mocked)"""
import contextlib
import importlib.util
import io
import json
import pathlib
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


if __name__ == "__main__":
    unittest.main()
