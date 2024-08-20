package com.github.gplassard.managedexcludes.services

import com.github.gplassard.managedexcludes.MyBundle
import com.github.gplassard.managedexcludes.helpers.PluginNotifications
import com.github.gplassard.managedexcludes.services.config.ConfigService
import com.github.gplassard.managedexcludes.settings.PluginSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class RefreshService(val project: Project)  {

    fun refreshAll(module: Module) {
        val settings = project.service<PluginSettings>()
        val configService = project.service<ConfigService>()
        val excludeService = project.service<ExcludeService>()

        val fromSettings = settings.state.resolveExcludedPaths(project)
        PluginNotifications.info(
            MyBundle.message("notification.exclusions.update.title"),
            MyBundle.message("notification.refreshall.cancel", fromSettings.size),
        )
        excludeService.cancelExcludePaths(module, fromSettings.toSet())

        // FIXME slow action on UI thread
        val fromConfig = configService.loadExcludeConfig(project)
        PluginNotifications.info(
            MyBundle.message("notification.exclusions.update.title"),
            MyBundle.message("notification.refreshall.exclude", fromConfig.size),
        )
        excludeService.excludePaths(module, fromConfig)

        settings.state.updateExcludedPaths(fromConfig)
    }

}
