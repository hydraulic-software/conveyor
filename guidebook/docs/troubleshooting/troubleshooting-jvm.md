# Troubleshooting JVM apps

Stuck? Can't find what you need here? If you're a commercial user then you can write to [contact@hydraulic.software](mailto:contact@hydraulic.software). Otherwise feel free to post in [the GitHub discussion forum](https://github.com/hydraulic-software/conveyor/discussions).

## Problems loading native libraries

Conveyor moves native libraries out of JARS to ensure [a better end-user experience](../configs/jvm.md#jar-stripping). It's conventional to always try to load native libraries using `System.loadLibrary` first unpacking bundled native code, but some libraries don't do this. They may require a custom system property to be set. You can do it like this:

```
app {
    jvm {
        system-properties {
            my.library.nativePathLib = <libpath>
        }
    }
}
```

It's often worth reporting these bugs to the upstream projects, so they can use `loadLibrary` first and only try unpacking libraries afterwards.

!!! tip
    JAR stripping can be disabled, but then you will face problems with Mac notarization detecting unsigned code inside your JARS. Future
    versions of Conveyor may add support for signing files inside JARs, but this wouldn't fix the user experience problems such as slower
    startup and littering home directories.

## FlatLAF doesn't use correct window decorations

Make sure you're using 2.6+ and that your config is including the JVM enhancements config (`include required("/stdlib/jvm/enhancements/client/v1.conf")`).

For more details see [problems loading native libraries](#problems-loading-native-libraries).

## Big delta updates

You can make updates faster by using individual JARs as inputs. This will work much better with delta compression (as used by MSIX/AppInstaller on Windows). Note that explicit JPMS modules will be still be bundled into a single `modules` file, as this can yield better startup times.
