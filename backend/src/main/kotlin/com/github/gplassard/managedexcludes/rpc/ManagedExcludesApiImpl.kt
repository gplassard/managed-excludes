package com.github.gplassard.managedexcludes.rpc

import com.github.gplassard.managedexcludes.services.RefreshService
import com.github.gplassard.managedexcludes.services.config.ConfigService
import com.github.gplassard.managedexcludes.settings.PluginSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.BaseProjectDirectories.Companion.getBaseDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.project.ProjectId
import com.intellij.platform.project.findProjectOrNull
import com.intellij.platform.rpc.backend.RemoteApiProvider
import com.intellij.project.isDirectoryBased
import fleet.rpc.remoteApiDescriptor

class ManagedExcludesApiImpl : ManagedExcludesApi {

    private fun findProject(projectId: ProjectId): Project? =
        projectId.findProjectOrNull()

    private fun findFile(filePath: String): VirtualFile? =
        LocalFileSystem.getInstance().findFileByPath(filePath)

    override suspend fun getExcludedPathsCount(projectId: ProjectId): Int {
        val project = findProject(projectId) ?: return 0
        return project.service<PluginSettings>().state.excludedPaths.size
    }

    override suspend fun isTrackedBazelProject(projectId: ProjectId, filePath: String): Boolean {
        val project = findProject(projectId) ?: return false
        val vf = findFile(filePath) ?: return false
        return project.service<PluginSettings>().state.isTrackedBazelProject(project, vf)
    }

    override suspend fun isExcludedBazelWorkspace(projectId: ProjectId, filePath: String): Boolean {
        val project = findProject(projectId) ?: return false
        val vf = findFile(filePath) ?: return false
        return project.service<PluginSettings>().state.isExcludedBazelWorkspace(project, vf)
    }

    override suspend fun trackBazelProject(projectId: ProjectId, filePath: String) {
        val project = findProject(projectId) ?: return
        val vf = findFile(filePath) ?: return
        project.service<PluginSettings>().state.addTrackedBazelProject(project, vf)
    }

    override suspend fun untrackBazelProject(projectId: ProjectId, filePath: String) {
        val project = findProject(projectId) ?: return
        val vf = findFile(filePath) ?: return
        project.service<PluginSettings>().state.removeTrackedBazelProject(project, vf)
    }

    override suspend fun excludeBazelWorkspace(projectId: ProjectId, filePath: String) {
        val project = findProject(projectId) ?: return
        val vf = findFile(filePath) ?: return
        project.service<PluginSettings>().state.addExcludedBazelWorkspace(project, vf)
    }

    override suspend fun unexcludeBazelWorkspace(projectId: ProjectId, filePath: String) {
        val project = findProject(projectId) ?: return
        val vf = findFile(filePath) ?: return
        project.service<PluginSettings>().state.removeExcludedBazelWorkspace(project, vf)
    }

    override suspend fun refreshAll(projectId: ProjectId, virtualFilePath: String) {
        val project = findProject(projectId) ?: return
        val vf = findFile(virtualFilePath) ?: return
        val module = ModuleUtilCore.findModuleForFile(vf, project) ?: return
        val refreshService = project.service<RefreshService>()
        refreshService.refreshAll(module)
    }

    override suspend fun getDebugInfo(projectId: ProjectId): DebugInfoDto {
        val project = findProject(projectId) ?: return DebugInfoDto(
            projectName = "unknown", projectBasePath = "unknown",
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

    class Provider : RemoteApiProvider {
        override fun RemoteApiProvider.Sink.remoteApis() {
            remoteApi(remoteApiDescriptor<ManagedExcludesApi>()) {
                ManagedExcludesApiImpl()
            }
        }
    }
}
