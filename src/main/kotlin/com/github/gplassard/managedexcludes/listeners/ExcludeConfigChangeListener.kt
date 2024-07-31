package com.github.gplassard.managedexcludes.listeners

import com.github.gplassard.managedexcludes.services.ExcludeService
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent

class ExcludeConfigChangeListener(private val project: Project): BulkFileListener {
    override fun after(events: MutableList<out VFileEvent>) {
        for (event in events) {
            if (event.path.endsWith(".managed-excludes")) {
                val virtualFile = event.file
                if (virtualFile != null && project != null) {
                    val module = ModuleUtilCore.findModuleForFile(virtualFile, project)
                    val excludeService = project.service<ExcludeService>()
// TODO
                    excludeService.excludePaths(module, excludes)
                }
                event.getData(PlatformCoreDataKeys.MODULE)
                excludeService.excludePaths(module, excludes)
            }
        }
    }
}
