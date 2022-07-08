# Conveyor

[Conveyor](https://conveyor.hydraulic.dev) makes distributing desktop and command line apps as easy as shipping a web app. It's a tool not a
service, it generates [self-upgrading packages for Windows, macOS and Linux using each platform's native package formats](https://conveyor.hydraulic.dev/latest/outputs.md)
without requiring you to have those operating systems.

**[ ➡️ Try installing an app that uses it](https://downloads.hydraulic.dev/eton-sample/download.html)**

**[ ➡️ Get started!](https://conveyor.hydraulic.dev)**

**[ ➡️ Say hello via chat](https://hydraulic.zulipchat.com/#narrow/stream/329916-general)**

**[ ➡️ Say hello using GitHub Discussions](https://github.com/hydraulic-software/conveyor/discussions)**

## This repository

This repo contains the parts of the product that are open source:

* The user guide.
* [The Gradle plugin](gradle-plugin/README.md).

and you can find the code + package config for the Eton Notes sample app in [a separate repository](https://github.com/hydraulic-software/eton-desktop).

## Features

* **Create packages for every OS on any OS.** Conveyor implements everything itself so doesn't rely on platform native tooling.
    * Build Windows applications that use the built-in [Windows 10 MSIX/AppInstaller system](https://conveyor.hydraulic.dev/latest/outputs/).
        * Windows keeps them up to date in the background automatically.
        * Updates use delta downloads and data is shared between apps, even from different vendors.
        * Has everything IT departments need to easily deploy to managed networks.
    * Build Mac applications that use the popular [Sparkle 2 update framework](https://sparkle-project.org/).
        * Sparkle is automatically used, without needing any code changes in your apps.
    * Build apt repositories for Debian/Ubuntu, tarballs for other distros. Integrates with systemd for servers and cron jobs.
* **Generate a static download site.**
    * Detects the user's operating system and CPU architecture.
    * [Release via GitHub releases](https://conveyor.hydraulic.dev/latest/configs/download-pages/#publishing-through-github).
* **Brainless code signing.**
    * Sign your apps with Apple/Windows certificates for a better download UX, or ignore it and get self-signed packages with a `curl | bash` style install.
    * You can sign/notarize apps on any OS.
    * You can backup your single root key by writing it down as words on paper.
* **Pre-made template projects.**
    * CMake + OpenGL to demonstrate building and packaging C++ native apps that use third party dependencies.
    * JetPack Compose Desktop and JavaFX for cross-platform GUI apps.
* **[Deep integration for JVM applications](https://conveyor.hydraulic.dev/latest/configs/jvm/).**
    * Uses JLink and jdeps to minimize the JDK size.
    * Import configuration from Maven and Gradle build systems.
    * Pre-canned template apps. Publish your first app in five minutes.
* **Pierce the abstraction!** Cross platform tooling doesn't mean giving up platform-specific features. Over 120 different settings let you precisely configure your packages, including your:
    * Mac `Info.plist` files.
    * Windows XML app manifests.
    * Linux `.desktop` files and package install/uninstall scripts.

## Helping out

If you'd like to:

* Contribute improvements to the documentation or Gradle plugin, just open up a pull request.
* Get help privately, [email us](mailto:contact@hydraulic.software).
* Ask questions, or get help publicly, use [GitHub discussions](https://github.com/hydraulic-software/conveyor/discussions) or [the chat room](https://hydraulic.zulipchat.com/#narrow/stream/329916-general).
