package com.github.gplassard.managedexcludes.editor

import com.github.gplassard.managedexcludes.Constants
import com.github.gplassard.managedexcludes.MyBundle
import com.github.gplassard.managedexcludes.services.RefreshService
import com.github.gplassard.managedexcludes.settings.PluginSettings
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.function.Function
import javax.swing.JComponent

class ManagedExcludeBanner : EditorNotificationProvider, DumbAware {
    private val scope = CoroutineScope(Dispatchers.EDT)

    override fun collectNotificationData(project: Project, vf: VirtualFile): Function<in FileEditor, out JComponent?> {
        val settings = project.service<PluginSettings>()
        return Function {
            if (vf.name != Constants.EXCLUDE_FILE_NAME && !vf.name.endsWith(Constants.BAZELPROJECT_FILE_EXTENSION)) {
                return@Function null
            }
            val panel = EditorNotificationPanel()
            panel.text(MyBundle.message("banner.exclusions", settings.state.excludedPaths.size))
            panel.createActionLabel(
                MyBundle.message("banner.exclusions.action.refreshall"),
                { refreshAll(vf, project) },
                true
            )
            panel
        }
    }

    private fun refreshAll(
        vf: VirtualFile,
        project: Project,
    ) {
        scope.launch {
            val refreshService = project.service<RefreshService>()
            val module = readAction {
                ModuleUtilCore.findModuleForFile(vf, project)
            } ?: return@launch
            refreshService.refreshAll(module)
            EditorNotifications.getInstance(project).updateNotifications(this@ManagedExcludeBanner)
        }
    }

}
