package com.github.gplassard.managedexcludes.intention

import com.intellij.psi.PsiManager
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class DebugIntentionTest : BasePlatformTestCase() {

    fun testAvailableForManagedExcludesFile() {
        assertTrue(DebugIntention().isAvailable(project, null, psiFile(".managed-excludes")))
    }

    fun testAvailableForBazelProjectFile() {
        assertTrue(DebugIntention().isAvailable(project, null, psiFile("project.bazelproject")))
    }

    fun testAvailableForWorkspaceFiles() {
        assertTrue(DebugIntention().isAvailable(project, null, psiFile("WORKSPACE")))
        assertTrue(DebugIntention().isAvailable(project, null, psiFile("WORKSPACE.bazel")))
    }

    fun testNotAvailableForOtherFiles() {
        assertFalse(DebugIntention().isAvailable(project, null, psiFile("README.md")))
    }

    fun testNotAvailableWithoutFile() {
        assertFalse(DebugIntention().isAvailable(project, null, null))
    }

    // addFileToProject + PsiManager instead of myFixture.configureByText: no editor is created,
    // so the test does not need AWT font native libraries (libfreetype) on headless machines
    private fun psiFile(name: String): PsiFile {
        val virtualFile = myFixture.addFileToProject(name, "").virtualFile
        return PsiManager.getInstance(project).findFile(virtualFile)!!
    }
}
