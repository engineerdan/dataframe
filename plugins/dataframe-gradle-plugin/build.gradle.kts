import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.15.0"
    id("org.jmailen.kotlinter")
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    google()
}

group = "org.jetbrains.kotlinx.dataframe"

dependencies {
    api(libs.kotlin.reflect)
    implementation(project(":core"))
    implementation(project(":dataframe-arrow"))
    implementation(project(":dataframe-openapi"))
    implementation(project(":dataframe-excel"))
    implementation(project(":dataframe-jdbc"))
    implementation(kotlin("gradle-plugin-api"))
    implementation(kotlin("gradle-plugin"))
    implementation("com.beust:klaxon:5.5")
    implementation(libs.ksp.gradle)
    implementation(libs.ksp.api)

    testImplementation("junit:junit:4.13.1")
    testImplementation("io.kotest:kotest-assertions-core:4.6.0")
    testImplementation("com.android.tools.build:gradle-api:7.3.1")
    testImplementation("com.android.tools.build:gradle:7.3.1")
    testImplementation("io.ktor:ktor-server-netty:1.6.7")
    testImplementation(libs.h2db)
    testImplementation(gradleApi())
}

tasks.withType<ProcessResources> {
    filesMatching("**/plugin.properties") {
        filter {
            it.replace("%PREPROCESSOR_VERSION%", "$version")
        }
    }
}

tasks.withType<ProcessResources> {
    filesMatching("**/df.properties") {
        filter {
            it.replace(
                "%DATAFRAME_JAR%",
                project(":core").configurations.getByName("instrumentedJars").artifacts.single().file.absolutePath.replace(
                    File.separatorChar,
                    '/'
                )
            )
        }
    }
}

gradlePlugin {
    plugins {
        create("schemaGeneratorPlugin") {
            id = "org.jetbrains.kotlinx.dataframe"
            implementationClass = "org.jetbrains.dataframe.gradle.ConvenienceSchemaGeneratorPlugin"
        }
        create("deprecatedSchemaGeneratorPlugin") {
            id = "org.jetbrains.kotlin.plugin.dataframe"
            implementationClass = "org.jetbrains.dataframe.gradle.DeprecatingSchemaGeneratorPlugin"
        }
    }
}

pluginBundle {
    // These settings are set for the whole plugin bundle
    website = "https://github.com/Kotlin/dataframe"
    vcsUrl = "https://github.com/Kotlin/dataframe"

    (plugins) {
        "schemaGeneratorPlugin" {
            // id is captured from java-gradle-plugin configuration
            displayName = "Kotlin Dataframe gradle plugin"
            description = "Gradle plugin providing task for inferring data schemas from your CSV or JSON data"
            tags = listOf("dataframe", "kotlin")
        }
        "deprecatedSchemaGeneratorPlugin" {
            // id is captured from java-gradle-plugin configuration
            displayName = "Kotlin Dataframe gradle plugin"
            description =
                "The plugin was moved to 'org.jetbrains.kotlinx.dataframe'. Gradle plugin providing task for inferring data schemas from your CSV or JSON data"
            tags = listOf("dataframe", "kotlin")
        }
    }

    mavenCoordinates {
        groupId = project.group.toString()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<JavaCompile>().all {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

sourceSets {
    val main by getting
    val test by getting
    val testRuntimeClasspath by configurations
    create("integrationTest") {
        kotlin.srcDir("src/integrationTest/kotlin")
        compileClasspath += main.output + test.output + testRuntimeClasspath
        runtimeClasspath += output + compileClasspath + test.runtimeClasspath
    }
}


val integrationTestConfiguration by configurations.creating {
    extendsFrom(configurations.testImplementation.get())
}

val integrationTestTask = task<Test>("integrationTest") {
    dependsOn(":plugins:symbol-processor:publishToMavenLocal")
    dependsOn(":dataframe-arrow:publishToMavenLocal")
    dependsOn(":dataframe-excel:publishToMavenLocal")
    dependsOn(":dataframe-jdbc:publishToMavenLocal")
    dependsOn(":dataframe-openapi:publishToMavenLocal")
    dependsOn(":publishApiPublicationToMavenLocal")
    dependsOn(":dataframe-arrow:publishDataframeArrowPublicationToMavenLocal")
    dependsOn(":dataframe-excel:publishDataframeExcelPublicationToMavenLocal")
    dependsOn(":dataframe-jdbc:publishDataframeJDBCPublicationToMavenLocal")
    dependsOn(":dataframe-openapi:publishDataframeOpenApiPublicationToMavenLocal")
    dependsOn(":plugins:symbol-processor:publishMavenPublicationToMavenLocal")
    dependsOn(":core:publishCorePublicationToMavenLocal")
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter("test")
}

tasks.check { dependsOn(integrationTestTask) }
