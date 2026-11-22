package com.github.gplassard.managedexcludes.services.config

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat

class BazelWorkspacesConfigServiceTest : BasePlatformTestCase() {

    fun testExcludesWorkspaceOutputDirs() {
        myFixture.addFileToProject("WORKSPACE.bazel", """workspace(name = "my_ws")""")
        myFixture.addFileToProject("bazel-bin/placeholder", "")
        myFixture.addFileToProject("bazel-out/placeholder", "")
        myFixture.addFileToProject("bazel-testlogs/placeholder", "")
        myFixture.addFileToProject("bazel-my-ws/placeholder", "")
        myFixture.addFileToProject("src/Main.java", "")

        val result = runBlocking { BazelWorkspacesConfigService().loadExcludeConfig(project, emptySet()) }

        assertThat(result.map { it.name }).containsExactlyInAnyOrder(
            "bazel-bin",
            "bazel-out",
            "bazel-testlogs",
            "bazel-my-ws",
        )
    }

    fun testExcludedWorkspaceIsSkipped() {
        val workspace = myFixture.addFileToProject("WORKSPACE.bazel", """workspace(name = "my_ws")""")
        myFixture.addFileToProject("bazel-bin/placeholder", "")

        val result = runBlocking { BazelWorkspacesConfigService().loadExcludeConfig(project, setOf(workspace.virtualFile)) }

        assertThat(result).isEmpty()
    }

    fun testWorkspaceWithoutNameStillExcludesBaseDirs() {
        myFixture.addFileToProject("WORKSPACE", "some_rule()")
        myFixture.addFileToProject("bazel-out/placeholder", "")

        val result = runBlocking { BazelWorkspacesConfigService().loadExcludeConfig(project, emptySet()) }

        assertThat(result.map { it.name }).containsExactly("bazel-out")
    }
}
