import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("rpc")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        bundledModule("intellij.platform.kernel.backend")
        bundledModule("intellij.platform.rpc.backend")
        bundledModule("intellij.platform.rpc")
        bundledModule("intellij.platform.backend")
        bundledModule("intellij.platform.eel.provider")
        testFramework(TestFrameworkType.Platform)
    }
    implementation(project(":shared"))

    testImplementation(libs.junit)
    testImplementation(libs.assertj)
}

kotlin {
    jvmToolchain(providers.gradleProperty("javaVersion").get().toInt())
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = providers.gradleProperty("javaVersion").get()
        targetCompatibility = providers.gradleProperty("javaVersion").get()
    }
}
