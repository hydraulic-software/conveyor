# Electron

## Overview

!!! warning
    Electron support is in beta, so you may encounter problems and missing features. It has only been tested with Electron 19. Please let us know in the [discussion forum](https://github.com/hydraulic-software/conveyor/discussions) or [chat rooms](https://hydraulic.zulipchat.com/#narrow/stream/329916-general) how you get along.

When using Conveyor to package [Electron](https://www.electronjs.org) apps the results differ slightly from other tools. There are several update engines that can be used with Electron, however they all have a variety of problems:

* [Squirrel.Windows is only intermittently maintained](https://github.com/Squirrel/Squirrel.Windows/issues/1470), has been abandoned several times in the past and has serious design issues like installing into the user's *roaming* home directory (meaning it can break Windows networks). 
* [Squirrel.Mac didn't have a release since 2017 and was completely abandoned in 2021](https://github.com/Squirrel/Squirrel.Mac). Switching to Sparkle (which is what Conveyor uses) [was requested by users](https://github.com/electron/electron/issues/29057) but got no response. 
* Electron update solutions often require a custom server, or the use of a centralized update service which only works for open source apps.

**Conveyor uses platform native updates or Sparkle 2 on macOS, and regular HTTP servers.**

The other standard features of Conveyor can also save a lot of time and hassle:

* No need to find machines for each target OS to build packages. You can make packages for every OS on your laptop.
* Use more convenient config syntax and features than raw JSON.
* Handles all the details of code signing without native tools, which are often awkward to use.
* Can self-sign if you don't want to use signing keys. Other tools make unsigned apps if you don't have keys, but these don't integrate with the host operating system as well.
* You get a self-contained download site with HTML that detects your users operating system and CPU.
* It's fully documented. Other Electron packaging tools sometimes point you to [doc pages that are 404s](https://www.electronforge.io/config/makers/appx).

Conveyor treats Electron apps mostly the same as native apps, meaning that no code changes are necessary to get working software updates.

## Synopsis

```
// You must import the electron stdlib config, otherwise things won't work properly.
include required("/stdlib/electron/electron.conf")

// You must also import your package-lock.json file because the defaults will be set based on it.
package-lock {
    include required("package-lock.json") 
}

// Override the Electron version. 
app.electron.version = 19.0.1

// Change where it's downloaded from. The default is GitHub:
app.electron.download-base-url = github.com/electron/electron/releases/download/ 
```

## Keys

**`app.electron.version`** If set then this config is for an Electron app, and this is the version of Electron to bundle. 

**`app.electron.download-base-url`** Where to find Electron builds to download. The URLs are composed like this: 

```
${app.electron.download-base-url}/v${app.electron.version}/electron-v${app.electron.version}-$os-$cpu.zip
```

so they must follow the same lahyout as that used on GitHub. You can specify a `file:` URL here if necessary.

## App resources

The contents of your base inputs will be copied to the `resources/app` directory. Thus, you should import at least a `package.json`,
an `index.html` and so on as inputs. You should also import your `node_modules` directory to the same location. In Conveyor, inputs are
by default always placed at the top level so you need to specify the destination explicitly. A simple example is like this:

```
app {
  inputs = ${app.inputs} [
    "*.{json,js,css,html}"

    {
      from = node_modules
      to = node_modules
      remap = [ "-electron/dist/**" ]
    }
  ]
}
```

It's important to drop the `dist` directory of the `electron` module because it contains a copy of the Electron Mac app, but that will 
interfere with notarization and isn't necessary. You can exclude other kinds of files from your `node_modules` here too. See the
[inputs section](inputs.md) for more information on remap specs and the input syntax used above.

## Caveats

Support for Electron is in beta. Be aware of the following caveats:

* There's no API to control or monitor updates. Note that such an API doesn't necessarily make sense on some platforms e.g. Linux where the user's package manager will apply updates, or on Windows where the app can be updated silently in the background.
* It has only been tested with Electron 19.
* Conveyor doesn't yet make ASAR files, so all files will be shipped unpacked. If your app consists of very large numbers of small files this may reduce performance. You can [make an asar file yourself](https://github.com/electron/asar) and supply it as an input to work around this limitation.
* You can't run any code during installation/uninstallation on Windows or macOS (only on Linux and only when using native packages).
* Conveyor supports fewer package formats.
