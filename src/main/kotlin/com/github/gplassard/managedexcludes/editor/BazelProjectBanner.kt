package com.github.gplassard.managedexcludes.editor

import com.github.gplassard.managedexcludes.Constants
import com.github.gplassard.managedexcludes.MyBundle
import com.github.gplassard.managedexcludes.services.RefreshService
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

class BazelProjectBanner : EditorNotificationProvider, DumbAware {

    override fun collectNotificationData(project: Project, vf: VirtualFile): Function<in FileEditor, out JComponent?> {
        val settings = project.service<PluginSettings>()
        return Function {
            if (vf.name != Constants.BAZELPROJECT_FILE_NAME) {
                return@Function null
            }
            val tracked = settings.state.isTrackedBazelProject(vf)
            val panel = EditorNotificationPanel()
            if (tracked) {
                panel.text(MyBundle.message("banner.bazelproject.tracked"))
                panel.createActionLabel(MyBundle.message("banner.bazelproject.tracked.actions"), { untrackFile(project, settings, vf) }, true)
            }
            else {
                panel.text(MyBundle.message("banner.bazelproject.untracked"))
                panel.createActionLabel(MyBundle.message("banner.bazelproject.untracked.actions"), { trackFile(project, settings, vf) }, true)
            }
            panel
        }
    }

    private fun untrackFile(project: Project, settings: PluginSettings, vf: VirtualFile) {
        settings.state.removeTrackedBazelProject(vf)
        EditorNotifications.getInstance(project).updateNotifications(this)

        val module = ModuleUtilCore.findModuleForFile(vf, project) ?: return
        val refreshService = project.service<RefreshService>()
        refreshService.refreshAll(module)
    }

    private fun trackFile(project: Project, settings: PluginSettings, vf: VirtualFile) {
        settings.state.addTrackedBazelProject(vf)
        EditorNotifications.getInstance(project).updateNotifications(this)

        val module = ModuleUtilCore.findModuleForFile(vf, project) ?: return
        val refreshService = project.service<RefreshService>()
        refreshService.refreshAll(module)
    }
}
