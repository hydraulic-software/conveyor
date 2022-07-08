# SWT

SWT is a small JVM UI toolkit that maps directly to the operating system's native widgets. 

Here's a Gradle config (in Kotlin syntax) that uses the Conveyor plugin and sets up dependencies as appropriate. This sample also demonstrates how to write a bit of custom Gradle code to minimize repetition when specifying platform specific dependencies:

```kotlin
plugins {
	`java-library`
    application
    id("dev.hydraulic.conveyor") version "1.0.1"
}

repositories {
    mavenCentral()
}

val swt_version = "3.119.0"

// Add the platform specific SWT dependency to the platform specific dependency configuration.
fun DependencyHandlerScope.swt(platformConveyor: String, platformSwt: String) {
    add(platformConveyor, "org.eclipse.platform:org.eclipse.swt.$platformSwt:$swt_version") {
        // We don't need the empty grouping artifact and it gets in the way.
        exclude("org.eclipse.platform", "org.eclipse.swt.\${osgi.platform}")
    }
}

dependencies {
    swt("macAmd64", "cocoa.macosx.x86_64")
    swt("macAarch64", "cocoa.macosx.aarch64")
    swt("windowsAmd64", "win32.win32.x86_64")
    swt("linuxAmd64", "gtk.linux.x86_64")
}

application {
    mainClass.set("yourMainClass")
    // SWT needs this JVM flag.
    applicationDefaultJvmArgs = listOf("-XstartOnFirstThread")
}

```
