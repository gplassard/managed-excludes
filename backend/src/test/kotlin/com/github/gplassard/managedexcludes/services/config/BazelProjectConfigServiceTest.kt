package com.github.gplassard.managedexcludes.services.config

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat

class BazelProjectConfigServiceTest : BasePlatformTestCase() {

    fun testResolvesExcludedDirectoriesAgainstBaseDirectories() {
        myFixture.addFileToProject("java/com/app/Main.java", "")
        myFixture.addFileToProject("javatests/com/app/Main.java", "")
        myFixture.addFileToProject("other/Main.java", "")
        val bazelProject = myFixture.addFileToProject(
            "project.bazelproject",
            """
            directories:
              java/com/app
              -javatests/com/app
              -does/not/exist
            something_else:
              -other
            """.trimIndent()
        )

        val result = runBlocking { BazelProjectConfigService().loadExcludeConfig(project, bazelProject.virtualFile) }

        assertThat(result.map { it.path }).containsExactly(
            myFixture.findFileInTempDir("javatests/com/app").path,
        )
    }

    fun testNoExcludedDirectoriesReturnsEmpty() {
        myFixture.addFileToProject("java/com/app/Main.java", "")
        val bazelProject = myFixture.addFileToProject(
            "project.bazelproject",
            """
            directories:
              java/com/app
            """.trimIndent()
        )

        val result = runBlocking { BazelProjectConfigService().loadExcludeConfig(project, bazelProject.virtualFile) }

        assertThat(result).isEmpty()
    }
}
