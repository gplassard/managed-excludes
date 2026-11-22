package com.github.gplassard.managedexcludes.services

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat

class ExcludeServiceTest : BasePlatformTestCase() {

    // ExcludeService commits the module root model inside edtWriteAction, which needs the EDT.
    // These tests block the current thread with runBlocking, so they must not run on the EDT
    // (that would deadlock); run them on the test worker thread instead.
    override fun runInDispatchThread(): Boolean = false

    fun testExcludeThenCancelPaths() {
        myFixture.addFileToProject("excluded-dir/placeholder", "")
        val dir = myFixture.findFileInTempDir("excluded-dir")
        val module = myFixture.module
        val service = project.service<ExcludeService>()

        runBlocking { service.excludePaths(module, setOf(dir)) }
        assertThat(excludeFolderUrls(module)).contains(dir.url)

        runBlocking { service.cancelExcludePaths(module, setOf(dir)) }
        assertThat(excludeFolderUrls(module)).doesNotContain(dir.url)
    }

    fun testExcludeOnlyAffectsGivenPaths() {
        myFixture.addFileToProject("excluded-dir/placeholder", "")
        myFixture.addFileToProject("kept-dir/placeholder", "")
        val excludedDir = myFixture.findFileInTempDir("excluded-dir")
        val keptDir = myFixture.findFileInTempDir("kept-dir")
        val module = myFixture.module
        val service = project.service<ExcludeService>()

        runBlocking { service.excludePaths(module, setOf(excludedDir)) }

        assertThat(excludeFolderUrls(module)).contains(excludedDir.url)
        assertThat(excludeFolderUrls(module)).doesNotContain(keptDir.url)
    }

    private fun excludeFolderUrls(module: Module): List<String> =
        ReadAction.compute<List<String>, Nothing> {
            ModuleRootManager.getInstance(module).contentEntries.flatMap { entry -> entry.excludeFolderUrls.toList() }
        }
}
