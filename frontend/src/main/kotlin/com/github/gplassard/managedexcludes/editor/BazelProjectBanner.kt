package com.github.gplassard.managedexcludes.editor

import com.github.gplassard.managedexcludes.Constants
import com.github.gplassard.managedexcludes.CoroutineScopeHolder
import com.github.gplassard.managedexcludes.MyBundle
import com.github.gplassard.managedexcludes.rpc.ManagedExcludesApi
import com.intellij.openapi.application.EDT
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.project.projectId
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.function.Function
import javax.swing.JComponent

class BazelProjectBanner : EditorNotificationProvider, DumbAware {

    override fun collectNotificationData(project: Project, vf: VirtualFile): Function<in FileEditor, out JComponent?> {
        val filePath = vf.path
        val scope = CoroutineScopeHolder.getInstance(project).getScope()
        return Function {
            if (!vf.name.endsWith(Constants.BAZELPROJECT_FILE_EXTENSION)) {
                return@Function null
            }
            val panel = EditorNotificationPanel()
            scope.launch(Dispatchers.EDT) {
                val api = ManagedExcludesApi.getInstance()
                val projectId = project.projectId()
                val tracked = api.isTrackedBazelProject(projectId, filePath)
                if (tracked) {
                    panel.text(MyBundle.message("banner.bazelproject.tracked"))
                    panel.createActionLabel(
                        MyBundle.message("banner.bazelproject.tracked.actions"),
                        {
                            scope.launch(Dispatchers.EDT) {
                                val innerApi = ManagedExcludesApi.getInstance()
                                val pid = project.projectId()
                                innerApi.untrackBazelProject(pid, filePath)
                                EditorNotifications.getInstance(project).updateAllNotifications()
                                innerApi.refreshAll(pid, vf.path)
                            }
                        },
                        true
                    )
                } else {
                    panel.text(MyBundle.message("banner.bazelproject.untracked"))
                    panel.createActionLabel(
                        MyBundle.message("banner.bazelproject.untracked.actions"),
                        {
                            scope.launch(Dispatchers.EDT) {
                                val innerApi = ManagedExcludesApi.getInstance()
                                val pid = project.projectId()
                                innerApi.trackBazelProject(pid, filePath)
                                EditorNotifications.getInstance(project).updateAllNotifications()
                                innerApi.refreshAll(pid, vf.path)
                            }
                        },
                        true
                    )
                }
            }
            panel
        }
    }
}
