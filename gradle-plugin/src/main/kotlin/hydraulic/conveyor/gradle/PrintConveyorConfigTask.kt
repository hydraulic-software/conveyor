package hydraulic.conveyor.gradle

import org.gradle.api.tasks.TaskAction

/**
 * A task that prints out the configuration.
 */
abstract class PrintConveyorConfigTask : ConveyorConfigTask() {
    init {
        group = "Conveyor"
        description = "Prints a snippet of Conveyor configuration."
    }

    @TaskAction
    fun print() {
        println(generate())
    }
}
