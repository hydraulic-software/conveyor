# Update escape hatch

!!! note
    * The escape hatch feature is only available for Windows.
    * This feature is **not** currently enabled by default. You must turn it on with config keys. It will be enabled by default in a future release.

There are some situations where you might need to alter your user's installations using a channel that's not the usual MSIX update mechanism.
For example:

* You may wish to stop using Conveyor and transition to an alternative update mechanism. 
* Your personal/corporate identity has changed, meaning you can no longer get access to certificates with the old identity that are valid 
  for signing updates (e.g. new legal name, or new location of your headquarters).
* You're switching to a different certificate authority which refuses to issue you a certificate with the same subject name as previously used. 
* You wish to completely rebrand the app including the package name. 
* You are switching from self-signing to certificate signing or vice versa.

Conveyor provides an "escape hatch" mechanism that allows you to run an installation repair tool in some situations.

Escape hatches are found in some other advanced update systems. For example, [Chrome has a similar feature](https://chromium.googlesource.com/chromium/src/+/HEAD/chrome/elevation_service/README.md).

## Automatic reinstall when the package family name changes

Windows identifies software using a [package family name](https://learn.microsoft.com/en-us/windows/apps/desktop/modernize/package-identity-overview#package-family-name) 
derived from your app's name and the subject name of the signing certificate you used. Changing these will cause Windows to become confused
and conclude that your new version is actually an entirely different app.

**app.windows.manifests.msix.reinstall-if-package-family-changes** When this key is set to true (defaults to false), Conveyor will set things
up so changes in your package family name automatically trigger uninstall and reinstall of the app. The user won't be asked to do anything,
but will see a progress bar appear and reinstallation proceed the next time they start the app after the escape hatch is used.

File `updatecheck.exe` is included on Conveyor generated MSIX packages. It contains the lightweight launcher that implements both aggressive
updates and a reinstall mechanism. When reinstalling it makes a best effort attempt to back up data under the 
[local](https://learn.microsoft.com/en-us/uwp/api/windows.storage.applicationdata.localfolder?view=winrt-22621), 
[roaming](https://learn.microsoft.com/en-us/uwp/api/windows.storage.applicationdata.roamingfolder?view=winrt-22621) and 
[local cache](https://learn.microsoft.com/en-us/uwp/api/windows.storage.applicationdata.localcachefolder?view=winrt-22621) virtual folders 
of your app.

The escape hatch will be triggered via a key in the generated `metadata.properties` file that you upload alongside your packages. Conveyor
also stores the family name in this file and will download it during the build process to detect changes.

This is a convenience setting that, when set to true, automatically sets up the appropriate escape hatch keys below to the following values:

```hocon

app.windows.manifests.msix {
  use-update-escape-hatch = true
  update-escape-hatch {
    exe = "ms-appx:///updatecheck.exe?CONVEYOR_INTERNAL_UPDATE_MODE=3"
    run-if = package-family-changed
  }
}
```

The escape hatch is an alternative to the Windows built-in [MSIX persistent
identity](https://learn.microsoft.com/en-us/windows/msix/package/persistent-identity) feature, which is currently not supported in Windows
10 and also doesn't work if you let your certificate expire.

## Manual control

These are the keys that control the escape hatch mechanism:

**`app.windows.manifests.msix.use-update-escape-hatch`** Set to true to enable the escape hatch mechanism. Upon launch, apps built with this setting will silently check the `metadata.properties` file for instructions
on running an escape hatch repair tool.

!!! important "Enable the Escape Hatch in advance"
    For the escape hatch mechanism to operate successfully, it has to be enabled *before* you actually need it, so that the apps installed by your user base already check for it.

**`app.windows.manifests.msix.update-escape-hatch.exe`** A URL containing the `.exe` file to be executed as a repair tool. The currently supported protocols are `http` and `https` for downloading an `.exe` on the fly,
and `ms-appx` to run an executable that's already in your deployed package. You can use the URL query to specify command line arguments that are sent to the escape hatch executable.

For convenience, Conveyor also sets the following environment variables that your escape hatch can use, for instance, to back up application data:

* `CONVEYOR_APPDATA_LOCAL_FOLDER`: Location of the [ApplicationData.LocalFolder](https://learn.microsoft.com/en-us/uwp/api/windows.storage.applicationdata.localfolder?view=winrt-22621)
* `CONVEYOR_APPDATA_ROAMING_FOLDER`: Location of the [ApplicationData.RoamingFolder](https://learn.microsoft.com/en-us/uwp/api/windows.storage.applicationdata.roamingfolder?view=winrt-22621)
* `CONVEYOR_APPDATA_LOCAL_CACHE_FOLDER`: Location of the [ApplicationData.LocalCacheFolder](https://learn.microsoft.com/en-us/uwp/api/windows.storage.applicationdata.localcachefolder?view=winrt-22621)

**app.windows.manifests.msix.update-escape-hatch.run-if** Defines the conditions on which to run the escape hatch. Currently supported values are:

* `package-family-changed`: Run the escape hatch if the [package family name](https://learn.microsoft.com/en-us/windows/apps/desktop/modernize/package-identity-overview#package-family-name) of the installed app is different from the one from the latest update in the download site.
* `not-up-to-date`: Run the escape hatch if the current installed version is older than the latest update on the download site.
* `always`: Run the escape hatch every time the app gets launched. This is for dire emergencies when you might need to run something even after installing a new version. Note that this setting is *sticky*, as the escape hatch will keep running until we make a release that removes this setting.
* `never` (default): Doesn't run the escape hatch. Used when you don't currently need to repair installations, but want to have the escape hatch on so your users are prepared when it's needed.

**app.windows.manifests.msix.append-publisher-id-to-appinstaller** If set to true, the [publisher id](https://learn.microsoft.com/en-us/windows/apps/desktop/modernize/package-identity-overview#publisher-id) part of the package family name gets appended to the name of the `.appinstaller` file.
This is needed if the package identity changes, because from the point of view of Windows that would be a completely different app, and only a single app can use the same `.appinstaller` URL for updates.
Normally you don't need to set this manually, as Conveyor keeps track of changes in the package family name and will set it automatically.
