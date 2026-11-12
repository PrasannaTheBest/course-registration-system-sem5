#!/usr/bin/env bash
# Simple latency measurement: sequential requests, reports average / median / 95th percentile / max.
#
# Usage: scripts/perf-test.sh [base_url] [requests=200]
# Needs the app running and the seeded admin account.

set -uo pipefail

BASE="${1:-http://localhost:8080}"; BASE="${BASE%/}"
N="${2:-200}"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
JAR="$WORK/jar"

token() { curl -s -c "$1" -b "$1" "$BASE$2" | grep -o 'name="_csrf" value="[^"]*"' | head -1 | sed 's/.*value="//; s/"$//'; }
T="$(token "$JAR" /login)"
curl -s -L -c "$JAR" -b "$JAR" -d "_csrf=$T" -d "email=admin@college.edu" -d "password=admin123" "$BASE/login" >/dev/null

measure() { # label path   (authenticated, like a real user)
  local label="$1" path="$2" f="$WORK/t.txt"
  : > "$f"
  for _ in $(seq 1 "$N"); do
    curl -s -o /dev/null -w '%{time_total}\n' -b "$JAR" "$BASE$path" >> "$f"
  done
  sort -n "$f" | awk -v label="$label" -v path="$path" -v n="$N" '
    { v[NR] = $1 * 1000; sum += v[NR] }
    END {
      printf "  %-28s %-22s n=%d  avg=%.1f ms  median=%.1f ms  p95=%.1f ms  max=%.1f ms\n",
             label, path, n, sum / NR, v[int((NR + 1) / 2)], v[int(NR * 0.95 + 0.5)], v[NR]
    }'
}

echo "Latency test: $N sequential requests per page against $BASE"
measure "Course catalogue (admin)" /admin/courses
measure "Course search"            "/admin/courses"
measure "Login page (public)"      /login
