# Migrating Electron apps to Conveyor

Porting an app that's already deployed using Electron Forge can be done in a few steps. This page documents how to set up a migration for an
app that uses the Squirrel update engine to Conveyor. The user will experience a brief pause on startup once the migration begins in which
the Conveyorized version will be downloaded/installed, and the previous version uninstalled.

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
    "electron": "^26.2.2"
  },
  "dependencies": {
    "electron-squirrel-startup": "^1.0.0",
    "update-electron-app": "^2.0.1"
  }
  
  // ...
  
}
```

## Step 1: Launch a Conveyor packaged version of your app

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
    "electron": "^26.2.2"
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

You can now release a Conveyor packaged version of your app. Nobody will know about it yet and nothing will happen to existing installs,
but you can start to point new users there:

 ```shell
 npx conveyor make copied-site
 ```

## Step 2: Start the migration

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

Publish a Squirrel version of the app using Forge:

```shell
npm run publish
```

And that's it! The next time users accept an update the Conveyor installer will show a download/installation UI, and on success the 
Squirrel version will be uninstalled in the background. The experience is similar to what Conveyor users observe when receiving an update in
aggressive mode.

This version of the app will be just a bridging version, so you could also remove the rest of the code of your app to make it smaller.
Also notice that it's safe to use the `@hydraulic/migrate-to-conveyor` even in the Conveyor packaged code, so if necessary both distributions 
can share the code, for convenience.

## Step 3: Clean up

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
    "electron": "^26.2.2"    
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
