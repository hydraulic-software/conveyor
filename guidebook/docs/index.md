---
hide:
- toc
---

# Welcome

Conveyor makes distributing desktop and command line apps as easy as shipping a web app. It's a tool not a service, it generates and signs [self-upgrading packages for Windows, macOS and Linux using each platform's native package formats](outputs.md) without requiring you to have those operating systems, and it looks like this:

<video width="100%" poster="https://conveyor.hydraulic.dev/assets/promo.jpg" controls><source src="https://conveyor.hydraulic.dev/assets/promo.mp4" type="video/mp4"></video>

## Try it

![Screenshot of the Music Sample](https://media.giphy.com/media/NMLgK1lJ8UGtNxx3ja/giphy.gif)

[Try an app that uses it](https://downloads.hydraulic.dev/compose-sample/download.html){ .md-button .md-button--primary }   [Try the self-signed version](https://downloads.hydraulic.dev/compose-sample/self-signed/download.html){ .md-button .md-button--primary }

Conveyor doesn't care how you do your UI, but this example uses the [JetPack Compose Desktop](https://www.jetbrains.com/lp/compose-desktop/) toolkit to mock up a slick, Material Design based music app. [Try installing it](https://public.hq.hydraulic.software/~mike/compose-music-sample/download.html) and then [get the source and packaging config](https://github.com/hydraulic-software/compose-music-app). Or use the `conveyor generate` command to create an out-of-the-box template app.

## Get started

[ :material-arrow-up-box: Get set up!](setting-up.md){ .md-button .md-button--primary } [ :material-forum: Discussion forum](https://github.com/hydraulic-software/conveyor/discussions){ .md-button .md-button--primary } [ :material-chat: Chat room](https://gitter.im/hydraulic-software/community){ .md-button .md-button--primary }

## Features

* **Create packages for every OS on any OS.** Conveyor implements everything itself so doesn't rely on platform native tooling.
    * Build Windows applications that use the built-in [Windows 10 MSIX/AppInstaller system](outputs.md).
        * Windows keeps them up to date in the background automatically.
        * Updates use delta downloads and data is shared between apps, even from different vendors.
        * Has everything IT departments need to easily deploy to managed networks.
    * Build Mac applications that use the popular [Sparkle 2 update framework](https://sparkle-project.org/).
    * Build apt repositories for Debian/Ubuntu, tarballs for other distros. Integrates with systemd for servers and cron jobs.
* **Generate a static download site.**
    * Detects the user's operating system and CPU architecture.
    * [Release via GitHub releases](configs/download-pages#publishing-through-github).
* **Brainless code signing.**
    * Sign your apps with Apple/Windows certificates for a better download UX, or ignore it (you'll get self-signed packages and a `curl | bash` style install).    
    * You can sign/notarize apps on any OS. 
    * You can backup your single root key by writing it down as words on paper.
* **[Deep support for JVM applications](configs/jvm.md).**
    * Pre-canned template apps. Publish your first app in five minutes.
    * Integration with Maven and Gradle.
    * Uses JLink and jdeps to minimize the JDK size.
* **Pierce the abstraction!** Cross platform tooling doesn't mean giving up platform-specific features. Over 120 different settings let you precisely configure your packages, including your:
    * Mac `Info.plist` files.
    * Windows XML app manifests.
    * Linux `.desktop` files and package install/uninstall scripts.
