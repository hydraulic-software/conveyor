# Getting started

[ :material-arrow-down-circle: Download](https://downloads.hydraulic.dev/conveyor/download.html){ .md-button .md-button--primary } [ :material-arrow-right-box: Start tutorial](tutorial/new.md){ .md-button .md-button--primary }

Conveyor makes distributing desktop apps as easy as shipping a web app. It's a tool not a service, it generates and signs [self-upgrading packages for Windows, macOS and Linux using each platform's native package formats](outputs.md) without requiring you to have those operating systems, and it looks like this:

<video width="100%" poster="https://conveyor.hydraulic.dev/assets/promo.jpg" controls><source src="https://conveyor.hydraulic.dev/assets/promo.mp4" type="video/mp4"></video>

It's free for open source apps and has [simple per-project pricing](https://www.hydraulic.software/pricing.html) for commercial apps.

## Learn more

[ :fontawesome-solid-rocket: Sample apps ](sample-apps.md){ .md-button .md-button--primary } [ :simple-discord: Chat](https://discord.gg/E87dFeuMFc){ .md-button .md-button--primary } [ :material-forum: Forum](https://github.com/hydraulic-software/conveyor/discussions){ .md-button .md-button--primary }

## Features

* [Self-updating packages](configs/index.md#update-modes) for every OS, built on any OS
    * Background updates or check-on-launch
* App framework support:
    * [Electron](configs/electron.md)
    * [JVM](configs/jvm.md)
    * [Flutter](configs/flutter.md)
* Automatic releasing to the Microsoft Store, web servers, S3 or [GitHub releases](configs/download-pages#publishing-through-github)
* Simple but powerful code signing support:
    * Handles keys of different formats
    * Can sign/notarize or self-sign apps for every OS from any OS
    * Microsoft Store apps don't need to be signed
    * HSM support
    * Generates CSRs for easier purchasing from certificate authorities
    * Can store root keys in the macOS keychain for extra security
* Icon generation, rendering and format conversion
* Generates download sites
* Generate starter projects for native C++, Electron, JavaFX (JVM) and Jetpack Compose Desktop (JVM)
* Pierce the abstraction! Over 120 different settings let you take control of every OS specific detail
* No lockin thanks to using standard package formats and open source frameworks
