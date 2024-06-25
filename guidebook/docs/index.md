# Getting started

[ :material-arrow-down-circle: Download](https://downloads.hydraulic.dev/conveyor/download.html){ .md-button .md-button--primary } [ :material-arrow-right-box: Start tutorial](tutorial/new.md){ .md-button .md-button--primary }

Conveyor makes distributing desktop apps as easy as shipping a web app. It's a tool not a service, it generates and signs [self-upgrading packages for Windows, macOS and Linux using each platform's native package formats](package-formats.md) without requiring you to have those operating systems, and it looks like this:

<iframe width="100%" height="600" src="https://www.youtube.com/embed/oYznJERB3mM?si=8BOC7z0BUejavqWF" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>

It's free for open source apps and has [simple per-project pricing](https://www.hydraulic.dev/pricing.html) for commercial apps.

## Learn more

[ :fontawesome-solid-rocket: Sample apps ](sample-apps.md){ .md-button .md-button--primary } [ :simple-discord: Chat](https://discord.gg/E87dFeuMFc){ .md-button .md-button--primary } [ :material-forum: Forum](https://github.com/hydraulic-software/conveyor/discussions){ .md-button .md-button--primary }

## Features

* **[Sophisticated software updates](configs/update-modes.md)**
    * [Platform native package types and update systems](package-formats.md), without any lockin.
    * Chrome-style silent background updates
    * Web-style synchronous update on launch
    * Advanced [delta updates](understanding-updates.md#delta-updates).
    * No code changes necessary.
* **App framework integration:**
    * [Electron](configs/electron.md): [simpler than Forge/Builder/Squirrel](comparisons/electron-comparisons.md), generates ASAR files, can read `package.json` files. 
    * [JVM](configs/jvm.md): bundles a `jlink` optimized JVM, custom native launchers, many optimizations and usability improvements for desktop apps.  
    * [Flutter](configs/flutter.md): easily ship apps that share code with mobile.
* **Excellent support for CI/CD:**
    * Package and deploy directly for _every platform you support_ from any Linux build agent, without needing Mac/Windows workers. 
    * Automatic releasing to the Microsoft Store, web servers, S3 or [GitHub releases](configs/download-pages.md#publishing-through-github)
    * A [GitHub Action](continuous-integration.md#using-github-actions)
    * Support for [cloud signing services and HSMs](configs/keys-and-certificates.md#cloud-remote-signing-windows-only), eliminating the need for USB Windows signing keys.
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
