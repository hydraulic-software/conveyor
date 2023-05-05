import com.typesafe.config.*
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.io.path.copyTo
import kotlin.io.path.toPath
import kotlin.io.path.writeText
import kotlin.test.assertEquals

/**
 * Basic test for the Conveyor Gradle Plugin.
 *
 * NOTE: Due to difficulties with using the Gradle TestKit, not all tests are covered here. Some are tested internally by Hydraulic.
 */
class ConveyorGradlePluginTest {

    @TempDir
    private lateinit var tmp: File

    private fun printConveyorConfig(testName: String): Config {
        this.javaClass.getResource("/build.gradle.kts.$testName")!!.toURI().toPath().copyTo(tmp.toPath().resolve("build.gradle.kts"))
        tmp.toPath().resolve("settings.gradle.kts").writeText("rootProject.name = \"$testName\"")
        val result = GradleRunner.create()
            .withProjectDir(tmp)
            .withArguments("-q", "printConveyorConfig")
            .withPluginClasspath().build()

        return ConfigFactory.parseString(result.output, ConfigParseOptions.defaults().setIncluder(object : ConfigIncluder {
            override fun include(context: ConfigIncludeContext?, what: String?): ConfigObject {
                if (what?.contains("\"") == true) {
                    throw UnsupportedOperationException("Can't include with quotes when testing")
                }
                return ConfigFactory.parseString("includes += \"$what\"").root()
            }

            override fun withFallback(fallback: ConfigIncluder?): ConfigIncluder {
                return this
            }
        })).withFallback(ConfigFactory.parseMap(mapOf("app.inputs" to emptyList<String>(), "includes" to emptyList<String>())))
            .resolve()
    }

    private val Config.inputs: Set<String>
        get() {
            return getStringList("app.inputs").map { it.substringAfterLast("/") }.toSet()
        }

    private val Config.includes: Set<String>
        get() {
            return getStringList("includes").toSet()
        }

    @Test
    fun `default dependencies resolve correctly`() {
        val name = "scenario1"
        val config = printConveyorConfig(name)
        assertEquals(name, config.getString("app.fsname"))
        assertEquals(setOf("/stdlib/jdk/17/openjdk.conf"), config.includes)
        assertEquals(
            setOf(
                "scenario1-1.0.jar",
                "kotlin-stdlib-common-1.8.21.jar",
                "kotlin-stdlib-jdk7-1.8.21.jar",
                "kotlin-stdlib-jdk8-1.8.21.jar",
                "kotlin-stdlib-1.8.21.jar",
                "kotlinx-coroutines-core-jvm-1.6.4.jar",
                "kotlinx-serialization-core-jvm-1.5.0.jar",
                "kotlinx-serialization-json-jvm-1.5.0.jar",
                "annotations-13.0.jar"
            ), config.inputs
        )
    }
}