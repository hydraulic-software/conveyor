# Release notes

## Conveyor 19

* :fontawesome-brands-windows: Microsoft's [Azure Trusted Signing (ATS) certificate authority is now supported](configs/keys-and-certificates.md#azure-trusted-signing). ATS is cheaper than most other Windows CAs, with a more Apple-like pricing structure.
* :simple-apple: The macOS update user experience has been simplified. When the control API is used to trigger an update and one is available, the user is no longer prompted to accept the update or trigger the restart. Instead, the update is downloaded, applied and the program is restarted immediately.

Other changes include a refresh of the JDK set in the config library, a refresh of the Compose Desktop template, and various bug fixes.