# Migrating Electron apps to Conveyor

Porting an app that's already deployed using Electron Forge can be done in a few steps. This page documents how to set up a migration for an
app that uses the Squirrel update engine to Conveyor. The user will experience a brief pause on startup once the migration begins in which
the Conveyorized version will be downloaded/installed, and the previous version uninstalled.

## Preparation

You should already have tested your app when packaged with Conveyor. Try creating packages and installing them on each OS you support, to
ensure your app works correctly. Make sure to pay attention to [operating system integration](configs/os-integration.md), which works
differently in Conveyor.

If you're looking for an example of how to do this conversion, [check out our tutorial on porting Github Desktop](https://www.hydraulic.dev/blog/8-packaging-electron-apps.html).

The rest of this document assumes you have a fully working Conveyorized version of your app and now wish to migrate an existing installed
user base from Squirrel.

## Starting point

Your current app using Squirrel should have code similar to the following:

```javascript title="Your app"
if(require('electron-squirrel-startup')) return;
require('update-electron-app')();

// The rest of the code of your app.
```

And your `package.json` file will have references to Electron Forge:

```json title="package.json"
{

  // ...
  
  "scripts": {
    "start": "electron-forge start",
    "package": "electron-forge package",
    "make": "electron-forge make",
    "publish": "electron-forge publish"
  },
  "devDependencies": {
    "@electron-forge/cli": "^6.4.2",
    "@electron-forge/maker-deb": "^6.4.2",
    "@electron-forge/maker-rpm": "^6.4.2",
    "@electron-forge/maker-squirrel": "^6.4.2",
    "@electron-forge/maker-zip": "^6.4.2",
    "@electron-forge/plugin-auto-unpack-natives": "^6.4.2",
    "@electron-forge/publisher-github": "^6.4.2",
    "electron": "^26.4.0"
  },
  "dependencies": {
    "electron-squirrel-startup": "^1.0.0",
    "update-electron-app": "^2.0.1"
  }
  
  // ...
  
}
```

## Step 1: Release a Conveyor packaged version of your app

Remove the lines from your app that handle Squirrel updates:

```diff title="Your app"
- if(require('electron-squirrel-startup')) return;
- require('update-electron-app')();

// The rest of the code of your app.
```

Remove the dependencies on those packages from your `package.json`:

```diff title="package.json"
{

  // ...
  
  "scripts": {
    "start": "electron-forge start",
    "package": "electron-forge package",
    "make": "electron-forge make",
    "publish": "electron-forge publish"
  },
  "devDependencies": {
    "@electron-forge/cli": "^6.4.2",
    "@electron-forge/maker-deb": "^6.4.2",
    "@electron-forge/maker-rpm": "^6.4.2",
    "@electron-forge/maker-squirrel": "^6.4.2",
    "@electron-forge/maker-zip": "^6.4.2",
    "@electron-forge/plugin-auto-unpack-natives": "^6.4.2",
    "@electron-forge/publisher-github": "^6.4.2",
    "electron": "^26.4.0"
  },
  "dependencies": {
-    "electron-squirrel-startup": "^1.0.0",
-    "update-electron-app": "^2.0.1"
  }
  
  // ...
  
}
```

Bump the version of your app in `package.json`. Run `npm i` to make sure the change will be reflected in `package-lock.json`.

Make sure you have [Conveyor](http://hydraulic.dev) installed. You can install globally with `npm i -g @hydraulic/conveyor`
or install locally for your project (make sure to make it a dev dependency): `npm i -D @hydraulic/conveyor`

Create a `conveyor.conf` to package your app with Conveyor. You can follow the instructions in the [tutorial](https://conveyor.hydraulic.dev/latest/tutorial/hare/electron).

!!! important 
    Make sure that the `app.site.base-url` you use for the Conveyor version of your app is different than the distribution 
    site used for Squirrel. This is necessary to avoid conflicts.

=== ":fontawesome-brands-apple: macOS" 
    Add the following to your `conveyor.conf` file:
    
    ```diff title="conveyor.conf"
      include required("/stdlib/electron/electron.conf")
      
      package-json {
        include required("package-lock.json")
      }
        
    + app.mac.aarch64.bundle-extras += ${squirrel.aarch64}
    + app.mac.amd64.bundle-extras += ${squirrel.amd64}
      
      app {
        // ... Your app config ...
      }
    ```
    
    This will include the Squirrel framework into your app. It is necessary so that your app gets launched correctly after transitioning on macOS.

    You should make sure that the Conveyor packaged app is signed with same certificate used to sign the Squirrel packaged app.
    Also make sure that following items match:
    
    | Item | Conveyor config key | Forge `packagerConfig` |
    | ---- | ------------------- | -------------------- | 
    | Bundle ID | `app.mac.info-plist.CFBundleIdentifier` (defaults to `app.rdns-name`) | `appBundleId` (defaults to `"com.electron.${name}"`) |
    | Application name | `app.display-name` | `name` (defaults to either `productName` or `name` from `package.json`) |
    | Main executable | `app.fsname` | `executableName` (defaults to the same value as `name`) |

=== ":fontawesome-brands-windows: Windows"

You can now release a Conveyor packaged version of your app. Nobody will know about it yet and nothing will happen to existing installs,
but you can start to point new users there:

 ```shell
 npx conveyor make copied-site
 ```

## Step 2: Start the migration

=== ":fontawesome-brands-apple: macOS"

    We provide an open source Electron Forge plugin to assist in the migration. Install this package as a dev dependency of your app:

    ```shell
    npm i -D @hydraulic/override-zip
    ```

    Download the Conveyor generated macOS .zip files for your app from your release site (e.g., `https://github.com/my-user/my-project/releases/latest/download/my-app-1.0.0-mac-amd64.zip`
    and `https://github.com/my-user/my-project/releases/latest/download/my-app-1.0.0-mac-aarch64.zip`). Place them along the rest of your package.

    Edit your Forge config with the following, adjusting `my-app-*.zip` to fit the name and version of your Conveyor generated zips:

    ```diff title="forge.config.js"    
      module.exports = {
        // ...
        plugins: [
    +     {
    +       name: '@hydraulic/override-zip',
    +       config: {
    +         zipPaths: {
    +           // TODO: Replace the zip file names below.
    +           arm64: 'my-app-1.0.0-mac-aarch64.zip',
    +           x64: 'my-app-1.0.0-mac-amd64.zip',
    +         },
    +       },
    +     },
          // ...
        ],
        // ...
      }
    ```

    This plugin will simply replace the `.zip` files produced by [@electron-forge/maker-zip](https://www.npmjs.com/package/@electron-forge/maker-zip) with the zips
    provided via the zipPaths config. This will make Squirrel install the Conveyor packaged app on the next update.

=== ":fontawesome-brands-windows: Windows"

    We provide an open source utility package to assist in the migration. Install this package as a dependency of your app:
    
    ```shell
    npm i @hydraulic/migrate-to-conveyor
    ```
    
    Download the Conveyor generated Windows installer for your app. It's the `.exe` file in your release site
    (e.g., `https://github.com/my-user/my-project/releases/latest/download/my-app.exe`). Place it along the rest of your package.
    
    Add the following snippet at the top of your entry point script, adjusting `my-app.exe` to fit the name of your installer EXE:
    
    ```diff title="Your app"
    + const path = require('path');
    + if (require('@hydraulic/migrate-to-conveyor')({
    +   // TODO: REPLACE 'my-app.exe' BELOW.
    +   windowsInstaller: path.join(__dirname, 'my-app.exe')
    + })) return;    
    
      // The rest of the code of your app.
    ```

    This version of the app will be just a bridging version, so you could also remove the rest of the code of your app to make it smaller.
    Also notice that it's safe to use the `@hydraulic/migrate-to-conveyor` even in the Conveyor packaged code, so if necessary both distributions 
    can share the code, for convenience. It also has no effect on macOS apps, so even macOS distributions can share the code.

Publish a Squirrel version of the app using Forge:

```shell
npm run publish
```

And that's it! The next time users accept an update the Conveyor installer will show a download/installation UI, and on success the 
Squirrel version will be uninstalled in the background. The experience is similar to what Conveyor users observe when receiving an update in
aggressive mode.

## Step 3: Clean up

=== ":fontawesome-brands-apple: macOS"

    At this point you can remove dependencies on Electron Forge, and also the migration plugin:
    
    ```diff title="package.json"
    {
    
      // ...
    
      "scripts": {
    -   "start": "electron-forge start",
    +   "start": "electron .",
    -   "package": "electron-forge package",
    -   "make": "electron-forge make",
    -   "publish": "electron-forge publish"
    +   "publish": "conveyor make copied-site"
      },
      "devDependencies": {
    -   "@electron-forge/cli": "^6.4.2",
    -   "@electron-forge/maker-deb": "^6.4.2",
    -   "@electron-forge/maker-rpm": "^6.4.2",
    -   "@electron-forge/maker-squirrel": "^6.4.2",
    -   "@electron-forge/maker-zip": "^6.4.2",
    -   "@electron-forge/plugin-auto-unpack-natives": "^6.4.2",
    -   "@electron-forge/publisher-github": "^6.4.2",    
        "@hydraulic/conveyor": "^11.4.0",
    -   "@hydraulic/override-zip": "^1.0.0",
        "electron": "^26.4.0"    
      },
      "dependencies": {
      }
      
      // ...
      
    }
    ```

    You can also remove the `forge.config.js` file.
    
    All updates coming from Squirrel will download and install the Conveyor pacakged app released above, which will then receive updates via Sparkle,
    so a single last Squirrel release is enough.

=== ":fontawesome-brands-windows: Windows"

    At this point you can remove dependencies on Electron Forge, and also the migration script:
    
    ```diff title="package.json"
    {
    
      // ...
    
      "scripts": {
    -   "start": "electron-forge start",
    +   "start": "electron .",
    -   "package": "electron-forge package",
    -   "make": "electron-forge make",
    -   "publish": "electron-forge publish"
    +   "publish": "conveyor make copied-site"
      },
      "devDependencies": {
    -   "@electron-forge/cli": "^6.4.2",
    -   "@electron-forge/maker-deb": "^6.4.2",
    -   "@electron-forge/maker-rpm": "^6.4.2",
    -   "@electron-forge/maker-squirrel": "^6.4.2",
    -   "@electron-forge/maker-zip": "^6.4.2",
    -   "@electron-forge/plugin-auto-unpack-natives": "^6.4.2",
    -   "@electron-forge/publisher-github": "^6.4.2",
        "@hydraulic/conveyor": "^11.4.0",
        "electron": "^26.4.0"    
      },
      "dependencies": {
    -    "@hydraulic/migrate-to-conveyor": "^1.0.0"
      }
      
      // ...
      
    }
    ```
    
    ```diff title="Your app"
    - const path = require('path');
    - if (require('@hydraulic/migrate-to-conveyor')({
    -   // Replace 'my-app.exe' below with the path to the Windows installer downloaded in the previous step, relative
    -   // to the root of your module.
    -   windowsInstaller: path.join(__dirname, 'my-app.exe')
    - })) return;    
    
      // The rest of the code of your app. 
    ```
    
    All updates coming from Squirrel will download and install whatever the latest Conveyor release is, so a single last Squirrel release is enough.
