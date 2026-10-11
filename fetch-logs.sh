#!/bin/bash
set -ex

SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})

# This makes strings of 26-03-24 and 26-03-23 for example.
TODAY_DATE_STR=$(date +%y-%m-%d)
YESTERDAY_DATE_STR=$(date -d "yesterday" +%y-%m-%d)

# --- RUN RSYNC ---
rsync -e "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null" \
      --info=progress2 \
      --include="akit_$TODAY_DATE_STR_*.wpilog" \
      --include="akit_$YESTERDAY_DATE_STR_*.wpilog" \
      --exclude="*" \
      -avz admin@10.4.88.2:/media/sda1/logs "SCRIPT_DIR/robot-logs"

echo "[SUCCESS] Logs for synced successfully."
