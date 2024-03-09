package com.github.gplassard.managedexcludes.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.roots.ModuleRootManager


class PopupDialogAction: AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        thisLogger().info("START")
        val module = event.getData(PlatformCoreDataKeys.MODULE)
        if (module == null) {
            thisLogger().warn("No module, aborting")
            return
        }
        val model = ModuleRootManager.getInstance(module).modifiableModel
        ApplicationManager.getApplication().runWriteAction {
            model.contentEntries.forEach { entry ->
                thisLogger().warn("Excluding for entry " + entry.file.toString())
                entry.file?.findChild("scripts")?.let { entry.addExcludeFolder(it) }
            }
            model.commit()
        }
        thisLogger().info("END")
    }
}
