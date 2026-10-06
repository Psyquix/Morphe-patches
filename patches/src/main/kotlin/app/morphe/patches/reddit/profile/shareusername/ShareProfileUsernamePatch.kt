package app.morphe.patches.reddit.profile.shareusername

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.shared.Constants.COMPATIBILITY_REDDIT

@Suppress("unused")
val shareProfileUsernamePatch = bytecodePatch(
    name = "Profile share actions",
    description = "Adds Copy username and Open ghostddit buttons to profile share sheets."
) {
    compatibleWith(COMPATIBILITY_REDDIT)

    extendWith("extensions/extension.mpe")

    execute {
        // STUB — Task 3 is BLOCKED (see task-3 report). Phase 0 decompiled
        // com.reddit.frontpage 2026.40.0 (+ spot-check 2026.39.0) and showed
        // the profile share sheet is Jetpack Compose fed by ActionItem data
        // objects: there is no [Copy link] View whose LayoutParams could be
        // cloned, so the planned clone-at-bind injection cannot apply. The
        // old shortenProfileLink/wasShortened prologue is fully deleted
        // ([Copy link] is stock again); injection stays intentionally empty
        // until the ActionItem-pipeline hook (fingerprints in
        // Fingerprints.kt, still unverified) lands with on-device testing.
    }
}
