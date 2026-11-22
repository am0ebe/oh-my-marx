#!/usr/bin/env bash
# 📦 ship = a release 4 friends: APK ➡ scagent.de/oh-my-marx/ (download page + version.json) + GitHub release · -h = all args
#   = appkit apptool ship (appkit/tool/apptool · D-079) · omm config + page = bin/lib.sh
#   -h, --help ➡ apptool's (y · args · examples)
set -euo pipefail
cd "$(dirname "$0")/.."
exec appkit/tool/apptool ship "$@"
