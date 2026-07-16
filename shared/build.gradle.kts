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
    jvmToolchain(25)
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "25"
        targetCompatibility = "25"
    }
}
