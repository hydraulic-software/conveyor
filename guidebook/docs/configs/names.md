# Names and metadata

## `app.version`

A version string made up of numbers separated by dots. Words like `-beta`, `SNAPSHOT` etc are not allowed, because this version number is used by update systems to determine if a new version is available.

## `app.revision`

The version of the config file itself. This can be incremented if you change the package definition but not the underlying software. It should be set to zero again when the underlying software version changes.

## `app.rdns-name`

A reverse DNS (Java style) name for the app package. Some operating systems like to use such names for metadata. The exact value doesn't matter much and it doesn't have to correspond to a real website. By default, one is created for you based on the site download URL but this is unlikely to be ideal.

## `app.fsname`, `app.long-fsname`

An *fsname* is a name that appears on the file system. The fsname of an app is used for forming default values for:

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

## `app.display-name`

The name of the software as meant for humans. Will be calculated from the fsname if not specified by simply uppercasing each word.

## `app.vendor`

Holds the display name of the organisation that makes the software.

## `app.description`

A one line summary of what the software does.

## `app.license`

The name of the copyright license the app is under, defaulting to `Proprietary`. Open source apps should use an [SPDX identifier](https://spdx.org/licenses/) here to qualify for a free license.

## `app.contact-email`

An email address used when a package system requires one. Defaults to `nobody@${app.site.host}` (where the host may be taken from the `app.site.base-url` URL).
