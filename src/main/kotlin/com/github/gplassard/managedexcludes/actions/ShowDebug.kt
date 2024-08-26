package com.github.gplassard.managedexcludes.actions

import com.github.gplassard.managedexcludes.dialog.DebugDialog
import com.github.gplassard.managedexcludes.services.config.ConfigService
import com.github.gplassard.managedexcludes.settings.PluginSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShowDebug : AnAction() {
    private val scope = CoroutineScope(Dispatchers.EDT)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val pluginSettings = project.service<PluginSettings>()
        val configService = project.service<ConfigService>()
        scope.launch {
            val excludedFromConfig = configService.loadExcludeConfig(project)
            val dialog = DebugDialog(project, pluginSettings.state, excludedFromConfig)
            dialog.show()
        }
    }
}
