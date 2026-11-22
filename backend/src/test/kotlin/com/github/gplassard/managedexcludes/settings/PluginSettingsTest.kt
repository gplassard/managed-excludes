package com.github.gplassard.managedexcludes.settings

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.assertj.core.api.Assertions.assertThat

class PluginSettingsTest : BasePlatformTestCase() {

    // Note: the light project (and its PluginSettings state) is reused across tests, so each test
    // uses unique file names to stay independent from state changes made by other tests.

    fun testTrackedBazelProjectRoundTrip() {
        val state = project.service<PluginSettings>().state
        val file = myFixture.addFileToProject("settings-tracked.bazelproject", "").virtualFile

        assertThat(state.isTrackedBazelProject(project, file)).isFalse()

        state.addTrackedBazelProject(project, file)
        assertThat(state.isTrackedBazelProject(project, file)).isTrue()
        assertThat(state.trackedBazelProjects).contains("settings-tracked.bazelproject")
        assertThat(state.resolveTrackedBazelProjects(project).map { it.path }).contains(file.path)

        state.removeTrackedBazelProject(project, file)
        assertThat(state.isTrackedBazelProject(project, file)).isFalse()
        assertThat(state.resolveTrackedBazelProjects(project).map { it.path }).doesNotContain(file.path)
    }

    fun testExcludedBazelWorkspaceRoundTrip() {
        val state = project.service<PluginSettings>().state
        val workspace = myFixture.addFileToProject("settings-excluded-workspace", "").virtualFile

        assertThat(state.isExcludedBazelWorkspace(project, workspace)).isFalse()

        state.addExcludedBazelWorkspace(project, workspace)
        assertThat(state.isExcludedBazelWorkspace(project, workspace)).isTrue()
        assertThat(state.excludedBazelWorkspaces).contains("settings-excluded-workspace")
        assertThat(state.resolveExcludedBazelWorkspaces(project).map { it.path }).contains(workspace.path)

        state.removeExcludedBazelWorkspace(project, workspace)
        assertThat(state.isExcludedBazelWorkspace(project, workspace)).isFalse()
        assertThat(state.resolveExcludedBazelWorkspaces(project).map { it.path }).doesNotContain(workspace.path)
    }

    fun testExcludedPathsRoundTrip() {
        val state = project.service<PluginSettings>().state
        myFixture.addFileToProject("excluded/placeholder", "")
        val dir = myFixture.findFileInTempDir("excluded")

        state.updateExcludedPaths(project, setOf(dir))

        assertThat(state.excludedPaths).containsExactly("excluded")
        assertThat(state.resolveExcludedPaths(project).map { it.path }).containsExactly(dir.path)
    }
}
