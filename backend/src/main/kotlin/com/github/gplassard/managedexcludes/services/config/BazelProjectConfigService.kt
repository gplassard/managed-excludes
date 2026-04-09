package com.github.gplassard.managedexcludes.services.config

import com.github.gplassard.managedexcludes.parser.BazelProjectParser
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.BaseProjectDirectories.Companion.getBaseDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

@Service(Service.Level.PROJECT)
class BazelProjectConfigService {

    suspend fun loadExcludeConfig(project: Project, bazelProject: VirtualFile): Set<VirtualFile> {
        return resolveExcludedFiles(project, bazelProject)
            .toSet()
            .also { thisLogger().info("Aggregation done, there is a total of ${it.size} distinct paths to exclude according to ${bazelProject.path}") }
    }

    private suspend fun resolveExcludedFiles(project: Project, excludeFile: VirtualFile): List<VirtualFile> {
        val content = readAction { FileDocumentManager.getInstance().getDocument(excludeFile) }
            ?.immutableCharSequence
        if (content == null) {
            return listOf()
        }
        return BazelProjectParser.parseExcludedDirectories(content.toString())
            .also { thisLogger().info("Planning to exclude ${it.joinToString()}") }
            .map { line -> project.getBaseDirectories().map { project -> project.findFileByRelativePath(line)} }
            .flatten()
            .filterNotNull()
            .filter { it.exists() }
            .toList()
    }

}
