package hydraulic.conveyor.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * A task that prints out the configuration.
 */
abstract class PrintConveyorConfigTask @Inject constructor(of: ObjectFactory) : ConveyorConfigTask(of) {
    init {
        group = "Conveyor"
        description = "Prints a snippet of Conveyor configuration."
    }

    @TaskAction
    fun print() {
        println(generate())
    }
}
