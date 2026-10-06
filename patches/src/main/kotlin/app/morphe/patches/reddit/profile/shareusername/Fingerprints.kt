package app.morphe.patches.reddit.profile.shareusername

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Phase-0 result (see task-3 report): the profile share sheet on 2026.40.0
 * and 2026.39.0 is Jetpack Compose driven by `ActionItem` data objects —
 * there is no `[Copy link]` View to clone, so a clone-at-bind hook cannot
 * apply. The fingerprints below target the real, observed ActionItem
 * pipeline instead:
 *
 * - [ProfileShareListFingerprint]: `handler/a.c(List)List`, which assembles
 *   the sheet's action list and inserts the Copy-link (`IconEnum.Link`) and
 *   Share-via (`IconEnum.Share`) items. Observed on 40.0 (method at
 *   `handler/a.smali:1121`) and 39.0 (`handler/a.smali:1071`) with identical
 *   structural signals. Resource IDs drift between versions
 *   (`label_copy_link_v2`: 0x7f1311f2 on 40.0, 0x7f1311f4 on 39.0), so the
 *   filters use the non-obfuscated `IconEnum` field references instead.
 * - [ProfileShareSheetFactoryFingerprint]: the static `actions/b.a(...)`
 *   `ActionSheet` factory both profile share entries funnel through
 *   (`OnShareClickEventHandler` share button via `kpi.a` on 40.0 /
 *   `w1f.I` on 39.0, plus the overflow-menu path). Obfuscated parameter
 *   types are declared as bare `L` because they shift between versions
 *   (e.g. `Lga0;` on 40.0 vs `Lba0;` on 39.0).
 *
 * UNVERIFIED: neither fingerprint has been resolution-tested against a live
 * APK inside the patcher, and no injection is wired yet (see the patch
 * stub). They are intentionally unused so they cannot fail a patch run.
 */
internal object ProfileShareListFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/List;",
    parameters = listOf("Ljava/util/List;"),
    filters = listOf(
        methodCall(smali = "Lcom/reddit/sharing/actions/ActionItem;-><init>(IILjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Lcom/reddit/ui/compose/icons/IconEnum;ZZLjava/util/List;ILandroid/os/Bundle;ZLjava/lang/String;I)V"),
        fieldAccess(smali = "Lcom/reddit/ui/compose/icons/IconEnum;->Link:Lcom/reddit/ui/compose/icons/IconEnum;"),
        fieldAccess(smali = "Lcom/reddit/ui/compose/icons/IconEnum;->Share:Lcom/reddit/ui/compose/icons/IconEnum;"),
        methodCall(smali = "Ljava/util/ArrayList;->add(ILjava/lang/Object;)V")
    )
)

internal object ProfileShareSheetFactoryFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Lcom/reddit/sharing/actions/ActionSheet;",
    parameters = listOf(
        "L",
        "L",
        "Ljava/util/List;",
        "L",
        "Z",
        "L",
        "Z",
        "Z",
        "L",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "I"
    ),
    filters = listOf(
        methodCall(smali = "Lcom/reddit/sharing/actions/ActionSheet;-><init>(Landroid/os/Bundle;)V")
    )
)
