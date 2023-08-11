# Basic configuration

Most `conveyor.conf` files are small because Conveyor infers sensible defaults for you. Follow the tutorials to get a minimal working config file.

Configs use the [HOCON configuration language](hocon.md). HOCON is a superset of JSON and thus all JSON is valid HOCON, but it comes with a variety of syntax features to make writing configuration files easier.

## Seeing JSON

To understand every setting that went into your app run the `conveyor json` command and examine the output. Because HOCON is a superset of JSON you can use this result in place of the original config, but we don't recommend that because you'll lose the smartly calculated defaults. Using raw JSON as a config file is only really useful if you're generating it from a tool.

## Per-user defaults

Config placed in the following location will be merged into every build file:

* **Windows:** `%USERPROFILE%\Hydraulic\Conveyor\defaults.conf`
* **Linux:** `~/.config/hydraulic/conveyor/defaults.conf`
* **macOS:** `~/Library/Preferences/Hydraulic/Conveyor/defaults.conf`

!!! note "Other files"
    Only `defaults.conf` is read from this directory. If you want other bits of config there too, you will need to use an `include` statement. Paths in config files are resolved relative to the config file in which they are found.

## Compatibility levels

The `conveyor.compatibility-level` key will be added automatically on first run if you don't specify it. If you use a key added in a newer version of Conveyor you'll be asked to increase your compatibility level to enable it's usage, and this will stop old versions from processing the config (which they would not fully understand). If new versions of Conveyor make changes that would be backwards incompatible, they will only take effect if you opt in by incrementing your compatibility level. Otherwise, the old behaviors will be preserved.

## License keys

To use Conveyor you must meet one of the following criteria:

1. **Testing:** `app.site.base-url` is set to `http://localhost` (any port). This is the mode used in the tutorial and is for trying things out / playing around.
2. **Open source:** `app.vcs-url` is set to the URL of your source repository. The project found there must match the one you're packaging and must be open source. This can be a git URL, GitHub URL, Mercurial URL etc. If it's a GitHub URL then the download site URL will default to the latest GitHub release for that app.
3. **Licensed:** `conveyor.license-key` contains an eight character license key (like `aaaa-bbbb`). This lets you package a proprietary application. To get a license key simply change your site URL to something that's not localhost and do a build. A new key will be generated and put in the config file for you, and a payment link will be provided. You can manage your subscription from the [customer portal](https://billing.stripe.com/p/login/4gwcPrdGX08u4ww7ss).

Because Conveyor is licensed per-project license keys are associated with site URLs. It's OK to change your site URL, so you can fix typos or switch from a private to public location. If you change to a new URL and then change back again, you'll get an error because this looks like using one key for multiple projects. If you're not trying to share keys but still need to change your site URL to an older one, please email [contact@hydraulic.dev](mailto:contact@hydraulic.dev) and we'll sort it out.

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

## Icons

```
app.icons = "myapp-icon-*.png"
app.icons = "myapp-icon.svg"

# Render a simple icon with a label, gradient 
# and macOS compliant rounded corners.
app.icons = {
  label = "AP"
  gradient = "blue;green"
}
```

**`app.icons`, `app.{windows,mac,linux,site}.icons`** An icon definition that can be either:

* An [input definition](inputs.md) that should import either square images or an SVG file.
  You can provide any number of square image files, as Conveyor will rescale the images to provide appropriate image
  sizes for all platforms. You can directly provide the appropriate sizes (see below) to avoid artifacts from resampling.
  Besides `.svg`, currently supported image file types are: `.bmp`, `.gif`, `.png`, `.tif` and `.tiff`. Only images that support
  transparency are allowed.
* An icon generation definition, consisting of the following fields:
    * **`label`** An optional short string containing one or two characters which will be rendered as the icon. If not provided, a
      label is derived from the `app.display-name` key.
    * **`gradient`** An optional string containing one or
      two [CSS style colors](https://developer.mozilla.org/en-US/docs/Web/CSS/color_value)
      separated by a semicolon (;). If not provided, a random gradient color is provided using the `label` as a seed.

  Generated icons have rounded corners on all systems except Windows, where they instead fill the entire icon to comply with the platform style.

The icons are processed and converted into square images of sizes 16, 32, 64, 128, 256, 512 and 1024, as well as 44 and 150 for
Windows. You can provide images with those sizes directly if you prefer to handle rescaling directly. Otherwise, it's a good idea to
provide at least a few different resolutions so scaling of the icon between sizes is smoother.
It can be useful to have different icons for each OS to match the platform native styles.

By default, icons will be generated with a label based on the contents of `app.display-name` with a random gradient background.

## Download site settings

The `site` section controls the generation of the download site. Currently you must have a download site because it's where the packages
will check for online updates, although you don't need to use the generated HTML file. [Learn more](download-pages.md).

## Target platforms

**`app.machines`** A list of target machines for which packages should be built. At this time the following machines are supported:

* `windows.amd64`
* `mac.amd64` (Intel Macs)
* `mac.aarch64` (Apple Silicon)
* `linux.amd64.glibc`

Additional supported targets may be added in future. You can also use shorthand like `mac` on its own to mean both CPU architectures.

## Signing

For full information about signing, see [Keys and certificates](keys-and-certificates.md).

### `app.sign`

 A default value for `app.mac.sign` and `app.windows.sign`, which control whether or not to digitally sign executables. If
this is true (the default) and you don't have signing certificates in your Conveyor data directory then you'll see a notice explaining what
to do.

### `app.signing-key`

 Should be set to a random string plus the date on which it was generated. A typical signing key will be encoded like
this:

```
app.signing-key = "loud apology vital team rent champion better pluck cargo love knee tornado tomato man mammal lake sick possible ozone giggle suggest sail aunt multiply/2022-08-09T12:07:08Z"
``` 

A piece of config like this can be generated with the `conveyor keys generate` command (
see [Keys and certificates](keys-and-certificates.md)). It will normally be placed in your `defaults.conf` file, but can be placed anywhere
or imported from the environment (see the [HOCON extensions](hocon.md#conveyor-extensions) page for details). On macOS, Conveyor will try to store
the random string in the system default login keychain (see [Keys and certificates: Root key in Keyring](keys-and-certificates.md#macos-root-key-in-keyring)).
The randomly chosen words aren't used as a private key directly. Instead, they're used to derive any other keys that aren't explicitly
provided. The following keys can be derived from this root entropy (randomness) are:

1. Windows and Mac code signing keys.
2. PGP keys for signing Debian apt repositories.
3. An EdDSA key for signing Sparkle (Mac) update repositories.

### `app.{mac,windows}.signing-key`

!!! important
    Private keys must be kept **secret**.

The private key to use for each platform. If they're set to `derived` (the default) then derivation from the root private key is used and
these specific keys aren't stored on disk, otherwise the value should contain the path to a standard PEM or PKCS#12 key store file relative
to the app config file, or check the detailed documentation to use a [hardware security
module](keys-and-certificates.md#hardware-security-modules) or a [cloud signing
service](keys-and-certificates.md#cloud-remote-signing-windows-only).

### `app.{mac,windows}.certificate`

!!! important
    Certificates are **public** and can be shared freely, checked into version control etc. They are embedded in the apps you ship.

Should point to files containing the certificates. See [Keys and certificates](keys-and-certificates.md) for information on supported
formats. The paths are relative to the app config file and they default to `{windows,apple}.cer` respectively. Alternatively, these can be
set like this: `app.mac.certificate = "self signed by CN=Nobody in particular"` (the default value is this but using your display name). In
that case a self-signed certificate is deterministically derived from the root key.

!!! note
    The words used to encode the signing key come from a predefined dictionary, so you can't choose your own sentences here. This encoding makes
    the key easier to write down with a pen and paper, which can be a low tech but reliable way to back it up.

## Character encodings

By default, your application will be configured to use UTF-8 universally on all platforms, regardless of what character encoding the user's host system is set to. On Windows the custom JVM launcher Conveyor uses will configure the terminal appropriately so Unicode works (when the encoding is set to UTF-8).

This behaviour can be controlled using several keys:

### `app.character-encoding`

By default "UTF-8". Acts as defaults for the other platform specific settings. Set to "host" to use whatever the operating system is configured to.

### `app.jvm.file-encoding`

Controls the `-Dfile.encoding` system property. See [JEP 400: UTF-8 by default](https://openjdk.java.net/jeps/400) for more details about this and how it changes in Java 18.

### `app.windows.manifests.exe.code-page`

Sets the code page in [the Win32 manifest](https://docs.microsoft.com/en-us/windows/win32/sbscs/application-manifests#activecodepage). Any setting _other_ than UTF-8 will only be used starting with Windows 11, and thus this setting exists primarily to handle the complex details of Windows' transition to UTF-8 by default.

### `app.windows.manifests.msix.code-page`

Sets the code page in the MSIX manifest. Any setting other than UTF-8 is equivalent to not setting it at all, preserving the default behaviour of using the operating system default encoding.

!!! warning Windows apps that have already been deployed
    This default is designed to simplify the lives of people distributing new apps. If you're switching a pre-existing Windows app that has been deployed to user machines before using Conveyor, and you did not already force your app to use UTF-8, then your users may have files or file names encoded in non UTF-8 encodings. This may especially cause compatibility issues for Asian Windows users. If this is the case, then you should set the `app.character-encoding` key to `host`, which means the app will use whatever the user's operating system is set to. Be aware though that this will cause (or continue) other kinds of bugs if users exchange files or data that aren't encoded the same way.

## Misc

### `app.compression-level`

Can be one of `none`, `low`, `medium`, and `high`. For zips, all levels are equal to standard deflate compression
except `none`. On Linux `low` maps to gzip, `medium` to bzip2 and `high` to LZMA. Higher compression levels are much, much slower to build
than lower levels but yield faster downloads for your users. When experimenting it can be convenient to set this to none.
