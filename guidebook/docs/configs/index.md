# Writing config files

There are many possible settings in Conveyor configs, but almost all of them can be ignored if you follow certain conventions because they'll be derived from the others. Any default you don't like can be fixed by adding the key yourself.

An app is defined using the HOCON configuration language. HOCON is a superset of JSON and thus all JSON is valid HOCON, but it comes with a variety of syntax features to make writing configuration files easier. Conveyor then adds [additional features](hocon-extensions.md) like brace expansion in string lists, per-user default files, access to environment variables and the ability to [include the results of external programs](maven-gradle.md). This lets you make dynamic configs that are partly or completely computed on the fly. You can read the [HOCON spec](hocon-spec.md) to learn how to write this small language and how to use the [Conveyor extensions](hocon-extensions.md).

To understand every setting that went into your app run the `conveyor json` command and examine the output. Because HOCON is a superset of JSON you can use this result in place of the original config, but we don't recommend that because you'll lose the smartly calculated defaults. Using raw JSON as a config file is normally only useful if you're generating it from a tool.

!!! important
    When working with lists it's good style to always add to them rather than overriding them. If you assign directly importing other configs from (for example) the Conveyor standard library won't work as you'll overwrite the values they place in shared lists. In other words write `key += value` rather than `key = [ value ]`. The first form adds to whatever `key` currently holds, the second replaces it.

## Schema versions

The Conveyor config schema may change over time. To ensure backwards compatibility, when you build a config for the first time a new key called `schema-version` will be added to it, if not already present. This records the latest schema version at the time you ran the program, ensuring that old configs can be read by new versions of the tool. When a new release changes the schema that'll be mentioned in the release notes and you can then choose when and whether to upgrade.

## Minimal app

Apps are built from [inputs](inputs.md). A config must specify at least one input file and the address where the downloads will be served from (as the packages need to know this to configure online updates). If the file name follows a conventional form all other app metadata can be derived from it:

```
app { 
  inputs = example-app-1.1.zip
  site {
    host = yourserver.net    # Where your downloads will be served from.
  }
}
```

You can also write this minimal config in other ways:

```
app.inputs = example-app-1.1.zip
app.site.base-url = yourserver.net/some/subpath
```

This will generate packages for all supported platforms from the contents of the zip file, using the name `example-app`, `Example App` and version `1.1` in the appropriate places.

All configuration Conveyor pays attention to goes under the `app` hierarchy, but you can place keys outside this area to hold your own variables.

## Names and metadata

**`app.fsname`** and **`app.long-fsname`**. An *fsname* is a name that appears on the file system. The fsname of an app is used for forming default values for:

* Package names.
* Directories where logs, caches and persistent data should be stored. 
* Executable names usable from the command line.

The fsname will be derived automatically from the file name of the first input, if that's in a conventional form (see above).

The *long fsname* is used where a file name might be in a shared namespace and needs to be globally unique as a consequence. If a **`app.vendor`** is specified then the default **`app.long-fsname`** will be the vendor name plus the application's fsname separated by a dash. The *long fsname* is used anywhere a file on disk might be in a namespace shared with other software, which is common on Linux systems but less relevant on macOS and Windows. There is also **`app.long-fsname-dir`**. This is the same concept but with a directory separator instead.

Here's an example. You write:

```hocon
app.vendor = LittleCorp
app.inputs += example-app-1.1.zip
```

Conveyor will calculate:

```hocon
app.version = 1.1
app.fsname = example-app
app.long-fsname = littlecorp-example-app
app.long-fsname-dir = littlecorp/example-app
app.display-name = LittleCorp Example App
```

**`app.display-name`** The name of the software as meant for humans. Will be calculated from the fsname if not specified by simply uppercasing each word.

**`app.vendor`** Holds the display name of the organisation that makes the software.

**`app.description`** A one line summary of what the software does.

**`app.license`** The name of the copyright license the app is under, defaulting to `Proprietary`. Open source apps should use an [SPDX identifier](https://spdx.org/licenses/) here to qualify for a free license.

**`app.revision`** The version of the config file itself. This can be incremented if you change the package definition but not the underlying software. It should be set to zero again when the underlying software version changes.

**`app.contact-email`** An email address used when a package system requires one. Defaults to `nobody@${app.site.host}` (where the host may be taken from the `app.site.base-url` URL).

**`app.rdns-name`** A reverse DNS (Java style) name for the app package. Some operating systems like to use such names for metadata. The exact value doesn't matter much and it doesn't have to correspond to a real website. By default one is created for you based on the site download URL.

## Icons

```
# Import some PNG files of different sizes/resolutions for the icon
# and alter the name pattern that it looks for.
app.inputs = "images/myapp-icon-*.png"
app.icons = "myapp-icon-*.png"
```

**`app.icons`** A list of PNG images taken from the inputs. The default is `icons-*.png`, so just adding files with this kind of name to the `app.inputs` list is enough. The images must be square 
and the dimensions must be a power of 2 between 16 and 1024 e.g. 256x256. It's a good idea to provide at least a few different resolutions so scaling of the icon between sizes is smoother. Windows also wants images sized 44x44 and 150x150.

Icons can be machine specific. Import the right icons for each platform the machine-specific [inputs](inputs.md), and possibly set `app.{windows,mac,linux}.icons` keys if you want to change the name pattern searched for.

!!! note
    Consider using actually different images for small sizes that have less detail in them, rather than just scaling down a larger image. Scaling a large image to small sizes can yield blurry images.

## Download site settings

The `site` section controls the generation of the download site. Currently you must have a download site because it's where the packages will check for online updates, although you don't need to use the generated HTML file. [Learn more](download-pages.md).

## Target machines

**`app.machines`** A list of target machines for which packages should be built. The full list is:

* `windows.amd64`
* `windows.aarch64`
* `mac.amd64`
* `mac.aarch64`   (Apple Silicon)
* `linux.amd64.glibc`
* `linux.amd64.muslc`
* `linux.aarch64.glibc`
* `linux.aarch64.muslc`

You can also identify machines with fewer than all components, for example `windows` is equivalent to Windows for all supported CPU architectures, and `linux.amd64` specifies that you want two packages, one for each C library found on Linux (muslc is used on Alpine Linux).

Normally you don't need to fill this key out. Reasonable defaults will be calculated for you based on whether your config lists inputs for those machine types. As a consequence, you can select what machines you want either additively:

```
// Only Linux, nothing else even if we could potentially build it
app.machines = [ linux.amd64 ]
```

... or subtractively ...

```
app {
  // Everything we have configured except macOS targets.
	mac = null
}
```

How to choose?

* Setting `app.machines` explicitly means you're opting in to new platforms and formats. Pick this for best control.
* Nulling out sections means you're opting out of existing platforms and formats. Pick this if you don't want to support a particular type of machine for some reason, but are OK with automatically getting as many packages as possible other than that.

## Signing

**`app.sign`** A default value for `app.mac.sign` and `app.windows.sign`, which control whether or not to digitally sign executables. If this is true (the default) and you don't have signing certificates in your Conveyor data directory then you'll see a notice explaining what to do.

**`app.signing-key`** Should be set to a random value plus the date on which it was generated, formatted as follows:

```
app.signing-key = "9a65621705d4ecb4a570f42167f757a04604a532b7b0834ea76fdc275166f8e1/2022-01-14T17:49:47+01:00"
```

A line of config like this can be generated with the `conveyor keys generate` command (see [Keys and certificates](../keys-and-certificates.md)) and then included into your main config (don't check it into version control). The random value isn't used as a private key directly, it's used to derive any other keys that aren't explicitly provided. The following keys can be derived from this root entropy (randomness) are:

1. Windows and Mac code signing keys.
2. PGP keys for signing Debian apt repositories.
3. An EdDSA key for signing Sparkle (Mac) update repositories.

**`app.mac.signing-key`**,**`app.windows.signing-key`** The private key to use for each platform. If they're set to `derived` (the default) then derivation from the root private key is used and these specific keys aren't stored on disk, otherwise the value should contain the path to a standard PEM or PKCS#12 key store file relative to the app config file.

**`app.mac.certificate`**, **`app.windows.certificate`** Should point to files containing the certificates file in PEM (ASCII) or PKCS#12 format. Other raw binary formats are also supported but JKS format isn't. The paths are relative to the app config file and they default to `{windows,apple}.cer` respectively.

## Character encodings

By default your application will be configured to use UTF-8 universally on all platforms, regardless of what character encoding the user's host system is set to. On Windows the custom JVM launcher Conveyor uses will configure the terminal appropriately so Unicode works (when the encoding is set to UTF-8).

This behaviour can be controlled using several keys:

**`app.character-encoding`** By default "UTF-8". Acts as defaults for the other platform specific settings. Set to "host" to use whatever the operating system is configured to.

**`app.jvm.file-encoding`** Controls the `-Dfile.encoding` system property. See [JEP 400: UTF-8 by default](https://openjdk.java.net/jeps/400) for more details about this and how it changes in Java 18.

**`app.windows.manifests.exe.code-page`** Sets the code page in [the Win32 manifest](https://docs.microsoft.com/en-us/windows/win32/sbscs/application-manifests#activecodepage). Any setting _other_ than UTF-8 will only be used starting with Windows 11, and thus this setting exists primarily to handle the complex details of Windows' transition to UTF-8 by default.

**`app.windows.manifests.msix.code-page`** Sets the code page in the MSIX manifest. Any setting other than UTF-8 is equivalent to not setting it at all, preserving the default behaviour of using the operating system default encoding.

!!! warning Windows apps that have already been deployed
    This default is designed to simplify the lives of people distributing new apps. If you're switching a pre-existing Windows app that has been deployed to user machines before using Conveyor, and you did not already force your app to use UTF-8, then your users may have files or file names encoded in non UTF-8 encodings. This may especially cause compatibility issues for Asian Windows users. If this is the case, then you should set the `app.character-encoding` key to `host`, which means the app will use whatever the user's operating system is set to. Be aware though that this will cause (or continue) other kinds of bugs if users exchange files or data that aren't encoded the same way.
