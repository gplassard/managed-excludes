package com.github.gplassard.managedexcludes

import com.intellij.driver.client.Remote
import com.intellij.driver.client.service
import com.intellij.driver.client.utility
import com.intellij.driver.sdk.BaseProjectDirectories
import com.intellij.driver.sdk.VirtualFile
import com.intellij.driver.sdk.getContentEntries
import com.intellij.driver.sdk.getModules
import com.intellij.driver.sdk.isPluginLoaded
import com.intellij.driver.sdk.singleProject
import com.intellij.driver.sdk.waitForProjectOpen
import com.intellij.ide.starter.driver.engine.runIdeWithDriver
import com.intellij.ide.starter.models.IdeInfo
import com.intellij.ide.starter.models.TestCase
import com.intellij.ide.starter.plugins.PluginConfigurator
import com.intellij.ide.starter.project.LocalProjectInfo
import com.intellij.ide.starter.project.NoProject
import com.intellij.ide.starter.runner.Starter
import com.intellij.tools.ide.starter.product.idea.ultimate.IdeaUltimate
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * ide-starter integration tests: launch a real IntelliJ IDEA Ultimate instance with the
 * built plugin installed, and drive it through the Driver (JMX/RMI into the IDE process).
 *
 * Relies on the `path.to.build.plugin` system property, set by the `test` task, pointing to the
 * plugin distribution zip produced by `buildPlugin`.
 *
 * See https://blog.jetbrains.com/platform/2025/02/integration-tests-for-plugin-developers-intro-dependencies-and-first-integration-test/
 * and https://plugins.jetbrains.com/docs/intellij/integration-tests-api.html
 *
 * Launching a real IDE requires a graphical session: on a headless machine run the tests under a
 * virtual display, e.g. `xvfb-run ./gradlew test`.
 */
class ManagedExcludesIntegrationTest {

    @Test
    fun `plugin is loaded in the launched IDE`() {
        val context = Starter.newContext(
            testName = "managed-excludes integration test",
            testCase = TestCase(IdeInfo.IdeaUltimate, NoProject).withVersion(IDE_VERSION),
        )

        PluginConfigurator(context).installPluginFromPath(Path.of(System.getProperty("path.to.build.plugin")))

        context.runIdeWithDriver().useDriverAndCloseIde {
            assertTrue(isPluginLoaded(PLUGIN_ID), "Plugin $PLUGIN_ID should be loaded in the launched IDE")
        }
    }

    @Test
    fun `editing a managed-excludes file excludes the listed directory in the opened project`() {
        // Prepare a project on disk: a module rooted at the project dir, plus an excluded dir
        // referenced by a .managed-excludes file that starts out empty of exclusions.
        val projectDir = Files.createTempDirectory("managed-excludes-it")
        try {
            Files.createDirectories(projectDir.resolve("excluded-dir"))
            Files.writeString(projectDir.resolve(".managed-excludes"), "# no exclusions yet\n")
            Files.createDirectories(projectDir.resolve(".idea"))
            Files.writeString(
                projectDir.resolve(".idea/modules.xml"),
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <project version="4">
                  <component name="ProjectModuleManager">
                    <modules>
                      <module fileurl="file://${'$'}PROJECT_DIR${'$'}/test.iml" filepath="file://${'$'}PROJECT_DIR${'$'}/test.iml" />
                    </modules>
                  </component>
                </project>
                """.trimIndent(),
            )
            Files.writeString(
                projectDir.resolve("test.iml"),
                """
                <?xml version="1.0" encoding="UTF-8"?>
                <module type="WEB_MODULE" version="4">
                  <component name="NewModuleRootManager">
                    <content url="file://${'$'}MODULE_DIR${'$'}" />
                  </component>
                </module>
                """.trimIndent(),
            )

            val context = Starter.newContext(
                testName = "managed-excludes exclusion flow",
                testCase = TestCase(IdeInfo.IdeaUltimate, LocalProjectInfo(projectDir)).withVersion(IDE_VERSION),
            )
            PluginConfigurator(context).installPluginFromPath(Path.of(System.getProperty("path.to.build.plugin")))

            // note: useDriverAndCloseIde already runs the block inside driver.withContext { }
            context.runIdeWithDriver().useDriverAndCloseIde {
                waitForProjectOpen()
                val project = singleProject()

                val baseDir = utility<BaseProjectDirectories>().getBaseDirectories(project).first()
                val excludeFile = baseDir.findFileByRelativePath(".managed-excludes")
                // modify the file inside the IDE so the plugin's VFS listener reacts to it
                withWriteAction {
                    utility<VfsUtilCoreStub>().saveText(excludeFile, "excluded-dir\n")
                }

                // the listener applies exclusions asynchronously: poll the plugin state
                val settings = service<PluginSettingsStub>(project)
                val deadline = System.currentTimeMillis() + 60_000
                var excludedPaths: Set<String> = emptySet()
                while (System.currentTimeMillis() < deadline) {
                    excludedPaths = settings.getState().getExcludedPaths()
                    if ("excluded-dir" in excludedPaths) break
                    Thread.sleep(500)
                }
                assertTrue(
                    "excluded-dir" in excludedPaths,
                    "expected 'excluded-dir' in plugin excluded paths, got $excludedPaths",
                )

                val module = getModules(project).first()
                val excludeFolderUrls = getContentEntries(module)
                    .flatMap { entry -> cast(entry, ContentEntryStub::class).getExcludeFolderUrls() }
                assertTrue(
                    excludeFolderUrls.any { it.endsWith("/excluded-dir") },
                    "expected an exclude folder for excluded-dir, got $excludeFolderUrls",
                )
            }
        } finally {
            projectDir.toFile().deleteRecursively()
        }
    }

    private companion object {
        const val PLUGIN_ID = "com.github.gplassard.managedexcludes"

        // must match `platformVersion` in gradle.properties
        const val IDE_VERSION = "2026.3"
    }
}

// Remote stubs: interfaces annotated with @Remote are turned into JMX/RMI proxies that invoke
// the matching class inside the launched IDE process (see "Integration Tests: API Interaction").
// The driver-sdk ships pre-generated stubs for common platform classes; anything else
// (like these) is declared here.
@Remote("com.intellij.openapi.vfs.VfsUtilCore")
private interface VfsUtilCoreStub {
    fun saveText(file: VirtualFile?, text: String)
}

@Remote("com.intellij.openapi.roots.ContentEntry")
private interface ContentEntryStub {
    fun getExcludeFolderUrls(): List<String>
}

// The plugin is modular (split mode): the classes live in the "managed-excludes.backend" content
// module, so the stubs point at that module name rather than the plugin id.
@Remote("com.github.gplassard.managedexcludes.settings.PluginSettings", plugin = "com.github.gplassard.managedexcludes/managed-excludes.backend")
private interface PluginSettingsStub {
    fun getState(): PluginSettingsStateStub
}

@Remote("com.github.gplassard.managedexcludes.settings.PluginSettingsState", plugin = "com.github.gplassard.managedexcludes/managed-excludes.backend")
private interface PluginSettingsStateStub {
    fun getExcludedPaths(): MutableSet<String>
}
