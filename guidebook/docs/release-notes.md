# Release notes

This is the first time the app has been made available for testing outside Hydraulic. Don't be gentle - please try and break it!

These builds will expire from time to time. Updates will be provided before expiry. During the private beta period Conveyor will switch to a per-project license key system instead of expiring builds. If you're still using and testing it by then, you'll be provided with a license key to put in your config.

### Known issues

* The config schema may change in minor but backwards incompatible ways during the private beta. No major changes are planned so keeping up should never take more than a minute or two.
* Missing features:
    * Supported apps and packages:
        * Only JVM apps are supported in this release. It's a temporary limitation that's at the top of our list to fix.
        * Only GUI apps are supported in this release. Support for CLI-only apps is implemented (Conveyor is packaged with itself), but the feature needs more polish before being ready to launch. Let us know if you'd like to try it out anyway.
        * Only DEB based distros get native packages, for the rest Conveyor creates a tarball (which doesn't auto update). RPM / FlatPak support is on the roadmap.
        * ARM Linux isn't yet supported.
        * Packages for the Mac/Windows app stores aren't yet supported.
    * Registration of URL handlers and file type associations isn't yet supported.
    * There's no way to customize the generated download HTML.
* Bugs
    * Changes in terminal size whilst a build is in progress won't be respected.
    * Maven integration doesn't work on Windows.
    * Debian packages will show in GNOME Software as proprietary even if they aren't.
    * Providing a directory as your primary input will cause an icons related crash. Make sure your first input points to a file.

## Version history

### Private beta 5

*May 2022*

* Simplified new project generation.
* Builds no longer expire.
* Added a message of the day feature to announce new versions.
* Bugfixes.

### Private beta 4

*May 2022*

* Self-signed download HTML now makes script links clickable so users can quickly view what they'd run. A tooltip is now displayed when
  the user copies the command.
* More helpful error messages if a file isn't found, if Windows isn't in developer mode or if you don't enter the right passphrase.
* Introduced the JVM client enhancements config, so these tweaks are no longer on by default. 
* Bugfixes.

### Private Beta 3

*May 2022*

* Support adding Conveyor to the bash shell PATH on macOS, not just zsh.
* Visual tweaks to the generated download HTML.
* Better usability around passphrase prompting.
* Simplified the guidebook material on signing.
* Small bug fixes.

### Private Beta 2

*May 2022*

* Full support for self-signing of packages. Both Mac and Windows packages can now be used without a code signing certificate. Users will be asked to copy/paste a command to the terminal.

### Private Beta 1

*April 2022*

First release available for testing.
