# Gradle plugin

This module contains a simple Gradle plugin that extracts settings from a build and emits a Conveyor config snippet. This makes it easy
to keep metadata and dependencies from your source build and packaging build in sync.

It adds two tasks:

* `printConveyorConfig` - emits the generated config to standard out where it's easy to examine, or can be immediately included into the
  main config using a hashbang include.
* `writeConveyorConfig` - emits the generated config to a file in the _project_ source directory (not the build directory). It can then be
  included into the main config using a regular include. The advantage is that Gradle doesn't get invoked each time, so it's faster, but
  you have to re-run when necessary.

The plugin extracts the following:

* `project.version`
* `project.group`
* From the `application` plugin: the main class and JVM arguments are extracted.
* From the JetPack Compose Desktop plugin: main class, JVM arguments, description, vendor.

It defines machine specific dependency configurations that can be used to add dependencies which should only take effect on particular
platforms:

* `linuxAmd64`
* `linuxAarch64`
* `linuxAmd64Muslc`
* `linuxAarch64Muslc`
* `macAmd64`
* `macAarch64` (Apple Silicon)
* `windowsAmd64`
* `windowsAarch64`

The results are emitted to the right section of config. Also, the host machine running Gradle is examined and the `implementation`
configuration is made to extend the appropriate machine-specific configuration. This can be helpful even if you don't use Conveyor.
