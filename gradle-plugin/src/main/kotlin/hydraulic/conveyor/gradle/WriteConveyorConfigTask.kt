package hydraulic.conveyor.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Writes the generated Conveyor config to disk.
 */
abstract class WriteConveyorConfigTask : ConveyorConfigTask() {
    @get:OutputFile
    abstract val destination: RegularFileProperty

    init {
        group = "Conveyor"
        description = "Writes a snippet of Conveyor configuration to the destination file."

        // This task is so fast, it's not worth trying to optimize whether it runs or not.
        outputs.upToDateWhen { false }
    }

    @TaskAction
    fun writeOut() {
        destination.get().asFile.writeText(generate())
    }
}
