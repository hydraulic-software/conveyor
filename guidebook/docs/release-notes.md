# Release notes

## 11.1

A big thanks to Florian Enner for his contribution of improved DigiCert KeyLocker documentation in this release!

* Fix a rare error that could occur whilst uploading to Amazon S3.
* Fix a rare error that could occur when using certificates from an encrypted p12 file.
* Fix a regression related to improved log redaction.
* Improve the handling of Digicert KeyLocker aliases.
* Updated the JDK list.

## 11

### New features

* **Delta updates for macOS.** Conveyor now generates delta patches to quickly upgrade older versions (the last five by default) to
  the latest version. This hugely increases update speed on this platform. Support for delta patching is already in your deployed apps,
  so users will immediately get faster upgrades once you start using the new Conveyor version for building releases. Conveyor will use
  the old releases in your disk cache if available, or download them from your distribution site if not. Set `conveyor.compatibility-level`
  to 11 or higher to enable this feature.
* **Relocating your update site.** You can now [change your site URL](configs/download-pages.md#relocating-your-download-site) after the app has been launched.
* **Install via NPM.** You can now just `npm install -g @hydraulic/conveyor` to get Conveyor ready for building.
* A new [**HOCON tutorial**](configs/hocon.md) to make it easier to learn the syntax of the config file.
* **Smarter defaults.** If you don't specify the `app.vendor` key then a vendor string for your Windows packages will now be taken from your
  code signing certificate.
* **Notarization authentication.** You can now use Apple's official notarization API, which uses a different authentication method.
  The docs have been updated to guide new users to the new protocol, but the prior authentication method is still supported and there's
  no need for existing users to change anything.
* **More control over Info.plist files.** You can now set `Info.plist` keys independently for Intel and ARM releases. Additionally, keys 
  required for usage of the microphone and camera are now set automatically.

Along with various minor bug fixes.

!!! note 
    For older release notes please use the version picker in the top bar.
