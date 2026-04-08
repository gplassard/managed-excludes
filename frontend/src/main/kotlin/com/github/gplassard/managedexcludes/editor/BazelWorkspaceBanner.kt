package com.github.gplassard.managedexcludes.editor

import com.github.gplassard.managedexcludes.Constants
import com.github.gplassard.managedexcludes.MyBundle
import com.github.gplassard.managedexcludes.rpc.ManagedExcludesApi
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.BaseProjectDirectories.Companion.getBaseDirectories
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtilCore
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
        val api = project.service<ManagedExcludesApi>()
        val projectPath = project.basePath ?: return Function { null }
        val relativePath = toRelativePath(project, vf)
        return Function {
            if (!Constants.BAZEL_WORKSPACE_FILE_NAMES.contains(vf.name)) {
                return@Function null
            }
            val panel = EditorNotificationPanel()
            scope.launch {
                val excluded = api.isExcludedBazelWorkspace(projectPath, relativePath)
                if (excluded) {
                    panel.text(MyBundle.message("banner.bazelworkspace.excluded"))
                    panel.createActionLabel(
                        MyBundle.message("banner.bazelworkspace.excluded.actions"),
                        {
                            scope.launch {
                                api.unexcludeBazelWorkspace(projectPath, relativePath)
                                EditorNotifications.getInstance(project).updateNotifications(this@BazelWorkspaceBanner)
                                api.refreshAll(projectPath, vf.path)
                            }
                        },
                        true
                    )
                } else {
                    panel.text(MyBundle.message("banner.bazelworkspace.unexcluded"))
                    panel.createActionLabel(
                        MyBundle.message("banner.bazelworkspace.unexcluded.actions"),
                        {
                            scope.launch {
                                api.excludeBazelWorkspace(projectPath, relativePath)
                                EditorNotifications.getInstance(project).updateNotifications(this@BazelWorkspaceBanner)
                                api.refreshAll(projectPath, vf.path)
                            }
                        },
                        true
                    )
                }
            }
            panel
        }
    }

    private fun toRelativePath(project: Project, file: VirtualFile): String {
        for (baseDir in project.getBaseDirectories()) {
            val relativePath = VfsUtilCore.getRelativePath(file, baseDir)
            if (relativePath != null) return relativePath
        }
        return file.path
    }
}
