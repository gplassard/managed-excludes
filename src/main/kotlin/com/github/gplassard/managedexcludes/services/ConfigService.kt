package com.github.gplassard.managedexcludes.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

@Service
class ConfigService {

    fun loadExcludeConfig(module: Module): Set<VirtualFile> {
        val excludeFiles = FilenameIndex.getVirtualFilesByName(".managed-excludes", true, GlobalSearchScope.projectScope(module.project))
        thisLogger().warn("Excluding files found $excludeFiles")
        return excludeFiles
            .asSequence()
            .map { excludeFile -> Pair(excludeFile, relativeFiles(excludeFile))  }
            .flatMap { (excludeFile, lines) -> lines.map { line -> excludeFile.parent.findFileByRelativePath(line)} }
            .filterNotNull()
            .filter { it.exists() }
            .toSet()
    }

    private fun relativeFiles(excludeFile: VirtualFile) =
        VfsUtil.loadText(excludeFile).split(System.lineSeparator()).filter { it.isNotBlank() }

}
