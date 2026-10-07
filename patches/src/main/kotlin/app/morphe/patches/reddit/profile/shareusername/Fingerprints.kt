package app.morphe.patches.reddit.profile.shareusername

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Hooks for the profile share sheet on 2026.40.0 and 2026.39.0.
 *
 * Phase-0 result (see `.superpowers/sdd/2026-10-06-profile-share-actions/task-3-report.md`
 * §1): the sheet is Jetpack Compose driven by `ActionItem` data objects.
 * Hook 1 appends 2 rows to the profile action list; Hook 2 intercepts their
 * clicks. All references below were transcribed from apktool output of both
 * APKs (trees at `/tmp/opencode/task3-apk/reddit{40,39}-decompiled/`).
 *
 * - [ProfileShareListFingerprint]: `handler/a.c(List)List`, the list
 *   post-processor both share entries funnel through (called with
 *   `ActionSheet$Args.b`). Matches exactly one method per APK (verified by
 *   whole-tree simulation script, `sim_fp2.py`). Filters use the
 *   non-obfuscated `IconEnum` refs because resource ids drift per version
 *   (`label_copy_link_v2`: 0x7f1311f2 on 40.0 → 0x7f1311f4 on 39.0).
 * - [ProfileShareClick40Fingerprint] / [ProfileShareClick39Fingerprint]:
 *   the click dispatch (`onActionItemClicked` state machine). The method name
 *   and wrapper type drifted (`g(Ldb0;)` on 40.0 → `f(Lya0;)` on 39.0), hence
 *   one fingerprint per version; each matches exactly one method on its
 *   version and nothing on the other (verified by simulation, `sim_fp3.py`).
 *   Shape filters: reads `ActionItem.a` (the row id) and compares against
 *   action-type `hashCode()`s.
 *
 * Resolution status: compile-checked here; live resolution inside the patcher
 * runs on the owner's device (Task 4 manual). On a miss the patch no-ops to
 * the stock sheet (see `ShareProfileUsernamePatch.execute`).
 */

// ActionItem ids for the injected rows. Must avoid the two literal ids the
// dispatch compares (`-0x3a13764e`, `-0x7fec8d81` on both versions); every
// other stock id is a runtime identity `hashCode()`, so fixed distinctive
// constants cannot collide in practice. 0x6D6F7270 = ASCII "morp".
internal const val COPY_USERNAME_ID = 0x6D6F7270
internal const val OPEN_GHOSTDDIT_ID = 0x6D6F7271

// Row icons. Single swappable constants per Ruling R2: custom U/ghost vectors
// cannot feed Compose rows without a painter bridge (deferred), so the stable
// non-obfuscated `IconEnum` entries are used (present on both versions).
internal const val COPY_USERNAME_ICON = "Clipboard"
internal const val OPEN_GHOSTDDIT_ICON = "External"

private const val ACTION_ITEM_CTOR =
    "Lcom/reddit/sharing/actions/ActionItem;-><init>" +
        "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;" +
        "Lcom/reddit/ui/compose/icons/IconEnum;ZZLjava/util/List;" +
        "ILandroid/os/Bundle;ZLjava/lang/String;I)V"

internal object ProfileShareListFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/List;",
    parameters = listOf("Ljava/util/List;"),
    filters = listOf(
        methodCall(smali = ACTION_ITEM_CTOR),
        fieldAccess(smali = "Lcom/reddit/ui/compose/icons/IconEnum;->Link:Lcom/reddit/ui/compose/icons/IconEnum;"),
        fieldAccess(smali = "Lcom/reddit/ui/compose/icons/IconEnum;->Share:Lcom/reddit/ui/compose/icons/IconEnum;"),
        methodCall(smali = "Ljava/util/ArrayList;->add(ILjava/lang/Object;)V")
    )
)

internal object ProfileShareClick40Fingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ldb0;", "Lkotlin/coroutines/jvm/internal/ContinuationImpl;"),
    filters = listOf(
        fieldAccess(smali = "Lcom/reddit/sharing/actions/ActionItem;->a:I"),
        methodCall(smali = "Ljava/lang/Object;->hashCode()I"),
        methodCall(
            smali = "Lcom/reddit/sharing/actions/handler/ActionsScreenEventHandler\$onActionItemClicked\$1" +
                ";-><init>(Lcom/reddit/sharing/actions/handler/a;Lasc;)V"
        )
    )
)

internal object ProfileShareClick39Fingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lya0;", "Lkotlin/coroutines/jvm/internal/ContinuationImpl;"),
    filters = listOf(
        fieldAccess(smali = "Lcom/reddit/sharing/actions/ActionItem;->a:I"),
        methodCall(smali = "Ljava/lang/Object;->hashCode()I"),
        methodCall(
            smali = "Lcom/reddit/sharing/actions/handler/ActionsScreenEventHandler\$onActionItemClicked\$1" +
                ";-><init>(Lcom/reddit/sharing/actions/handler/a;Lloc;)V"
        )
    )
)
