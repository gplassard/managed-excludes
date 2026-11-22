package com.github.gplassard.managedexcludes.services.config

import com.github.gplassard.managedexcludes.settings.PluginSettings
import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat

class ConfigServiceTest : BasePlatformTestCase() {

    fun testAggregatesManagedExcludesBazelProjectsAndWorkspaces() {
        myFixture.addFileToProject("managed/lib/Main.java", "")
        myFixture.addFileToProject(".managed-excludes", "managed/lib")

        myFixture.addFileToProject("bazelproj/excluded/Main.java", "")
        val bazelProject = myFixture.addFileToProject(
            "project.bazelproject",
            """
            directories:
              -bazelproj/excluded
            """.trimIndent()
        )
        project.service<PluginSettings>().state.addTrackedBazelProject(project, bazelProject.virtualFile)

        myFixture.addFileToProject("WORKSPACE.bazel", """workspace(name = "ws")""")
        myFixture.addFileToProject("bazel-ws/placeholder", "")

        val result = runBlocking { project.service<ConfigService>().loadExcludeConfig(project) }

        assertThat(result.map { it.path }).containsExactlyInAnyOrder(
            myFixture.findFileInTempDir("managed/lib").path,
            myFixture.findFileInTempDir("bazelproj/excluded").path,
            myFixture.findFileInTempDir("bazel-ws").path,
        )
    }

    fun testUntrackedBazelProjectIsIgnored() {
        // unique names: the light project (and its PluginSettings state) is reused across tests,
        // so a name already tracked by another test would leak into this one
        myFixture.addFileToProject("untracked-bazel/excluded/Main.java", "")
        myFixture.addFileToProject(
            "untracked.bazelproject",
            """
            directories:
              -untracked-bazel/excluded
            """.trimIndent()
        )

        val result = runBlocking { project.service<ConfigService>().loadExcludeConfig(project) }

        assertThat(result).isEmpty()
    }

    fun testExcludedWorkspaceIsIgnored() {
        myFixture.addFileToProject("WORKSPACE", """workspace(name = "ws")""")
        myFixture.addFileToProject("bazel-ws/placeholder", "")
        val workspace = myFixture.findFileInTempDir("WORKSPACE")
        project.service<PluginSettings>().state.addExcludedBazelWorkspace(project, workspace)

        val result = runBlocking { project.service<ConfigService>().loadExcludeConfig(project) }

        assertThat(result).isEmpty()
    }
}
