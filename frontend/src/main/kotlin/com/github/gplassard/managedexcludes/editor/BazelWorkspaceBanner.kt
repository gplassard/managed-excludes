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

class BazelWorkspaceBanner : EditorNotificationProvider, DumbAware {

    companion object {
        private val LOG = logger<BazelWorkspaceBanner>()
    }

    override fun collectNotificationData(project: Project, vf: VirtualFile): Function<in FileEditor, out JComponent?> {
        val filePath = vf.path
        val scope = CoroutineScopeHolder.getInstance(project).getScope()
        return Function {
            if (!Constants.BAZEL_WORKSPACE_FILE_NAMES.contains(vf.name)) {
                return@Function null
            }
            val panel = EditorNotificationPanel()
            scope.launch(Dispatchers.EDT) {
                try {
                    val api = ManagedExcludesApi.getInstance()
                    val projectId = project.projectId()
                    val excluded = api.isExcludedBazelWorkspace(projectId, filePath)
                    if (excluded) {
                        panel.text(MyBundle.message("banner.bazelworkspace.excluded"))
                        panel.createActionLabel(
                            MyBundle.message("banner.bazelworkspace.excluded.actions"),
                            {
                                scope.launch(Dispatchers.EDT) {
                                    try {
                                        val innerApi = ManagedExcludesApi.getInstance()
                                        val pid = project.projectId()
                                        innerApi.unexcludeBazelWorkspace(pid, filePath)
                                        EditorNotifications.getInstance(project).updateNotifications(this@BazelWorkspaceBanner)
                                        innerApi.refreshAll(pid, vf.path)
                                    } catch (e: UnresolvedServiceException) {
                                        LOG.warn("managed-excludes backend not available", e)
                                        PluginNotifications.notifyError(project, MyBundle.message("banner.backend.unavailable"))
                                    }
                                }
                            },
                            true
                        )
                    } else {
                        panel.text(MyBundle.message("banner.bazelworkspace.unexcluded"))
                        panel.createActionLabel(
                            MyBundle.message("banner.bazelworkspace.unexcluded.actions"),
                            {
                                scope.launch(Dispatchers.EDT) {
                                    try {
                                        val innerApi = ManagedExcludesApi.getInstance()
                                        val pid = project.projectId()
                                        innerApi.excludeBazelWorkspace(pid, filePath)
                                        EditorNotifications.getInstance(project).updateNotifications(this@BazelWorkspaceBanner)
                                        innerApi.refreshAll(pid, vf.path)
                                    } catch (e: UnresolvedServiceException) {
                                        LOG.warn("managed-excludes backend not available", e)
                                        PluginNotifications.notifyError(project, MyBundle.message("banner.backend.unavailable"))
                                    }
                                }
                            },
                            true
                        )
                    }
                } catch (e: UnresolvedServiceException) {
                    LOG.warn("managed-excludes backend not available, is the plugin installed on the remote host?", e)
                    panel.text(MyBundle.message("banner.backend.unavailable"))
                }
            }
            panel
        }
    }
}
