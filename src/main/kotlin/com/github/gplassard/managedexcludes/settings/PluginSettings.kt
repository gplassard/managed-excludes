package com.github.gplassard.managedexcludes.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.resolveFromRootOrRelative

@Service(Service.Level.PROJECT)
@State(name = "PluginSettings", storages = [Storage("managed-excludes.xml", roamingType = RoamingType.DISABLED)])
class PluginSettings : SimplePersistentStateComponent<PluginSettingsState>(PluginSettingsState())

class PluginSettingsState : BaseState() {
    var excludedPaths by stringSet()
    var trackedBazelProjects by stringSet()

    fun updateExcludedPaths(files: Set<VirtualFile>) {
        excludedPaths = files.mapNotNull { it.canonicalPath }.toMutableSet()
        this.incrementModificationCount()
    }

    fun resolveExcludedPaths(project: Project): Set<VirtualFile> {
        return excludedPaths
            .mapNotNull { project.projectFile?.resolveFromRootOrRelative(it) }
            .toSet()
    }

    fun addTrackedBazelProject(virtualFile: VirtualFile) {
        val path = virtualFile.canonicalPath ?: return
        trackedBazelProjects.add(path)
        this.incrementModificationCount()
    }

    fun removeTrackedBazelProject(virtualFile: VirtualFile) {
        val path = virtualFile.canonicalPath ?: return
        trackedBazelProjects.remove(path)
        this.incrementModificationCount()
    }

    fun isTrackedBazelProject(virtualFile: VirtualFile): Boolean {
        val path = virtualFile.canonicalPath ?: return false
        println("Checking track ${virtualFile.canonicalPath} $trackedBazelProjects")
        return trackedBazelProjects.contains(path)
    }
}
