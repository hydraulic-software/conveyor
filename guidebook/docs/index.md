---
hide:
- toc
---

# Welcome

Conveyor makes distributing desktop and command line apps as easy as shipping a web app. It's a tool not a service, it generates and signs [self-upgrading packages for Windows, macOS and Linux using each platform's native package formats](outputs.md) without requiring you to have those operating systems, and it looks like this:

<video width="100%" poster="https://conveyor.hydraulic.dev/assets/promo.jpg" controls><source src="https://conveyor.hydraulic.dev/assets/promo.mp4" type="video/mp4"></video>

## Try an app that uses Conveyor

[Download Eton Notes](https://downloads.hydraulic.dev/eton-sample/download.html){ .md-button .md-button--primary }   [Download Eton Notes (self-signed version)](https://downloads.hydraulic.dev/eton-sample/selfsigned/download.html){ .md-button .md-button--primary }

<video width="100%" playsinline autoplay muted loop><source src="https://downloads.hydraulic.dev/eton-sample/eton.mp4" type="video/mp4"></video>

[Eton Notes](https://github.com/hydraulic-software/eton-desktop) is a simple open source mockup of a note-taking app that uses Material Design and the [Jetpack Compose Desktop](https://www.jetbrains.com/lp/compose-desktop/) toolkit, which is a desktop port of the new Android GUI framework.

## Get started

[ :material-arrow-up-box: Get set up!](download-conveyor.md){ .md-button .md-button--primary } [ :material-forum: Discussion forum](https://github.com/hydraulic-software/conveyor/discussions){ .md-button .md-button--primary } [ :material-chat: Chat room](https://hydraulic.zulipchat.com/#narrow/stream/329916-general){ .md-button .md-button--primary }

## Features

* **Create packages for every OS on any OS.** Conveyor implements everything itself so doesn't rely on platform native tooling.
    * Build Windows applications that use the built-in [Windows 10 MSIX/AppInstaller system](outputs.md).
        * Windows keeps them up to date in the background automatically.
        * Updates use delta downloads and data is shared between apps, even from different vendors.
        * Has everything IT departments need to easily deploy to managed networks.
    * Build Mac applications that use the popular [Sparkle 2 update framework](https://sparkle-project.org/).
        * Sparkle is automatically used, without needing any code changes in your apps.
    * Build apt repositories for Debian/Ubuntu, tarballs for other distros. Integrates with systemd for servers and cron jobs.
* **Automatic icon conversion.**
    * Supply a set of PNGs and Conveyor turns them into the platform specific formats for you. 
* **Generate a static download site.**
    * Detects the user's operating system and CPU architecture.
    * [Release via GitHub releases](configs/download-pages#publishing-through-github).
* **Brainless code signing.**
    * Sign your apps with Apple/Windows certificates for a better download UX, or ignore it and get self-signed packages with a `curl | bash` style install.    
    * You can sign/notarize apps on any OS. 
    * You can backup your single root key by writing it down as words on paper.
* **Pre-made template projects.**
    * CMake + OpenGL to demonstrate building and packaging C++ native apps that use third party dependencies.
    * Jetpack Compose Desktop and JavaFX for cross-platform JVM GUI apps.
    * Electron Quick Start for building Chromium based apps.
* **Package apps using any runtime or framework.**
    * No code changes are necessary to benefit from the integrated auto update.
    * On Windows/Linux the OS takes care of updates, on macOS the Sparkle framework is injected into your binary and automatically initialized by Conveyor.
* **[Deep integration for JVM applications](configs/jvm.md).**
    * Uses JLink and jdeps to minimize the JDK size.
    * Import configuration from Maven and Gradle build systems.
    * Pre-canned template apps. Publish your first app in five minutes.
* **Support for Electron apps.** (*beta!*)
    * Benefit from well maintained platform native software updates without relying on Squirrel or any centralized update servers.
* **Pierce the abstraction!** Cross platform tooling doesn't mean giving up platform-specific features. Over 120 different settings let you precisely configure your packages, including your:
    * Mac `Info.plist` files.
    * Windows XML app manifests.
    * Linux `.desktop` files and package install/uninstall scripts.
