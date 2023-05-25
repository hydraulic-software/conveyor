---
hide:
- navigation
---

# Maven projects

Here's an example of how to package a Maven project. Firstly, adjust the `pom.xml` in your app module to configure the Maven dependencies 
plugin. We'll use this to get a directory of JARs to ship. Pay attention to the exclusions. Maven will only give us the JARs for whatever
platform happens to be running the build, but we will need all of them. They can be added back manually later.

```
<project>
<build>
<plugins>

<!-- Copy all dependencies (excluding those with native components). -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <id>copy-dependencies</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>copy-dependencies</goal>
            </goals>
            <configuration>
                <outputDirectory>target/classpath-jars</outputDirectory>
                <includeScope>runtime</includeScope>
                
                <!-- Exclude JavaFX because we'll get that from JMODs if we need to. -->
                <excludeGroupIds>org.openjfx</excludeGroupIds>
                
                <!-- Exclude anything else here where the artifacts are machine specific. We'll add them back later. -->
            </configuration>
        </execution>
    </executions>
</plugin>

</plugins>
</build>
</project>
```

Now before building the packages you'll need to run `mvn prepare-package` to populate the `target/classpath-jars` directory.

```javascript title="conveyor.conf" linenums="1"
include "/stdlib/jdk/17/openjdk.conf"  // (1)!

include "#!=app.version mvn -q help:evaluate -Dexpression=project.version -DforceStdout"  // (2)!

app {
    display-name = myPROGRAM  // (3)!
    vendor = Global MegaCorp  // (4)!
    version = 1.0
    rdns-name = org.some-org.some-product

    inputs += app/target/my-program-1.0-SNAPSHOT.jar  // (5)!
    inputs += app/target/classpath-jars
    
    site.base-url = downloads.myproject.org/some/path  // (6)!
}
```

1. You can import JDKs by major version, major/minor version, and by naming a specific distribution.
2. You can assign Conveyor keys from Maven properties by using [hashbang includes](../../configs/hocon-extensions.md#including-the-output-of-external-commands).
   Note that `-SNAPSHOT` isn't valid in package versions.
3. You may not need to set this if the display name of your project is trivially derivable from the fsname. The default here would be `My Program`.
4. This is optional. It'll be prefixed to the display name and used as a directory name  in various places; skip it if you don't work for an organization.
5. This will import the app JAR and contents of the directory made by `mvn prepare-package`.
6. This is where the created packages will look for update metadata.

## Adapting a JavaFX app

Given the above we can make some small adaptations for JavaFX. Make sure your POM fragment is excluding the JavaFX JARs and then do 
something like this:

```
include required("/stdlib/jdk/17/openjdk.conf")

// Import JavaFX.
include "#!=javafx.version mvn -q help:evaluate -Dexpression=openjfx.version -DforceStdout"
include required("/stdlib/jvm/javafx/from-jmods.conf")

app {
  jvm {
    gui.main-class = your.app.MainClass
    modules = [ javafx.controls, javafx.swing, javafx.web ]
  }
}
```

Please note:

* You should list the JavaFX modules you need in the `app.jvm.modules` list.
* You can read the JavaFX version from whatever property you're using in your POM to control that. Here 
  it's `openjfx.version` but it could be anything.
* The `/stdlib/jvm/javafx/from-jmods.conf` file imports the JMODs from the Gluon releases.

Please see the [Gradle page](2-gradle.md#adapting-a-javafx-app) to see how to set your stage icons.
