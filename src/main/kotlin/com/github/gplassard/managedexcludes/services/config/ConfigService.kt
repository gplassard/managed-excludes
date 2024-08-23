package com.github.gplassard.managedexcludes.services.config

import com.github.gplassard.managedexcludes.settings.PluginSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

@Service(Service.Level.PROJECT)
class ConfigService {

    suspend fun loadExcludeConfig(project: Project): Set<VirtualFile> {
        val settings = project.service<PluginSettings>()
        val managedExcludesConfigService = project.service<ManagedExcludesConfigService>()
        val bazelProjectConfigService = project.service<BazelProjectConfigService>()

        val bazelProjectsExcluded = settings.state.resolveTrackedBazelProjects(project)
            .flatMap { bazelProjectConfigService.loadExcludeConfig(project, it) }
            .toSet()
        val managedExcludeExcluded = managedExcludesConfigService.loadExcludeConfig(project)


        return bazelProjectsExcluded
            .plus(managedExcludeExcluded)
            .also { thisLogger().info("Aggregation done, there is a total of ${it.size} distinct paths to exclude") }
    }
}
