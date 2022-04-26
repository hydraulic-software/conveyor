# Release notes

## Private Beta 1

This is the first time the app has been made available for testing outside Hydraulic. Don't be gentle - please try and break it!

This build will expire at the end of May. Updates will be provided before expiry. During the private beta period Conveyor will switch to a per-project license key system instead of expiring builds. If you're still using and testing it by then, you'll be provided with a license key to put in your config.

### Known issues

* The config schema may change in minor but backwards incompatible ways during the private beta. No major changes are planned so keeping up should never take more than a minute or two.
* Supported apps and packages:
    * Only JVM apps are supported in this release. It's a temporary limitation that's at the top of our list to fix.
    * Only GUI apps are supported in this release. Support for CLI-only apps is implemented (Conveyor is packaged with itself), but the feature needs more polish before being ready to launch. Let us know if you'd like to try it out anyway.
    * Only DEB based distros get native packages, for the rest Conveyor creates a tarball (which doesn't auto update). RPM / FlatPak support is on the roadmap.
    * ARM Linux isn't yet supported.
    * Packages for the Mac/Windows app stores aren't yet supported.
* Registration of URL handlers and file type associations isn't yet supported.
* You currently need a Windows Authenticode signing certificate to produce MSIX packages. Self-signed MSIX support is nearly ready and should become available during the private beta period.
* You currently need an Apple Developer Programme membership to create M1 Mac apps. Self-signed ARM macOS support is in development.
* There's no way to customize the generated download HTML.
* Changes in terminal size whilst a build is in progress won't be respected.
* Maven integration doesn't work on Windows.
* Debian packages will show in GNOME Software as proprietary even if they aren't.
* Providing a directory as your primary input will cause an icons related crash. Make sure your first input points to a file.