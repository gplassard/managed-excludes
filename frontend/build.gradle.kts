import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("rpc")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        bundledModule("intellij.platform.frontend")
        bundledModule("intellij.platform.rpc")
        testFramework(TestFrameworkType.Platform)
    }
    compileOnly(libs.kotlin.serialization.core)
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
