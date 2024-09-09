# Conveyor

[Conveyor](https://conveyor.hydraulic.dev) makes distributing desktop apps a whole lot easier:

* **[Sophisticated software updates](https://conveyor.hydraulic.dev/latest/understanding-updates/)**
  * [Platform native package types and update systems](https://conveyor.hydraulic.dev/latest/package-formats/), without any lockin.
  * Chrome-style silent background updates
  * Web-style synchronous update on launch
  * Advanced [delta updates](https://conveyor.hydraulic.dev/15.0/understanding-updates/#delta-updates).
  * No code changes necessary.
* **App framework integration:**
  * [Electron](https://conveyor.hydraulic.dev/latest/configs/electron/): [simpler than Forge/Builder/Squirrel](https://conveyor.hydraulic.dev/15.0/comparisons/electron-comparisons/), generates ASAR files, can read `package.json` files.
  * [JVM](https://conveyor.hydraulic.dev/latest/configs/jvm/): bundles a `jlink` optimized JVM, custom native launchers, many optimizations and usability improvements for desktop apps.
  * [Flutter](https://conveyor.hydraulic.dev/latest/configs/flutter/): easily ship apps that share code with mobile.
* **Excellent support for CI/CD:**
  * Package and deploy directly for _every platform you support_ from any Linux build agent, without needing Mac/Windows workers.
  * Automatic releasing to the Microsoft Store, web servers, S3 or [GitHub releases](https://conveyor.hydraulic.dev/latest/configs/download-pages#publishing-through-github)
  * A [GitHub Action](https://conveyor.hydraulic.dev/latest/continuous-integration/#using-github-actions)
  * Support for [cloud signing services and HSMs](https://conveyor.hydraulic.dev/latest/configs/keys-and-certificates/#cloud-remote-signing-windows-only), eliminating the need for USB Windows signing keys.
* **Easy and powerful code signing:**
  * Can sign/notarize apps for every OS from any OS
  * Generates CSRs for easier purchasing from certificate authorities
  * Can store root keys in the macOS keychain for extra security
  * Handles keys of different formats
  * Self-signing and Microsoft Store support for reducing the cost of certificates
* **Icon generation**, rendering and format conversion
* **Generates a download page for you**
  * Auto-detects the user's OS and CPU architecture
  * "Big Green Button" download UX.
* **Scaffold projects** for native C++, Electron, JavaFX (JVM) and Jetpack Compose Desktop (JVM)
* Pierce the abstraction! Over 120 different settings let you take control of every OS specific detail, or ignore them to accept the smart defaults.

## Try it out

**[ ➡️ Try installing an app that uses it](https://conveyor.hydraulic.dev/13.0/sample-apps/)**

**[ ➡️ Get started!](https://conveyor.hydraulic.dev)**

**[ ➡️ Say hello via chat](https://discord.gg/E87dFeuMFc)**

**[ ➡️ Say hello using GitHub Discussions](https://github.com/hydraulic-software/conveyor/discussions)**

## This repository

This repo contains the parts of the product that are open source:

* [The user guide](https://conveyor.hydraulic.dev/latest/).
* [The Gradle plugin](gradle-plugin/README.md).
* [A GitHub Action](actions/build/README.md).

## Helping out

If you'd like to:

* Contribute improvements to the documentation or Gradle plugin, just open up a pull request.
* Get help privately, [email us](mailto:contact@hydraulic.dev).
* Ask questions, or get help publicly, use [GitHub discussions](https://github.com/hydraulic-software/conveyor/discussions) or [the chat room](https://discord.gg/E87dFeuMFc).
