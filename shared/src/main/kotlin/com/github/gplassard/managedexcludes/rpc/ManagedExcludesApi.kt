package com.github.gplassard.managedexcludes.rpc

import fleet.rpc.RemoteApi
import fleet.rpc.Rpc

@Rpc
interface ManagedExcludesApi : RemoteApi<Unit> {

    // Queries (frontend -> backend)
    suspend fun getExcludedPathsCount(projectPath: String): Int
    suspend fun isTrackedBazelProject(projectPath: String, relativePath: String): Boolean
    suspend fun isExcludedBazelWorkspace(projectPath: String, relativePath: String): Boolean

    // Mutations (frontend -> backend)
    suspend fun trackBazelProject(projectPath: String, relativePath: String)
    suspend fun untrackBazelProject(projectPath: String, relativePath: String)
    suspend fun excludeBazelWorkspace(projectPath: String, relativePath: String)
    suspend fun unexcludeBazelWorkspace(projectPath: String, relativePath: String)
    suspend fun refreshAll(projectPath: String, virtualFilePath: String)

    // Debug info
    suspend fun getDebugInfo(projectPath: String): DebugInfoDto
}
