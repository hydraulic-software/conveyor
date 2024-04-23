package hydraulic.conveyor.gradle

import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * Writes the generated Conveyor config to disk.
 */
@Suppress("LeakingThis")
abstract class WriteConveyorConfigTask @Inject constructor(
    machineConfigs: Map<String, Configuration>
) : ConveyorConfigTask(machineConfigs) {
    @get:OutputFile
    abstract val destination: RegularFileProperty

    init {
        group = "Conveyor"
        description = "Writes a snippet of Conveyor configuration to the destination file."
        destination.convention(project.layout.projectDirectory.file("generated.conveyor.conf"))
    }

    @TaskAction
    fun writeOut() {
        destination.get().asFile.writeText(generate())
    }
}
