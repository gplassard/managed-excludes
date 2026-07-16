plugins {
    id("rpc")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerialization)
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
    }
    compileOnly(libs.kotlin.serialization.core)

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
