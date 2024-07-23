package com.github.gplassard.managedexcludes.state

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.vfs.VirtualFile

class PluginState : BaseState() {
    var excludedPaths by stringSet()

    fun updatePaths(files: Set<VirtualFile>) {
        excludedPaths.addAll(files.map { it.path })
        this.incrementModificationCount()
    }
}
