package com.github.gplassard.managedexcludes.dialog

import com.github.gplassard.managedexcludes.rpc.DebugInfoDto
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import java.awt.Dimension
import javax.swing.JComponent

class DebugDialog(
    project: Project,
    private val debugInfo: DebugInfoDto,
) : DialogWrapper(project, null, true, IdeModalityType.MODELESS, false) {
    init {
        init()
        title = "Debug Managed Excludes"
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            group("Project") {
                row {
                    label("name: ${debugInfo.projectName}")
                }
                row {
                    label("isDefault: ${debugInfo.isDefault}")
                }
                row {
                    label("isDirectoryBased: ${debugInfo.isDirectoryBased}")
                }
                row {
                    label("basePath: ${debugInfo.projectBasePath}")
                }
                row {
                    label("baseDirectories: ${debugInfo.baseDirectories.joinToString(", ")}")
                }
                row {
                    label("workspaceFile: ${debugInfo.workspaceFile}")
                }
                row {
                    label("projectFilePath: ${debugInfo.projectFilePath}")
                }
            }
            group("Tracked Projects:") {
                for (p in debugInfo.trackedBazelProjects) {
                    row {
                        label(p)
                    }
                }
            }
            group("Resolved Tracked Projects:") {
                for (p in debugInfo.resolvedTrackedProjects) {
                    row {
                        label(p)
                    }
                }
            }
            group("Excluded Bazel Workspaces:") {
                for (p in debugInfo.excludedBazelWorkspaces) {
                    row {
                        label(p)
                    }
                }
            }
            group("Resolved Excluded Bazel Workspaces:") {
                for (p in debugInfo.resolvedExcludedWorkspaces) {
                    row {
                        label(p)
                    }
                }
            }
            group("Excluded Paths:") {
                for (p in debugInfo.excludedPaths) {
                    row {
                        label(p)
                    }
                }
            }
            group("Resolved Excluded Paths:") {
                for (p in debugInfo.resolvedExcludedPaths) {
                    row {
                        label(p)
                    }
                }
            }
            group("Excluded From Config:") {
                for (p in debugInfo.excludedFromConfig) {
                    row {
                        label(p)
                    }
                }
            }
        }.apply {
            minimumSize = Dimension(400, 300)
        }
    }
}
