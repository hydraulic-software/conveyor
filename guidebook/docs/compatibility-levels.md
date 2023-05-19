# Compatibility levels

Configs are versioned via the `conveyor.compatibility-level` key. When missing it's set to the current major version of Conveyor. The idea
is that you keep this setting along with the rest of your config in version control, and that allows defaults to change over time. You can
opt-in to new behaviour that might not be fully backwards compatible at your leisure.

Here is how behaviour is by incrementing the level.

## Level 9

* Optional inputs are now supported.
* New keys:
    * app.compression-level
    * app.mac.check-binary-versions
    * app.windows.manifests.msix.application-id
    * app.windows.store.publisher
    * app.windows.store.publisher-display-name
    * app.windows.store.identity-name

## Level 8

At level 8 or higher the `LocalAppData\Temp` directory is de-virtualized automatically for JVM apps. This is to work around a Windows bug.

Some defaults change at this level. When the compatibility level is 7 or lower:

* The app icons searched for in files called `icon-*.png`
* On Linux various symlinks are added to your app directory, pointing into /var
* For JVM apps:
    * library extraction is on by default
    * Some extra system properties are added

At level 8 or higher these defaults were changed:

* Icons are now generated automatically.
* The default symlinks were removed.
* Native library extraction from JARs is turned off and the extra system properties were moved to an include file.

To migrate to the new library extraction approach, add this to your config:

```
include required("https://raw.githubusercontent.com/hydraulic-software/conveyor/master/configs/jvm/extract-native-libraries.conf")
```

The old default config looked like this: 
```
app {
    icons = [ "icon-*.png" ]

    linux.symlinks = [
        ${"$"}{app.linux.prefix}/bin/${"$"}{app.fsname} -> ${"$"}{app.linux.install-path}/bin/${"$"}{app.fsname}
        logs -> /var/log/${"$"}{app.long-fsname-dir}
        data -> /var/lib/${"$"}{app.linux.var-lib-dir}
        cache -> /var/cache/${"$"}{app.long-fsname-dir}
    ]
    
    jvm {
        extract-native-libraries = true
        
        system-properties {
            // Java Native Access: https://github.com/java-native-access/jna/issues/384
            jna.nosys = false
        
            // FlatLAF: a modern Swing theme.
            flatlaf.nativeLibraryPath = system
        }
    }
}
```

These new keys were added:

* app.site.github.oauth-token
* app.site.github.pages-branch
* app.site.s3.bucket
* app.site.s3.access-key-id
* app.site.s3.secret-access-key
* app.windows.manifests.msix.additional-properties-xml
* app.jvm.windows.override-appdata-env
* app.windows.manifests.msix.virtualization.excluded-directories

## Level 7

At level 7 or above:

* Windows installs will no longer downgrade. This is to align behavior with macOS and Linux which don't support downgrades.
* DLLs in the generic app inputs for JVM apps are also moved to the JVM directory out of the `app` directory. This places 
  them in the same location as other libraries and ensures they can be found by `System.loadLibrary`.

The following keys were added:

* app.site.consistency-checks
* app.windows.manifests.msix.dependencies
* app.windows.override-icon

## Level 6

The following keys were added:

* app.site.flat

## Level 5

The following keys were added:

* app.url-schemes
* app.site.copy-to
* app.mac.bundle-extras
* app.mac.bundle-extras.amd64
* app.mac.bundle-extras.aarch64
* app.updates

## Level 4

At level 4 or above, EXEs in JVM generic app inputs are moved to the JVM directory, out of the `app` directory. 

The following keys were added:

* app.site.extra-header-html

## Level 3

The following keys were added:

* app.windows.start-on-login

## Level 2

Keys were added to control JVM options per machine, and additionally:

* app.jvm.unwanted-jdk-files
* app.jvm.extract-native-libraries
