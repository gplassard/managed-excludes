plugins {
    id("rpc")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        bundledModule("intellij.platform.frontend")
    }
    compileOnly(libs.kotlin.serialization.core)
    implementation(project(":shared"))
}

kotlin {
    jvmToolchain(25)
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "25"
        targetCompatibility = "25"
    }
}
