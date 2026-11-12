#!/usr/bin/env bash
# Concurrency test: many students try to take the seats of one small course at the same moment.
# The system must never enrol more students than there are seats.
#
# Usage: scripts/concurrency-test.sh [base_url] [students=30] [seats=10]
# Needs the app running (mvn jetty:run -Pdemo) and the seeded admin account.

set -uo pipefail

BASE="${1:-http://localhost:8080}"; BASE="${BASE%/}"
STUDENTS="${2:-30}"
SEATS="${3:-10}"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
STAMP="$(date +%s)"
CODE="CNC${STAMP: -6}"

token() { curl -s -c "$1" -b "$1" "$BASE$2" | grep -o 'name="_csrf" value="[^"]*"' | head -1 | sed 's/.*value="//; s/"$//'; }
post()  { local jar="$1" path="$2"; shift 2; curl -s -L -c "$jar" -b "$jar" "$@" "$BASE$path"; }

echo "Concurrency test: $STUDENTS students competing for $SEATS seats at $BASE"

# 1. Admin creates the course
ADMIN="$WORK/admin.jar"
T="$(token "$ADMIN" /login)"
post "$ADMIN" /login -d "_csrf=$T" -d "email=admin@college.edu" -d "password=admin123" >/dev/null
T="$(token "$ADMIN" /admin/course-form)"
post "$ADMIN" /admin/course-form -d "_csrf=$T" -d "id=0" -d "code=$CODE" -d "title=Concurrency Test Course" \
     -d "instructor=Prof Race" -d "credits=2" -d "seats=$SEATS" -d "description=Seat race" >/dev/null
CID="$(curl -s -c "$ADMIN" -b "$ADMIN" "$BASE/admin/courses" | grep -A15 ">$CODE<" | grep -o 'courseId=[0-9]*' | head -1 | grep -o '[0-9]*')"
[[ -n "$CID" ]] || { echo "FAIL: could not create the test course"; exit 1; }

# 2. Register and log in all students (in parallel, to save time)
for i in $(seq 1 "$STUDENTS"); do
  (
    jar="$WORK/s$i.jar"; email="race${STAMP}_$i@example.com"
    t="$(token "$jar" /register)"
    post "$jar" /register -d "_csrf=$t" -d "name=Racer $i" --data-urlencode "email=$email" \
         -d "password=secret12" -d "confirm=secret12" >/dev/null
    t="$(token "$jar" /login)"
    post "$jar" /login -d "_csrf=$t" --data-urlencode "email=$email" -d "password=secret12" >/dev/null
    token "$jar" /student/courses > "$WORK/tok$i"
  ) &
done
wait

# 3. Everyone presses "Enroll" at the same moment
START_NS="$(date +%s%N)"
for i in $(seq 1 "$STUDENTS"); do
  (
    t="$(cat "$WORK/tok$i")"
    post "$WORK/s$i.jar" /student/enroll -d "_csrf=$t" -d "courseId=$CID" -d "q=" > "$WORK/res$i.html"
  ) &
done
wait
ELAPSED_MS=$(( ($(date +%s%N) - START_NS) / 1000000 ))

# 4. Count the outcomes
OK=0; FULL=0; OTHER=0
for i in $(seq 1 "$STUDENTS"); do
  if   grep -q "You are now enrolled" "$WORK/res$i.html"; then OK=$((OK + 1))
  elif grep -q "course is full"       "$WORK/res$i.html"; then FULL=$((FULL + 1))
  else OTHER=$((OTHER + 1)); fi
done

# 5. Ask the admin page how many students the database really holds
ROW_COUNT="$(curl -s -c "$ADMIN" -b "$ADMIN" "$BASE/admin/enrollments?courseId=$CID" | grep -c 'Racer ')"

echo "  students competing        : $STUDENTS"
echo "  seats available           : $SEATS"
echo "  enrolled (success message): $OK"
echo "  rejected as full          : $FULL"
echo "  other outcome             : $OTHER"
echo "  rows in database (admin)  : $ROW_COUNT"
echo "  all requests finished in  : ${ELAPSED_MS} ms"

# 6. Clean up the test course (cascades to its enrolments)
T="$(token "$ADMIN" /admin/courses)"
post "$ADMIN" /admin/course-delete -d "_csrf=$T" -d "id=$CID" >/dev/null

if [[ "$OK" -eq "$SEATS" && "$ROW_COUNT" -eq "$SEATS" && "$FULL" -eq $((STUDENTS - SEATS)) && "$OTHER" -eq 0 ]]; then
  echo "RESULT: PASS - exactly $SEATS students enrolled, no overbooking"
else
  echo "RESULT: FAIL"
  exit 1
fi
