# Release notes

## 10.1

* Fix some regressions with the new Mac signing implementation.
* Fall back to the old Mac signing implementation if something goes wrong and submit a report automatically.
* Don't report eSigner server errors as crashes.
* JVM: update JDK definitions for the latest releases.

## 10.0

### New features

* Support for the use of [cloud HSMs](configs/keys-and-certificates.md#cloud-remote-signing-windows-only) from SSL.com eSigner, DigiCert ONE/KeyLocker and AWS. This is useful because Microsoft now require the use of a hardware security module even for non-EV certificates, which doesn't play well with deploying from cloud-based continuous integration machines where you don't have access to the build machines. With this feature you can now use hardware-protected signing keys without any hardware.
* A new [escape hatch mechanism for Windows](configs/escape-hatch.md) allows for update of installs in cases where the usual update mechanism can't be used, for example, if your signing identity has changed or if you wish to migrate away from Conveyor.
* Commercial projects can now remove the "Packaged with Conveyor" badge using the `app.site.show-conveyor-badge` key.
* When you generate a project with a reverse DNS name of the form `io.github.your_username.your_repo_name`, the project is automatically set up for GitHub pushes and releases.
* More control over [Linux packages](configs/linux.md):
  * An `app.linux.root-inputs` set of keys, which allow files to be placed outside the app install path or prefix. This is useful for adding extra integration points with the OS that Conveyor itself doesn't support.
  * `app.linux.debian.distribution.{name,mirrors}` keys which allow you to change which Debian/Ubuntu distribution is used to resolve shared library names to packages for dependency detection.

### Other changes

* The signing code for macOS has been rewritten. This yields small conformance improvements such as switching to [DER encoded entitlements](https://developer.apple.com/documentation/xcode/using-the-latest-code-signature-format). If you notice any new problems with Mac code signing, please let us know and we can give you a config key to switch back to the old implementation.
* The `app` task now builds the executable app directory for the current host OS, not the prior behavior of the single machine named in the `app.machines` key.
* JVM: You can now override the default disabling of the app attach mechanism by adding `-XX:-DisableAttachMechanism` to the `app.jvm.options` list.

### Bug fixes

* The `keys export` command now exports certificates when using self-signing.
* Conveyor can now load PEM elliptic curve private keys that lack the public key part.
* Fixed many bugs related to usernames with spaces in them (can happen on Windows).
* Improved compatibility with YubiKey HSMs.
* Many other small fixes.

!!! note 
    For older release notes please use the version picker in the top bar.
