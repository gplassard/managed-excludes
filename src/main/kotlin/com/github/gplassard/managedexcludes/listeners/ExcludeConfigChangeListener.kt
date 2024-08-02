package com.github.gplassard.managedexcludes.listeners

import com.github.gplassard.managedexcludes.MyBundle
import com.github.gplassard.managedexcludes.services.ConfigService
import com.github.gplassard.managedexcludes.services.ExcludeService
import com.github.gplassard.managedexcludes.settings.PluginSettings
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.resolveFromRootOrRelative

class ExcludeConfigChangeListener(private val project: Project) : BulkFileListener {
    override fun after(events: MutableList<out VFileEvent>) {
        for (event in events) {
            if (event.path.endsWith(".managed-excludes")) {
                val virtualFile = event.file ?: continue
                val module = ModuleUtilCore.findModuleForFile(virtualFile, project) ?: continue

                val excludeService = project.service<ExcludeService>()
                val configService = project.service<ConfigService>()
                val stateService = project.service<PluginSettings>()

                thisLogger().info("Loading previous exclusions ${stateService.state.excludedPaths}")

                val fromState = stateService.state.excludedPaths
                    .mapNotNull { project.projectFile?.resolveFromRootOrRelative(it) }
                    .toSet()
                val fromConfig = configService.loadExcludeConfig(project).toSet()

                thisLogger().info("New exclusions ${stateService.state.excludedPaths}")

                val toExclude = fromConfig.minus(fromState)
                val toCancelExclude = fromState.minus(fromConfig)

                excludeService.excludePaths(module, toExclude)
                excludeService.cancelExcludePaths(module, toCancelExclude)
                stateService.state.updateExcludedPaths(toExclude)

                if (toExclude.isNotEmpty() || toCancelExclude.isNotEmpty()) {
                    Notifications.Bus.notify(
                        Notification(
                            MyBundle.message("notifications.group"),
                            MyBundle.message("notification.exclusions.update.title"),
                            MyBundle.message(
                                "notification.exclusions.update.content",
                                toExclude.size,
                                toCancelExclude.size
                            ),
                            NotificationType.INFORMATION,
                        )
                    )
                }
            }
        }
    }
}
