# Conveyor

[![Gitter](https://badges.gitter.im/hydraulic-software/community.svg)](https://gitter.im/hydraulic-software/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

8[Conveyor](https://conveyor.hydraulic.dev) makes distributing desktop and command line apps as easy as shipping a web app. It's a tool not a
service, it generates [self-upgrading packages for Windows, macOS and Linux using each platform's native package formats](https://conveyor.hydraulic.dev/latest/outputs.md)
without requiring you to have those operating systems.

**[ ➡️ See what it looks like ](https://conveyor.hydraulic.dev/assets/promo.mp4)**

**[ ➡️ Get started!](https://conveyor.hydraulic.dev)**

## This repository

Conveyor is free to use for open source projects, otherwise it requires a license. This repo contains the parts of the product that are
open source:

* The user guide.
* [The Gradle plugin](gradle-plugin/README.md).

and you can find the code + package config for the music sample app below in [a separate repository](https://github.com/hydraulic-software/compose-music-app).

## Example

**[ ➡️ Try installing an app that uses it](https://downloads.hydraulic.dev/compose-sample/download.html)**

![Screenshot of the Music Sample](https://media.giphy.com/media/NMLgK1lJ8UGtNxx3ja/giphy.gif)

It uses the new JetPack Compose Desktop UI toolkit to mock up a slick, Material Design based music app inspired by Spotify. Credit 
to [Gurupreet Singh](https://github.com/Gurupreet) for the original code that it's based on. 

## Features

* Create packages for every OS on any OS - Conveyor implements everything itself so doesn't rely on platform native tooling.
    * Build Windows applications that use the built-in [Windows 10 MSIX/AppInstaller system](https://conveyor.hydraulic.dev/latest/outputs.md).
        * Apps launch direct from the web.
        * Windows keeps them up to date in the background automatically.
        * Has everything IT departments need to easily deploy to managed networks.
    * Build Mac applications that use the popular [Sparkle 2 update framework](https://sparkle-project.org/).
        * Signing and notarization without needing macOS.
    * Build apt repositories for Debian/Ubuntu.
        * Integrates with systemd for servers and cron jobs.
        * Servers automatically (re)started on upgrade/reboot.
* Generate a static download site that detects the user's operating system and CPU architecture.
* [Deep support for JVM applications](https://conveyor.hydraulic.dev/latest/outputs.md#jvm-applications).
* Pierce the abstraction! Cross platform tooling doesn't mean giving up platform specific features. You can precisely configure OS specific files to get the perfectly tuned experience, such as:
    * Mac `Info.plist` files
    * Windows XML app manifests
    * Linux `.desktop` files and package install/uninstall scripts.

## Helping out

If you'd like to contribute improvements to the documentation or Gradle plugin, just open up a pull request.

If you'd like to report a bug or get help privately, please [email us](mailto:contact@hydraulic.software).

If you'd like to ask questions, get help publicly, or volunteer to sign open source packages, use [GitHub discussions](https://github.com/hydraulic-software/conveyor/discussions) or [the chat room](https://gitter.im/hydraulic-software/community).
