#!/usr/bin/env bash
# gpu_night — wait til tow's GPU is free, then run 1 omm LLM job + check_langs, all logged (scheduled via `at`)
#
# why: the GPU is shared (audio crew clones voices on it). qwen3 30b needs ~20 GB VRAM; if another job holds it,
#      ollama silently falls back to CPU = hours instead of minutes. So: wait, don't fight.
# what: every 5 min checks VRAM held by NON-ollama processes · free enough (GPU_NEED_MB, dflt 20000) ➡ runs the
#       command · still busy at GPU_UNTIL (dflt 08:00) ➡ gives up, logs why · then bin/check_langs.py
#       nothing is committed: the result waits in the work tree for the next crew session (check ➡ commit ➡ deploy)
#
# examples:
#   bin/gpu_night.sh bin/translate.py --only a                       # run now (waits if busy)
#   echo 'bin/gpu_night.sh bin/translate.py --only a' | at 03:00     # tonight · see it: atq · at -c <nr> | tail -3
#   GPU_UNTIL=12:00 bin/gpu_night.sh bin/translate.py --only ui      # allow waiting til noon
set -uo pipefail
cd "$(dirname "$0")/.."

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" || $# -eq 0 ]]; then sed -n '2,14p' "$0" | sed 's/^# \{0,1\}//'; exit 0; fi

need=${GPU_NEED_MB:-20000}
deadline=$(date -d "${GPU_UNTIL:-08:00}" +%s)
log() { echo "$(date '+%F %T') $*"; }
total_mb() { nvidia-smi --query-gpu=memory.total --format=csv,noheader,nounits | head -1; }
others_mb() {  # VRAM o evy compute process th isn't ollama (ollama evicts its own idle models itself)
  nvidia-smi --query-compute-apps=process_name,used_memory --format=csv,noheader,nounits \
    | awk -F', *' '$1 !~ /ollama|llama-server/ {s += $2} END {print s + 0}'
}

log "start · job: $* · need ${need} MiB free o non-ollama VRAM · give up at ${GPU_UNTIL:-08:00}"
while :; do
  free=$(( $(total_mb) - $(others_mb) ))
  (( free >= need )) && break
  if (( $(date +%s) >= deadline )); then
    log "❌ GPU still busy (${free} MiB free) ➡ gave up, nothing ran"
    nvidia-smi --query-compute-apps=pid,process_name,used_memory --format=csv
    exit 2
  fi
  log "⏳ GPU busy (${free} MiB free < ${need}) ➡ retry in 5 min"
  sleep 300
done

log "▶ GPU free (${free} MiB) ➡ running"
t0=$(date +%s)
# *.py via python3: a lost +x bit (git mode 644) killed th 1122 translate run w exit 126 i 0 min
if [[ "$1" == *.py ]]; then python3 "$@"; else "$@"; fi; rc=$?
log "$( ((rc == 0)) && echo ✔️ || echo ❌ ) job exit ${rc} after $(( ($(date +%s) - t0) / 60 )) min"
python3 bin/check_langs.py; ck=$?
if ((rc)); then next="❌ job failed ➡ nothing new 2 commit · fix + re-run"; else next="next: git diff --stat data/i18n android/app/src/main/assets ➡ commit + deploy"; fi
log "$( ((ck == 0)) && echo ✔️ check_langs green || echo ❌ check_langs failed ) · ${next}"
exit $(( rc || ck ))
