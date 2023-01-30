---
hide:
  - navigation
---

# Other JVM build systems

Create a `conveyor.conf` that looks like this:

```javascript title="conveyor.conf" linenums="1"
include "/stdlib/jdk/17/openjdk.conf"  // (1)!

app {
    inputs += build/jars/my-program-1.0.jar
    inputs += build/jars
    
    icons = "icon-*.png"

    vendor = Global MegaCorp  // (2)!
    
    site.base-url = global-megacorp.com/myproduct  // (3)!
}
```

1. You can import JDKs by major version, major/minor version, and by naming a specific distribution.
2. This is optional. It'll be prefixed to the display name and used as a directory name  in various places; skip it if you don't work for an organization.
3. This is where the created packages will look for update metadata.

This configuration adds your app's main JAR as the first input, allowing package metadata like the main class name, the version number and name to be derived from the file name. Then it adds the directory containing all the app JARs (duplicates are ignored), and finally a set of icon files. That's all you need! The extra inferred configuration will look like this:

```javascript
// This will all be figured out by Conveyor automatically:
app {
    fsname = my-program  // (1)!
    display-name = My Program  // (2)!
    version = 1.0  // (3)!
    rdns-name = com.global-megacorp.myproduct
    
    jvm {
        main-class = com.example.MyProgram  // (4)!
        
        modules = [ ... ]  // (5)!
    }
}
```

1. The `fsname` is what's used for names on Linux e.g. in the `bin` directory, for directories under `lib`. In fact when specified the vendor is also used, and the program will be called `global-megacorp-my-program` unless the `long-fsname` key is overridden.
2. The display name is the human readable brand name. It's generated from the `app.fsname` here by replacing dashes with spaces and re-capitalizing.
3. The version number was taken from the file name of the first input.
4. The main class was read from the MANIFEST.MF of the JAR file. If your JAR isn't actually executable by running `java -jar` or if you have more than one JAR with a main class, then this will fail and you'll need to specify it by hand.
5. The JDK modules to include in the JVM will be inferred by scanning your JARs with the `jdeps` tool, so the JVM will be shrunk automatically.

Sometimes you don't want the settings to be inferred from the first input like that. In this case you can specify the config directly. Look at these sections of the guidebook to learn more:

* [Generic configuration for any kind of app](../../configs/index.md)
* [Configuration for JVM apps](../../configs/jvm.md)
