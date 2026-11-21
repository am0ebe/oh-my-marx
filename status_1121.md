# omm status @1121 — brief 4 🦠 (via M8) · no build · DEV = `0.13.0-dev.1121.0014`

1. 🔊 **Voice: yes, but only th fon's own Android TTS**: 1 plain voice reads th text i its lang (any o 13 if th fon hs that voice installed, else a toast "voice missing ➡ install") · no audio files, nothing deployed on a server · 3 "holy" voices (God · Gottvater Marx · Marx) exist i code, picker hidden (D-012 = 1 sober voice) · ❌ missing 4 v1: good natural voices per lang + how 2 ship them (inside th APK · download · server) ➡ audio crew
2. 🌍 **Translated locally, 11 langs** (it es pt fr ru zh ar hi bn ur id) · DE = Marx original · EN = Moore/Aveling · = **whole book** (1948 paragraphs + footnotes) + all quotes + Andachten + "Und heute?" bridges + plan texts + chapter titles + UI · M = `qwen3:30b-a3b-instruct-2507` (ollama, tow GPU) · fixes by hand i `data/i18n/hand_fix.json`
   - 🦠 ck: `bin/translate_full.py --show 5 --langs it es` (DE | EN | xx side by side) · `--stats` (coverage + warnings) · `bin/check_langs.py` (ship guard) · o switch lang i th app
   - 👤 native needed: zh · ar · hi · bn · ur (u can't judge them) · ideally 1 reader per lang 4 tone
3. 💬 **Matrix built**: Synapse on VPS `scagent.de` (live ✔️ answers now · federation off · signup only w token) · 31 chapter rooms · app = comments per book page + Lesekreise (private rooms by code, polled ~hourly, no push) · `MatrixTest` 13 vs a fake server · ❌ **missing: `bin/matrix_e2e.py` was nvr written** (todo names it, file doesn't exist) · nothing ever tested vs live · no E2E encryption
   - **standalone** (Element, ~5 min, `server/matrix/README.md` § friends): `ssh -t vps docker exec -it omm-matrix register_new_matrix_user -c /data/homeserver.yaml -u strix -a http://localhost:8008` ➡ app.element.io ➡ homeserver `scagent.de` ➡ log in ➡ join `#omm-kap-8:scagent.de` ➡ post 1 line
   - **in-app** (DEV): ⚙️ Mitlesen on ➡ book ch 8 ➡ 💬 ➡ name ➡ post ➡ shows i Element? ➡ Kreis tab ➡ create ➡ join it fr Element (`#omm-kreis-<code>:scagent.de`) ➡ write both ways
   - ok 2 write + run `matrix_e2e.py` vs live? (mks real test accounts + rooms on Synapse) ❓
4. 👀 **ck now on DEV** (r1): [qa_1121.md § r2](qa_1121.md) (4 checks ~3 min) · shots `android/app/build/shots/egg_arch_{de,ru,ar}.png` · Decide: GPU night run 4 translate re-ask ❓ · widget egg on Black Friday Fr 11/27

## r2 · 🦠's answers (via M8) ➡ done @1121 13:30

### ① 🐛 `--show` fixed (96db6e6)
- was: 1 block per lang · th translation = an unflagged `➡ it:` line under 2×600 chars DE/EN ➡ looked like DE + EN only
- now: th **same** samples 4 all langs, evy lang = own flagged row (`🇩🇪 de` · `🇬🇧 en` · `🇮🇹 it` · `🇪🇸 es`) · `❌ not translated` · `⚠️ stale` · cut at a word
- cmd: `cd ~/gopro/go/pr/omm && bin/translate_full.py --show 5 --langs it es` · tsts `ShowSamples` 4 · 50 py green

### ② 💬 `bin/matrix_e2e.py` vs live Synapse = 18/18 ✔️
| # | step | ok |
|---|---|---|
| 1 | server answers `/versions` | ✔️ |
| 2 | signup wo token refused (401) | ✔️ |
| 3 | signup w wrong token refused (401) | ✔️ |
| 4-5 | register `@e2e-1121-1327-a` + `-b` w th app's token | ✔️ |
| 6 | display name set ("e2e Rosa") | ✔️ |
| 7 | A joins `#omm-kap-8` | ✔️ |
| 8 | A comments w MEW page 49 | ✔️ |
| 9 | B reads it (txt + page + name) | ✔️ |
| 10 | A deletes it ➡ gone 4 B | ✔️ |
| 11 | A creates Lesekreis `#omm-kreis-pqv6wu` (code PQV6WU) | ✔️ |
| 12 | B joins by code | ✔️ |
| 13 | members = both, w names | ✔️ |
| 14 | B sees Kreis name | ✔️ |
| 15-16 | chat A ➡ B · B ➡ A | ✔️ |
| 17 | B sends Amen ➡ A sees it | ✔️ |
| 18 | unknown Kreis code = soft 404 | ✔️ |

**👀 see th tst msgs urself i Element**
1. app.element.io ➡ Sign in ➡ Edit homeserver ➡ `scagent.de`
2. log in as th tst user: `bin/matrix_e2e.py --login` prints user + pw (o ur own account, `server/matrix/README.md` § friends)
3. Join room ➡ `#omm-kreis-pqv6wu:scagent.de` ➡ 2 chat lines (Rosa ⬄ Juno) + th Amen (Element shows it as a hidden/unknown event)
4. `#omm-kap-8:scagent.de` ➡ 1 "message deleted" by e2e-a (= step 10)
5. done ➡ `bin/matrix_e2e.py --cleanup` (leave · drop alias · deactivate both) · state = `priv/matrix_e2e.json` (git-ignored)

### ③ 🌙 GPU night run scheduled
- `at` job **62** · **Sun 11/22 03:00** · `bin/gpu_night.sh bin/translate.py --only a` = 3 src-fixed Andachten × 11 langs
  - waits (5-min checks) while non-ollama jobs hold the VRAM (audio) · still busy at 08:00 ➡ gives up, logs it · then `check_langs`
- see it's scheduled: `atq` (job 62) · `at -c 62 | tail -2` · result: `data/translate_night_1122.log` + `data/i18n/*.jsonl` + assets `i18n_*.json` (uncommitted til a crew sesh cks + deploys)
- ⚠️ **tags retag NOT included**: no script exists (todo 🔵 "tags over-applied" = new score pass + build + chip logic) ➡ separate item, after QA ❓
- ⚠️ 63 quotes/lang r "stale" only cuz th EN reference changed (DE same) ➡ left out: re-ask could undo th #248 hand fixes
