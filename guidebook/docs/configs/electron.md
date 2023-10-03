# Electron

## Overview

No changes to your JS are necessary to get working software updates with Conveyor. [Curious why you'd use Conveyor over other tools?](../comparisons/electron-comparisons.md)

!!! tip
    The [tutorial](../tutorial/new.md) shows you how to generate and package a simple Electron app.

??? note "Advanced demo: GitHub Desktop"
    Learn how we packaged [GitHub Desktop with Conveyor](https://hydraulic.dev/blog/8-packaging-electron-apps.html), a production-grade Electron app. Over 1,500 lines of code can be deleted, the resulting config is easy to read, and the app can be packaged and released from your laptop. We show how to combine it with GitHub Releases, Sites and Actions.


## Synopsis

A basic `conveyor.conf` looks like this:

```
// This line is required.
include required("/stdlib/electron/electron.conf")

// Set defaults from package-lock when possible.
package-json {
    include required("package-lock.json") 
}

// Override the Electron version. 
app.electron.version = 19.0.1

// Change where it's fetched from. The default is GitHub.
app.electron.download-base-url = github.com/electron/electron/releases/download/
```

## Keys

### `app.electron.version`

The version of Electron to bundle. This must be set for any Electron app. If you don’t set this you may encounter an error message like
“Windows App for Intel: No EXE files were found in the root of the Windows inputs” or something similar.

The simplest option is to set this by hand to whatever Electron you are using.

The next simplest option is to let it be taken from your `package-lock.json` file, if you have one. It will be set to the value of
`package-json.packages.node_modules/electron.version` if you `include required("/stdlib/electron/electron.conf")` and also include your
`package-lock.json` file under the `package-json` key, as seen above. 

If you don't have a `package-lock.json` file that's OK, you can still read the Electron version from any other file. For example, you can specify an exact version instead of a range in `package.json` and then import that instead:

    package-json {
        include required("package.json")
    }

    app {
        electron.version = ${package-json.devDependencies.electron}
    }

#### Reading the Electron version from yarn config

There are a couple of ways to do this.

You can convert your `yarn.lock` to `package-lock.json` using [synp](https://github.com/imsnif/synp).

Importing config from non-JSON files is easy because Conveyor can run programs and read their stdout as part of loading a config file, meaning you can compute config from scripts. This can be used to read the Electron version we're using from the `yarn.lock` file.

Let's put this into the file `get-electron-version.sh` (you'd have to translate it to PowerShell if building on Windows)

```shell
#!/usr/bin/env bash
yarn list --depth=0 --json -s --no-progress 2>/dev/null | jq -r '.data.trees[] | select(.name | startswith("electron@")) | .name | split("@")[1]'
```

Mark it executable (`chmod +x get-electron-version.sh`) and it should print out the locked version of Electron you're using. Now we can import that into our `conveyor.conf`:

    include "#!=app.electron.version get-electron-version.sh"


### `app.electron.download-base-url`

Where to find Electron builds to download. The URLs are composed like this: 

```
${app.electron.download-base-url}/v${app.electron.version}/electron-v${app.electron.version}-$os-$cpu.zip
```

so they must follow the same layout as that used on GitHub. You can specify a `file:` URL here if necessary.

### `app.electron.asar`

If present, your app will be packaged as using [Electron's archive format](https://github.com/electron/asar). Can be set to one of:
   * `true`: (the default), pack the app into `app.asar` using the default options.
   * `false`: do not pack the app into `app.asar`. 
   * an object containing the following fields:
      * `ordering`: path to a text file for ordering the contents of the `app.asar` package.
      * `unpack`: files matching this [minimatch glob expression](https://github.com/isaacs/minimatch#features) will be placed in a directory named `app.asar.unpacked`.
      * `unpack-dir`: directories matching this [minimatch glob expression](https://github.com/isaacs/minimatch#features) will be placed in a directory named `app.asar.unpacked`.
      * `version`: which version of the `@electron/asar` package will be used, defaults to `3.2.7`

      Those are simply passed down as flags to the `asar` command.
    
If your inputs already include a pre-built `app.asar` file, this key will have no effect.

### `app.electron.prune`

If set to `true` (the default), doesn't include dev dependencies from `node_modules` into the packaged app.

If your inputs already include a pre-built `app.asar` file, this key will have no effect.

## App resources

The default config imported from `/stdlib/electron/electron.conf` will import the following files to an ASAR file (see above) or 
your `resources/app` directory:

- `*.json`
- `*.js`
- `*.css`
- `*.html`
- The `node_modules` directory.

You may need to adjust or replace the inputs to suit your app. For example, if you use a bundler like Webpack you may want to import the
output of that instead of the source files. See [inputs](inputs.md) for more information.

## Adapting a project that used `npx create-electron-app`

The easiest way to get started with Conveyor and Electron is to create a fresh project using `conveyor create electron com.example.my-app`,
replacing the reverse DNS name with one that is unique to your project (e.g. `io.github.username.projectname`). [Learn more in the Electron
tutorial](../tutorial/hare/electron.md).

If you have a project that was already created using `npx create-electron-app` you'll need to adapt it for Conveyor. We'll be removing
references to Electron Forge and Squirrel.

1. Run `npx create-electron-app`.
2. `rm forge.config.js`, this file isn't needed anymore.
3. In `package.json`:
   - Remove references to electron-forge and Squirrel. Conveyor doesn't use Squirrel for updates.
   - Replace script "start" with "electron ."
4. In `src/index.js`, remove the reference to Squirrel.
5. `rm -rf node_modules && npm install`. This will get rid of the packages you don't need any more.

Now add a new `conveyor.conf` file and adapt it for your needs:

```hocon
include required("/stdlib/electron/electron.conf")

// Import metadata from your package.json file, like your fsname, version and which version of Electron to use.
package-json {
  include required("package-lock.json")
}

app {
  display-name = "My project"
  rdns-name = org.example.my-project
  site.base-url = "localhost:3000"

  inputs = [
    package.json
    src -> src    

    {
      from = node_modules
      to = node_modules      
      remap = [ "-electron/dist/**" ]
    }
  ]
}
```

## Caveats

Be aware of the following caveats:

* There's no API to control or monitor updates yet. Note that such an API doesn't necessarily make sense on some platforms e.g. Linux where the user's package manager will apply updates, or on Windows where the app can be updated silently in the background when it's not running.
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
