#!/usr/bin/env bash
# 🧪 deploy = a dev build 4 ur own fon ➡ <web>/dev/ · -a = also straight over wireless adb · -h = all flags
#   = appkit apptool deploy (appkit/tool/apptool · D-079) · omm config + page = bin/lib.sh
#   -h, --help ➡ apptool's (y · flags · examples)
set -euo pipefail
cd "$(dirname "$0")/.."
exec appkit/tool/apptool deploy "$@"
