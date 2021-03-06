package hydraulic.conveyor.gradle

import dev.hydraulic.types.machines.Machine
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.Internal
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

        // TODO(low): Import more stuff, including:
        //
        // - Notarization details?
        // - Icons?
    }

    private val runtimeConfigCopy: Configuration by lazy { project.configurations.getByName("runtimeClasspath").copyRecursive() }

    private fun StringBuilder.importFromJavaFXPlugin(project: Project) {
        val jfxExtension: JavaFXOptions? = project.extensions.findByName("javafx") as? JavaFXOptions
        if (jfxExtension != null) {
            runtimeConfigCopy.dependencies.removeAll { it.group == "org.openjfx" }
            appendLine()
            appendLine("// Config imported from the OpenJFX plugin.")
            appendLine("include required(\"/stdlib/jvm/javafx/from-jmods.conf\")")
            appendLine("javafx.version = ${jfxExtension.version}")
            appendLine("app.jvm.modules = ${'$'}{app.jvm.modules} " + jfxExtension.modules.joinToString(", ", prefix = "[ ", postfix = " ]"))
        }
    }

    private fun StringBuilder.importFromDependencyConfigurations(project: Project) {
        appendLine()
        appendLine("// Inputs from dependency configurations and the JAR task.")
        // Emit app JAR input. jvmJar task is used by Compose Multiplatform projects.
        val jarTask = project.tasks.findByName("jvmJar") ?: project.tasks.getByName("jar")
        appendLine("app.inputs += " + quote(jarTask.outputs.files.singleFile.toString()))

        // Emit cross-platform artifacts.
        val crossPlatformDeps: Set<File> = runtimeConfigCopy.files - machineConfigs[Machine.current()]!!.files
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
        // TODO: Import JVM version and vendor from the toolchain.

        val appExtension = project.extensions.findByName("application") as? JavaApplication
        if (appExtension != null) {
            appendLine()
            appendLine("// Config from the application plugin.")
            appendLine("app.jvm.gui.main-class = ${appExtension.mainClass.get()}")
            val jvmArgs = appExtension.applicationDefaultJvmArgs
            importJVMArgs(jvmArgs, project)
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

            // This strips deps so must run before we calculate dep configurations. Also, we run it early so the import
            // statement is at the top.
            importFromJavaFXPlugin(project)

            val version = project.version.toString()
            if (version.isBlank() || version == "unspecified")
                throw Exception("You must set the 'version' property of the project, because all package formats require one.")
            appendLine("app.version = ${project.version}")

            val group = project.group.toString()
            if (group.isBlank())
                throw Exception("You must set the 'group' property of the project, because some package formats require a reverse DNS name.")
            appendLine("app.rdns-name = ${project.group}.${'$'}{app.fsname}")

            importFromJavaPlugin(project)
            importFromComposePlugin(project)
            importFromDependencyConfigurations(project)
        }
    }
}
