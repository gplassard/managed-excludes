package com.github.gplassard.managedexcludes.services.config

import com.github.gplassard.managedexcludes.Constants
import com.github.gplassard.managedexcludes.parser.BazelWorkspaceParser
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

@Service(Service.Level.PROJECT)
class BazelWorkspacesConfigService {

    suspend fun loadExcludeConfig(project: Project, excludedWorkspaces: Set<VirtualFile>): Set<VirtualFile> {
        val workspaceFiles = readAction {
            Constants.BAZEL_WORKSPACE_FILE_NAMES
                .flatMap { filename ->
                    FilenameIndex.getVirtualFilesByName(
                        filename,
                        true,
                        GlobalSearchScope.projectScope(project)
                    )
                }
        }
        thisLogger().info("Workspace files found $workspaceFiles")

        val trackedWorkspaces = workspaceFiles.minus(excludedWorkspaces)

        thisLogger().info("Tracked workspace files found $trackedWorkspaces")

        return trackedWorkspaces.flatMap { resolveBazelWorkspaceFiles(it) }
            .toSet()
            .also { thisLogger().info("Aggregation done, there is a total of ${it.size} distinct paths to exclude") }
    }

    private suspend fun resolveBazelWorkspaceFiles(workspaceFile: VirtualFile): List<VirtualFile> {
        val content = readAction { FileDocumentManager.getInstance().getDocument(workspaceFile) }
            ?.immutableCharSequence
        val workspaceName = BazelWorkspaceParser.parseWorkspaceName(content.toString())
        val outputDirs = BazelWorkspaceParser.workspaceOutputDirs(workspaceName)

        return outputDirs
            .mapNotNull { workspaceFile.parent.findChild(it) }
            .filter { it.exists() }
    }
}
