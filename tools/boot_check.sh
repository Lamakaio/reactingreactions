#!/usr/bin/env bash
# Boots the dev server and stops it as soon as it reports "Done" (or fails), instead of waiting for a timeout.
# Usage: tools/boot_check.sh   (needs JAVA_HOME on Java 21; prints the Done line or the errors)
cd "$(dirname "$0")/.." || exit 1
LOG=$(mktemp)
./gradlew runServer -q > "$LOG" 2>&1 &
PID=$!
for _ in $(seq 1 120); do
    if grep -q "Done (\|Failed to initialize\|Encountered an unexpected exception" "$LOG"; then
        break
    fi
    sleep 1
done
grep -h "Done (" "$LOG" | cut -c1-160
grep -h "Exception\|Failed to initialize\|Couldn't parse" "$LOG" | grep -v "DEBUG\|mixin/" | cut -c1-200 | head -5
pkill -P $PID 2>/dev/null; kill $PID 2>/dev/null
pkill -f "net.neoforged.devlaunch\|ServerMain\|runServer" 2>/dev/null
rm -f "$LOG"
