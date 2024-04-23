package hydraulic.conveyor.gradle

import dev.hydraulic.types.machines.CLibraries
import dev.hydraulic.types.machines.LinuxMachine
import dev.hydraulic.types.machines.Machine
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import java.util.*

/**
 * Adds tasks that generate Conveyor configuration snippets based on the Gradle project to which it's applied.
 */
@Suppress("unused")
class ConveyorGradlePlugin : Plugin<Project> {
    private val machineConfigs = HashMap<Machine, Configuration>()
    private val currentMachine = Machine.current()

    private fun String.capitalize(): String = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    private fun machineConfig(project: Project, machine: Machine): Configuration {
        val configsMap: SortedMap<String, Configuration> = project.configurations.asMap
        // The Kotlin Multiplatform plugin puts dependencies in a different configuration than the normal Java plugin.
        val impl: Configuration? =
            configsMap["implementation"] ?: configsMap["jvmMainImplementation"] ?: configsMap["commonMainImplementation"]
        return machineConfigs.getOrPut(machine) {
            var configName = "${machine.os.identifier}${machine.cpu.identifier.capitalize()}"
            if (machine is LinuxMachine && machine.cLibrary != CLibraries.GLIBC)
                configName += machine.cLibrary.identifier.capitalize()
            project.configurations.create(configName).also {
                if (machine == currentMachine)
                // Make implementation extend from the current machine config, so that those dependencies get included to the runtime when
                // executing './gradlew run'.
                    impl?.extendsFrom(it)
            }
        }
    }

    override fun apply(project: Project) {
        // Supply configurations for all the supported machines. Type safe accessors will be created.
        for (m in setOf(
            Machine.LINUX_AARCH64, Machine.LINUX_AMD64, Machine.LINUX_AARCH64_MUSLC, Machine.LINUX_AMD64_MUSLC,
            Machine.WINDOWS_AMD64, Machine.WINDOWS_AARCH64,
            Machine.MACOS_AMD64, Machine.MACOS_AARCH64
        )) machineConfig(project, m)

        // Register the two tasks.
        project.tasks.register("writeConveyorConfig", WriteConveyorConfigTask::class.java) {
            it.destination.set(project.layout.projectDirectory.file("generated.conveyor.conf"))
            it.machineConfigs = machineConfigs
        }
        project.tasks.register("printConveyorConfig", PrintConveyorConfigTask::class.java) {
            it.machineConfigs = machineConfigs
        }
    }
}
