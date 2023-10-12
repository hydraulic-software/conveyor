# Conveyor vs alternative Electron tools

!!! tip "See for yourself"
    Learn how we packaged [GitHub Desktop with Conveyor](https://hydraulic.dev/blog/8-packaging-electron-apps.html), a production-grade Electron app. Over 1,500 lines of code can be deleted, the resulting config is easy to read, and the app can be packaged and released from your laptop. We show how to combine it with GitHub Releases, Sites and Actions.

## Unique features of Conveyor compared to Electron autoUpdater

* CI friendly, can package/sign/deploy for every OS from any machine including dev laptops or Linux CI workers. [Learn more about releasing from CI using cloud code signing](https://hydraulic.dev/blog/21-shipping-electron-apps-from-ci-using-hsm-certificates.html).
* Updates can be Chrome-style (background, silent) or web-style (immediate on launch, synchronous).
* No code changes to your app are needed.
* Many improvements for Windows:
    * Users won't need to download Electron again if they have any existing Electron app from the MS Store (or that was Conveyorized).
    * Network/admin friendly (doesn't install everything into user's home dirs).
    * Updates apps in the background even when they're not running.
    * Can deploy straight to the Microsoft Store (saves money because MS sign apps for you, in this mode).
    * Fast, declarative, interaction-free installs.
* No special update servers needed. Static HTTP / S3 / GitHub Releases are fine.
* Makes a download HTML page for you.

## Simple

* One way to do things that all works out of the box, not dozens of half-maintained plugins. 
* Convenient declarative JSON superset that can include JSON files.
* Lots of features to make signing easy and simple including self signing, cloud HSMs, CSR generation for purchase and so on.

## Alternative update engines

Conveyor doesn't use Squirrel for updates. On macOS the popular and robust [Sparkle 2](https://sparkle-project.org/) framework is used. On Windows the Windows 10/11 native next-generation MSIX package format is used. For Ubuntu/Debian users there's an apt repository.

This is useful because [Squirrel is hardly maintained](https://github.com/Squirrel/Squirrel.Windows/issues/1470) and has serious design issues like installing into the user's *roaming* home directory (meaning it can break Windows networks). Also, [Squirrel.Mac didn't have a release since 2017 and was completely abandoned in 2021](https://github.com/Squirrel/Squirrel.Mac). Switching to Sparkle (which is what Conveyor uses) [was requested by users](https://github.com/electron/electron/issues/29057) but got no response.

## Docs and commercial support

Electron Forge [isn't fully documented (e.g. for the config keys)](https://www.electronforge.io/config/makers/appx). Conveyor has extensive docs and samples.

If you're a commercial user you can file support tickets with us and we'll help you work through any issues or bugs that may arise from the packaging process.
