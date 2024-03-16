package com.github.gplassard.managedexcludes.services

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.*

@Service
class ConfigService {

    suspend fun loadExcludeConfig(module: Module): Set<VirtualFile> {
        val excludeFiles = ReadAction.compute<Collection<VirtualFile>, RuntimeException> {
            FilenameIndex.getVirtualFilesByName(
                ".managed-excludes",
                true,
                GlobalSearchScope.projectScope(module.project)
            )
        }
        thisLogger().warn("Excluding files found $excludeFiles")

        return coroutineScope {
            excludeFiles
                .map {
                    async {
                        refreshAndResolveRelativeFiles(it)
                    }
                }
                .awaitAll()
                .also { thisLogger().info("Finished refresh and resolving") }
                .asSequence()
                .flatMap { it }
                .toSet()
                .also { thisLogger().info("Aggregation done, there is a total of ${it.size} distinct paths to exclude") }
        }
    }


    suspend fun refreshAndResolveRelativeFiles(excludeFile: VirtualFile): List<VirtualFile> {
        val refreshed = suspendCoroutine { continuation ->
            thisLogger().info("Start refreshing $excludeFile")
            excludeFile.refresh(true, false) {
                thisLogger().info("Callback refreshing $excludeFile")
                continuation.resume(excludeFile)
            }
        }
        thisLogger().info("Finished refreshing $excludeFile")
        return withContext(Dispatchers.IO) {
            resolveRelativeFiles(refreshed)
        }
    }

    private fun resolveRelativeFiles(excludeFile: VirtualFile): List<VirtualFile> =
        VfsUtil.loadText(excludeFile)
            .split(System.lineSeparator())
            .asSequence()
            .filter { it.isNotBlank() }
            .filter { !it.startsWith("#") }
            .map { line -> excludeFile.parent.findFileByRelativePath(line) }
            .filterNotNull()
            .filter {
                thisLogger().info("Planning to exclude $it, exists ${it.exists()}")
                it.exists()
            }
            .toList()

}
