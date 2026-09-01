# Update modes

Conveyor supports five different approaches to software updates:

1. `app.updates = background` (the default)
2. `app.updates = aggressive`
3. `app.updates = aggressive-if-possible`
4. `app.updates = none`
5. `app.updates = manual`

In aggressive mode an update check will be performed synchronously on each app start. If a new version is available then the update process will start and the update downloaded and applied, without any user interaction being required.

In `aggressive-if-possible` mode the behavior is identical to aggressive mode, except that updates are only applied when the new version is compatible with the current operating system. This prevents users on older OS versions from getting stuck unable to launch their app when a new version requires a newer OS. On macOS, this checks the `LSMinimumSystemVersion` plist entry against the current OS version. On Windows, this checks the `TargetDeviceFamily MinVersion` from the MSIX manifest against the current Windows version.

In background mode the behaviour differs by OS:

* :fontawesome-brands-windows: The OS itself will check for updates every 8 hours and upgrade the app in the background, even if it's not being used. Your users will never see an update prompt.
* :simple-apple: The app will check for updates on startup without blocking the user, and on a schedule whilst the app runs. Once the user agrees, updates will be downloaded and applied in the background ready for the next launch.
* :simple-linux: No difference, forced updates are not supported.

When `app.updates = none` support for auto updates is removed, except on Linux where apt repositories will continue to be installed (this is a [known issue](../known-issues.md)). Note that this doesn't simply turn off polling: on macOS Sparkle won't be shipped, and there isn't any way to trigger an update.

When `app.update = manual` support for updating is included on Windows and macOS but regular background checks are disabled. The only way an app will update is if the updates API is used. Note that on Linux manual updates has no effect, as the updates will still be managed by the system package manager in any case (updates are not automatic there).

Which mode to use depends heavily on how often your users will start the app and how important it is for updates to be applied quickly. If your app is a client for a server that speaks a complex protocol and you don't want to preserve protocol backwards compatibility, aggressive mode is appropriate. If you want the freshness benefits of aggressive updates but your app can operate even if unsupported, use `aggressive-if-possible` mode instead. If your app is self-contained or the protocols it speaks evolve in a compatible way, background mode gives a better user experience as the user won't be interrupted by the update process.

!!! tip "Silent upgrades on macOS"
    If you don't want your users to ever see an update prompt you can set `app.mac.sparkle-options.SUAutomaticallyUpdate = true`. However, if your users don't run your app for long enough for updates to download and apply then they may fall behind and be unaware of it. Use this mode with caution.

## Aggressive updates and aggressive-if-possible

When updates are aggressive or aggressive-if-possible you can force an update by restarting your app:

=== ":fontawesome-brands-windows: Restarting on Windows"

    Updates are implemented by a program called `updatecheck.exe`. The start menu entry points at this program, which will do a check and then invoke your real EXE. You can run this and then shut down, or open your app URL in the same way as the start menu does by opening the URL `shell:appsFolder\\$appUserModeID` where the app user model ID can be obtained by running `conveyor make app-user-model-id` (please note that it depends on the signing certificate used so if you sign with test certificates, pay attention to that).

=== ":simple-apple: Restarting on macOS"

    Run the `open` command with your `.app` directory, or use the Launch Services API pointed at your own app.

=== ":simple-linux: Restarting on Linux"

    On Linux aggressive updates aren't supported. You can of course run `apt-get update` then `apt-get install <your package name>` if you're installed via a deb, or invoke the GNOME UI for it yourself.

Here are some strategies for knowing when to restart the app:

1. Poll the `metadata.properties` file Conveyor generates as part of the download site. See below for how to do this.
2. If your app relies on a server, incorporate the required version of the app into the protocol so you can reject clients that are too old. For example you can put the min required version in an HTTP header.
3. Simply set a maximum uptime for the app.
4. Wait until the computer has been idle with the app on-screen for long enough before restarting automatically.

Be aware of the following:

**Updates are only checked on startup.** Users can leave their app running for long periods, so the server might change whilst instances are running. Your protocol should ideally check on each request if the client is out of date and return an error if so that tells the user to restart the app (or does it for them). Note that this is actually no different to a web app because users can leave tabs open for long periods, so if you roll out a new web server version that renames an endpoint you might break users who are using the app at that time, necessitating either a way to detect that (hard) or simply telling users to reload tabs if they break.

**Your app will start a bit slower.** This is of course also no different to a web app, which will also check with a web server on every startup. If the update check takes longer than two seconds then a progress UI will appear.

**Aggressive updates and aggressive-if-possible aren't implemented on Linux**. It's unconventional for apps to control their own update process on this platform. If you're distributing Linux tarballs, the recommended first step is a metadata-driven "update available" flow: check [the `metadata.properties` file](download-pages.md#exporting-to-metadataproperties) generated as part of your download/update site (`conveyor make site`), compare its `app.version` key to the version currently running, and then prompt the user to update themselves in your UI.

For Debian/Ubuntu packages the system package manager remains the update mechanism, so the metadata-driven approach is mainly for tarball-style distributions and CLI apps.

??? info "Checking for new versions in JVM apps"
    When packaged the `app.repositoryUrl` system property will contain the site base URL, and `app.version` contains the content of the
    `app.version` key. Note: this does not include the revision, as that's meant only for changes to the package itself not the contained
    software, but `app.revision` is also set if you want to compare against that too. So to implement the recommended Linux tarball update-available check, add
    `/metadata.properties` to the value of that system property, download it over HTTPS, parse it with the `java.util.Properties` class,
    extract the `app.version` key from the parsed file and then compare it with the `app.version` system property. The
    [`ComparableVersion`](https://github.com/apache/maven/blob/master/maven-artifact/src/main/java/org/apache/maven/artifact/versioning/ComparableVersion.java)
    class from the Maven source code is an easy way to compare version numbers; it can be copy/pasted into your source tree. If the app
    is behind then show a message to the user asking them to download or install the newer tarball. If you're using Debian/Ubuntu packages
    instead, ask them to upgrade via apt or their normal graphical software updater.

??? info "Checking for new versions in Electron apps"
    After fetching the `metadata.properties` file the [dot-properties](https://github.com/eemeli/dot-properties) package can be used to
    parse it, or you can just do some simple string manipulation: split to lines, drop any line starting with `#`, split each
    line by `=`, convert to a map and (optionally, if you're adding custom keys) un-backslash-escape the values. Supporting the full
    specification isn't necessary for parsing this file. To learn what the current version use your `package.json` file
    include your site URL in that file and then in.

??? info "Checking for new versions in other apps"
    Although there is [a precise specification for the `.properties` format](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Properties.html#load(java.io.Reader)),
    there's no need to fully implement it. The format can be treated as a simple set of `key=value` pairs in which lines starting with `#`
    are to be ignored, and the values should be un-backslash-escaped before use.
