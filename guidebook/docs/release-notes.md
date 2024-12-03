# Release notes

## Conveyor 16

This is primarily a bugfix and maintenance release. However, it adds these new features:

* Thanks to Bonny Rivers, the Electron control API now has TypeScript annotations.
* You can now set `app.site.github.draft = true` to create draft releases when releasing via GitHub.
* The Windows [App User Model ID](configs/os-integration.md#windows-appusermodelid-aumid) is now exposed at runtime in JVM and Electron apps.

This release fixes these bugs:

* Improve the error message printed when SSH authentication fails due to having an encrypted key without supplied passphrase.
* Fix a regression in the error message printed when an HSM PIN expires. ([GitHub Issue #129](https://github.com/hydraulic-software/conveyor/issues/129))
* Fix the download page to not show scrollbars when they aren't necessary.
* :simple-windows: Fix a bug that could occur when using `app.updates = none` to entirely remove updates support. 
* :simple-windows: Fix a crash that could occur when a custom MSIX manifest fragment fails to validate.
* :simple-windows: Fix a crash that could occur when the inputs contain a file with an EXE extension that isn't a valid EXE.
* :simple-windows: Fix a crash that could occur for certain EXE names used as helpers.
* :simple-windows: Fix a crash that could occur for pre-signed EXE/DLL files that don't place their certificate table after the signature. Such files won't have their pre-existing signaure preserved.   
* :simple-windows: Increase the upload timeout for Windows Store publishing to two hours.
* :simple-apple: When updates on macOS are configured to show user a prompt, don't show the `.0` appended to the version number to implement the revision number  ([GitHub Issue #118](https://github.com/hydraulic-software/conveyor/issues/118))
* :simple-electron: Fix an annoying but harmless popup that can occur in Electron apps when using the Darwin build (versus the Mac App Store build), in combination with video conferencing.
* :fontawesome-brands-java: Fix the logic used by the "Check for update" button in the JavaFX sample app.
* :fontawesome-brands-java: Ensure the `app.repositoryUrl` system property always has a protocol prefix. 
