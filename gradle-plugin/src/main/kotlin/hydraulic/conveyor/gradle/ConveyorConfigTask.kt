package hydraulic.conveyor.gradle

import dev.hydraulic.types.machines.Machine
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Internal
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.jvm.toolchain.JvmVendorSpec.*
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.openjfx.gradle.JavaFXOptions

/**
 * A base class for tasks that work with generated Conveyor configuration.
 */
abstract class ConveyorConfigTask : DefaultTask() {
    @get:Internal
    internal lateinit var machineConfigs: Map<Machine, Configuration>

    private val hoconForbiddenChars = setOf(
        '$', '"', '{', '}', '[', ']', ':', '=', ',', '+', '#', '`', '^', '?', '!', '@', '*', '&', '\\'
    )

    private fun hasHoconForbiddenChars(str: String) = str.any { it in hoconForbiddenChars }

    private fun quote(str: Any) = str.toString().let {
        if (hasHoconForbiddenChars(it))
            "\"" + it.replace("\\", "\\\\") + "\""
        else
            it
    }

    private fun StringBuilder.importFromComposePlugin(project: Project) {
        try {
            val composeExt: ComposeExtension = project.extensions.findByName("compose") as? ComposeExtension ?: return
            val desktopExt: DesktopExtension = composeExt.extensions.findByName("desktop") as? DesktopExtension ?: return
            val app = desktopExt.application
            appendLine()
            appendLine("// Config from the Jetpack Compose Desktop plugin.")
            app.mainClass?.let {
                appendLine("app.jvm.gui.main-class = " + quote(it))
                appendLine("app.linux.desktop-file.\"Desktop Entry\".StartupWMClass = " + quote(it.replace('.', '-')))
            }

            importJVMArgs(app.jvmArgs, project)

            app.nativeDistributions.packageName?.let { appendLine("app.fsname = " + quote(it)) }
            app.nativeDistributions.description?.let { appendLine("app.description = " + quote(it)) }
            app.nativeDistributions.vendor?.let { appendLine("app.vendor = " + quote(it)) }
            app.nativeDistributions.appResourcesRootDir.let {
                if (it.isPresent) {
                    appendLine("app.jvm.system-properties.\"compose.application.resources.dir\" = ${quote("&&")}")
                    for ((key, source) in mapOf(
                        "inputs" to "common",
                        "mac.inputs" to "macos",
                        "windows.inputs" to "windows",
                        "linux.inputs" to "linux",
                        "mac.amd64.inputs" to "macos-x64",
                        "mac.aarch64.inputs" to "macos-arm64",
                        "windows.amd64.inputs" to "windows-x64",
                        "windows.aarch64.inputs" to "windows-arm64",
                        "linux.amd64.inputs" to "linux-x64",
                        "linux.aarch64.inputs" to "linux-arm64",
                    )) {
                        val dir = it.dir(source).get()
                        if (dir.asFile.exists()) {
                            appendLine("app.$key += ${quote(dir)}")
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            val extra = if (e is NoSuchMethodError && "dsl.JvmApplication" in e.message!!) {
                "If you're using Compose 1.1 or below, try upgrading to Compose 1.2 or higher, or using version 1.0.1 of the Conveyor Gradle plugin."
            } else {
                ""
            }
            throw Exception("Could not read Jetpack Compose configuration, likely plugin version incompatibility? $extra".trim(), e)
        }

        // TODO(low): Import more stuff, including:
        //
        // - Notarization details?
        // - Icons?
    }

    // Can't use type JavaFXOptions here because Gradle can't decorate the class.
    private val javafxExtension: Any? by lazy {
        project.extensions.findByName("javafx")
    }

    private fun StringBuilder.importFromJavaFXPlugin() {
        try {
            val jfxExtension = javafxExtension as? JavaFXOptions
            if (jfxExtension != null) {
                appendLine()
                appendLine("// Config from the OpenJFX plugin.")
                appendLine("include required(\"/stdlib/jvm/javafx/from-jmods.conf\")")
                appendLine("javafx.version = ${jfxExtension.version}")
                appendLine(
                    "app.jvm.modules = ${'$'}{app.jvm.modules} " + jfxExtension.modules.joinToString(
                        ", ",
                        prefix = "[ ",
                        postfix = " ]"
                    )
                )
            }
        } catch (e: Throwable) {
            throw Exception("Could not read JavaFX configuration, possible version incompatibility?", e)
        }
    }

    private fun StringBuilder.importFromDependencyConfigurations(project: Project) {
        appendLine()
        appendLine("// Inputs from dependency configurations and the JAR task.")

        // Emit app JAR input. jvmJar task is used by Compose Multiplatform projects.
        val jarTask = project.tasks.findByName("jvmJar") ?: project.tasks.getByName("jar")
        appendLine("app.inputs += " + quote(jarTask.outputs.files.singleFile.toString()))

        val runtimeClasspath =
            project.configurations.findByName("runtimeClasspath") ?: project.configurations.getByName("jvmRuntimeClasspath")

        // We need to resolve the runtimeClasspath before copying out the dependencies because the at the start of the resolution process
        // the set of dependencies can be changed via [Configuration.defaultDependencies] or [Configuration.withDependencies].
        runtimeClasspath.resolve()

        val currentMachineConfig = machineConfigs[Machine.current()]!!

        // Exclude current machine specific config from the runtime classpath, to retain only the dependencies that should go
        // to all platforms.
        val currentMachineDependencies = currentMachineConfig.dependencies
        val commonClasspath = runtimeClasspath.copyRecursive {
            // We need to filter the runtimeClasspath here, before making the recursive copy, otherwise the dependencies from the current
            // machine config won't match.
            it !in currentMachineDependencies && (javafxExtension == null || it.group != "org.openjfx")
        }

        // Make machine configs extend the common classpath so the dependencies are resolved correctly.
        val expandedConfigs = machineConfigs.mapNotNull { (machine, config) ->
            if (config.isEmpty) null else {
                machine to config.copy().extendsFrom(commonClasspath).copyRecursive()
            }
        }.toMap().toSortedMap()

        // If there are any expanded configs, use the intersection as the common files. Otherwise, just use all files from the common
        // classpath.
        // If there are any expanded configs, we can't really use the common classpath, because it might need the platform specific
        // dependencies to properly resolve versions.
        val commonFiles = if (expandedConfigs.isNotEmpty()) expandedConfigs.values.map { it.files }
            .reduce { a, b -> a.intersect(b) } else commonClasspath.files

        if (commonFiles.isNotEmpty()) {
            appendLine("app.inputs = ${'$'}{app.inputs} [")
            for (entry in commonFiles.sorted())
                appendLine("    " + quote(entry.toString()))
            appendLine("]")
        }

        // Emit platform specific artifacts into the right config sections.
        for ((platform, config) in expandedConfigs) {
            val files = config.files - commonFiles
            if (files.isEmpty()) continue
            appendLine()
            appendLine("app.$platform.inputs = ${'$'}{app.$platform.inputs} [")
            for (entry in files.sorted())
                appendLine("    " + quote(entry.toString()))
            appendLine("]")
        }
    }

    private fun StringBuilder.importFromJavaPlugin(project: Project) {
        val appExtension = project.extensions.findByName("application") as? JavaApplication
        if (appExtension != null) {
            appendLine()
            appendLine("// Config from the application plugin.")
            val mainClass = quote(appExtension.mainClass.get())
            appendLine("app.jvm.gui.main-class = $mainClass")
            appendLine("app.linux.desktop-file.\"Desktop Entry\".StartupWMClass = $mainClass")
            val jvmArgs = appExtension.applicationDefaultJvmArgs
            importJVMArgs(jvmArgs, project)
            appendLine()
        }

        val javaExtension = project.extensions.findByName("java") as? JavaPluginExtension
        if (javaExtension != null) {
            val jvmVersion = javaExtension.toolchain.languageVersion.orNull
            val vendor: JvmVendorSpec = javaExtension.toolchain.vendor.orNull ?: kotlin.runCatching { ADOPTIUM }.getOrNull() ?: ADOPTOPENJDK
            if (jvmVersion == null) {
                appendLine("// Java toolchain doesn't specify a version. Not importing a JDK.")
            } else {
                var conveyorVendor = if (vendor.toString() == "any") "openjdk" else when (vendor) {
                    AMAZON -> "amazon"
                    AZUL -> "azul"
                    ORACLE -> "openjdk"
                    else -> null
                }
                try {
                    conveyorVendor = when (vendor) {
                        MICROSOFT -> "microsoft"
                        ADOPTIUM -> "eclipse"
                        GRAAL_VM -> "graalvm"
                        else -> conveyorVendor
                    }
                } catch (e: NoSuchFieldError) {
                    // Ignore - added in Gradle 7.4
                }
                if (conveyorVendor != null) {
                    appendLine("// Config from the Java plugin.")
                    appendLine("include required(\"/stdlib/jdk/$jvmVersion/${conveyorVendor}.conf\")")
                } else {
                    appendLine("// Gradle build requests a JVM from $vendor but this vendor isn't known to Conveyor at this time.")
                    appendLine("// You can still use it, you'll just have to add JDK inputs that define where to download or find it.")
                    appendLine("//")
                    appendLine("// Please see https://conveyor.hydraulic.dev/latest/configs/jvm/#importing-a-jvmjdk for assistance.")
                    appendLine("internal.conveyor.warnings += \"unknown-jdk-vendor:$vendor\"")
                }
            }
        }
    }

    private fun StringBuilder.importJVMArgs(jvmArgs: MutableIterable<String>,
                                            project: Project) {
        val argsNotPointingIntoTree = jvmArgs
            .toList()
            .filterNot { project.rootProject.rootDir.toString() in it }
        if (argsNotPointingIntoTree.isNotEmpty()) {
            appendLine("app.jvm.options = ${'$'}{app.jvm.options} " +
                argsNotPointingIntoTree.joinToString(", ", "[ ", " ]") { quote(it) })
        }
    }

    protected fun generate(): String {
        return buildString {
            appendLine("// Generated by the Conveyor Gradle plugin.")
            appendLine()
            appendLine("// Gradle project data. The build directory is useful for importing built files.")
            appendLine("gradle.build-dir = ${quote(project.buildDir)}")
            appendLine("gradle.project-name = ${quote(project.name)}")
            appendLine("app.fsname = ${quote(project.name.lowercase())}")

            val version = project.version.toString()
            if (version.isBlank() || version == "unspecified")
                throw Exception("You must set the 'version' property of the project, because all package formats require one.")
            appendLine("app.version = ${project.version}")

            val group = project.group.toString()
            if (group.isBlank())
                throw Exception("You must set the 'group' property of the project, because some package formats require a reverse DNS name.")
            appendLine("app.rdns-name = ${project.group}.${'$'}{app.fsname}")

            // This strips deps so must run before we calculate dep configurations.
            importFromJavaFXPlugin()
            importFromJavaPlugin(project)
            importFromComposePlugin(project)
            importFromDependencyConfigurations(project)
        }
    }
}
