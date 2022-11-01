# Electron

!!! tip
    The [tutorial](../tutorial/1-get-started.md) shows you how to generate and package a simple Electron app.

## Overview

There are several packaging tools and update engines that can be used with Electron, however they all have a variety of problems:

* [Squirrel.Windows is only intermittently maintained](https://github.com/Squirrel/Squirrel.Windows/issues/1470), has been abandoned several times in the past and has serious design issues like installing into the user's *roaming* home directory (meaning it can break Windows networks). 
* [Squirrel.Mac didn't have a release since 2017 and was completely abandoned in 2021](https://github.com/Squirrel/Squirrel.Mac). Switching to Sparkle (which is what Conveyor uses) [was requested by users](https://github.com/electron/electron/issues/29057) but got no response. 
* Electron update solutions often require a custom server, or the use of a centralized update service which only works for open source apps.
* They sometimes point you to [doc pages that don't document the config keys](https://www.electronforge.io/config/makers/appx).

Conveyor uses [native updates on Windows/Linux, Sparkle 2 on macOS, and regular HTTP servers](../outputs.md).

The other standard features of Conveyor can also eliminate hassle:

* No need to find machines for each target OS to build packages. You can make packages for every OS on your laptop.
* Use more convenient config syntax and features than raw JSON.
* Handles all the details of code signing without native tools, which are often awkward to use.
* Can self-sign if you don't want to use signing keys. Other tools make unsigned apps if you don't have keys, but these don't integrate with the host operating system as well.
* You get a self-contained download site with HTML that detects your users operating system and CPU.
* It's fully documented.

Conveyor treats Electron apps mostly the same as native apps, meaning that no code changes are necessary to get working software updates.

## Synopsis

A basic `conveyor.conf` looks like this:

```
// You must import the electron stdlib config.
include required("/stdlib/electron/electron.conf")

// You may also import your package.json file. Defaults will be set from it when possible.
package-json {
    include required("package.json") 
}

// Override the Electron version. This is otherwise taken from your package.json file. 
app.electron.version = 19.0.1

// Change where it's fetched from. The default is GitHub.
app.electron.download-base-url = github.com/electron/electron/releases/download/
```

## Keys

**`app.electron.version`** If set then this config is for an Electron app, and this is the version of Electron to bundle. 

**`app.electron.download-base-url`** Where to find Electron builds to download. The URLs are composed like this: 

```
${app.electron.download-base-url}/v${app.electron.version}/electron-v${app.electron.version}-$os-$cpu.zip
```

so they must follow the same layout as that used on GitHub. You can specify a `file:` URL here if necessary.

## App resources

The stdlib `electron.conf` file will import the following files to your `resources/app` directory:

- `*.json`
- `*.js`
- `*.css`
- `*.html`
- The `node_modules` directory.

You can add more [inputs](inputs.md) or replace the default set like this:

```
app {
    // Add more top level files.
    inputs += "*.ts"
    
    // Add a  directory preserving the placement. 
    // (otherwise, the contents are copied to the top level).
    inputs += node_modules -> node_modules
    
    // Replace the inputs entirely.
    inputs = [ "app.asar" ]
}
```

## Caveats

Be aware of the following caveats:

* There's no API to control or monitor updates yet. Note that such an API doesn't necessarily make sense on some platforms e.g. Linux where the user's package manager will apply updates, or on Windows where the app can be updated silently in the background when it's not running.
* Conveyor doesn't make ASAR files at the moment, so all files will be shipped unpacked. If your app consists of very large numbers of small files this may reduce performance. You could use a bundler, or [make an asar file yourself](https://github.com/electron/asar) and supply it as an input to work around this limitation.
* You should remove any code that invokes Squirrel.

## stdlib config

The standard Electron config you're asked to import looks like this:

```hocon
app {
  fsname = ${?package-json.name}
  display-name = ${?package-json.productName}
  version = ${?package-json.version}
  electron.version = ${?package-json.packages.node_modules/electron.version}
  contact-email = ${?package-json.author.email}

  // Electron doesn't place the binary in a bin directory, so we have to fix that up here.
  linux {
    desktop-file."Desktop Entry".Exec = ${app.linux.install-path}/${app.fsname}
    symlinks += ${app.linux.prefix}/bin/${app.fsname} -> ${app.linux.install-path}/${app.fsname}
  }

  mac {
    info-plist {
      NSSupportsAutomaticGraphicsSwitching = true
      NSRequiresAquaSystemAppearance = false
      NSHighResolutionCapable = true
      NSQuitAlwaysKeepsWindows = false

      LSEnvironment {
        MallocNanoZone = "0"
      }
      NSAppTransportSecurity {
        NSAllowsArbitraryLoads = true
      }
      NSBluetoothAlwaysUsageDescription = This app needs access to Bluetooth
      NSBluetoothPeripheralUsageDescription = This app needs access to Bluetooth
      NSCameraUsageDescription = This app needs access to the camera
      NSMainNibFile = MainMenu
      NSPrincipalClass = AtomApplication
    }
  }

  // A simple default input.
  inputs = ${app.inputs} [
    "*.{json,js,ts,css,html}"

    {
      from = node_modules
      to = node_modules
      remap = [ "-electron/dist/**" ]
    }
  ]
}
```
