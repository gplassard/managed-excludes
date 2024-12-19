package com.github.gplassard.managedexcludes.intention

import com.github.gplassard.managedexcludes.Constants
import com.github.gplassard.managedexcludes.dialog.DebugDialog
import com.github.gplassard.managedexcludes.services.config.ConfigService
import com.github.gplassard.managedexcludes.settings.PluginSettings
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.LowPriorityAction
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.codeInspection.util.IntentionName
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DebugIntention : IntentionAction, LowPriorityAction {
    private val scope = CoroutineScope(Dispatchers.EDT)
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
                        || file.name == Constants.BAZEL_WORKSPACE_FILE_NAME
                )
    }

    override fun invoke(
        project: Project,
        editor: Editor?,
        file: PsiFile?
    ) {
        val pluginSettings = project.service<PluginSettings>()
        val configService = project.service<ConfigService>()
        scope.launch {
            val excludedFromConfig = configService.loadExcludeConfig(project)
            val dialog = DebugDialog(project, pluginSettings.state, excludedFromConfig)
            dialog.show()
        }
    }

    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun getFamilyName(): @IntentionFamilyName String {
        return "Debug managed excludes"
    }
}
