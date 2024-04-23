package hydraulic.conveyor.gradle

import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * A task that prints out the configuration.
 */
abstract class PrintConveyorConfigTask @Inject constructor(
    machineConfigs: Map<String, Configuration>
) : ConveyorConfigTask(machineConfigs) {
    init {
        group = "Conveyor"
        description = "Prints a snippet of Conveyor configuration."
    }

    @TaskAction
    fun print() {
        println(generate())
    }
}
