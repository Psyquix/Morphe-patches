# Task 5 report: 6h upstream checker + retarget (test-gated)

## TDD RED evidence
- `bash scripts/check-upstream-reddit.sh --help` → `bash: scripts/check-upstream-reddit.sh: No such file or directory`, exit 127 (script not implemented).
- Fixtures staged first in `/tmp/opencode/`: `upstream_new.kt` (2026.41.0), `upstream_same.kt` (2026.40.0), `upstream_bad.kt` (garbage), `t.txt` (2026.40.0).

## GREEN evidence (all pass post-implementation)
- Case 1 (new): `--upstream-file upstream_new.kt --target-file t.txt --dry-run` → stdout contains `2026.41.0`, exit **10**. ✅
- Case 2 (same): `--upstream-file upstream_same.kt ...` → `no change (upstream 2026.40.0 ...)`, exit **0**. ✅
- Case 3 (garbage): `--upstream-file upstream_bad.kt ...` → `warning: could not parse upstream version ...` on stderr, exit **0**, no retarget. ✅
- Extra: `--apply` wrote `2026.41.0` to target copy (exit 10); `--help` exits 0; no-`COMPATIBILITY_REDDIT` fixture falls back to latest `version = "` (2026.42.0, exit 10).
- `python3 -c yaml.safe_load` on `check-upstream.yml` → YAML OK; `bash -n` + shellcheck → clean.

## Files
- `scripts/check-upstream-reddit.sh` (new, mode 100755, **51 lines** < 80, `set -euo pipefail`). Flags: `--upstream-file/--target-file/--dry-run/--apply/--help`. Live mode curls upstream raw `Constants.kt` on main when no fixture given. Primary parse: first `version = "X"` at/after `COMPATIBILITY_REDDIT`; fallback: last `version = "` in file; parse fail → stderr warning + exit 0. Differ → print new version, exit 10 (`--apply` writes target unless `--dry-run`).
- `.github/workflows/check-upstream.yml` (new). `on: schedule cron "0 */6 * * *"` + `workflow_dispatch`. `check` job runs script `--apply` (live); on exit 10 marker-scoped python3 edit of first `version = "` at/after `COMPATIBILITY_REDDIT` in `Constants.kt` (same rule as script; fails loudly if marker/version missing), pushes `chore/reddit-retarget-<ver>` + `gh pr create --label upstream-retarget` (no auto-merge anywhere). `test-gated-build` job (needs check, if changed) checks out the branch and runs `./gradlew test` gate then `:patches:buildAndroid`. Secrets only via `secrets.GITHUB_TOKEN` at runtime; nothing hardcoded.
- `reddit-target.txt`: **unmodified**, seed `2026.40.0` intact (runtime-updated by CI only).

## Self-review
- Exit contract matches brief exactly (10 retarget / 0 no-change / 0+warning parse-fail); `--dry-run` never writes.
- Script uses portable `sed` (no `grep -P`), `mktemp`+trap cleanup for live fetch, `tr`-trimmed target compare tolerates trailing newline.
- Workflow branch name is version-derived; commit includes both `reddit-target.txt` and `Constants.kt`.
- `git status` clean after commit; no remotes created, nothing pushed.

## Fix report (round 1/5 resume — marker-scoped workflow retarget)
- Change: `.github/workflows/check-upstream.yml` `check` step — replaced `sed -i '0,/version = "[^"]*"/s//.../' "$KT"` (first-version-in-file) with a marker-scoped `python3 - "$KT" "$NEW_VER" <<'EOF'` edit: finds `COMPATIBILITY_REDDIT` line index, replaces the first `version\s*=\s*"[^"]*"` at/after it, `sys.exit(...)` non-zero if marker missing or no version after marker. Same rule as `scripts/check-upstream-reddit.sh` primary parse. No other workflow lines touched (cron, PR creation, test gate intact); script untouched.
- Covering checks:
  - (1) `bash -n scripts/check-upstream-reddit.sh` → clean; `python3 -c yaml.safe_load` on `check-upstream.yml` → YAML OK.
  - (2) Fixture re-run (brief Step 5): new → exit 10 (`2026.41.0`, would-retarget); same → exit 0 (no change); garbage → stderr `warning: could not parse upstream version`, exit 0. Matches 10/0/0+warning.
  - (3) Decoy fixture `/tmp/decoy_Constants.kt` (`version = "9.9.9"` before marker, `version = "2026.41.0"` after): workflow snippet with `NEW_VER=2026.42.0` → exit 0, decoy line 2 still `9.9.9`, post-marker line now `2026.42.0`. Missing-marker copy → `error: COMPATIBILITY_REDDIT marker not found`, exit 1 (fails loudly).
- Commands/outputs: `bash -n scripts/check-upstream-reddit.sh && echo OK` → OK; fixture exits `10/0/0`; decoy `grep -n 'version = '` → `2: ... 9.9.9` + `5: ... 2026.42.0`; nomarker run → exit 1.
- Diff scope: `git diff --stat` → only `.github/workflows/check-upstream.yml` (+16/−1).

## Concerns
- Live `curl` failure (network) aborts the job via `set -e` — noisy but visible; no retry/backoff.
- Downstream gate uses explicit gradle test+build steps rather than `workflow_call` reuse of `build.yml` (brief allows either); the PR also triggers `build.yml` via `pull_request`, so gating is doubled, not missing.
