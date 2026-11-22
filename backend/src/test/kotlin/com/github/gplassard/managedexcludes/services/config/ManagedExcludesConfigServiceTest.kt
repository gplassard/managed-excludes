package com.github.gplassard.managedexcludes.services.config

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat

class ManagedExcludesConfigServiceTest : BasePlatformTestCase() {

    fun testResolvesPathsRelativeToExcludeFile() {
        myFixture.addFileToProject("apps/go-app/lib/Main.java", "")
        myFixture.addFileToProject("apps/go-app/app/Main.java", "")
        myFixture.addFileToProject("apps/go-app/other/Main.java", "")
        myFixture.addFileToProject(
            ".managed-excludes",
            """
            # a comment
            apps/go-app/lib

            apps/go-app/app
            apps/go-app/does-not-exist
            """.trimIndent()
        )

        val result = runBlocking { ManagedExcludesConfigService().loadExcludeConfig(project) }

        assertThat(result.map { it.path }).containsExactlyInAnyOrder(
            myFixture.findFileInTempDir("apps/go-app/lib").path,
            myFixture.findFileInTempDir("apps/go-app/app").path,
        )
    }

    fun testResolvesPathsRelativeToNestedExcludeFile() {
        myFixture.addFileToProject("sub/build/output.txt", "")
        myFixture.addFileToProject("sub/.managed-excludes", "build")

        val result = runBlocking { ManagedExcludesConfigService().loadExcludeConfig(project) }

        assertThat(result.map { it.path }).containsExactly(
            myFixture.findFileInTempDir("sub/build").path,
        )
    }

    fun testNoExcludeFileReturnsEmpty() {
        myFixture.addFileToProject("apps/go-app/lib/Main.java", "")

        val result = runBlocking { ManagedExcludesConfigService().loadExcludeConfig(project) }

        assertThat(result).isEmpty()
    }
}
