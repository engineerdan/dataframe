plugins {
    kotlin("jvm")
    kotlin("libs.publisher")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.kover")
    id("org.jmailen.kotlinter")
    kotlin("jupyter.api")
}

group = "org.jetbrains.kotlinx"

val jupyterApiTCRepo: String by project

repositories {
    mavenLocal()
    mavenCentral()
    maven(jupyterApiTCRepo)
}

dependencies {
    api(project(":core"))

    implementation(libs.kotlinLogging)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinpoet)
    api(libs.swagger) {
        // Fix for Android
        exclude("jakarta.validation")
    }

    testApi(project(":core"))
    testImplementation(libs.junit)
    testImplementation(libs.kotestAssertions) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    }
}

kotlinPublications {
    publication {
        publicationName.set("dataframeOpenApi")
        artifactId.set(project.name)
        description.set("OpenAPI support for Kotlin Dataframe")
        packageName.set(artifactId)
    }
}

kotlin {
    explicitApi()
}
