package com.github.gplassard.managedexcludes.actions

import com.github.gplassard.managedexcludes.services.ExcludeService
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VirtualFile


class AddManagedExcludesAction : BaseManagedExcludesAction() {
    override fun perform(excludeService: ExcludeService, module: Module, excludes: Set<VirtualFile>) {
        thisLogger().info("Going to exclude ${excludes.size} files")
        excludeService.excludePaths(module, excludes)
    }
}
