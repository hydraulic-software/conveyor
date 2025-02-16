# Release notes

## Conveyor 17 (16th Feb 2025)

* Conveyor now prompts you to upgrade your `conveyor.compatibility-level` key when your project has fallen behind. [Compatibility levels](compatibility-levels.md) help Conveyor introduce backwards-incompatible improvements when you explicitly opt-in to them, as well as  ensuring old Conveyor versions don't try to work with a config intended for a newer version.
* There's a new `app.site.s3.retries` key to let you configure [the retry with exponential backoff](configs/download-pages.md#publishing-through-amazon-s3) Conveyor uses for S3 uploads.
* Conveyor now automatically builds a newly created template project. To disable this you can pass the `--no-build` flag.
* A bug has been fixed relating to chains of symlinks inside packages.
* The disk cache has been improved: it now uses hysteresis when lowering disk usage due to going oversize, and the logs can now include a textual diff of cache keys to aid in understanding why there was a cache miss (set `LOG_CACHE_MISS_DIFFS=true` in the environment and then use `--show-log` to view the results).
* There is now a `--silent` flag that suppresses progress tracking output.
* Uploads to Amazon S3 can now take [credentials from the environment using the default SDK behavior](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-chain.html#credentials-default) instead of requiring credentials to be supplied via Conveyor's configuration. This enables scenarios like using OIDC for Amazon authentication, or any other scheme that requires temporary credentials.

### :simple-windows: Windows

* You can now configure SafeNet or Yubikey signing tokens in a simpler way. Just set `app.windows.signing-key = hsm` and the drivers will be located automatically.
* A `User-Agent` header is now added to requests from the installer/upgrader for `metadata.properties`, which improves compatibility with some CDNs that block requests without a User-Agent by default.
* Conveyor no longer removes the Win32 EXE version resource, it's now updated in place with metadata from your `conveyor.conf` file. This fixes the Electron APIs that read the program version metadata.
* The "max version tested" metadata in your MSIX package has been increased by default to `10.0.26100.2454`, indicating you've tested on 24H2 Windows 11. You can lower this if you aren't testing your app on an up-to-date Windows 11 install.
* Conveyor now handles edge cases in EXE names without creating manifest metadata that fails schema validation.

### :simple-apple: macOS

* The minimum OS version is now added to the Sparkle metadata feed, ensuring users won't be offered upgrades to versions that require a newer OS than the one they have.
* The default minimum OS version has been increased to `12.3.0`, but can be adjusted downwards if your app is compatible with older versions by setting the `app.mac.info-plist.LSMinimumSystemVersion` key.
* Conveyor now supports ARM-only builds, without an Intel build.
* Sparkle was upgraded to 2.6.4
* Sparkle is no longer imported via the `bundle-extras` key so if you accidentally replace the contents of that key it won't yield a broken package anymore.
* The robustness of delta patch generation has been improved to handle edge cases that could previously cause a stack overflow.
* Conveyor now prints a useful error message when trying to store the root key in the keychain and the display server is locked or unreachable (e.g. because it's being run via SSH into a Mac).

### :simple-linux: Linux

* When [URL schemes/deep linking is used](configs/os-integration.md#url-handlers--deep-linking), the `%U` flag is automatically added to your `.desktop` file now if you don't do this yourself. This ensures the URL will be passed via your command line without additional configuration work.
* Ubuntu 24 breaks Electron apps via a backwards incompatible change that requires either app packaging changes or for the user to disable the new security mode. Conveyor now sets up the OS correctly whilst respecting the security changes when the user installs the `.deb`, or prints an error telling the user what they have to do if they use the tarball version.

### :simple-electron: Electron

* The `app.isPackaged` API now works correctly, along with app APIs for reading the app's own version metadata (vs reading it from `package.json`).
* Improved the diagnostics when a `.bin` directory is found in an Electron app tree.
* Conveyor now optimizes download size and disk usage by dropping native libraries (`.node` files) that target the wrong OS or CPU architecture for the package being built. 

### :fontawesome-brands-java: JVM

* Updated JDK definitions with the latest versions. The refresh process for this now catches cases where builds for different CPU architectures are released at different times from each other.
* Updated the version of Gradle used in the template projects.
* The template apps now download the needed JDK version automatically if it's not available locally.
