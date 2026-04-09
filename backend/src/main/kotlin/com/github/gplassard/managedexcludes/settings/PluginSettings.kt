package com.github.gplassard.managedexcludes.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.BaseProjectDirectories.Companion.getBaseDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.resolveFromRootOrRelative

@Service(Service.Level.PROJECT)
@State(name = "PluginSettings", storages = [Storage("managed-excludes.xml", roamingType = RoamingType.DISABLED)])
class PluginSettings : SimplePersistentStateComponent<PluginSettingsState>(PluginSettingsState())

class PluginSettingsState : BaseState() {
    var excludedPaths by stringSet()
    var trackedBazelProjects by stringSet()
    var excludedBazelWorkspaces by stringSet()

    fun updateExcludedPaths(project: Project, files: Set<VirtualFile>) {
        thisLogger().info("Updating excluded paths: ${files.map { it.path }}")
        excludedPaths = files.map { toRelativePath(project, it) }.toMutableSet()
        this.incrementModificationCount()
    }

    fun resolveExcludedPaths(project: Project): Set<VirtualFile> {
        thisLogger().info("Resolving excluded paths: $excludedPaths")
        val resolved = excludedPaths
            .flatMap { path -> project.getBaseDirectories().map { it.resolveFromRootOrRelative(path) } }
            .filterNotNull()
            .toSet()
        thisLogger().info("Resolved excluded paths: ${resolved.map { it.path }}")
        return resolved
    }

    fun addTrackedBazelProject(project: Project, virtualFile: VirtualFile) {
        thisLogger().info("Adding tracked Bazel project: ${virtualFile.path}")
        val path = toRelativePath(project, virtualFile)
        trackedBazelProjects.add(path)
        this.incrementModificationCount()
    }

    fun removeTrackedBazelProject(project: Project, virtualFile: VirtualFile) {
        thisLogger().info("Removing tracked Bazel project: ${virtualFile.path}")
        val path = toRelativePath(project, virtualFile)
        trackedBazelProjects.remove(path)
        this.incrementModificationCount()
    }

    fun isTrackedBazelProject(project: Project, virtualFile: VirtualFile): Boolean {
        val path = toRelativePath(project, virtualFile)
        return trackedBazelProjects.contains(path)
    }

    fun resolveTrackedBazelProjects(project: Project): Set<VirtualFile> {
        thisLogger().info("Resolving tracked Bazel projects files: $trackedBazelProjects")
        val resolved = trackedBazelProjects
            .flatMap { path -> project.getBaseDirectories().map { it.resolveFromRootOrRelative(path) } }
            .filterNotNull()
            .toSet()
        thisLogger().info("Resolved tracked Bazel projects files: ${resolved.map { it.path }}")
        return resolved
    }

    fun addExcludedBazelWorkspace(project: Project, virtualFile: VirtualFile) {
        thisLogger().info("Adding excluded Bazel workspace: ${virtualFile.path}")
        val path = toRelativePath(project, virtualFile)
        excludedBazelWorkspaces.add(path)
        this.incrementModificationCount()
    }

    fun removeExcludedBazelWorkspace(project: Project, virtualFile: VirtualFile) {
        thisLogger().info("Removing excluded Bazel workspace: ${virtualFile.path}")
        val path = toRelativePath(project, virtualFile)
        excludedBazelWorkspaces.remove(path)
        this.incrementModificationCount()
    }

    fun isExcludedBazelWorkspace(project: Project, virtualFile: VirtualFile): Boolean {
        val path = toRelativePath(project, virtualFile)
        return excludedBazelWorkspaces.contains(path)
    }

    fun resolveExcludedBazelWorkspaces(project: Project): Set<VirtualFile> {
        thisLogger().info("Resolving excluded Bazel workspace files: $excludedBazelWorkspaces")
        val resolved = excludedBazelWorkspaces
            .flatMap { path -> project.getBaseDirectories().map { it.resolveFromRootOrRelative(path) } }
            .filterNotNull()
            .toSet()
        thisLogger().info("Resolved excluded Bazel workspace files: ${resolved.map { it.path }}")
        return resolved
    }

    companion object {
        fun toRelativePath(project: Project, file: VirtualFile): String {
            for (baseDir in project.getBaseDirectories()) {
                val relativePath = VfsUtilCore.getRelativePath(file, baseDir)
                if (relativePath != null) return relativePath
            }
            return file.path
        }
    }
}
