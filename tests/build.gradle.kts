import org.jmailen.gradle.kotlinter.tasks.LintTask

@Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage")
plugins {
    id("java")
    kotlin("jvm")
    id("org.jetbrains.kotlinx.dataframe")
    id("io.github.devcrocod.korro") version libs.versions.korro
    id("org.jmailen.kotlinter")
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
    mavenLocal() // for local development
}

dependencies {
    implementation(project(":core"))
    implementation(project(":dataframe-excel"))
    implementation(project(":dataframe-jdbc"))
    implementation(project(":dataframe-arrow"))
    testImplementation(libs.junit)
    testImplementation(libs.kotestAssertions) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
    }
    testImplementation(libs.kotlin.datetimeJvm)
    testImplementation(libs.poi)
    testImplementation(libs.arrow.vector)
}

kotlin.sourceSets {
    main {
        kotlin.srcDir("build/generated/ksp/main/kotlin/")
    }
    test {
        kotlin.srcDir("build/generated/ksp/test/kotlin/")
    }
}

korro {
    docs = fileTree(rootProject.rootDir) {
        include("docs/StardustDocs/topics/read.md")
        include("docs/StardustDocs/topics/write.md")
    }

    samples = fileTree(project.projectDir) {
        include("src/test/kotlin/org/jetbrains/kotlinx/dataframe/samples/*.kt")
        include("src/test/kotlin/org/jetbrains/kotlinx/dataframe/samples/api/*.kt")
    }

    groupSamples {

        beforeSample.set("<tab title=\"NAME\">\n")
        afterSample.set("\n</tab>")

        funSuffix("_properties") {
            replaceText("NAME", "Properties")
        }
        funSuffix("_accessors") {
            replaceText("NAME", "Accessors")
        }
        funSuffix("_strings") {
            replaceText("NAME", "Strings")
        }
        beforeGroup.set("<tabs>\n")
        afterGroup.set("</tabs>")
    }
}

tasks.formatKotlinMain {
    dependsOn("kspKotlin")
}

tasks.formatKotlinTest {
    dependsOn("kspTestKotlin")
}

tasks.lintKotlinMain {
    dependsOn("kspKotlin")
}

tasks.lintKotlinTest {
    dependsOn("kspTestKotlin")
}

tasks.withType<LintTask> {
    exclude("**/*keywords*/**")
    exclude {
        it.name.endsWith(".Generated.kt")
    }
    exclude {
        it.name.endsWith("\$Extensions.kt")
    }
    enabled = true
}

tasks.test {
    jvmArgs = listOf("--add-opens", "java.base/java.nio=ALL-UNNAMED")
}
