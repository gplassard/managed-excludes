package com.github.gplassard.managedexcludes.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.vfs.VirtualFile

@Service(Service.Level.PROJECT)
@State(name = "PluginSettings", storages = [Storage("managed-excludes.xml", roamingType = RoamingType.DISABLED)])
class PluginSettings : SimplePersistentStateComponent<PluginSettingsState>(PluginSettingsState())

class PluginSettingsState : BaseState() {
    var excludedPaths by stringSet()

    fun updateExcludedPaths(files: Set<VirtualFile>) {
        excludedPaths = files.mapNotNull { it.canonicalPath }.toMutableSet()
        this.incrementModificationCount()
    }
}
