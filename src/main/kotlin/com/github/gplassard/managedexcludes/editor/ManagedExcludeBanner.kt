package com.github.gplassard.managedexcludes.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import java.util.function.Function
import javax.swing.JComponent

class ManagedExcludeBanner : EditorNotificationProvider, DumbAware {

    override fun collectNotificationData(p0: Project, p1: VirtualFile): Function<in FileEditor, out JComponent?>? {
        return Function {
            val panel = EditorNotificationPanel()
            panel.text("This is my message")
            panel
        }
    }

}
