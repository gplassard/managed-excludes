package com.github.gplassard.managedexcludes.starter

import com.intellij.ide.starter.ide.IdeProductProvider
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.project.GitHubProject
import com.intellij.ide.starter.runner.Starter
import com.intellij.tools.ide.performanceTesting.commands.CommandChain
import com.intellij.tools.ide.performanceTesting.commands.exitApp
import com.intellij.util.io.createParentDirectories
import org.junit.Test
import kotlin.io.path.div

class SampleStarterTest {

    @Test
    fun openBazelProject() {
        val testCase = TestCase(IdeProductProvider.IC, GitHubProject.fromGithub(
            branchName = "master",
            repoRelativeUrl = "https://github.com/bazelbuild/bazel.git"
        )).useRelease("2024.1")
        val context = Starter.newContext("openBazelProject", testCase).also {
            it.pluginConfigurator.installPluginFromPluginManager("com.google.idea.bazel.ijwb", "2024.04.09.0.1-api-version-241")
            (it.resolvedProjectHome / "tools"/ "intellij" / ".managed.bazelproject").createParentDirectories().toFile().writeText("""
targets:
  //examples:srcs

directories:
  .

    """)
        }

        val results = context.runIDE(commands = CommandChain().exitApp())
    }
}
