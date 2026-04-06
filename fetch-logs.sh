#!/bin/bash
set -ex

SCRIPT_DIR=$(dirname ${BASH_SOURCE[0]})

:: --- GET CURRENT DATE ---
:: This extracts YYYY, MM, and DD regardless of local format
TODAY_DATE_STR=$(date +%y-%m-%d)
YESTERDAY_DATE_STR=$(date -d "yesterday" +%y-%m-%d)

:: --- RUN RSYNC ---
rsync -e "ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null" \
      --info=progress2 \
      --include="akit_$TODAY_DATE_STR_*.wpilog" \
      --include="akit_$YESTERDAY_DATE_STR_*.wpilog" \
      --exclude="*" \
      -avz admin@10.4.88.2:/media/sda1/logs "SCRIPT_DIR/robot-logs"

echo [SUCCESS] Logs for %LOG_DATE% synced successfully.
