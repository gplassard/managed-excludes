package com.github.gplassard.managedexcludes.editor

import com.github.gplassard.managedexcludes.Constants
import com.github.gplassard.managedexcludes.MyBundle
import com.github.gplassard.managedexcludes.helpers.PluginNotifications
import com.github.gplassard.managedexcludes.services.ConfigService
import com.github.gplassard.managedexcludes.services.ExcludeService
import com.github.gplassard.managedexcludes.settings.PluginSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import java.util.function.Function
import javax.swing.JComponent

class ManagedExcludeBanner : EditorNotificationProvider, DumbAware {

    override fun collectNotificationData(project: Project, vf: VirtualFile): Function<in FileEditor, out JComponent?> {
        val settings = project.service<PluginSettings>()
        return Function {
            if (vf.name != Constants.EXCLUDE_FILE_NAME) {
                return@Function null
            }
            val panel = EditorNotificationPanel()
            panel.text(MyBundle.message("banner.exclusions", settings.state.excludedPaths.size))
            panel.createActionLabel(MyBundle.message("banner.refreshall"), { refreshAll(vf, project, settings) }, true)
            panel
        }
    }

    private fun refreshAll(
        vf: VirtualFile,
        project: Project,
        settings: PluginSettings
    ) {
        val module = ModuleUtilCore.findModuleForFile(vf, project) ?: return
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
            MyBundle.message("notification.refreshall.exclude", fromSettings.size),
        )
        excludeService.excludePaths(module, fromConfig)

        settings.state.updateExcludedPaths(fromConfig)
        EditorNotifications.getInstance(project).updateNotifications(this)
    }

}
