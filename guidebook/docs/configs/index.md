# Writing config files

There are many possible settings in Conveyor configs, but almost all of them can be ignored if you follow certain conventions because they'll be derived from the others. Any default you don't like can be fixed by adding the key yourself.

An app is defined using the HOCON configuration language. HOCON is a superset of JSON and thus all JSON is valid HOCON, but it comes with a variety of syntax features to make writing configuration files easier. Conveyor then adds [additional features](hocon-extensions.md) like brace expansion in string lists, per-user default files, access to environment variables and the ability to [include the results of external programs](maven-gradle.md). This lets you make dynamic configs that are partly or completely computed on the fly. You can read the [HOCON spec](hocon-spec.md) to learn how to write this small language and how to use the [Conveyor extensions](hocon-extensions.md).

To understand every setting that went into your app run the `conveyor json` command and examine the output. Because HOCON is a superset of JSON you can use this result in place of the original config, but we don't recommend that because you'll lose the smartly calculated defaults. Using raw JSON as a config file is normally only useful if you're generating it from a tool.

## Extending the default settings

When working with lists it's good style to always add to them rather than overriding them. If you assign directly importing other configs from (for example) the Conveyor standard library won't work as you'll overwrite the values they place in shared lists. In other words write `key += value` rather than `key = [ value ]`. The first form adds to whatever `key` currently holds, the second replaces it.

## Per-user defaults

Config placed in the following paths will be merged into every build file:

* **Windows:** `%USERPROFILE%\Hydraulic\Conveyor\defaults.conf`
* **Linux:** `~/.config/hydraulic/conveyor/defaults.conf`
* **macOS:** `~/Library/Preferences/Hydraulic/Conveyor/defaults.conf`

## Compatibility levels

The `conveyor.compatibility-level` key will be added automatically on first run if you don't specify it. If you use a key added in a newer version of Conveyor you'll be asked to increase your compatibility level to enable it's usage, and this will stop old versions from processing the config (which they would not fully understand). If new versions of Conveyor make changes that would be backwards incompatible, they will only take effect if you opt in by incrementing your compatibility level. Otherwise, the old behaviors will be preserved.

## License keys

To use Conveyor you must meet one of the following criteria:

1. **Testing:** `app.site.base-url` is set to `http://localhost` (any port). This is the mode used in the tutorial and is for trying things out / playing around.
2. **Open source:** `app.vcs-url` is set to the URL of your source repository. The project found there must match the one you're packaging and must be open source. This can be a git URL, GitHub URL, Mercurial URL etc. If it's a GitHub URL then the download site URL will default to the latest GitHub release for that app.
3. **Licensed:** `conveyor.license-key` contains an eight character license key (like `aaaa-bbbb`). This lets you package a proprietary application. To get a license key simply change your site URL to something that's not localhost and do a build. A new key will be generated and put in the config file for you, and a payment link will be provided. You can manage your subscription from the [customer portal](https://billing.stripe.com/p/login/4gwcPrdGX08u4ww7ss).

Because Conveyor is licensed per-project license keys are associated with site URLs. It's OK to change your site URL, so you can fix typos or switch from a private to public location. If you change to a new URL and then change back again, you'll get an error because this looks like using one key for multiple projects. If you're not trying to share keys but still need to change your site URL to an older one, please email [contact@hydraulic.software](mailto:contact@hydraulic.software) and we'll sort it out.

## Minimal app

Apps are built from [inputs](inputs.md). A config must specify at least one input file and the address where the downloads will be served from (as the packages need to know this to configure online updates). Archives can be extracted and will be by default. If the file name follows a conventional form all other app metadata can be derived from it:

```
app { 
  windows.amd64.inputs = win/example-app-1.1.zip
  mac.amd64.inputs = mac/intel/example-app-1.1.zip
  mac.aarch64.inputs = mac/arm/example-app-1.1.zip
  
  site { 
    base-url = yourserver.net/downloads    # Where your downloads and update files will be served from.
  }
}
```

This will generate packages for Windows, Mac Intel and Mac ARM from the contents of the zip files, using the name `example-app`, `Example App` and version `1.1` in the appropriate places.

All configuration Conveyor pays attention to goes under the `app` hierarchy, but you can place keys outside this area to hold your own variables.

## Names and metadata

**`app.version`** A version string made up of numbers separated by dots. Words like `-beta`, `SNAPSHOT` etc are not allowed, because this version number is used by update systems to determine if a new version is available.

**`app.revision`** The version of the config file itself. This can be incremented if you change the package definition but not the underlying software. It should be set to zero again when the underlying software version changes.

**`app.rdns-name`** A reverse DNS (Java style) name for the app package. Some operating systems like to use such names for metadata. The exact value doesn't matter much and it doesn't have to correspond to a real website. By default, one is created for you based on the site download URL but this is unlikely to be ideal.

**`app.fsname`** and **`app.long-fsname`**. An *fsname* is a name that appears on the file system. The fsname of an app is used for forming default values for:

* Package names.
* Directories where logs, caches and persistent data should be stored. 
* Executable names usable from the command line.

The fsname will be derived automatically from the file name of the first input, if that's in a conventional form (see above). You shouldn't put any version numbers in your fsname: it should consist only of words.

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

**`app.contact-email`** An email address used when a package system requires one. Defaults to `nobody@${app.site.host}` (where the host may be taken from the `app.site.base-url` URL).

## Icons

```
# Import some PNG files of different sizes/resolutions for the icon
# and alter the name pattern that it looks for.
app.icons = "myapp-icon-*.png"
```

**`app.icons`, `app.{windows,mac,linux,site}.icons`** An [input definition](inputs.md) that should import square PNG images. The default is `icons-*.png` for all platforms, so just having files with this name pattern next to your `conveyor.conf` will work. The images must be sized as a power of 2 between 16 and 1024 e.g. 256x256. It's a good idea to provide at least a few different resolutions so scaling of the icon between sizes is smoother. Windows also wants images sized 44x44 and 150x150. It can be useful to have different icons for each OS to match the platform native styles.

!!! warning
    Conveyor currently won't rescale your images for you. Therefore you must supply at least one image <= 512x512 as larger images won't be reduced, and Windows requires smaller images in some circumstances. Also, consider using actually different images for small sizes that have less detail in them, rather than just scaling down a larger image. Scaling a large image to small sizes can yield blurry images.

## Download site settings

The `site` section controls the generation of the download site. Currently you must have a download site because it's where the packages will check for online updates, although you don't need to use the generated HTML file. [Learn more](download-pages.md).

## Machines

**`app.machines`** A list of target machines for which packages should be built. At this time the following machines are supported:

* `windows.amd64`
* `mac.amd64` (Intel Macs)
* `mac.aarch64`   (Apple Silicon)
* `linux.amd64.glibc`

Additional supported targets may be added in future, in particular ARM Linux distributions and muslc based Linux distributions.

You can also write just `mac`, which expands to both Intel and Apple Silicon Macs. However if you request a short form like `windows` or `linux` then you'll get an error, because that expands to also request `windows.aarch64`, which isn't currently supported. To avoid being surprised by Conveyor updates that add such support, which could create packages targeting a CPU architecture you didn't want, you're required to be explicit about what you wish for here.

Normally you don't need to fill this key out. Reasonable defaults will be calculated for you based on whether your config lists inputs for those machine types and whether the relevant config section for that OS is nulled out. As a consequence, you can select what machines you want either additively:

```
// Only Intel Linux, nothing else even if we could potentially build it
app.machines = [ linux.amd64.glibc ]
```

... or subtractively ...

```
app {
  // Everything we can support, except macOS.
	mac = null
}
```

How to choose?

* Setting `app.machines` explicitly means you're opting in. Pick this for best control.
* Nulling out sections means you're opting out. Pick this if you don't want to support a particular type of machine, but are OK with automatically getting newly supported package types other than that.

## Signing

For full information about signing, see [Keys and certificates](../keys-and-certificates.md).

**`app.sign`** A default value for `app.mac.sign` and `app.windows.sign`, which control whether or not to digitally sign executables. If this is true (the default) and you don't have signing certificates in your Conveyor data directory then you'll see a notice explaining what to do.

**`app.signing-key`** Should be set to a random string plus the date on which it was generated. A typical signing key will be encoded like this:

```
app.signing-key = "loud apology vital team rent champion better pluck cargo love knee tornado tomato man mammal lake sick possible ozone giggle suggest sail aunt multiply/2022-08-09T12:07:08Z"
```

A piece of config like this can be generated with the `conveyor keys generate` command (see [Keys and certificates](../keys-and-certificates.md)). It will normally be placed in your `defaults.conf` file, but can be placed anywhere or imported from the environment (see the [HOCON extensions](hocon-extensions.md) page for details). The randomly chosen words aren't used as a private key directly. Instead they're used to derive any other keys that aren't explicitly provided. The following keys can be derived from this root entropy (randomness) are:

1. Windows and Mac code signing keys.
2. PGP keys for signing Debian apt repositories.
3. An EdDSA key for signing Sparkle (Mac) update repositories.

**`app.mac.signing-key`**,**`app.windows.signing-key`** The private key to use for each platform. If they're set to `derived` (the default) then derivation from the root private key is used and these specific keys aren't stored on disk, otherwise the value should contain the path to a standard PEM or PKCS#12 key store file relative to the app config file.

**`app.mac.certificate`**, **`app.windows.certificate`** Should point to files containing the certificates. See [Keys and certificates](../keys-and-certificates.md) for information on supported formats. The paths are relative to the app config file and they default to `{windows,apple}.cer` respectively. Alternatively, these can be set like this: `app.mac.certificate = "self signed by CN=Nobody in particular"` (the default value is this but using your display name). In that case a self-signed certificate is deterministically derived from the root key. 

!!! note
    The words used to encode the signing key come from a predefined dictionary, so you can't choose your own sentences here. This encoding makes the key easier to write down with a pen and paper, which can be a low tech but reliable way to back it up.

## Update modes

Conveyor supports three different approaches to software updates:

1. Background mode: the default.
2. Aggressive mode. Set `app.updates = aggressive`.
3. None. Set `app.updates = none`.

In aggressive mode an update check will be performed synchronously on each app start. If a new version is available then the update process will start and the update downloaded and applied, without any user interaction being required. In background mode the behaviour differs by OS:

* Windows: the OS itself will check for updates every 8 hours and upgrade the app in the background, even if it's not being used. Your users will never see an update prompt.
* macOS: the app will check for updates on startup without blocking the user, and on a schedule whilst the app runs. Once the user agrees, updates will be downloaded and applied in the background ready for the next launch.

Which mode to use depends heavily on how often your users will start the app and how important it is for updates to be applied quickly. If your app is a client for a server that speaks a complex protocol and you don't want to preserve protocol backwards compatibility, aggressive mode is appropriate. If your app is self-contained or the protocols it speaks evolve in a compatible way, background mode gives a better user experience as the user won't be interrupted by the update process.

!!! info
    - **Updates are only checked on startup.** Users can leave their app running for long periods, so the server might change whilst instances are running. Your protocol should ideally check on each request if the client is out of date and return an error if so that tells the user to restart the app (or does it for them). Note that this is actually no different to a web app because users can leave tabs open for long periods, so if you roll out a new web server version that renames an endpoint you might break users who are using the app at that time, necessitating either a way to detect that (hard) or simply telling users to reload tabs if they break.
    - **Your app will start a bit slower.** This is of course also no different to a web app, which will also check with a web server on every startup. If the update check takes longer than two seconds then a progress UI will appear.

### Aggressive updates on Linux

Aggressive updates aren't implemented on Linux because it's unconventional for apps to control their own update process on this platform.

If distributing to Linux you should check [the `metadata.properties` file](download-pages.md#exporting-to-metadataproperties) that's
generated as part of your download/update site (`conveyor make site`) by looking at the `app.version` key, and then ask the user to 
update themselves if they've fallen behind in your UI.

??? info "Checking for new versions in JVM apps"
    When packaged the `app.repositoryUrl` system property will contain the site base URL, and `app.version` contains the content of the
    `app.version` key. Note: this does not include the revision, as that's meant only for changes to the package itself not the contained
    software, but `app.revision` is also set if you want to compare against that too. So to implement an update check on Linux add 
    `/metadata.properties` to the value of that system property, download it over HTTPS, parse it with the `java.util.Properties` class,
    extract the `app.version` key from the parsed file and then compare it with the `app.version` system property. The 
    [`ComparableVersion`](https://github.com/apache/maven/blob/master/maven-artifact/src/main/java/org/apache/maven/artifact/versioning/ComparableVersion.java)
    class from the Maven source code is an easy way to compare version numbers; it can be copy/pasted into your source tree. If the app
    is behind then hide the main screen and show a message to the user asking them to upgrade their package by using apt-get. 

??? info "Checking for new versions in Electron apps"
    After fetching the `metadata.properties` file the [dot-properties](https://github.com/eemeli/dot-properties) package can be used to 
    parse it, or you can just do some simple string manipulation: split to lines, drop any line starting with `#`, split each
    line by `=`, convert to a map and (optionally, if you're adding custom keys) un-backslash-escape the values. Supporting the full
    specification isn't necessary for parsing this file. To learn what the current version use your `package.json` file
    include your site URL in that file and then in.

??? info "Checking for new versions in other apps"
    Although there is [a precise specification for the `.properties` format](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Properties.html#load(java.io.Reader)),
    there's no need to fully implement it. The format can be treated as a simple set of `key=value` pairs in which lines starting with `#`
    are to be ignored, and the values should be un-backslash-escaped before use.

## Character encodings

By default, your application will be configured to use UTF-8 universally on all platforms, regardless of what character encoding the user's host system is set to. On Windows the custom JVM launcher Conveyor uses will configure the terminal appropriately so Unicode works (when the encoding is set to UTF-8).

This behaviour can be controlled using several keys:

**`app.character-encoding`** By default "UTF-8". Acts as defaults for the other platform specific settings. Set to "host" to use whatever the operating system is configured to.

**`app.jvm.file-encoding`** Controls the `-Dfile.encoding` system property. See [JEP 400: UTF-8 by default](https://openjdk.java.net/jeps/400) for more details about this and how it changes in Java 18.

**`app.windows.manifests.exe.code-page`** Sets the code page in [the Win32 manifest](https://docs.microsoft.com/en-us/windows/win32/sbscs/application-manifests#activecodepage). Any setting _other_ than UTF-8 will only be used starting with Windows 11, and thus this setting exists primarily to handle the complex details of Windows' transition to UTF-8 by default.

**`app.windows.manifests.msix.code-page`** Sets the code page in the MSIX manifest. Any setting other than UTF-8 is equivalent to not setting it at all, preserving the default behaviour of using the operating system default encoding.

!!! warning Windows apps that have already been deployed
    This default is designed to simplify the lives of people distributing new apps. If you're switching a pre-existing Windows app that has been deployed to user machines before using Conveyor, and you did not already force your app to use UTF-8, then your users may have files or file names encoded in non UTF-8 encodings. This may especially cause compatibility issues for Asian Windows users. If this is the case, then you should set the `app.character-encoding` key to `host`, which means the app will use whatever the user's operating system is set to. Be aware though that this will cause (or continue) other kinds of bugs if users exchange files or data that aren't encoded the same way.

## Additional settings

There are many other settings available. Check out the sections on the left to learn more about what you can configure.
