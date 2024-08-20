package com.github.gplassard.managedexcludes.services.config

import com.github.gplassard.managedexcludes.Constants
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

    private fun resolveRelativeFiles(excludeFile: VirtualFile): List<VirtualFile> =
        FileDocumentManager.getInstance()
            .getDocument(excludeFile)
            ?.immutableCharSequence
            ?.split("\n")
            ?.asSequence()
            ?.filter { it.isNotBlank() }
            ?.filter { !it.startsWith(Constants.COMMENT_PREFIX) }
            ?.also { thisLogger().info("Planning to exclude ${it.joinToString()}") }
            ?.map { line -> excludeFile.parent.findFileByRelativePath(line) }
            ?.filterNotNull()
            ?.filter { it.exists() }
            ?.toList()
            .orEmpty()
}
