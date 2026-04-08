package com.github.gplassard.managedexcludes.services.config

import com.github.gplassard.managedexcludes.Constants
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

@Service(Service.Level.PROJECT)
class ManagedExcludesConfigService {
    suspend fun loadExcludeConfig(project: Project): Set<VirtualFile> {
         return readAction {
             val excludeFiles = FilenameIndex.getVirtualFilesByName(
                Constants.EXCLUDE_FILE_NAME,
                true,
                GlobalSearchScope.projectScope(project)
            )
             thisLogger().info("Excluding files found $excludeFiles")

             excludeFiles
                 .flatMap { resolveRelativeFiles(it) }
                 .toSet()
                 .also { thisLogger().info("Aggregation done, there is a total of ${it.size} distinct paths to exclude") }
        }
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
