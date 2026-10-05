#!/usr/bin/env bash
# Poll upstream Constants.kt for a new Reddit AppTarget version.
# Exit 10 = would-retarget/applied, 0 = no change or parse fail (warning).
set -euo pipefail
UPSTREAM_URL="https://raw.githubusercontent.com/MorpheApp/morphe-patches/main/patches/src/main/kotlin/app/morphe/patches/reddit/shared/Constants.kt"
UPSTREAM_FILE=""
TARGET_FILE="reddit-target.txt"
DRY_RUN=0
APPLY=0
usage() { echo "Usage: $0 [--upstream-file F] [--target-file F] [--dry-run] [--apply] [--help]"; }
while [ $# -gt 0 ]; do
  case "$1" in
    --upstream-file) UPSTREAM_FILE="${2:?}"; shift 2 ;;
    --target-file) TARGET_FILE="${2:?}"; shift 2 ;;
    --dry-run) DRY_RUN=1; shift ;;
    --apply) APPLY=1; shift ;;
    --help|-h) usage; exit 0 ;;
    *) echo "unknown arg: $1" >&2; usage >&2; exit 2 ;;
  esac
done
SRC="$UPSTREAM_FILE"
if [ -z "$SRC" ]; then
  SRC="$(mktemp)"
  trap 'rm -f "$SRC"' EXIT
  curl -fsSL "$UPSTREAM_URL" -o "$SRC"
fi
NEW_VER="$(sed -n '/COMPATIBILITY_REDDIT/,$p' "$SRC" \
  | sed -n 's/.*version *= *"\([^"]*\)".*/\1/p' | head -n 1)"
if [ -z "$NEW_VER" ]; then
  NEW_VER="$(sed -n 's/.*version *= *"\([^"]*\)".*/\1/p' "$SRC" | tail -n 1)"
fi
if [ -z "$NEW_VER" ]; then
  echo "warning: could not parse upstream version from $SRC" >&2
  exit 0
fi
CUR=""
if [ -f "$TARGET_FILE" ]; then
  CUR="$(tr -d ' \t\r\n' < "$TARGET_FILE")"
fi
if [ "$NEW_VER" = "$CUR" ]; then
  echo "no change (upstream $NEW_VER matches $TARGET_FILE)"
  exit 0
fi
echo "$NEW_VER"
if [ "$APPLY" -eq 1 ] && [ "$DRY_RUN" -eq 0 ]; then
  printf '%s\n' "$NEW_VER" > "$TARGET_FILE"
  echo "retargeted $TARGET_FILE to $NEW_VER"
else
  echo "would retarget $TARGET_FILE ($CUR -> $NEW_VER)"
fi
exit 10
