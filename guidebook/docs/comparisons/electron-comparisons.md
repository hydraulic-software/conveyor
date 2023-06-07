# Conveyor vs alternative Electron tools

There are several reasons to consider using Conveyor vs Electron Forge/Builder/Installer.

!!! tip "See for yourself"
    Learn how we packaged [GitHub Desktop with Conveyor](https://hydraulic.dev/blog/8-packaging-electron-apps.html), a production-grade Electron app. Over 1,500 lines of code can be deleted, the resulting config is easy to read, and the app can be packaged and released from your laptop. We show how to combine it with GitHub Releases, Sites and Actions.

## Simple

* Conveyor provides *one* way to do things, which we then optimize the heck out of. Other tools force you to make many decisions up front (e.g. formats) with no guidance on what will provide the best end-user experience. Especially if you're new to desktop development, Conveyor should be less overwhelming. 
* You get a self-contained download site with optional "big green button" HTML that detects your user's operating system and CPU.
* Compatible with regular static web servers, Amazon S3 compatible hosting or GitHub Releases. The Electron Forge documentation states that [you will need to run a special update server](https://www.electronforge.io/advanced/auto-update) if you want to have software updates. Although they provide a free service it's for open source projects only, and the servers you can run are mostly abandoned.
* Use a convenient declarative JSON superset, whilst still importing `package.json` to avoid duplicate metadata.
* Can self-sign packages.
* Handles all the details of code signing for you, even generating certificate request files that can be uploaded to Apple and Windows CAs. All your keys are derived from a single root key. Can use USB dongles for signing with EV certificates.
* You can package and release for every OS from your laptop, because Conveyor doesn't simply wrap native tools: it does everything itself.

## Alternative update engines

Conveyor doesn't use Squirrel for updates. On macOS the popular and robust [Sparkle 2](https://sparkle-project.org/) framework is used. On Windows the Windows 10/11 native next-generation MSIX package format is used. Conveyor enhances and works around bugs in older versions of Windows to give you the best possible experience. MSIX is what enables background updates and file data sharing.

This is useful because [Squirrel.Windows is only intermittently maintained](https://github.com/Squirrel/Squirrel.Windows/issues/1470), has been abandoned several times in the past and has serious design issues like installing into the user's *roaming* home directory (meaning it can break Windows networks). Also, [Squirrel.Mac didn't have a release since 2017 and was completely abandoned in 2021](https://github.com/Squirrel/Squirrel.Mac). Switching to Sparkle (which is what Conveyor uses) [was requested by users](https://github.com/electron/electron/issues/29057) but got no response.

## Features

### Two different upgrade modes

**Background:**

  * Your app will be upgraded by Windows in the background even if it's not running at all, Chrome style, so it's always fresh and you don't need to prompt the user.
  * On macOS your app will silently upgrade itself in the background. The update will take effect at next start.

**Aggressive:**

  * Force apps to check for upgrades automatically on every start. If an update is available your app will update and relaunch without requiring the user to click anything.
  * Update UI and progress tracking is provided for you.

On Linux the DEB will be upgraded automatically as part of the regular OS update procedure. The user doesn't have to register `apt` repositories or use the command line, because that's done for them when they install the DEB.

### Declarative OS integration

Register URL handlers, notification center callbacks etc all using simple config. If Conveyor doesn't have built-in support for the integration you need, you can [add custom config keys to control OS specific metadata](../configs/os-integration.md). 

### Optimized Windows downloads

On Windows the install process will re-use data blocks from any other Electron apps the user has installed via the MSIX packaging system. Downloads and updates are done using the same Background Intelligent Transfer Subsystem that Windows Update uses, so transfers will respect the user's bandwidth and connection metering, as well as pulling data from other nearby machines if they have it. 

## Documentation and commercial support

Electron Forge [isn't fully documented (e.g. for the config keys)](https://www.electronforge.io/config/makers/appx). Conveyor has extensive docs and samples.

If you're a commercial user you can file support tickets with us and we'll help you work through any issues or bugs that may arise from the packaging process.
