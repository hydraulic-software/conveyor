package hydraulic.conveyor.gradle

import dev.hydraulic.types.machines.Machine
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.jvm.toolchain.internal.DefaultJvmVendorSpec
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.desktop.DesktopExtension
import org.openjfx.gradle.JavaFXOptions
import java.io.File
import java.util.*

/**
 * A base class for tasks that work with generated Conveyor configuration.
 */
@Suppress("LeakingThis")
abstract class ConveyorConfigTask(
    machineConfigs: Map<String, Configuration>
) : DefaultTask() {
    @get:Input
    abstract val buildDirectory: Property<String>

    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    abstract val projectVersion: Property<Any>

    @get:Input
    abstract val projectGroup: Property<Any>

    @get:Input
    @get:Optional   // Might be set via a Compose plugin setting instead of from application plugin.
    abstract val mainClass: Property<String>

    @get:Input
    abstract val applicationDefaultJvmArgs: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val jvmLanguageVersion: Property<JavaLanguageVersion>

    @get:Input
    @get:Optional
    abstract val jvmVendorValue: Property<String>

    @get:Input
    abstract val rootProjectDir: Property<File>

    @get:Input
    @get:Optional
    abstract val javafxVersion: Property<String>

    @get:Input
    @get:Optional
    abstract val javafxModules: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val composeMainClass: Property<String>

    @get:Input
    @get:Optional
    abstract val composeJvmArgs: ListProperty<String>

    @get:Input
    @get:Optional
    abstract val composePackageName: Property<String>

    @get:Input
    @get:Optional
    abstract val composeDescription: Property<String>

    @get:Input
    @get:Optional
    abstract val composeVendor: Property<String>

    @get:Input
    @get:Optional
    abstract val composeAppResourcesRootDir: Property<File>

    @get:Input
    abstract val appJar: Property<File>

    @get:Input
    abstract val runtimeClasspath: ListProperty<String>

    @get:Input
    abstract val commonFiles: ListProperty<File>

    @get:Input
    abstract val expandedConfigs: MapProperty<String, SortedSet<File>>

    // Can't use type JavaFXOptions here because Gradle can't decorate the class.
    private val javafx: Boolean

    init {
        buildDirectory.convention(project.layout.buildDirectory.get().toString())
        projectName.convention(project.name)
        projectVersion.convention(project.version)
        projectGroup.convention(project.group)

        val javafxExtension = project.extensions.findByName("javafx") as? JavaFXOptions
        javafx = javafxExtension != null
        if (javafxExtension != null) {
            javafxVersion.set(javafxExtension.version)
            javafxModules.set(javafxExtension.modules)
        }

        val appExtension = project.extensions.findByName("application") as? JavaApplication
        if (appExtension != null) {
            mainClass.set(appExtension.mainClass)
            applicationDefaultJvmArgs.set(appExtension.applicationDefaultJvmArgs)
        }

        val javaExtension = project.extensions.findByName("java") as? JavaPluginExtension
        if (javaExtension != null) {
            jvmLanguageVersion.set(javaExtension.toolchain.languageVersion.orNull)

            // We have to go to and from strings because JvmVendorSpec isn't serializable. Go to Gradle "indicator strings" here.
            @Suppress("UnstableApiUsage")
            val v = when (val vendor: JvmVendorSpec = javaExtension.toolchain.vendor.get()) {
                JvmVendorSpec.AMAZON -> "amazon"
                JvmVendorSpec.AZUL -> "azul systems"
                JvmVendorSpec.ORACLE -> "oracle"
                JvmVendorSpec.MICROSOFT -> "microsoft"
                JvmVendorSpec.ADOPTIUM -> "adoptium"
                JvmVendorSpec.GRAAL_VM -> "graalvm community"
                JvmVendorSpec.JETBRAINS -> "jetbrains"
                DefaultJvmVendorSpec.any() -> "openjdk"
                else -> vendor.toString()
            }
            jvmVendorValue.set(v)
        }

        rootProjectDir.set(project.rootProject.rootDir)

        try {
            val composeExt: ComposeExtension? = project.extensions.findByName("compose") as? ComposeExtension
            val desktopExt: DesktopExtension? = composeExt?.extensions?.findByName("desktop") as? DesktopExtension
            if (desktopExt != null) {
                val app = desktopExt.application
                composeMainClass.set(app.mainClass)
                composeJvmArgs.set(app.jvmArgs)
                val dist = app.nativeDistributions
                composePackageName.set(dist.packageName)
                composeDescription.set(dist.description)
                composeVendor.set(dist.vendor)
                composeAppResourcesRootDir.set(dist.appResourcesRootDir.orNull?.asFile)
            }
        } catch (e: Throwable) {
            val extra = if (e is NoSuchMethodError && "dsl.JvmApplication" in e.message!!) {
                "If you're using Compose 1.1 or below, try upgrading to Compose 1.2 or higher, or using version 1.0.1 of the Conveyor Gradle plugin."
            } else {
                ""
            }
            throw Exception("Could not read Jetpack Compose configuration, likely plugin version incompatibility? $extra".trim(), e)
        }

        // Initialize appJar property
        val jarTask: Task = project.tasks.findByName("desktopJar") ?: project.tasks.findByName("jvmJar") ?: project.tasks.getByName("jar")
        appJar.set(jarTask.outputs.files.singleFile)

        // Initialize runtimeClasspath property
        val configNames = setOf("runtimeClasspath", "desktopRuntimeClasspath", "jvmRuntimeClasspath")
        val runtimeClasspathConfiguration = try {
            configNames.firstNotNullOf { project.configurations.findByName(it) }
        } catch (e: NoSuchElementException) {
            throw Exception("Could not locate the classpath configuration, tried $configNames")
        }
        // We need to resolve the runtimeClasspath before copying out the dependencies because the at the start of the resolution process
        // the set of dependencies can be changed via [Configuration.defaultDependencies] or [Configuration.withDependencies].
        // Also, we can't store a Configuration as a task input property because it doesn't serialize into the configuration cache.
        runtimeClasspath.addAll(project.files(runtimeClasspathConfiguration.resolve()).map { it.absolutePath })

        val currentMachineConfig = machineConfigs[Machine.current().toString()]!!

        // Exclude current machine specific config from the runtime classpath, to retain only the dependencies that should go
        // to all platforms.
        val currentMachineDependencies: DependencySet = currentMachineConfig.dependencies
        val commonClasspath = runtimeClasspathConfiguration.copyRecursive {
            // We need to filter the runtimeClasspath here, before making the recursive copy, otherwise the dependencies from the current
            // machine config won't match.
            it !in currentMachineDependencies && (!javafx || it.group != "org.openjfx")
        }

        // Make machine configs extend the common classpath so the dependencies are resolved correctly.
        val expandedConfigsMap: SortedMap<String, Configuration> = machineConfigs.mapNotNull { (machine, config) ->
            if (config.isEmpty) null else {
                machine to config.copy().extendsFrom(commonClasspath).copyRecursive()
            }
        }.toMap().toSortedMap()

        // If there are any expanded configs, use the intersection as the common files. Otherwise, just use all files from the common
        // classpath. If there are any expanded configs, we can't really use the common classpath, because it might need the platform
        // specific dependencies to properly resolve versions.
        val commonFilesAsFiles: Set<File> = if (expandedConfigsMap.isNotEmpty())
            expandedConfigsMap.values.map { it.files }.reduce { a, b -> a.intersect(b) }
        else
            commonClasspath.files
        commonFiles.addAll(commonFilesAsFiles)

        for ((platform: String, config: Configuration) in expandedConfigsMap) {
            expandedConfigs.put(platform, config.files.toSortedSet())
        }
    }

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

    private fun StringBuilder.importFromComposePlugin() {
        appendLine()
        appendLine("// Config from the Jetpack Compose Desktop plugin.")
        composeMainClass.orNull?.let {
            appendLine("app.jvm.gui.main-class = " + quote(it))
            appendLine("app.linux.desktop-file.\"Desktop Entry\".StartupWMClass = " + quote(it.replace('.', '-')))
        }

        importJVMArgs(composeJvmArgs.get())

        composePackageName.orNull?.let { appendLine("app.fsname = " + quote(it)) }
        composeDescription.orNull?.let { appendLine("app.description = " + quote(it)) }
        composeVendor.orNull?.let { appendLine("app.vendor = " + quote(it)) }
        composeAppResourcesRootDir.orNull?.let {
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
                val dir = it.resolve(source)
                if (dir.exists()) {
                    appendLine("app.$key += ${quote(dir)}")
                }
            }
        }

        // TODO(low): Import more stuff, including:
        //
        // - Notarization details?
        // - Icons?
    }

    private fun StringBuilder.importFromJavaFXPlugin() {
        try {
            if (javafx) {
                appendLine()
                appendLine("// Config from the OpenJFX plugin.")
                appendLine("include required(\"/stdlib/jvm/javafx/from-jmods.conf\")")
                appendLine("javafx.version = ${javafxVersion.get()}")
                appendLine(
                    "app.jvm.modules = ${'$'}{app.jvm.modules} " + javafxModules.get().joinToString(
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

    private fun StringBuilder.importFromDependencyConfigurations() {
        val fileNames = HashSet<String>()

        appendLine()
        appendLine("// Inputs from dependency configurations and the JAR task.")
        val appJarFile = appJar.get()
        fileNames.add(appJarFile.name)
        appendLine("app.inputs += " + quote(appJarFile.toString()))

        fun emitInputLine(entry: File) {
            append("    " + quote(entry.toString()))
            if (fileNames.add(entry.name)) {
                appendLine()
            } else {
                // There's a file name conflict. Rename the file to deduplicate.
                var counter = 2
                while (true) {
                    val newName = "${entry.nameWithoutExtension}-${counter}.${entry.extension}"
                    if (fileNames.add(newName)) {
                        appendLine(" -> ${quote(newName)}")
                        break
                    } else {
                        counter++
                    }
                }
            }
        }

        if (commonFiles.get().isNotEmpty()) {
            appendLine("app.inputs = ${'$'}{app.inputs} [")
            for (entry: File in commonFiles.get().sorted()) {
                emitInputLine(entry)
            }
            appendLine("]")
        }

        // Emit platform specific artifacts into the right config sections.
        for ((platform, config: SortedSet<File>) in expandedConfigs.get()) {
            val files: Set<File> = config - commonFiles.get().toSortedSet()
            if (files.isEmpty()) continue
            appendLine()
            appendLine("app.$platform.inputs = ${'$'}{app.$platform.inputs} [")
            for (entry: File in files) {
                emitInputLine(entry)
            }
            appendLine("]")
        }
    }

    private fun StringBuilder.importFromJavaPlugin() {
        if (mainClass.isPresent) {
            appendLine()
            appendLine("// Config from the application plugin.")
            val mainClassValue = quote(mainClass.get())
            appendLine("app.jvm.gui.main-class = $mainClassValue")
            appendLine("app.linux.desktop-file.\"Desktop Entry\".StartupWMClass = $mainClassValue")
            importJVMArgs(applicationDefaultJvmArgs.get())
            appendLine()
        }

        if (jvmLanguageVersion.isPresent) {
            val jvmVersion: JavaLanguageVersion? = jvmLanguageVersion.orNull
            if (jvmVersion == null) {
                appendLine("// Java toolchain doesn't specify a version. Not importing a JDK.")
            } else {
                val vendorValue = jvmVendorValue.getOrElse("adoptium")
                val conveyorVendor = when (vendorValue) {
                    "amazon" -> "amazon"
                    "azul systems" -> "azul"
                    "adoptium" -> "eclipse"
                    "openjdk" -> "openjdk"
                    "microsoft" -> "microsoft"
                    "graalvm community" -> "graalvm"
                    "jetbrains" -> "jetbrains"
                    else -> null
                }
                if (conveyorVendor != null) {
                    appendLine("// Config from the Java plugin.")
                    appendLine("include required(\"/stdlib/jdk/$jvmVersion/$conveyorVendor.conf\")")
                } else {
                    appendLine("// Gradle build requests a JVM from $vendorValue but this vendor isn't known to Conveyor at this time.")
                    appendLine("// You can still use it, you'll just have to add JDK inputs that define where to download or find it.")
                    appendLine("//")
                    appendLine("// Please see https://conveyor.hydraulic.dev/latest/configs/jvm/#importing-a-jvmjdk for assistance.")
                    appendLine("internal.conveyor.warnings += \"unknown-jdk-vendor:$vendorValue\"")
                }
            }
        }
    }

    private fun StringBuilder.importJVMArgs(jvmArgs: MutableIterable<String>) {
        val argsNotPointingIntoTree = jvmArgs
            .toList()
            .filterNot { rootProjectDir.get().toString() in it }
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
            appendLine("gradle.build-dir = ${quote(buildDirectory.get())}")
            appendLine("gradle.project-name = ${quote(projectName.get())}")
            appendLine("app.fsname = ${quote(projectName.get().lowercase())}")

            val version = projectVersion.get().toString()
            if (version.isBlank() || version == "unspecified")
                throw Exception("You must set the 'version' property of the project, because all package formats require one.")
            appendLine("app.version = $version")

            val group = projectGroup.get().toString()
            if (group.isBlank())
                throw Exception("You must set the 'group' property of the project, because some package formats require a reverse DNS name.")
            appendLine("app.rdns-name = $group.${'$'}{app.fsname}")

            // This strips deps so must run before we calculate dep configurations.
            importFromJavaFXPlugin()
            importFromJavaPlugin()
            importFromComposePlugin()
            importFromDependencyConfigurations()
        }
    }
}
