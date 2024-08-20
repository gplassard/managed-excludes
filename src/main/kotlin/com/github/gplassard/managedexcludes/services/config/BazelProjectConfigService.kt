package com.github.gplassard.managedexcludes.services.config

import com.github.gplassard.managedexcludes.parser.BazelProjectParser
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VirtualFile

@Service(Service.Level.PROJECT)
class BazelProjectConfigService {

    fun loadExcludeConfig(bazelProject: VirtualFile): Set<VirtualFile> {
        return resolveRelativeFiles(bazelProject)
            .toSet()
            .also { thisLogger().info("Aggregation done, there is a total of ${it.size} distinct paths to exclude according to ${bazelProject.path}") }
    }

    private fun resolveRelativeFiles(excludeFile: VirtualFile): List<VirtualFile> {
        val content = FileDocumentManager.getInstance()
            .getDocument(excludeFile)
            ?.immutableCharSequence
        if (content == null) {
            return listOf()
        }
        return BazelProjectParser.parseExcludedDirectories(content.toString())
            .also { thisLogger().info("Planning to exclude ${it.joinToString()}") }
            .mapNotNull { line -> excludeFile.parent.findFileByRelativePath(line) }
            .filter { it.exists() }
            .toList()
    }

}
