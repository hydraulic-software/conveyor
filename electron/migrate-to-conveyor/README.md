# Migrate to Conveyor

This module provides a simple way to migrate your users from a Squirrel installation into a new package built with [Conveyor](https://www.hydraulic.dev).

Conveyor is an easier and more powerful alternative to Electron Forge/Builder/Installer. It can build self-updating packages for all desktop
operating systems from any OS, can do background and forced-on-launch updates and has many other useful features.

**[ ➡️ Tutorial](https://conveyor.hydraulic.dev/latest/tutorial/hare/electron/)**

**[ ➡️ Read the docs](https://conveyor.hydraulic.dev)**

**[ ➡️ Say hello via chat](https://discord.gg/E87dFeuMFc)**

**[ ➡️ Say hello using GitHub Discussions](https://github.com/hydraulic-software/conveyor/discussions)**

## How to migrate your app from Squirrel to Conveyor

To start, your app using Squirrel should have code similar to the following:

```javascript
if(require('electron-squirrel-startup')) return;
require('update-electron-app')();

// The rest of the code of your app.
```

And your `package.json` file will have references to Electron Forge:

```json
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

### Step 1: Launch a Conveyor packaged version of your app

1. Remove the lines from your app that handle Squirrel updates:
 
    ```diff
    - if(require('electron-squirrel-startup')) return;
    - require('update-electron-app')();
   
    // The rest of the code of your app.
    ```
   
2. Remove the dependencies on those packages from your `package.json`:

    ```diff
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
3. Bump the version of your app in `package.json`. Run `npm i` to make sure the change will be reflected in `package-lock.json`.

4. Make sure you have [Conveyor](http://hydraulic.dev) installed. You can install globally with:

    ```shell
    npm i -g @hydraulic/conveyor
    ```
   
    or install locally for your project (make sure to make it a dev dependency):

    ```shell
    npm i -D @hydraulic/conveyor
    ```
   
5. Create a `conveyor.conf` to package your app with Conveyor. You can follow the instructions in the [tutorial](https://conveyor.hydraulic.dev/latest/tutorial/hare/electron).
   
   **IMPORTANT**: Make sure that the `app.site.base-url` you use for the Conveyor version of your app is different than the distribution site used for Squirrel.
   This is necessary to avoid conflicts.

6. Release a Conveyor packaged version of your app:

    ```shell
    npx conveyor make copied-site
    ```

### Step 2: Launch a Squirrel packaged version of your app to migrate existing user to the new app. 

1. Install this package as a dependency of your app:

    ```shell
    npm i @hydraulic/migrate-to-conveyor
    ```

2. Download the Conveyor generated Windows installer for your app. It's the `.exe` file in your release site 
   (e.g., `https://github.com/my-user/my-project/releases/latest/download/my-app.exe`). Place it along the rest of your package.
   
3. Add the following snippet at the top of your entry point script:

    ```diff
    + const path = require('path');
    + if (require('@hydraulic/migrate-to-conveyor')({
    +   // Replace 'my-app.exe' below with the path to the Windows installer downloaded in the previous step, relative
    +   // to the root of your module.
    +   windowsInstaller: path.join(__dirname, 'my-app.exe')
    + })) return;    
   
      // The rest of the code of your app.
    ```   

4. Publish a Squirrel version of the app using Forge:

    ```shell
    npm run publish
    ```
   
And that's it! On the next update for the Squirrel user base, the Conveyor installer will be downloaded in the background, and if you have
it configured users will be prompted to restart the app. On the next app launch, the Conveyor installer will kick in, showing an installation UI,
and the Squirrel version will be uninstalled in the background. The experience is similar to what Conveyor users observe when receiving an update in 
aggressive mode.

This version of the app will be just a bridging version, so you could also remove the rest of the code of your app to make it shorter.
Also notice that it's safe to use the `@hydraulic/migrate-to-conveyor` even in the Conveyor packaged code, so if necessary both
distributions can share the code, for convenience.

### Step 3: Clean up Forge dependencies.

At this point you can remove dependencies on Electron Forge, and also the migration script:

* `package.json`:

    ```diff
    {

      // ...
  
      "scripts": {
   -    "start": "electron-forge start",
   +    "start": "electron .",
   -    "package": "electron-forge package",
   -    "make": "electron-forge make",
   -    "publish": "electron-forge publish"
   +    "publish": "conveyor make copied-site"
      },
      "devDependencies": {
   -    "@electron-forge/cli": "^6.4.2",
   -    "@electron-forge/maker-deb": "^6.4.2",
   -    "@electron-forge/maker-rpm": "^6.4.2",
   -    "@electron-forge/maker-squirrel": "^6.4.2",
   -    "@electron-forge/maker-zip": "^6.4.2",
   -    "@electron-forge/plugin-auto-unpack-natives": "^6.4.2",
   -    "@electron-forge/publisher-github": "^6.4.2",
        "@hydraulic/conveyor": "^11.4.0",
        "electron": "^26.2.2"    
      },
      "dependencies": {
   -    "@hydraulic/migrate-to-conveyor": "^1.0.0"
      }
      
      // ...
      
    }
    ```

* Your app:

     ```diff
    - const path = require('path');
    - if (require('@hydraulic/migrate-to-conveyor')({
    -   // Replace 'my-app.exe' below with the path to the Windows installer downloaded in the previous step, relative
    -   // to the root of your module.
    -   windowsInstaller: path.join(__dirname, 'my-app.exe')
    - })) return;    
   
      // The rest of the code of your app. 
     ```

All updates coming from Squirrel will download and install whatever the latest Conveyor release is, so a single last Squirrel release is enough.
