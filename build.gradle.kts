import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.Constants.Constraints
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.aware.SplitModeAware.SplitModeTarget
import java.io.FileWriter
import javax.xml.stream.XMLOutputFactory

plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.qodana)
    alias(libs.plugins.kover)

    alias(libs.plugins.rpc) apply false
    alias(libs.plugins.kotlinSerialization) apply false
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(21)
}

subprojects {
    apply(plugin = "org.jetbrains.intellij.platform.module")
}

allprojects {
    repositories {
        mavenCentral()
        intellijPlatform {
            defaultRepositories()
        }
        maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies/")
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.assertj)

    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        pluginModule(implementation(project(":shared")))
        pluginModule(implementation(project(":frontend")))
        pluginModule(implementation(project(":backend")))

        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    splitMode = true
    splitModeTarget = SplitModeTarget.BOTH

    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")

        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog
        changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        channels = providers.gradleProperty("pluginVersion").map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }
}

changelog {
    groups.empty()
    repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
}

kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    publishPlugin {
        dependsOn(patchChangelog)
    }
}

val runIdeForUiTests by intellijPlatformTesting.runIde.registering {
    task {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf(
                "-Drobot-server.port=8082",
                "-Dide.mac.message.dialogs.as.sheets=false",
                "-Djb.privacy.policy.text=<!--999.999-->",
                "-Djb.consents.confirmation.enabled=false",
            )
        }
    }

    plugins {
        robotServerPlugin(Constraints.LATEST_VERSION)
    }
}


tasks.register("generateUpdatePlugins") {
    val theGroup = providers.gradleProperty("pluginGroup").get()
    val theVersion = providers.gradleProperty("pluginVersion").get()
    val since = providers.gradleProperty("pluginSinceBuild").get()
    val until = providers.gradleProperty("pluginUntilBuild").get()
    val name = providers.gradleProperty("pluginName").get()

    doLast {
        val filename = "updatePlugins.xml"


        FileWriter(filename).use { fileWriter ->
            val outputFactory = XMLOutputFactory.newInstance()

            val xmlWriter = outputFactory.createXMLStreamWriter(fileWriter)

            xmlWriter.writeStartDocument()
            xmlWriter.writeComment("See https://plugins.jetbrains.com/docs/intellij/custom-plugin-repository.html#format-of-updatepluginsxml-file")
            xmlWriter.writeStartElement("plugins")

            xmlWriter.writeStartElement("plugin")
            xmlWriter.writeAttribute("id", theGroup)
            xmlWriter.writeAttribute("url", "https://github.com/gplassard/managed-excludes/releases/download/v$theVersion/managed-excludes-$theVersion-signed.zip")
            xmlWriter.writeAttribute("version", theVersion)


            xmlWriter.writeEmptyElement("idea-version")
            xmlWriter.writeAttribute("since-build", since)
            xmlWriter.writeAttribute("until-build", until)

            xmlWriter.writeStartElement("name")
            xmlWriter.writeCharacters(name)
            xmlWriter.writeEndElement()

            xmlWriter.writeEndElement() // end "plugin"
            xmlWriter.writeEndElement() // end "plugins"
            xmlWriter.writeEndDocument()

            xmlWriter.flush()
            xmlWriter.close()
        }
    }
}
