import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.tooling.core.closure
import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.developer
import org.jetbrains.kotlinx.publisher.githubRepo
import org.jmailen.gradle.kotlinter.KotlinterExtension

@Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")
plugins {
    kotlin("jvm") version libs.versions.kotlin
    kotlin("libs.publisher") version libs.versions.libsPublisher
    kotlin("plugin.serialization") version libs.versions.kotlin
    id("org.jetbrains.kotlinx.dataframe") version libs.versions.dataframe apply false
    kotlin("jupyter.api") version libs.versions.kotlinJupyter apply false

    id("org.jetbrains.dokka") version libs.versions.dokka
    id("org.jetbrains.kotlinx.kover") version libs.versions.kover
    id("org.jmailen.kotlinter") version libs.versions.ktlint
    id("nl.jolanrensen.docProcessor") version libs.versions.docProcessor apply false
    id("xyz.ronella.simple-git") version libs.versions.simpleGit apply false
}

val jupyterApiTCRepo: String by project
val projectName: String by project

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    maven(jupyterApiTCRepo)
}

configurations {
    testImplementation.get().extendsFrom(compileOnly.get())
}

dependencies {
    api(project(":core"))
    api(project(":dataframe-arrow"))
    api(project(":dataframe-excel"))
    api(project(":dataframe-openapi"))
    api(project(":dataframe-jdbc"))
}

allprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    // Attempts to configure kotlinter for each sub-project that uses the plugin
    afterEvaluate {
        try {
            kotlinter {
                ignoreFailures = false
                reporters = arrayOf("checkstyle", "plain")
                experimentalRules = true
                disabledRules = arrayOf(
                    "no-wildcard-imports",
                    "experimental:spacing-between-declarations-with-annotations",
                    "experimental:enum-entry-name-case",
                    "experimental:argument-list-wrapping",
                    "experimental:annotation",
                    "max-line-length",
                    "filename",
                    "comment-spacing",
                    "curly-spacing",
                    "experimental:annotation-spacing"
                )
            }
        } catch (_: UnknownDomainObjectException) {
            logger.warn("Could not set kotlinter config on :${this.name}")
        }
    }
}

koverMerged {
    enable()
    filters {
        projects {
            excludes += listOf(
                ":examples:idea-examples:youtube",
                ":examples:idea-examples:titanic",
                ":examples:idea-examples:movies",
                ":examples:idea-examples",
                ":examples",
                ":plugins",
                ":plugins:dataframe-gradle-plugin",
                ":plugins:symbol-processor",
                ":plugins:dataframe-gradle-plugin",
            )
        }
    }
}

group = "org.jetbrains.kotlinx"

fun detectVersion(): String {
    val buildNumber = rootProject.findProperty("build.number") as String?
    val versionProp = property("version") as String
    return if (hasProperty("release")) {
        versionProp
    } else if (buildNumber != null) {
        if (rootProject.findProperty("build.number.detection") == "true") {
            "$versionProp-dev-$buildNumber"
        } else {
            error("use build.number + build.number.detection = true or release build")
        }
    } else {
        "$versionProp-dev"
    }
}

val detectVersionForTC by tasks.registering {
    doLast {
        println("##teamcity[buildNumber '$version']")
    }
}

version = detectVersion()
println("Current DataFrame version: $version")

subprojects {
    this.version = rootProject.version
}

kotlinPublications {
    fairDokkaJars.set(false)

    sonatypeSettings(
        project.findProperty("kds.sonatype.user") as String?,
        project.findProperty("kds.sonatype.password") as String?,
        "dataframe project, v. ${project.version}"
    )

    signingCredentials(
        project.findProperty("kds.sign.key.id") as String?,
        project.findProperty("kds.sign.key.private") as String?,
        project.findProperty("kds.sign.key.passphrase") as String?
    )

    pom {
        githubRepo("Kotlin", "dataframe")
        inceptionYear.set("2021")
        licenses {
            apache2()
        }
        developers {
            developer("nikitinas", "Anatoly Nikitin", "Anatoly.Nikitin@jetbrains.com")
        }
    }

    publication {
        publicationName.set("api")
        artifactId.set(projectName)
        description.set("Data processing in Kotlin")
        packageName.set(artifactId)
    }

    localRepositories {
        maven {
            url = project.file(File(buildDir, "maven")).toURI()
        }
    }
}
