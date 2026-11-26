# todo appkit

## 🏔️

## ⏳

## 👀

## 🔵
- 🤖 apps' subtree src = th Forgejo url (README § 🔄) · local path still works on tow ➡ only matters on a 2nd box
- ⏳ later items (README § 📋): update-dialog UI skin slot · 💬 chat `core/matrix` (sketch) · `:testrig` · i18n kit · release-notes fmt
- 🤖 moji 🧰 ➡ `pr/kb/bin/tags.py` (kb) · alias `2k`? (`ta 2k` 1st)

## ✔️
- ✔️ @1122 #335 (omm crew) · **apps on apptool 0.2.1**: cali ✔️ · omm subtree pull (omm c8d8cf7) ➡ `test_apptool.sh` 118 ✔️ + omm `bin/deploy.sh -n` exit 0 · Forgejo `am0/appkit` = origin, main pushed (ls-remote = e8836e8)
- ✔️ @1122 #335 (cali crew) · **apptool hs a tst + nvr dies silently** ([adr D-003](adr.md#d-003)): `tool/test_apptool.sh` (fake gradlew · local folders · 8 s) · `ERR` trap ➡ `🐛 apptool died i <fn>() line N` · apptool 0.2.1 · 3 lessons ➡ `lessons/lessons.md` § 📥
    - [x] 🤖 `tool/test_apptool.sh` green (118) · case "a hook th dies says where" red wo th trap · omm read-only: `bash -n` + its `bin/lib.sh` hooks read 4 cmds th end ≠ 0
- ✔️ @1122 #328 · cali adopted: in-app updater + shared ship / deploy (apptool 0.2.0 = cali's extras, dflt off) ➡ cali adr D-048
- ✔️ @1122 #329 lift fr `pr/omm/appkit/` ➡ this repo (`git init`, history kept via `subtree split`) · omm vendors it (`subtree add`), 149 unit + lint + py green · D-002 (= omm D-079)
- ✔️ @1122 #327 4 shelves (D-001) laid out: 🔧 `tool/` apptool + app-version · 📄 `templates/` page + README · 🪞 `lessons/` (⬅ omm app_lessons + cali lessons + fon_debug · `pr/cali/app_kit.md` didn't exist) · 🧩 `core/selfupdate` · in / later / no list ➡ README § 📋
