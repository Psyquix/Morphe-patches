# Morphe-patches

Reddit patches for the Morphe patcher.

![Build](https://github.com/Psyquix/Morphe-patches/actions/workflows/build.yml/badge.svg)

## Patches

| Patch | Description | Target |
| ----- | ----------- | ------ |
| Share profile as username | Shares user profile links as username only. | com.reddit.frontpage 2026.40.0 (exp) |

## Compatibility

- `com.reddit.frontpage` version `2026.40.0` (experimental).
- Current target is pinned in `reddit-target.txt`.

## Toggle behavior

- ON: profile share links matching `reddit.com/user/<name>` are shortened to the bare username.
- OFF: stock share behavior (full profile URL, unmodified).
- Non-profile links pass through unchanged.

## Upstream tracking

- `reddit-target.txt` pins the Reddit version the patches build against.
- The 6h checker (`.github/workflows/check-upstream.yml`) polls upstream on a 6-hour
  schedule; on a version move it opens a PR labeled `upstream-retarget`.
- Retarget PRs are test-gated (tests must pass before the patch bundle builds) with
  no auto-merge — merging stays a human decision.

## Morphed wiring

Add both patch sources, then scope with `|`-separated lists:

```toml
patches-source = "'MorpheApp/morphe-patches' 'Psyquix/Morphe-patches'"
excluded-patches = "Share profile as username | <other-patch>"
included-patches = "Share profile as username | <other-patch>"
```

> Remote visibility (public vs private) for `github.com/Psyquix/Morphe-patches`
> stays human-confirmed at finish; the remote has not been created yet.
