package com.github.gplassard.managedexcludes.dialog

import com.github.gplassard.managedexcludes.settings.PluginSettingsState
import com.intellij.openapi.project.BaseProjectDirectories.Companion.getBaseDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.project.isDirectoryBased
import com.intellij.ui.dsl.builder.panel
import java.awt.Dimension
import javax.swing.JComponent

class DebugDialog(
    val project: Project,
    private val state: PluginSettingsState,
    private val excludedFromConfig: Set<VirtualFile>,
) : DialogWrapper(project, null, true, IdeModalityType.MODELESS, false) {
    init {
        init()
        title = "Debug Managed Excludes"
    }

    override fun createCenterPanel(): JComponent {
        val trackedProjects = state.trackedBazelProjects
        val resolvedTrackedProjects = state.resolveTrackedBazelProjects(project)
        val excludedPaths = state.excludedPaths
        val resolvedExcludedPaths = state.resolveExcludedPaths(project)
        return panel {
            group("Project") {
                row {
                    label("name: ${project.name}")
                }
                row {
                    label("isDefault: ${project.isDefault}")
                }
                row {
                    label("isDirectoryBased: ${project.isDirectoryBased}")
                }
                row {
                    label("basePath: ${project.basePath}")
                }
                row {
                    label("baseDirectories: ${project.getBaseDirectories().joinToString(", ") { it.path }}")
                }
                row {
                    label("workspaceFile: ${project.workspaceFile?.path}")
                }
                row {
                    label("projectFilePath: ${project.projectFile?.path}")
                }
            }
            group("Tracked Projects:") {
                for (p in trackedProjects) {
                    row {
                        label(p)
                    }
                }
            }
            group("Resolved Tracked Projects:") {
                for (p in resolvedTrackedProjects) {
                    row {
                        label("${p.canonicalPath}")
                    }
                }
            }
            group("Excluded Paths:") {
                for (p in excludedPaths) {
                    row {
                        label(p)
                    }
                }
            }
            group("Resolved Excluded Paths:") {
                for (p in resolvedExcludedPaths) {
                    row {
                        label("${p.canonicalPath}")
                    }
                }
            }
            group("Excluded From Config:") {
                for (p in excludedFromConfig) {
                    row {
                        label("${p.canonicalPath}")
                    }
                }
            }
        }.apply {
            minimumSize = Dimension(400, 300)
        }
    }
}
