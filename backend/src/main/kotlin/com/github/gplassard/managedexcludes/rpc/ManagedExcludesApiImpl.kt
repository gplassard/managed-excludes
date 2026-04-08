package com.github.gplassard.managedexcludes.rpc

import com.github.gplassard.managedexcludes.services.RefreshService
import com.github.gplassard.managedexcludes.services.config.ConfigService
import com.github.gplassard.managedexcludes.settings.PluginSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.BaseProjectDirectories.Companion.getBaseDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.resolveFromRootOrRelative
import com.intellij.platform.rpc.backend.RemoteApiProvider
import com.intellij.project.isDirectoryBased

class ManagedExcludesApiImpl : ManagedExcludesApi {

    private fun findProject(projectPath: String): Project? =
        ProjectManager.getInstance().openProjects.find { it.basePath == projectPath }

    override suspend fun getExcludedPathsCount(projectPath: String): Int {
        val project = findProject(projectPath) ?: return 0
        return project.service<PluginSettings>().state.excludedPaths.size
    }

    override suspend fun isTrackedBazelProject(projectPath: String, relativePath: String): Boolean {
        val project = findProject(projectPath) ?: return false
        return project.service<PluginSettings>().state.trackedBazelProjects.contains(relativePath)
    }

    override suspend fun isExcludedBazelWorkspace(projectPath: String, relativePath: String): Boolean {
        val project = findProject(projectPath) ?: return false
        return project.service<PluginSettings>().state.excludedBazelWorkspaces.contains(relativePath)
    }

    override suspend fun trackBazelProject(projectPath: String, relativePath: String) {
        val project = findProject(projectPath) ?: return
        val vf = resolveRelativePath(project, relativePath) ?: return
        project.service<PluginSettings>().state.addTrackedBazelProject(project, vf)
    }

    override suspend fun untrackBazelProject(projectPath: String, relativePath: String) {
        val project = findProject(projectPath) ?: return
        val vf = resolveRelativePath(project, relativePath) ?: return
        project.service<PluginSettings>().state.removeTrackedBazelProject(project, vf)
    }

    override suspend fun excludeBazelWorkspace(projectPath: String, relativePath: String) {
        val project = findProject(projectPath) ?: return
        val vf = resolveRelativePath(project, relativePath) ?: return
        project.service<PluginSettings>().state.addExcludedBazelWorkspace(project, vf)
    }

    override suspend fun unexcludeBazelWorkspace(projectPath: String, relativePath: String) {
        val project = findProject(projectPath) ?: return
        val vf = resolveRelativePath(project, relativePath) ?: return
        project.service<PluginSettings>().state.removeExcludedBazelWorkspace(project, vf)
    }

    override suspend fun refreshAll(projectPath: String, virtualFilePath: String) {
        val project = findProject(projectPath) ?: return
        val vf = LocalFileSystem.getInstance().findFileByPath(virtualFilePath) ?: return
        val module = ModuleUtilCore.findModuleForFile(vf, project) ?: return
        val refreshService = project.service<RefreshService>()
        refreshService.refreshAll(module)
    }

    override suspend fun getDebugInfo(projectPath: String): DebugInfoDto {
        val project = findProject(projectPath) ?: return DebugInfoDto(
            projectName = "unknown", projectBasePath = projectPath,
            isDefault = false, isDirectoryBased = false, baseDirectories = emptySet(),
            workspaceFile = null, projectFilePath = null,
            trackedBazelProjects = emptySet(), resolvedTrackedProjects = emptySet(),
            excludedBazelWorkspaces = emptySet(), resolvedExcludedWorkspaces = emptySet(),
            excludedPaths = emptySet(), resolvedExcludedPaths = emptySet(),
            excludedFromConfig = emptySet(),
        )
        val settings = project.service<PluginSettings>()
        val configService = project.service<ConfigService>()
        val state = settings.state
        val excludedFromConfig = configService.loadExcludeConfig(project)
        return DebugInfoDto(
            projectName = project.name,
            projectBasePath = project.basePath,
            isDefault = project.isDefault,
            isDirectoryBased = project.isDirectoryBased,
            baseDirectories = project.getBaseDirectories().map { it.path }.toSet(),
            workspaceFile = project.workspaceFile?.path,
            projectFilePath = project.projectFile?.path,
            trackedBazelProjects = state.trackedBazelProjects.toSet(),
            resolvedTrackedProjects = state.resolveTrackedBazelProjects(project).map { it.canonicalPath ?: it.path }.toSet(),
            excludedBazelWorkspaces = state.excludedBazelWorkspaces.toSet(),
            resolvedExcludedWorkspaces = state.resolveExcludedBazelWorkspaces(project).map { it.canonicalPath ?: it.path }.toSet(),
            excludedPaths = state.excludedPaths.toSet(),
            resolvedExcludedPaths = state.resolveExcludedPaths(project).map { it.canonicalPath ?: it.path }.toSet(),
            excludedFromConfig = excludedFromConfig.map { it.canonicalPath ?: it.path }.toSet(),
        )
    }

    private fun resolveRelativePath(project: Project, relativePath: String): com.intellij.openapi.vfs.VirtualFile? {
        return project.getBaseDirectories()
            .map { it.resolveFromRootOrRelative(relativePath) }
            .firstOrNull { it != null }
    }

    class Provider : RemoteApiProvider {
        override fun RemoteApiProvider.Sink.remoteApis() {
            remoteApi(ManagedExcludesApi._generated_RemoteApiDescriptor) {
                ManagedExcludesApiImpl()
            }
        }
    }
}
