package app.morphe.patches.reddit.profile.shareusername

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.reddit.shared.Constants.COMPATIBILITY_REDDIT

private const val HANDLER = "Lcom/reddit/sharing/actions/handler/a;"
private const val ARGS = "Lcom/reddit/sharing/actions/ActionSheet\$Args;"
private const val SHAREABLE_DATA = "Lcom/reddit/sharing/custom/ShareableData;"
private const val PROFILE_DATA = "Lcom/reddit/sharing/custom/ShareableData\$ShareableProfileData;"
private const val ACTION_ITEM = "Lcom/reddit/sharing/actions/ActionItem;"
private const val ICON_ENUM = "Lcom/reddit/ui/compose/icons/IconEnum;"
private const val EXT = "Lapp/morphe/extension/reddit/profile/ProfileShareActions;"

private const val ACTION_ITEM_CTOR =
    "${ACTION_ITEM}-><init>" +
        "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;" +
        "${ICON_ENUM}ZZLjava/util/List;" +
        "ILandroid/os/Bundle;ZLjava/lang/String;I)V"

// TEMPORARY DEBUG (revert before release): toasts tracing Hook 1 on-device.
// Context via ActivityThread (version-independent). v23 holds the app
// context for the whole prologue; nothing else touches v23.
private fun dbgToast(tag: String, label: String, valueReg: String? = null, isInt: Boolean = false): String {
    val msg = if (valueReg == null) {
        """
        const-string v0, "$label"
        """.trimIndent()
    } else if (isInt) {
        """
        new-instance v0, Ljava/lang/StringBuilder;
        invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V
        const-string v1, "$label"
        invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
        move-result-object v0
        invoke-virtual {v0, $valueReg}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
        move-result-object v0
        invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
        move-result-object v0
        """.trimIndent()
    } else {
        """
        new-instance v0, Ljava/lang/StringBuilder;
        invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V
        const-string v1, "$label"
        invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
        move-result-object v0
        invoke-virtual {v0, $valueReg}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
        move-result-object v0
        invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
        move-result-object v0
        """.trimIndent()
    }
    return """
        if-eqz v23, :morphe_dbg_skip_$tag
        $msg
        const/4 v1, 0x1
        invoke-static {v23, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;
        move-result-object v0
        invoke-virtual {v0}, Landroid/widget/Toast;->show()V
        :morphe_dbg_skip_$tag
    """.trimIndent()
}

// Hook 1 prologue, inserted at index 0 of handler/a.c(List)List on both
// versions (all references below are version-independent — verified identical
// on 40.0 and 39.0). Uses v0-v9 scratch plus build range v8-v22; method c has
// .locals 24 on both, so no register bump is needed. Falls through to the
// original first instruction; every exit except the fallthrough is internal.
private fun appendSmali(): String {
    fun item(id: String, label: String, icon: String) = """
        new-instance v8, $ACTION_ITEM
        const v9, $id
        const/4 v10, 0x0
        const-string v11, "$label"
        const/4 v12, 0x0
        const/4 v13, 0x0
        sget-object v14, ${ICON_ENUM}->$icon:$ICON_ENUM
        const/4 v15, 0x0
        const/4 v16, 0x0
        const/4 v17, 0x0
        const/16 v18, -0x2
        const/4 v19, 0x0
        const/4 v20, 0x0
        const/4 v21, 0x0
        const v22, 0x1f7da
        invoke-direct/range {v8 .. v22}, $ACTION_ITEM_CTOR
        invoke-interface {v7, v8}, Ljava/util/List;->add(Ljava/lang/Object;)Z
        move-result v8
    """.trimIndent()

    return """
        invoke-static {}, Landroid/app/ActivityThread;->currentApplication()Landroid/app/Application;
        move-result-object v23
        ${dbgToast("entry", "MORPHE dbg: c() hit")}
        move-object v7, p1
        if-eqz v7, :morphe_list_end
        iget-object v0, p0, ${HANDLER}->a:$ARGS
        if-eqz v0, :morphe_list_end
        iget-object v0, v0, ${ARGS}->a:$SHAREABLE_DATA
        if-eqz v0, :morphe_list_end
        instance-of v1, v0, $PROFILE_DATA
        if-eqz v1, :morphe_list_end
        check-cast v0, $PROFILE_DATA
        iget-object v0, v0, ${PROFILE_DATA}->a:Ljava/lang/String;
        if-eqz v0, :morphe_list_end
        new-instance v1, Ljava/lang/StringBuilder;
        invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V
        const-string v2, "https://www.reddit.com"
        invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
        move-result-object v1
        invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
        move-result-object v1
        invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
        move-result-object v1
        invoke-static {v1}, ${EXT}->extractUsername(Ljava/lang/String;)Ljava/lang/String;
        move-result-object v6
        if-eqz v6, :morphe_list_end
        ${dbgToast("user", "MORPHE dbg: profile=", "v6")}
        invoke-interface {v7}, Ljava/util/List;->size()I
        move-result v2
        const/4 v3, 0x0
        :morphe_list_loop
        if-ge v3, v2, :morphe_list_build
        invoke-interface {v7, v3}, Ljava/util/List;->get(I)Ljava/lang/Object;
        move-result-object v4
        check-cast v4, $ACTION_ITEM
        iget v4, v4, ${ACTION_ITEM}->a:I
        const v5, 0x${COPY_USERNAME_ID.toString(16)}
        if-eq v4, v5, :morphe_list_end
        const v5, 0x${OPEN_GHOSTDDIT_ID.toString(16)}
        if-eq v4, v5, :morphe_list_end
        add-int/lit8 v3, v3, 0x1
        goto :morphe_list_loop
        :morphe_list_build
        ${item("0x${COPY_USERNAME_ID.toString(16)}", "Copy username", COPY_USERNAME_ICON)}
        ${item("0x${OPEN_GHOSTDDIT_ID.toString(16)}", "Open ghostddit", OPEN_GHOSTDDIT_ICON)}
        invoke-interface {v7}, Ljava/util/List;->size()I
        move-result v4
        ${dbgToast("done", "MORPHE dbg: rows, size=", "v4", true)}
        :morphe_list_end
    """.trimIndent()
}

// Hook 2 prologue, inserted at index 0 of the click dispatch (g on 40.0,
// f on 39.0). Both methods are suspend state machines returning Object with
// .locals 17; handled clicks return Kotlin Unit in-method (same-method
// precedent at :goto_2..:goto_5), so returning Unit for our ids is correct.
// All other ids fall through untouched. The username is re-validated through
// extractUsername on the rebuilt share URL (same gate as Hook 1); any
// unexpected null/non-profile data falls through to stock handling.
// Context comes from handler fields: field i (resource accessor) -> its
// Context field a. Uses v0-v5, within .locals 17.
private fun clickSmali(
    wrapper: String,
    accessorType: String,
    accessorClass: String,
): String = """
    if-eqz p1, :morphe_click_end
    iget-object v0, p1, ${wrapper}->a:$ACTION_ITEM
    iget v0, v0, ${ACTION_ITEM}->a:I
    const v1, 0x${COPY_USERNAME_ID.toString(16)}
    if-ne v0, v1, :morphe_click_ghost
    const/4 v1, 0x0
    goto :morphe_click_resolve
    :morphe_click_ghost
    const v1, 0x${OPEN_GHOSTDDIT_ID.toString(16)}
    if-ne v0, v1, :morphe_click_end
    const/4 v1, 0x1
    :morphe_click_resolve
    iget-object v2, p0, ${HANDLER}->a:$ARGS
    if-eqz v2, :morphe_click_end
    iget-object v2, v2, ${ARGS}->a:$SHAREABLE_DATA
    if-eqz v2, :morphe_click_end
    instance-of v4, v2, $PROFILE_DATA
    if-eqz v4, :morphe_click_end
    check-cast v2, $PROFILE_DATA
    iget-object v2, v2, ${PROFILE_DATA}->a:Ljava/lang/String;
    if-eqz v2, :morphe_click_end
    new-instance v4, Ljava/lang/StringBuilder;
    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V
    const-string v5, "https://www.reddit.com"
    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v4
    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    move-result-object v4
    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v4
    invoke-static {v4}, ${EXT}->extractUsername(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v3
    if-eqz v3, :morphe_click_end
    invoke-virtual {v3}, Ljava/lang/String;->length()I
    move-result v4
    if-eqz v4, :morphe_click_end
    iget-object v2, p0, ${HANDLER}->i:$accessorType
    if-eqz v2, :morphe_click_end
    check-cast v2, $accessorClass
    iget-object v2, v2, ${accessorClass}->a:Landroid/content/Context;
    if-eqz v2, :morphe_click_end
    if-nez v1, :morphe_click_open
    invoke-static {v2, v3}, ${EXT}->copyUsername(Landroid/content/Context;Ljava/lang/String;)V
    goto :morphe_click_swallow
    :morphe_click_open
    invoke-static {v2, v3}, ${EXT}->openGhostddit(Landroid/content/Context;Ljava/lang/String;)V
    :morphe_click_swallow
    sget-object v0, Lkotlin/Unit;->a:Lkotlin/Unit;
    return-object v0
    :morphe_click_end
""".trimIndent()

@Suppress("unused")
val shareProfileUsernamePatch = bytecodePatch(
    name = "Profile share actions",
    description = "Adds Copy username and Open ghostddit buttons to profile share sheets."
) {
    compatibleWith(COMPATIBILITY_REDDIT)

    extendWith("extensions/extension.mpe")

    execute {
        // Atomic: both hooks or neither — appended rows without a click
        // intercept would be dead buttons, so any miss no-ops to stock.
        val listMethod = ProfileShareListFingerprint.matchOrNull()?.method
        val click40 = ProfileShareClick40Fingerprint.matchOrNull()?.method
        val click39 = ProfileShareClick39Fingerprint.matchOrNull()?.method
        if (listMethod == null || (click40 == null && click39 == null)) return@execute

        listMethod.addInstructions(0, appendSmali())
        if (click40 != null) {
            // 2026.40.0: g(Ldb0;), resource accessor Llea0 check-cast Lkj2.
            click40.addInstructions(0, clickSmali("Ldb0;", "Llea0;", "Lkj2;"))
        } else {
            // 2026.39.0: f(Lya0;), resource accessor Lc3a0 check-cast Loi2.
            click39!!.addInstructions(0, clickSmali("Lya0;", "Lc3a0;", "Loi2;"))
        }
    }
}
