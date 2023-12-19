# Release notes

## Conveyor 13.0 (Dec 19th 2023)

### New features

* Conveyor will now validate the certificate chain for your Windows certificates ahead of time, and fill out any missing intermediates by
  using AIA extensions where possible. This should catch and fix a common source of mistakes and pain when code signing. This new feature
  can be controlled using the [`app.windows.verify-certificate-chain` key](configs/windows.md#appwindowssigning-key-appwindowscertificate-appwindowsverify-certificate-chain)
* You can now use the Google Cloud Key Management Service with Conveyor. This lets you hold your Windows signing key in an HSM, which is now
  a requirement imposed by Microsoft. [Instructions are provided here](configs/keys-and-certificates.md#google-cloud-platform).
* We've also added support for the Azure Key Vault.
* Improved robustness against cache corruption that can occur if your machine kernel panics during a build.

### Performance improvements

* Improved the GitHub Action to check for the existence of the cache before downloading it, so the actions can be efficiently reused more than once in the same workflow.
* Lazily download Debian package metadata, so we don't do so when building for other platforms.

### Other

* Improvements to error messages.
* Refreshed the AppxManifest XML schemas. 
* Fixed an incompatibility between the Microsoft Store and the escape hatch feature.
* Ask for a passphrase when the signing key comes from a p12/pfx file.
* JVM: Restored compatibility with old versions of Windows 10 that may not have the more modern VC runtime available.
* JVM: Updated the set of known JDKs. 
