# JVM clients

```
include required("/stdlib/jvm/enhancements/client/v1.conf")
```

Importing the above config is recommended for all CLI/desktop JVM based apps. It will do the following:

* Set system properties for various libraries to make them work better in the packaged environment.
* Enable HTTP proxy auto-detection.
* Ensure the `jdk.crypto.ec` module is always linked in, as otherwise some TLS protected websites may not work.

Other enhancements may be added in future releases, for example, tuning JVM flags. Any that may be backwards incompatible will be put in 
a separately versioned file.

The current contents of the file are:

```
app {
  jvm {
    system-properties {
      # Force JNA to load its library from the normal path here.
      #
      # https://github.com/java-native-access/jna/issues/384
      "jna.nosys" = false

      # Same for FlatLAF (a modern Swing theme). Supported starting from the October 2022 release.
      "flatlaf.nativeLibraryPath" = system

      # Force PicoCLI to always use ANSI mode even on Windows, where our launcher enables them.
      "picocli.ansi" = tty

      # Read HTTP proxy settings from the OS.
      "java.net.useSystemProxies" = true
    }

    // Needed for Ed25519 provider and modern SSL.
    modules += jdk.crypto.ec
  }
}
```
