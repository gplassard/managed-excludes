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

class BazelWorkspaceBanner : EditorNotificationProvider, DumbAware {
    private val scope = CoroutineScope(Dispatchers.EDT)

    override fun collectNotificationData(project: Project, vf: VirtualFile): Function<in FileEditor, out JComponent?> {
        val settings = project.service<PluginSettings>()
        return Function {
            if (!Constants.BAZEL_WORKSPACE_FILE_NAMES.contains(vf.name)) {
                return@Function null
            }
            val excluded = settings.state.isExcludedBazelWorkspace(vf)
            val panel = EditorNotificationPanel()
            if (excluded) {
                panel.text(MyBundle.message("banner.bazelworkspace.excluded"))
                panel.createActionLabel(
                    MyBundle.message("banner.bazelworkspace.excluded.actions"),
                    { unexcludeFile(project, settings, vf) },
                    true
                )
            } else {
                panel.text(MyBundle.message("banner.bazelworkspace.unexcluded"))
                panel.createActionLabel(
                    MyBundle.message("banner.bazelworkspace.unexcluded.actions"),
                    { excludeFile(project, settings, vf) },
                    true
                )
            }
            panel
        }
    }

    private fun unexcludeFile(project: Project, settings: PluginSettings, vf: VirtualFile) {
        scope.launch {
            settings.state.removeExcludedBazelWorkspace(vf)
            EditorNotifications.getInstance(project).updateNotifications(this@BazelWorkspaceBanner)

            val module = readAction {
                ModuleUtilCore.findModuleForFile(vf, project)
            } ?: return@launch
            val refreshService = project.service<RefreshService>()
            refreshService.refreshAll(module)
        }
    }

    private fun excludeFile(project: Project, settings: PluginSettings, vf: VirtualFile) {
        scope.launch {
            settings.state.addExcludedBazelWorkspace(vf)
            EditorNotifications.getInstance(project).updateNotifications(this@BazelWorkspaceBanner)

            val module = readAction {
                ModuleUtilCore.findModuleForFile(vf, project)
            } ?: return@launch
            val refreshService = project.service<RefreshService>()
            refreshService.refreshAll(module)
        }
    }
}
