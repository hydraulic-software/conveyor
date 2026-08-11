import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
    kotlin("jvm") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.0"
    // Better test output.
    id("com.adarshr.test-logger") version "3.0.0"
}

group = "dev.hydraulic"
version = "2.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://maven.hq.hydraulic.software")
    }
}

tasks {
    shadowJar {
        dependsOn(jar)
        archiveClassifier.set("")
        mergeServiceFiles()
        exclude("kotlin/**")
    }
}

dependencies {
    implementation("dev.hydraulic:dev.hydraulic.types:1.0") {
        because("Machine, OperatingSystem, CPUArchitecture types.")
    }
    compileOnly("org.jetbrains.compose:compose-gradle-plugin:1.2.0") {
        because("Supporting Jetpack Compose Desktop apps.")
    }
    compileOnly("org.openjfx:javafx-plugin:0.0.14") {
        because("Supporting projects that use the OpenJFX Gradle plugin.")
    }
    testImplementation(gradleTestKit())
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1")
    testImplementation("com.typesafe:config:1.4.2")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    testlogger {
        setTheme("mocha-parallel")
        showStandardStreams = false
        showFailedStandardStreams = true
        logLevel = LogLevel.QUIET
        showStackTraces = true
        showFullStackTraces = true
        showExceptions = true
    }
}

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
    website.set("https://www.hydraulic.dev")
    vcsUrl.set("https://github.com/hydraulic-software/conveyor")

    plugins {
        create("conveyorPlugin") {
            id = "dev.hydraulic.conveyor"
            displayName = "Conveyor Gradle Plugin"
            description = "Generates snippets of configuration for the Conveyor packaging tool."
            implementationClass = "hydraulic.conveyor.gradle.ConveyorGradlePlugin"
            tags = listOf("conveyor", "packaging", "hydraulic", "deb", "mac", "dmg", "msi", "msix")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "localPlugin"
            url = uri(rootProject.projectDir.resolve("build/repo").toURI())
        }
    }
}
