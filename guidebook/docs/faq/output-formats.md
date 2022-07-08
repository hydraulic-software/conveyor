# Output formats

## 1. RPMs?

It's planned. [Vote for support here](https://github.com/hydraulic-software/conveyor/issues/3).

## 2. DMGs?

No plans because [zips work better](../outputs.md#macos). The downside is you don't get to theme the 'install experience'. If you need
DMGs for some reason we've overlooked, please [email us to let us know](mailto:contact@hydraulic.software).

## 3. Mac PKGs?

We'd like to add PKG support in the future. These are needed for uploading to the app store, and for installing files to unconventional places.

## 4. Installer EXEs? MSIs?

At the moment we have no plans to do this. MSI is deprecated. MSIX is Microsoft's current recommended install technology, but we're interested in learning about use cases that require alternatives. We may in future support creating a small stub EXE that installs a custom self-signed certificate and then triggers App Installer. This would let you benefit from the MSIX feature set without needing code signing, albeit the initial EXE may be more prone to antivirus false positives.

## 5. FlatPak? Other distros? Non-Intel Linux?

Conveyor produces tarballs for x86 distros that use other packaging systems. 

FlatPak looks promising, doesn't it? [Vote for support here](https://github.com/hydraulic-software/conveyor/issues/4).

Linux/Aarch64 may be added later. [Vote for support here](https://github.com/hydraulic-software/conveyor/issues/8).

## 6. App stores?

Not yet. [Vote for Mac App Store support here](https://github.com/hydraulic-software/conveyor/issues/9).

## 7. Why does the Debian install process differ to normal?

A common approach to installing packages from third party repositories is to manually import a GPG key, then manually add an `apt` source, then finally use `apt-get update && apt-get install <program>`  to fetch it. This is tedious and manual. Conveyor DEBs include the GPG key and apt source file inside them, so you can install them by just downloading the file and then running `apt install ./program-name*.deb` (this command will also do dependency resolution so no features are lost). Users can also double click the DEB in their file manager if they want a GUI.

The level of security is the same because the original DEB will have been downloaded over SSL, and you'd get the GPG key from the same location anyway.

## 8. Are Mac apps fat?

For native apps, it depends on what your build system produces. The CMake template app does create fat binaries.

For JVM apps, no. Conveyor produces separate downloads for ARM and Intel Macs. This is to keep download times fast. Chrome users will have their CPU auto detected and will only be offered the right package for their CPU. Safari advertises itself as Intel even on ARM Macs (deliberately), so Safari users will be given a choice and are expected to know what kind of Mac they have.

If you'd like your JVM Mac apps to be fat please [vote for support here](https://github.com/hydraulic-software/conveyor/issues/11).

## 9. Ubuntu Snaps?

Not at this time. Snaps are a format specific to the Ubuntu app store. If you want Snap support, please [let us know](mailto:contact@hydraulic.software) so we can consider raising its priority. Without strong feedback on this we'll probably focus on FlatPak.

## 10. Docker?

Conveyor supports making Linux servers with full [systemd integration](../configs/linux.md#systemd-units). It doesn't produce Docker images at this time. Other tools exist that make Docker images and so Conveyor probably doesn't have much to add here, but if you'd like this feature please [let us know](mailto:contact@hydraulic.software) so we can understand your needs. 

Incidentally, although Docker is a great tool Conveyor supports systemd because we use Conveyor-generated packages for our own servers. We feel that:

1. SystemD has better service management features like task dependencies, activation, logging etc.
2. SystemD has better command line tools, including neat third party tools like [sysz](https://github.com/joehillen/sysz).
3. SystemD has better support for sophisticated sandboxing.
4. Docker requires careful configuration to avoid accidentally deleting persistent data saved to disk by the server. SystemD is harder to misconfigure.
5. Native package formats allow for depending on things like database engines, which would otherwise require additional setup in Docker.
6. Distribution package managers have evolved a wealth of features useful for devops and sysadmin teams, like merging changes to config files (DEBs produced by Conveyor support the Debian conffiles mechanism).

## 11. A corporate IT department needs to repackage my app.

MSIX files are fully supported by Windows network administration systems like Active Directory, but if an IT department doesn't want to use MSIX that's OK. Conveyor produces plain zips too. In fact MSIX files are also just signed zip files with extra metadata. IT departments can extract the MSIX and read the `AppxManifest.xml` file to learn what integration points are needed (e.g. start menu items), then repackage as needed.

## 12. How do I find my app's install folder on Windows?

Windows doesn't store MSIX packages directly under `c:\Program Files` as you might expect, due to the containerization. Instead it's stored under `c:\Program Files\WindowsApps` in a folder name containing a hash of the certificate. The best way to view it and do many other tasks with MSIX is to grab the wonderful [MSIX Hero](https://msixhero.net/), pick your app from the list and then click open and then "Installation folder". This will open an Explorer in the right place. MSIX Hero also lets you run command shells and other tools inside the container.
