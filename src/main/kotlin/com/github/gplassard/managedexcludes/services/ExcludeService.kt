package com.github.gplassard.managedexcludes.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.function.Consumer

@Service
class ExcludeService {

    fun excludePaths(module: Module, paths: Set<VirtualFile>) {
        process(module, paths) { (entry, path) ->
            thisLogger().info("Excluding folder $path")
            entry.addExcludeFolder(path)
        }
    }

    fun cancelExcludePaths(module: Module, paths: Set<VirtualFile>) {
        process(module, paths) { (entry, path) ->
            thisLogger().info("Cancel excluding folder $path")
            entry.removeExcludeFolder(path.url)
        }
    }

    private fun process(
        module: Module,
        paths: Set<VirtualFile>,
        processor: Consumer<Pair<ContentEntry, VirtualFile>>
    ) {
        val model = ModuleRootManager.getInstance(module).modifiableModel
        ApplicationManager.getApplication().runWriteAction {
            for (entry in model.contentEntries) {
                for (path in paths) {
                    processor.accept(Pair(entry, path))
                }
            }
            thisLogger().info("Committing model for module $module")
            model.commit()
        }
    }

}
