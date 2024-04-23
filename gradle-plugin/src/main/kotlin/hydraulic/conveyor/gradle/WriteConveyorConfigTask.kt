package hydraulic.conveyor.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * Writes the generated Conveyor config to disk.
 */
abstract class WriteConveyorConfigTask @Inject constructor(of: ObjectFactory) : ConveyorConfigTask(of) {
    @get:OutputFile
    abstract val destination: RegularFileProperty

    init {
        group = "Conveyor"
        description = "Writes a snippet of Conveyor configuration to the destination file."

        // This task is so fast, it's not worth trying to optimize whether it runs or not.
        outputs.upToDateWhen { false }
    }

    @Input
    val generatedConfig: String = generate()

    @TaskAction
    fun writeOut() {
        destination.get().asFile.writeText(generatedConfig)
    }
}
