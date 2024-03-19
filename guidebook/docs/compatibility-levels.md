# Compatibility levels

Configs are versioned via the `conveyor.compatibility-level` key. When missing it's set to the current major version of Conveyor. The idea
is that you keep this setting along with the rest of your config in version control, and that allows defaults to change over time. You can
opt-in to new behaviour that might not be fully backwards compatible at your leisure.

Additionally, using new config keys requires increasing your compatibility level. This is to catch the case where the Conveyor being used 
is older than the one expected by the author of the config.

Here is how behaviour changes at each level.
                                   
## Level 14

These new keys were added:

 * `app.mac.sign.scripts`
 * `app.windows.sign.scripts`
 * `app.jvm.additional-ca-certs`
 * `app.mac.notarization.best-effort-stapling` 
 * `app.mac.notarization.script` 
 * `app.mac.notarization.timeout-seconds` 
 * `app.windows.sign.scripts.msix` 
 * `app.windows.sign.scripts.binary`
 
## Level 13

These new keys were added:

 * `app.windows.signing-key.azure-key-vault.vault-name`
 * `app.windows.signing-key.azure-key-vault.api-access-token`
 * `app.site.s3.endpoint`
 * `app.site.move-from.s3.endpoint`

## Level 12

The default value for `app.windows.manifests.msix.background-color` is now `transparent`. Previously, the default was `#ffffff`.

From level 12 onwards, the Temp directory is no longer devirtualized on Windows. This avoids problems with Microsoft Store permission
reviews. Instead, for JVM apps the temp directory is pointed into the user's home directory, to work around bugs in the Windows kernel
that would otherwise break TCP connections.

These new keys were added:

* `app.electron.asar`
* `app.electron.prune`
* `app.windows.installer.theme`
* `app.site.theme`
* `app.theme`

## Level 11

These new keys were added:

* `app.mac.deltas`
* `app.site.move-from.base-url`
* `app.site.move-from.copy-to`
* `app.site.move-from.github.oauth-token`
* `app.site.move-from.github.pages-branch`
* `app.site.move-from.s3.region`
* `app.site.move-from.s3.access-key-id`
* `app.site.move-from.s3.secret-access-key`
* `app.windows.exe-installer-basename`

Sparkle deltas are generated for up to 5 previous versions by default.

## Level 10

These new keys were added:

* `app.linux.debian.distribution.mirrors`
* `app.linux.debian.distribution.name`
* `app.site.ignore-connection-issues-for-hosts`
* `app.site.show-conveyor-badge`
* `app.windows.manifests.msix.append-publisher-id-to-appinstaller`
* `app.windows.manifests.msix.reinstall-if-required`
* `app.windows.manifests.msix.update-escape-hatch.exe`
* `app.windows.manifests.msix.update-escape-hatch.run-if`
* `app.windows.manifests.msix.use-update-escape-hatch`

The [escape hatch mechanism](configs/windows.md#escape-hatch-mechanism) is enabled by default starting from compatibility level 10.
It may be enabled for all compatibility levels in a later release.

The distribution package index used for computing dependencies for Linux DEB packages changes from Ubuntu Focal to Ubuntu Jammy. 

## Level 9

These new keys were added:

* `app.compression-level`
* `app.mac.check-binary-versions`
* `app.windows.manifests.msix.application-id`
* `app.windows.store.publisher`
* `app.windows.store.publisher-display-name`
* `app.windows.store.identity-name`

Optional inputs are now supported.

## Level 8

These new keys were added:

* `app.site.github.oauth-token`
* `app.site.github.pages-branch`
* `app.site.s3.bucket`
* `app.site.s3.access-key-id`
* `app.site.s3.secret-access-key`
* `app.windows.manifests.msix.additional-properties-xml`
* `app.jvm.windows.override-appdata-env`
* `app.windows.manifests.msix.virtualization.excluded-directories`

At level 8 or higher the `LocalAppData\Temp` directory is de-virtualized automatically for JVM apps. This is to work around a Windows bug.

Some defaults change at this level. When the compatibility level is 7 or lower:

* The app icons are assumed to match `icon-*.png` and be next to the `conveyor.conf` file.
* On Linux, some symlinks are added to your app directory which point into /var
* For JVM apps:
    * library extraction is on by default
    * Some extra system properties are added

At level 8 or higher these defaults were changed:

* Icons are now generated automatically.
* The default symlinks were removed.
* Native library extraction from JARs is turned off and the extra system properties were moved to an include file.

To re-activate library extraction add this to your config:

```
include required("https://raw.githubusercontent.com/hydraulic-software/conveyor/master/configs/jvm/extract-native-libraries.conf")
```

The old default config added before level 8 looked like this:

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

## Level 7

The following keys were added:

* `app.site.consistency-checks`
* `app.windows.manifests.msix.dependencies`
* `app.windows.override-icon`

At level 7 or above:

* Windows installs will no longer downgrade. This is to align behavior with macOS and Linux which don't support downgrades.
* DLLs in the generic app inputs for JVM apps are also moved to the JVM directory out of the `app` directory. This places 
  them in the same location as other libraries and ensures they can be found by `System.loadLibrary`.

## Level 6

The following keys were added:

* `app.site.flat`

## Level 5

The following keys were added:

* `app.url-schemes`
* `app.site.copy-to`
* `app.mac.bundle-extras`
* `app.mac.bundle-extras.amd64`
* `app.mac.bundle-extras.aarch64`
* `app.updates`

## Level 4

The following keys were added:

* `app.site.extra-header-html`

At level 4 or above, EXEs in JVM generic app inputs are moved to the JVM directory, out of the `app` directory.

## Level 3

The following keys were added:

* `app.windows.start-on-login`

## Level 2

Keys were added to control JVM options per machine, and additionally:

* `app.jvm.unwanted-jdk-files`
* `app.jvm.extract-native-libraries`
