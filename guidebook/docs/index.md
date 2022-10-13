# Getting started

[ :material-arrow-up-box: Start tutorial](tutorial/1-get-started.md){ .md-button .md-button--primary } [ :material-forum: Discussion forum](https://github.com/hydraulic-software/conveyor/discussions){ .md-button .md-button--primary }

Conveyor makes distributing desktop and command line apps as easy as shipping a web app. It's a tool not a service, it generates and signs [self-upgrading packages for Windows, macOS and Linux using each platform's native package formats](outputs.md) without requiring you to have those operating systems, and it looks like this:

<video width="100%" poster="https://conveyor.hydraulic.dev/assets/promo.jpg" controls><source src="https://conveyor.hydraulic.dev/assets/promo.mp4" type="video/mp4"></video>

## Features

* **Create packages for every OS on any OS.** Conveyor implements everything itself so doesn't rely on platform native tooling.
    * Build applications that use the built-in [Windows MSIX package manager](outputs.md).
        * Windows keeps them up to date in the background.
        * Installs and updates reuse data blocks and hard link files, even between apps from different vendors.
        * Has everything IT departments need to easily deploy to managed networks.
    * Build Mac applications that automatically use the popular [Sparkle 2 update framework](https://sparkle-project.org/), without code changes.
    * Build apt repositories for Debian/Ubuntu, tarballs for other distros. Integrates with systemd for servers and cron jobs.
* **[Support for Electron apps](configs/electron.md).**
    * Benefit from well maintained platform native software updates without relying on Squirrel or any centralized update servers.
    * Import config directly from your `package-lock.json` file.
* **[Support for JVM apps](configs/jvm.md).**
    * Bundles a JVM from a vendor of your choice, and then uses jlink/jdeps to minimize the size.
    * Uses a native launcher that [adds useful features](configs/jvm.md#launcher-features).
    * Import configuration from Maven and Gradle.
* **Automatic icon conversion.**
    * Supply a set of PNGs and Conveyor turns them into the platform specific formats for you. 
* **Generates download sites.**
    * Static HTML detects the user's operating system and CPU architecture.
    * [Release via GitHub releases](configs/download-pages#publishing-through-github).
* **Brainless code signing.**
    * Sign your Windows/Mac downloads for a better UX, or ignore it and get self-signed packages with a `curl | bash` style install.    
    * You can sign and notarize on any OS. 
    * You can back up your single root key by writing it down as words on paper.
* **Pre-made template projects** for native C++, Electron, JavaFX (JVM) and Jetpack Compose Desktop (JVM).
* **Pierce the abstraction!** Cross-platform tooling doesn't mean giving up platform-specific features. Over 120 different settings let you precisely configure your packages, including your:
    * Mac `Info.plist` files.
    * Windows manifests.
    * Linux `.desktop` files and package install/uninstall scripts.

## Try sample apps that use Conveyor

### Compose Multiplatform

<video width="100%" playsinline autoplay muted loop style="margin-left: -50px"><source src="https://www.hydraulic.software/assets/images/video/Eton.mp4" type="video/mp4"></video>

<img src="images/compose-multiplatform.svg" style="float: left; padding-right: 1em"></img>

[Download Eton Notes (fully signed)](https://downloads.hydraulic.dev/eton-sample/download.html){ .md-button .md-button--primary }

[Download Eton Notes (self signed)](https://downloads.hydraulic.dev/eton-sample/selfsigned/download.html){ .md-button .md-button--primary }

<br style="clear: right" />

[Eton Notes](https://github.com/hydraulic-software/eton-desktop) is an open source example app written in Kotlin. It uses Material Design and the [Jetpack Compose](https://www.jetbrains.com/lp/compose-desktop/) reactive UI toolkit, which is the new standard Android GUI toolkit and also has a desktop port supported by JetBrains.

### AtlantaFX Sampler

<video width="100%" playsinline autoplay muted loop style="margin-left: -70px"><source type="video/mp4" src="https://www.hydraulic.software/assets/images/video/AtlantaFX-1.1.mp4"/></video>

[Download AtlantaFX sampler](https://downloads.hydraulic.dev/atlantafx/sampler/download.html){ .md-button .md-button--primary }

[AtlantaFX](https://github.com/mkpaz/atlantafx) is an open source theme for JavaFX that implements a modern design language using the GitHub Primer color system. The sampler app provides a gallery of the available controls and stylings. It's written in Java with Maven. Read [the blog post](https://hydraulic.software/blog/3-atlantafx-sampler.html) to learn how this app was packaged.
