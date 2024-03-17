package com.github.gplassard.managedexcludes.actions

import com.github.gplassard.managedexcludes.services.ExcludeService
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VirtualFile


class CancelManagedExcludesAction : BaseManagedExcludesAction() {
    override suspend fun perform(excludeService: ExcludeService, module: Module, excludes: Set<VirtualFile>) {
        thisLogger().warn("Going to cancel exclude ${excludes.size} files")
        excludeService.cancelExcludePaths(module, excludes)
    }
}
