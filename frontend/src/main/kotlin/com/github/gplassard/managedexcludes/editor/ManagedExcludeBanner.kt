package com.github.gplassard.managedexcludes.editor

import com.github.gplassard.managedexcludes.Constants
import com.github.gplassard.managedexcludes.CoroutineScopeHolder
import com.github.gplassard.managedexcludes.MyBundle
import com.github.gplassard.managedexcludes.helpers.PluginNotifications
import com.github.gplassard.managedexcludes.rpc.ManagedExcludesApi
import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.project.projectId
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import fleet.rpc.client.UnresolvedServiceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.function.Function
import javax.swing.JComponent

class ManagedExcludeBanner : EditorNotificationProvider, DumbAware {

    companion object {
        private val LOG = logger<ManagedExcludeBanner>()
    }

    override fun collectNotificationData(project: Project, vf: VirtualFile): Function<in FileEditor, out JComponent?> {
        val scope = CoroutineScopeHolder.getInstance(project).getScope()
        return Function {
            if (vf.name != Constants.EXCLUDE_FILE_NAME && !vf.name.endsWith(Constants.BAZELPROJECT_FILE_EXTENSION)) {
                return@Function null
            }
            val panel = EditorNotificationPanel()
            scope.launch(Dispatchers.EDT) {
                try {
                    val api = ManagedExcludesApi.getInstance()
                    val projectId = project.projectId()
                    val count = api.getExcludedPathsCount(projectId)
                    panel.text(MyBundle.message("banner.exclusions", count))
                } catch (e: UnresolvedServiceException) {
                    LOG.warn("managed-excludes backend not available, is the plugin installed on the remote host?", e)
                    panel.text(MyBundle.message("banner.backend.unavailable"))
                }
            }
            panel.createActionLabel(
                MyBundle.message("banner.exclusions.action.refreshall"),
                {
                    scope.launch(Dispatchers.EDT) {
                        try {
                            val api = ManagedExcludesApi.getInstance()
                            api.refreshAll(project.projectId(), vf.path)
                            EditorNotifications.getInstance(project).updateNotifications(this@ManagedExcludeBanner)
                        } catch (e: UnresolvedServiceException) {
                            LOG.warn("managed-excludes backend not available", e)
                            PluginNotifications.notifyError(project, MyBundle.message("banner.backend.unavailable"))
                        }
                    }
                },
                true
            )
            panel
        }
    }
}
