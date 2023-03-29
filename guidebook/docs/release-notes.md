# Release notes

## New features

* [File associations](configs/os-integration.md#file-associations) are now supported and can be easily registered with one line of config.
  The macOS UTI file associations system is available but automatically configured based known metadata and inference rules.
* You can now [deploy directly to GitHub Releases](configs/download-pages.md#publishing-through-github).
* You can also [deploy directly to Amazon S3 buckets](configs/download-pages.md#publishing-through-amazon-s3).
* More Electron versions are now supported, and new versions become automatically supported.
* [Control over Windows filesystem virtualization](configs/windows.md#virtualization), which is used to track changes made by your app to the user's `AppData` directory, `c:\Windows` and other locations.
* You can now print your Windows "app user model ID" using `conveyor make app-user-model-id`, if you need it. Some frameworks require this string for OS integration features to work.
* Conveyor can now use [specific files directly inside zips as inputs](configs/inputs.md#object-syntax), without needing to download or extract the zip first. This is useful when fetching files out of zips on remote HTTP servers.  

## Usability improvements

* The user experience of self-signed apps has been improved on Windows. The PowerShell script is no longer needed, as the installer EXE
  will add your certificate to the certificate store for the user (nb: this requires administrator access).
* The new `conveyor run` command packages your app for the current OS and then executes it. This makes it more convenient to test your app.
  On macOS the app will be a normal signed bundle, on Windows and Linux the app will be executed from a plain directory (i.e. not installed first).
* More download site checks. Conveyor now probes your server to ensure it supports HTTP Range requests correctly. 
* For JVM apps, if the main class is inferred automatically a warning is now printed. This catches the case where you've mis-remembered the key used to specify the main class and an unrelated entry point is being picked.

## Bugfixes

* Internal symbols in the injected native code components are now hidden and inside a C++ namespace.
* Linux: Debian packages that contained no native ELF files but did contain a `Depends` key in the config were generated incorrectly.
* JVM: Restrict the dependency scope when reading Maven classpaths.
* Linux: fix a permissions error that could occur when creating new projects.
* Windows: Exclude the temp directory from filesystem virtualization when a JVM is imported and `conveyor.compatibility-level >= 8`. This fixes network related crashes that appear on some Windows machines when using a very recent JVM.
* Windows: Refreshed the schemas used to validate AppX manifests.
* macOS: Don't show an error on macOS when updates are set to aggressive mode and the update site isn't reachable.

!!! note 
    For older release notes please use the version picker in the top bar.
