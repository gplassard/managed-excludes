package com.github.gplassard.managedexcludes.intention

import com.github.gplassard.managedexcludes.Constants
import com.github.gplassard.managedexcludes.CoroutineScopeHolder
import com.github.gplassard.managedexcludes.MyBundle
import com.github.gplassard.managedexcludes.dialog.DebugDialog
import com.github.gplassard.managedexcludes.helpers.PluginNotifications
import com.github.gplassard.managedexcludes.rpc.ManagedExcludesApi
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.project.projectId
import com.intellij.psi.PsiFile
import fleet.rpc.client.UnresolvedServiceException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DebugIntention : IntentionAction, LowPriorityAction {

    companion object {
        private val LOG = logger<DebugIntention>()
    }

    override fun getText(): @IntentionName String {
        return "Debug managed excludes"
    }

    override fun isAvailable(
        project: Project,
        editor: Editor?,
        file: PsiFile?
    ): Boolean {
        return file != null && (
                file.name.endsWith(Constants.BAZELPROJECT_FILE_EXTENSION)
                        || file.name == Constants.EXCLUDE_FILE_NAME
                        || Constants.BAZEL_WORKSPACE_FILE_NAMES.contains(file.name)
                )
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        file: PsiFile?
    ) {
        val scope = CoroutineScopeHolder.getInstance(project).getScope()
        scope.launch(Dispatchers.EDT) {
            try {
                val api = ManagedExcludesApi.getInstance()
                val debugInfo = api.getDebugInfo(project.projectId())
                val dialog = DebugDialog(project, debugInfo)
                dialog.show()
            } catch (e: UnresolvedServiceException) {
                LOG.warn("managed-excludes backend not available", e)
                PluginNotifications.notifyError(project, MyBundle.message("banner.backend.unavailable"))
            }
        }
    }

    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun getFamilyName(): @IntentionFamilyName String {
        return "Debug managed excludes"
    }
}
