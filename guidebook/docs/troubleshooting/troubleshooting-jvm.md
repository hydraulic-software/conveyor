# Troubleshooting JVM apps

Stuck? Can't find what you need here? If you're a commercial user then you can write to [contact@hydraulic.software](mailto:contact@hydraulic.software). Otherwise feel free to post in [the GitHub discussion forum](https://github.com/hydraulic-software/conveyor/discussions).

## Problems loading native libraries

Conveyor can move native libraries out of JARS to ensure [a better end-user experience](../configs/jvm.md#jar-processing). It's conventional to always try to load native libraries using `System.loadLibrary` first unpacking bundled native code, but some libraries don't do this. 

You have two possible solutions:

* Set `app.jvm.extract-native-libraries = false`. This will cause Conveyor to find and sign any native libraries inside the JARs, allowing your app to extract them to the user's home directory or temp directory at startup. It's not ideal because it will litter your user's system with stuff that uninstalls won't clean up, it slows down app startup and it bloats the size of your download. But it *is* simple for the developer.
* Provide the right system properties for your third party library to find its native code. You can do it using the special `<libpath>` token, like this:

```
app {
    jvm {
        system-properties {
            "my.library.nativePathLib" = <libpath>
        }
    }
}
```

It's often worth reporting these bugs to the upstream projects, so they can use `loadLibrary` first and only try unpacking libraries afterwards. We are collecting [such system properties here](https://conveyor.hydraulic.dev/7.0/configs/jvm/#library-sysprops-project) - why not send us a PR.

## Big delta updates

You can make updates faster by using individual JARs as inputs. This will work much better with delta compression (as used by MSIX/AppInstaller on Windows). Note that explicit JPMS modules will be still be bundled into a single `modules` file, as this can yield better startup times.

## JavaFX fails to launch on Windows citing missing dependencies in glass.dll 

Upgrade to JavaFX 19 or higher. Earlier versions of JavaFX rely on the Microsoft VC++ runtime DLLs either coming with the JVM or being already installed on the user's system. Depending on what version of Visual Studio was used to compile your JDK distribute, on a clean Windows install the right DLLs may not be available. In JavaFX 19 the right DLLs are bundled in the jmods. See [JDK-8281089](https://bugs.openjdk.org/browse/JDK-8281089). Conveyor will warn you if you're using JavaFX older than 19. 
