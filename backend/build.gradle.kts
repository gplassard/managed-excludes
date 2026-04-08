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
    jvmToolchain(21)
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}
