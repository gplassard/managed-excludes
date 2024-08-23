package com.github.gplassard.managedexcludes.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.resolveFromRootOrRelative

@Service(Service.Level.PROJECT)
@State(name = "PluginSettings", storages = [Storage("managed-excludes.xml", roamingType = RoamingType.DISABLED)])
class PluginSettings : SimplePersistentStateComponent<PluginSettingsState>(PluginSettingsState())

class PluginSettingsState : BaseState() {
    var excludedPaths by stringSet()
    private var trackedBazelProjects by stringSet()

    fun updateExcludedPaths(files: Set<VirtualFile>) {
        thisLogger().info("Updating excluded paths: ${files.map { it.path }}")
        excludedPaths = files.map { it.path }.toMutableSet()
        this.incrementModificationCount()
    }

    fun resolveExcludedPaths(project: Project): Set<VirtualFile> {
        thisLogger().info("Resolving excluded paths: $trackedBazelProjects")
        val resolved = excludedPaths
            .mapNotNull { project.projectFile?.resolveFromRootOrRelative(it) }
            .toSet()
        thisLogger().info("Resolved excluded paths: ${resolved.map { it.path }}")
        return resolved
    }

    fun addTrackedBazelProject(virtualFile: VirtualFile) {
        thisLogger().info("Adding tracked Bazel project: ${virtualFile.path}")
        val path = virtualFile.path
        trackedBazelProjects.add(path)
        this.incrementModificationCount()
    }

    fun removeTrackedBazelProject(virtualFile: VirtualFile) {
        thisLogger().info("Removing tracked Bazel project: ${virtualFile.path}")
        val path = virtualFile.path
        trackedBazelProjects.remove(path)
        this.incrementModificationCount()
    }

    fun isTrackedBazelProject(virtualFile: VirtualFile): Boolean {
        val path = virtualFile.path
        return trackedBazelProjects.contains(path)
    }

    fun resolveTrackedBazelProjects(project: Project): Set<VirtualFile> {
        thisLogger().info("Resolving tracked Bazel projects files: $trackedBazelProjects")
        val resolved =  trackedBazelProjects
            .mapNotNull { project.projectFile?.resolveFromRootOrRelative(it) }
            .toSet()
        thisLogger().info("Resolved tracked Bazel projects files: ${resolved.map { it.path }}")
        return resolved
    }
}
