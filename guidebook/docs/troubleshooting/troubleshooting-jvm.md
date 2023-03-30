# Troubleshooting JVM apps

Stuck? Can't find what you need here? If you're a commercial user then you can write to [contact@hydraulic.software](mailto:contact@hydraulic.software). Otherwise feel free to post in [the GitHub discussion forum](https://github.com/hydraulic-software/conveyor/discussions).

## Problems loading native libraries

Conveyor can move native libraries out of JARS to ensure [a better end-user experience](../configs/jvm.md#jar-processing). It's conventional to always try to load native libraries using `System.loadLibrary` before unpacking bundled native code, but some libraries don't do this. 

You have two possible solutions:

* Set `app.jvm.extract-native-libraries = false`. This will cause Conveyor to find and sign any native libraries inside the JARs, allowing your app to extract them to the user's home directory or temp directory at startup. It's not ideal because it will litter your user's system with stuff that uninstalls won't clean up, it slows down app startup, bloats your download and can cause problems with Apple's GateKeeper (see below). But it *is* simple for the developer and will get you running fast.
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

## "UnsatisfiedLinkError: ... Mapping process and mapped file (non-platform) have different Team IDs ..."

The file identified as being unloadable is probably in a cache or temp location in your home directory.

This error from macOS can occur when using an app on the same machine used to develop it. It's related to the discussion above about native libraries. When `app.jvm.extract-native-libraries = false` (which is the default when `conveyor.compatibility-level >= 7`) Java libraries with native components will go back to the regular behavior of extracting their libs at startup to somewhere in the user's home directory (or a temporary directory). During development the library that gets extracted to this cache path will be unsigned, but when you build your app the embedded .jnilib or .dylib file inside the JAR will be code signed with your identity and the operating system expects them to match. If they don't then you'll get this error. This problem is inherent to the way the JVM ecosystem handles native code and Conveyor gives you several options to resolve it.

The best solution is to opt in to native library extraction. This will place native code in the right places inside the Mac app ensuring no conflicts, fast startup time and avoiding problems with GateKeeper. The downside is you may need to [set extra system properties](../configs/jvm.md#library-sysprops-project) to make your Java libraries look in the right place for their native components.

The quickest solution is to delete the file that the error identifies as being unloadable, then run your packaged and signed app. This will replace the cached version with the version from your app, signed by you. This should also work fine whilst developing your app. The problem with this is that if any of your users have two different apps that use the _same_ Java library, then they will fight over the shared cache location and the signature mismatch error may return.

The final solution is to opt out of Apple's library validation feature. This will make the error go away in all cases without needing extra system properties. However, Apple views apps that do this to be less secure than normal and GateKeeper will run extra checks on them, which might cause your app to be blocked. Do it like this:

```
app {
    mac { 
        entitlements-plist {
            "com.apple.security.cs.disable-library-validation" = true
        }
    }
}
```

## Localization doesn't work when packaged

Conveyor uses `jlink` to shrink the size of the bundled JVM. Translations can be large, so by default `jlink` throws out non-US locales.
To add e.g. German locale data, use config like this:

```
app {
    jvm {
        modules += jdk.localedata
        jlink-flags += "--include-locales=en,de"
    }
}
```

To learn how to specify languages, please read the [jlink user guide](https://docs.oracle.com/en/java/javase/17/docs/specs/man/jlink.html#plugin-include-locales).

## usb4java

This popular library needs a special fix when deploying with `app.jvm.extract-native-libraries = true`. It uses a way to load native code 
that isn't quite normal nor compatible with the Java API specification. To fix it include the following class and call the `fix` method 
before accessing usb4java for the first time:

```java
public class Usb4JavaPackaging {
    public static void fix() {
        if (System.getProperty("app.dir") == null)
            return;   // In dev, nothing to do.

        try {
            var field = org.usb4java.Loader.class.getDeclaredField("loaded");
            field.setAccessible(true);
            field.set(null, true);

            try {
                System.loadLibrary("usb4java");
                return;
            } catch (UnsatisfiedLinkError e) {
            }

            // On Windows usb4java uses an non-Java-spec compliant DLL 
            // name, so we have to try again.
            System.loadLibrary("libusb4java");
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
```

!!! note "If you don't use machine specific dependencies"
    If you add usb4java to your build system in the standard way, you'll end up with every possible native library JAR on your classpath.
    This is OK! But be aware that:

    * Your app will contain every native library JAR on every platform. If you look inside you'll notice they're empty. Conveyor 
      extracted the native library, noticed it was for a different OS and didn't include it in the package. So don't worry, you aren't 
      bloating your app. These empty JARs are harmless and can be ignored.
    * You will get warnings of the form `libusb4java-1.3.0-linux-x86-64.jar doesn't seem to contain artifacts for mac.amd64`. This is
      Conveyor telling you that you seem to have native libraries for the wrong OS on your classpath for each machine. Again, this is
      harmless in this context and can be ignored, because Conveyor will fix things automatically.

## Big delta updates

You can make updates faster by using individual JARs as inputs. This will work much better with delta compression (as used by MSIX/AppInstaller on Windows). Note that explicit JPMS modules will be still be bundled into a single `modules` file, as this can yield better startup times.

## JavaFX fails to launch on Windows citing missing dependencies in glass.dll 

Upgrade to JavaFX 19 or higher. Earlier versions of JavaFX rely on the Microsoft VC++ runtime DLLs either coming with the JVM or being already installed on the user's system. Depending on what version of Visual Studio was used to compile your JDK distribute, on a clean Windows install the right DLLs may not be available. In JavaFX 19 the right DLLs are bundled in the jmods. See [JDK-8281089](https://bugs.openjdk.org/browse/JDK-8281089). Conveyor will warn you if you're using JavaFX older than 19. 
