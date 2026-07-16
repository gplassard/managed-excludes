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
        bundledModule("intellij.platform.backend")
    }
    implementation(project(":shared"))
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
