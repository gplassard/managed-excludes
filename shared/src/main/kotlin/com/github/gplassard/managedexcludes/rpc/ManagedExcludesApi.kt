package com.github.gplassard.managedexcludes.rpc

import com.intellij.platform.project.ProjectId
import com.intellij.platform.rpc.RemoteApiProviderService
import fleet.rpc.RemoteApi
import fleet.rpc.Rpc
import fleet.rpc.remoteApiDescriptor

@Rpc
interface ManagedExcludesApi : RemoteApi<Unit> {

    companion object {
        suspend fun getInstance(): ManagedExcludesApi {
            return RemoteApiProviderService.resolve(remoteApiDescriptor<ManagedExcludesApi>())
        }
    }

    // Queries (frontend -> backend)
    suspend fun getExcludedPathsCount(projectId: ProjectId): Int
    suspend fun isTrackedBazelProject(projectId: ProjectId, filePath: String): Boolean
    suspend fun isExcludedBazelWorkspace(projectId: ProjectId, filePath: String): Boolean

    // Mutations (frontend -> backend)
    suspend fun trackBazelProject(projectId: ProjectId, filePath: String)
    suspend fun untrackBazelProject(projectId: ProjectId, filePath: String)
    suspend fun excludeBazelWorkspace(projectId: ProjectId, filePath: String)
    suspend fun unexcludeBazelWorkspace(projectId: ProjectId, filePath: String)
    suspend fun refreshAll(projectId: ProjectId, virtualFilePath: String)

    // Debug info
    suspend fun getDebugInfo(projectId: ProjectId): DebugInfoDto
}
