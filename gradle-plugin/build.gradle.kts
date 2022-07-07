plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.20.0"
    kotlin("jvm") version "1.5.30"
}

group = "dev.hydraulic"
version = "1.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven {
        url = uri("https://maven.hq.hydraulic.software")
    }
}

dependencies {
    implementation("dev.hydraulic:dev.hydraulic.types:1.0") {
        because("Machine, OperatingSystem, CPUArchitecture types.")
    }
    compileOnly("org.jetbrains.compose:compose-gradle-plugin:1.1.1") {
        because("Supporting JetPack Compose Desktop apps.")
    }
    compileOnly("org.openjfx:javafx-plugin:0.0.11") {
        because("Supporting projects that use the OpenJFX Gradle plugin.")
    }
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(11))
    }
}

gradlePlugin {
    plugins {
        create("conveyorPlugin") {
            id = "dev.hydraulic.conveyor"
            displayName = "Conveyor Gradle Plugin"
            description = "Generates snippets of configuration for the Conveyor packaging tool."
            implementationClass = "hydraulic.conveyor.gradle.ConveyorGradlePlugin"
        }
    }
}

pluginBundle {
    website = "https://www.hydraulic.dev"
    vcsUrl = "https://github.com/hydraulic-software/conveyor"
    tags = listOf("conveyor", "packaging", "hydraulic", "deb", "mac", "dmg", "msi", "msix")
}

publishing {
    repositories {
        maven {
            name = "localPlugin"
            url = uri(rootProject.projectDir.resolve("build/repo").toURI())
        }
    }
}
