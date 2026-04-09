package com.github.gplassard.managedexcludes.rpc

import kotlinx.serialization.Serializable

@Serializable
data class DebugInfoDto(
    val projectName: String,
    val projectBasePath: String?,
    val isDefault: Boolean,
    val isDirectoryBased: Boolean,
    val baseDirectories: Set<String>,
    val workspaceFile: String?,
    val projectFilePath: String?,
    val trackedBazelProjects: Set<String>,
    val resolvedTrackedProjects: Set<String>,
    val excludedBazelWorkspaces: Set<String>,
    val resolvedExcludedWorkspaces: Set<String>,
    val excludedPaths: Set<String>,
    val resolvedExcludedPaths: Set<String>,
    val excludedFromConfig: Set<String>,
)
