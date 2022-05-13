# JVM clients

```
include required("/stdlib/jvm/enhancements/client/v1.conf")
```

Importing the above config is recommended for all CLI/desktop JVM based apps. It will do the following:

* Set JNA and PicoCLI related system properties to make them work better in the packaged environment.
* Enable HTTP proxy auto-detection.
* Ensure the `jdk.crypto.ec` module is always linked in, as otherwise some TLS protected websites may not work.

Other enhancements may be added in future releases, for example, tuning JVM flags. Any that may be backwards incompatible will be put in 
a separately versioned file.
