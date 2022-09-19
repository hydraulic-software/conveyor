package hydraulic.conveyor.gradle

import dev.hydraulic.types.machines.Machine
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Internal
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.openjfx.gradle.JavaFXOptions
import java.io.File

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
            appendLine("app.jvm.gui.main-class = ${app.mainClass}")
            importJVMArgs(app.jvmArgs, project)

            app.nativeDistributions.packageName?.let { appendLine("app.fsname = " + quote(it)) }
            app.nativeDistributions.description?.let { appendLine("app.description = " + quote(it)) }
            app.nativeDistributions.vendor?.let { appendLine("app.vendor = " + quote(it)) }
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

    // This copy of the runtime dependency set ("configuration") will be modified to remove libraries that aren't cross-platform.
    private val crossPlatformRuntimeClasspath: Configuration by lazy {
        project.configurations.getByName("runtimeClasspath").copyRecursive()
    }

    private fun StringBuilder.importFromJavaFXPlugin(project: Project) {
        try {
            val jfxExtension: JavaFXOptions? = project.extensions.findByName("javafx") as? JavaFXOptions
            if (jfxExtension != null) {
                crossPlatformRuntimeClasspath.dependencies.removeAll { it.group == "org.openjfx" }
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

        // Emit cross-platform artifacts.
        val crossPlatformDeps: Set<File> = crossPlatformRuntimeClasspath.files - machineConfigs[Machine.current()]!!.files
        if (crossPlatformDeps.isNotEmpty()) {
            appendLine("app.inputs = ${'$'}{app.inputs} [")
            for (entry in crossPlatformDeps.sorted())
                appendLine("    " + quote(entry.toString()))
            appendLine("]")
        }

        // Emit platform specific artifacts into the right config sections.
        for ((platform, config) in machineConfigs) {
            if (config.isEmpty) continue
            appendLine()
            appendLine("app.$platform.inputs = ${'$'}{app.$platform.inputs} [")
            for (entry in (config - crossPlatformDeps).sorted())
                appendLine("    " + quote(entry.toString()))
            appendLine("]")
        }
    }

    private fun StringBuilder.importFromJavaPlugin(project: Project) {
        val appExtension = project.extensions.findByName("application") as? JavaApplication
        if (appExtension != null) {
            appendLine()
            appendLine("// Config from the application plugin.")
            appendLine("app.jvm.gui.main-class = ${appExtension.mainClass.get()}")
            val jvmArgs = appExtension.applicationDefaultJvmArgs
            importJVMArgs(jvmArgs, project)
        }

        val javaExtension = project.extensions.findByName("java") as? JavaPluginExtension
        if (javaExtension != null) {
            val jvmVersion = javaExtension.toolchain.languageVersion.orNull
            val vendor: JvmVendorSpec = javaExtension.toolchain.vendor.orNull ?: JvmVendorSpec.ADOPTIUM
            if (jvmVersion == null) {
                appendLine()
                appendLine("// Java toolchain doesn't specify a version. Not importing a JDK.")
            } else {
                val conveyorVendor = if (vendor.toString() == "any") "openjdk" else when (vendor) {
                    JvmVendorSpec.ADOPTIUM -> "eclipse"
                    JvmVendorSpec.AMAZON -> "amazon"
                    JvmVendorSpec.AZUL -> "azul"
                    JvmVendorSpec.MICROSOFT -> "microsoft"
                    JvmVendorSpec.ORACLE -> "openjdk"
                    else -> null
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
            importFromJavaFXPlugin(project)
            importFromJavaPlugin(project)
            importFromComposePlugin(project)
            importFromDependencyConfigurations(project)
        }
    }
}
