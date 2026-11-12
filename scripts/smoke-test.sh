#!/usr/bin/env bash
# End-to-end smoke test: drives the running web app over HTTP with curl.
#
# Usage:
#   mvn jetty:run -Pdemo            # terminal 1 (starts the app on an empty H2 database)
#   scripts/smoke-test.sh           # terminal 2 (defaults to http://localhost:8080)
#   scripts/smoke-test.sh http://localhost:8080/course-registration   # Tomcat
#
# Needs the seeded admin account (admin@college.edu / admin123).

set -uo pipefail

BASE="${1:-http://localhost:8080}"
BASE="${BASE%/}"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
STUDENT="$WORK/student.jar"
STUDENT2="$WORK/student2.jar"
ADMIN="$WORK/admin.jar"

STAMP="$(date +%s)"
EMAIL1="smoke1.${STAMP}@example.com"
EMAIL2="smoke2.${STAMP}@example.com"
CODE="SMK${STAMP: -6}"
NAME1="Zoë Ñandú"

PASS=0
FAIL=0
ok()  { echo "  PASS  $1"; PASS=$((PASS + 1)); }
bad() { echo "  FAIL  $1"; FAIL=$((FAIL + 1)); }
contains()     { if grep -qF -- "$3" <<<"$2"; then ok "$1"; else bad "$1  (expected to find: $3)"; fi; }
not_contains() { if grep -qF -- "$3" <<<"$2"; then bad "$1  (should not contain: $3)"; else ok "$1"; fi; }
equals()       { if [[ "$2" == "$3" ]]; then ok "$1"; else bad "$1  (got '$2', expected '$3')"; fi; }

# token JAR PATH            -> CSRF token found on that page (also stores the session cookie)
token() { curl -s -c "$1" -b "$1" "$BASE$2" | grep -o 'name="_csrf" value="[^"]*"' | head -1 | sed 's/.*value="//; s/"$//'; }
# get JAR PATH              -> page body
get()   { curl -s -L -c "$1" -b "$1" "$BASE$2"; }
# post JAR PATH CURL_ARGS.. -> body after following redirects
post()  { local jar="$1" path="$2"; shift 2; curl -s -L -c "$jar" -b "$jar" "$@" "$BASE$path"; }
# code JAR PATH [ARGS..]    -> HTTP status of a single request (redirects NOT followed)
code()  { local jar="$1" path="$2"; shift 2; curl -s -o /dev/null -w '%{http_code}' -c "$jar" -b "$jar" "$@" "$BASE$path"; }

register() { # JAR EMAIL NAME PASSWORD CONFIRM
  local t; t="$(token "$1" /register)"
  post "$1" /register -d "_csrf=$t" --data-urlencode "name=$3" --data-urlencode "email=$2" \
       --data-urlencode "password=$4" --data-urlencode "confirm=$5"
}
login() { # JAR EMAIL PASSWORD
  local t; t="$(token "$1" /login)"
  post "$1" /login -d "_csrf=$t" --data-urlencode "email=$2" --data-urlencode "password=$3"
}

echo "Smoke testing $BASE"

echo "== Public pages"
contains "home page renders"      "$(get "$WORK/anon.jar" /)"          "Register for your courses online"
equals   "login page returns 200" "$(code "$WORK/anon.jar" /login)"    200
equals   "register page returns 200" "$(code "$WORK/anon.jar" /register)" 200
equals   "stylesheet is served"   "$(code "$WORK/anon.jar" /css/style.css)" 200
equals   "unknown URL returns 404" "$(code "$WORK/anon.jar" /no-such-page)" 404

echo "== Access control and CSRF"
equals "anonymous /student/courses redirects" "$(code "$WORK/anon.jar" /student/courses)" 302
equals "anonymous /admin/courses redirects"   "$(code "$WORK/anon.jar" /admin/courses)"   302
equals "POST without CSRF token is rejected"  "$(code "$WORK/anon.jar" /login -X POST -d 'email=a@b.co&password=x')" 302
contains "rejected POST explains why" "$(post "$WORK/anon.jar" /login -d 'email=a@b.co&password=x')" "session expired"

echo "== Registration"
contains "student registers (UTF-8 name)" "$(register "$STUDENT" "$EMAIL1" "$NAME1" secret12 secret12)" "Account created"
contains "duplicate email is rejected"    "$(register "$STUDENT" "$EMAIL1" "Someone" secret12 secret12)" "already exists"
contains "short password is rejected"     "$(register "$STUDENT2" "x$EMAIL2" "Short" abc abc)"           "at least 6 characters"
contains "password mismatch is rejected"  "$(register "$STUDENT2" "y$EMAIL2" "Mismatch" secret12 secret13)" "do not match"
contains "bad email is rejected"          "$(register "$STUDENT2" "not-an-email" "Bad" secret12 secret12)"  "valid email"

echo "== Login"
contains "wrong password is rejected" "$(login "$STUDENT" "$EMAIL1" wrongpass)" "Invalid email or password"
contains "student logs in"            "$(login "$STUDENT" "$EMAIL1" secret12)" "Available courses"

echo "== Student: browse, search, enrol, drop"
PAGE="$(get "$STUDENT" /student/courses)"
contains "seeded courses are listed" "$PAGE" "Introduction to Programming"
contains "search finds a course"     "$(get "$STUDENT" '/student/courses?q=database')" "Database Management Systems"
not_contains "search filters others" "$(get "$STUDENT" '/student/courses?q=database')" "Operating Systems"
contains "empty search says so"      "$(get "$STUDENT" '/student/courses?q=zzzzqqq')" "No courses found"

CID="$(grep -o 'name="courseId" value="[0-9]*"' <<<"$PAGE" | head -1 | grep -o '[0-9]*')"
T="$(token "$STUDENT" /student/courses)"
contains "enrol succeeds"   "$(post "$STUDENT" /student/enroll -d "_csrf=$T" -d "courseId=$CID" -d "q=")" "You are now enrolled"
T="$(token "$STUDENT" /student/courses)"
contains "double enrol is blocked" "$(post "$STUDENT" /student/enroll -d "_csrf=$T" -d "courseId=$CID" -d "q=")" "already enrolled"
MINE="$(get "$STUDENT" /student/my-courses)"
contains "My Courses lists it"  "$MINE" "Drop"
contains "credits are summed"   "$MINE" "credits registered"
T="$(token "$STUDENT" /student/my-courses)"
contains "drop succeeds" "$(post "$STUDENT" /student/drop -d "_csrf=$T" -d "courseId=$CID")" "Course dropped"
contains "empty My Courses page" "$(get "$STUDENT" /student/my-courses)" "not registered for any course"

echo "== Admin: course management"
equals   "student cannot open admin pages" "$(code "$STUDENT" /admin/courses)" 403
contains "admin logs in" "$(login "$ADMIN" admin@college.edu admin123)" "Manage courses"
contains "dashboard shows stats" "$(get "$ADMIN" /admin/courses)" "Registered students"

course_form() { # TITLE SEATS CODE
  local t; t="$(token "$ADMIN" /admin/course-form)"
  post "$ADMIN" /admin/course-form -d "_csrf=$t" -d "id=0" --data-urlencode "code=$3" --data-urlencode "title=$1" \
       --data-urlencode "instructor=Prof Smoke" -d "credits=3" -d "seats=$2" --data-urlencode "description=Created by the smoke test"
}
contains "invalid seats rejected"   "$(course_form "Smoke Course" 0 "$CODE")" "Seats must be"
contains "course is added"          "$(course_form "Smoke Course" 1 "$CODE")" "added"
contains "duplicate code rejected"  "$(course_form "Other Course" 1 "$CODE")" "already uses the code"

LIST="$(get "$ADMIN" /admin/courses)"
SID="$(grep -A15 ">$CODE<" <<<"$LIST" | grep -o 'courseId=[0-9]*' | head -1 | grep -o '[0-9]*')"
if [[ -z "$SID" ]]; then bad "could not find the new course id"; SID=0; else ok "new course appears in the table"; fi

echo "== Seat limit (1 seat, two students)"
T="$(token "$STUDENT" /student/courses)"
contains "first student takes the seat" "$(post "$STUDENT" /student/enroll -d "_csrf=$T" -d "courseId=$SID" -d "q=")" "You are now enrolled"
register "$STUDENT2" "$EMAIL2" "Second Student" secret12 secret12 >/dev/null
login "$STUDENT2" "$EMAIL2" secret12 >/dev/null
T="$(token "$STUDENT2" /student/courses)"
contains "second student is told it is full" "$(post "$STUDENT2" /student/enroll -d "_csrf=$T" -d "courseId=$SID" -d "q=")" "course is full"

ENR="$(get "$ADMIN" "/admin/enrollments?courseId=$SID")"
contains "admin sees the enrolled student"  "$ENR" "$EMAIL1"
contains "UTF-8 name round-trips"           "$ENR" "$NAME1"
not_contains "full-course student not listed" "$ENR" "$EMAIL2"

echo "== Cleanup and logout"
T="$(token "$ADMIN" /admin/courses)"
contains "course is deleted" "$(post "$ADMIN" /admin/course-delete -d "_csrf=$T" -d "id=$SID")" "Course deleted"
not_contains "deleted course is gone" "$(get "$ADMIN" /admin/courses)" ">$CODE<"
T="$(token "$STUDENT" /student/my-courses)"
post "$STUDENT" /logout -d "_csrf=$T" >/dev/null
equals "logged-out session is redirected" "$(code "$STUDENT" /student/courses)" 302

echo
echo "Result: $PASS passed, $FAIL failed"
[[ "$FAIL" -eq 0 ]]
