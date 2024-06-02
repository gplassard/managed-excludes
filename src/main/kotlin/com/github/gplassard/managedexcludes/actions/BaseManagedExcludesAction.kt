package com.github.gplassard.managedexcludes.actions

import com.github.gplassard.managedexcludes.services.ConfigService
import com.github.gplassard.managedexcludes.services.ExcludeService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VirtualFile

abstract class BaseManagedExcludesAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        thisLogger().info("START")
        val module = event.getData(PlatformCoreDataKeys.MODULE)
        if (module == null) {
            thisLogger().warn("No module, aborting")
            return
        }
        val excludeService = event.project?.service<ExcludeService>()
        if (excludeService == null) {
            thisLogger().warn("No exclude service, aborting")
            return
        }
        val configService = event.project?.service<ConfigService>()
        if (configService == null) {
            thisLogger().warn("No config service, aborting")
            return
        }
        val excludes = configService.loadExcludeConfig(module)
        perform(excludeService, module, excludes)
        thisLogger().info("END")
    }

    abstract fun perform(excludeService: ExcludeService, module: Module, excludes: Set<VirtualFile>)
}
