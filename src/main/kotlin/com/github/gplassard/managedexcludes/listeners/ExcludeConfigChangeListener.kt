package com.github.gplassard.managedexcludes.listeners

import com.github.gplassard.managedexcludes.Constants
import com.github.gplassard.managedexcludes.MyBundle
import com.github.gplassard.managedexcludes.helpers.PluginNotifications
import com.github.gplassard.managedexcludes.services.config.ConfigService
import com.github.gplassard.managedexcludes.services.ExcludeService
import com.github.gplassard.managedexcludes.settings.PluginSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.ui.EditorNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ExcludeConfigChangeListener(private val project: Project, private val cs: CoroutineScope) : BulkFileListener {
    override fun after(events: MutableList<out VFileEvent>) {
        cs.launch {

            for (event in events) {
                val excludeChange = event.path.endsWith(Constants.EXCLUDE_FILE_NAME)
                val bazelProjectChange = event.path.endsWith(Constants.BAZELPROJECT_FILE_EXTENSION)
                if (excludeChange || bazelProjectChange) {
                    val virtualFile = event.file ?: continue
                    val module = ModuleUtilCore.findModuleForFile(virtualFile, project) ?: continue

                    val excludeService = project.service<ExcludeService>()
                    val configService = project.service<ConfigService>()
                    val stateService = project.service<PluginSettings>()

                    thisLogger().info("Loading previous exclusions ${stateService.state.excludedPaths}")

                    val fromState = stateService.state.resolveExcludedPaths(project)
                    val fromConfig = configService.loadExcludeConfig(project).toSet()

                    thisLogger().info("New exclusions ${stateService.state.excludedPaths}")

                    val toExclude = fromConfig.minus(fromState)
                    val toCancelExclude = fromState.minus(fromConfig)

                    if (toExclude.isNotEmpty() || toCancelExclude.isNotEmpty()) {
                        excludeService.excludePaths(module, toExclude)
                        excludeService.cancelExcludePaths(module, toCancelExclude)

                        stateService.state.updateExcludedPaths(fromConfig)
                        EditorNotifications.getInstance(project).updateAllNotifications()

                        PluginNotifications.info(
                            MyBundle.message("notification.exclusions.update.title"),
                            MyBundle.message(
                                "notification.exclusions.update.content",
                                toExclude.size,
                                toCancelExclude.size
                            ),
                        )
                    }
                }
            }
        }
    }
}
