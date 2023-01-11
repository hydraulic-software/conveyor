# Release notes

## Conveyor 7

This release adds support for Flutter apps and adds new icon features.

* **Flutter support:** See the [sample Flutter app on GitHub](https://github.com/hydraulic-software/flutter-demo) to see how to cross-compile the app using GitHub Actions and then package it.
* **SVG support:** Conveyor can now render an SVG file to icons using the appropriate style for Windows, macOS and Linux, with a rounded rectangle+drop shadow background compliant with Apple's guidelines being used automatically. Run `conveyor make rendered-icons` to see the results for each platform.
* **Synthetic icons**. Conveyor can now make icons for you. This reduces the required effort to quickly package apps like internal tools, dashboards and demos where drawing an icon would be too much effort. The generated icon has a one or two character label on a gradient background.
* **MSVC++ redistributables**. Apps often require these DLLs on Windows, but they don't come with the OS. [You can now easily bundle them into your app](stdlib/index.md#microsoft-visual-c-redistributables).


Usability improvements:

* Consistency checks against the target upload site, to catch cases where you're changing your signing certificates or where you're overwriting already published packages with different contents. This can help avoid broken updates.
* Tweaked the colors used for code blocks in printed Markdown messages to improve readability on different background colors.
* Windows update metadata no longer allows downgrades when `conveyor.compatibility-level >= 7`. Downgrades aren't allowed by Sparkle on macOS and require admin intervention on Linux, so it's better to be consistent and disallow them on Windows too. This has the advantage that you can distribute packages of a higher version than the update stream it's connected to, e.g. to distribute one-off patched versions to specific users. They will return to the mainline when you next do an upgrade past the version used for the one-off builds.
* You are now warned if using a JavaFX version lower than 19 on Windows, as earlier versions can fail to start on some Windows installs.
* Rolled the JVM client enhancements config into the defaults as some are necessary for apps to run successfully when packaged.
* EXEs added to the app inputs will be moved to the bin directory and added to the PATH automatically.

Bug fixes:

* Updated the OpenJFX Gradle plugin to resolve an issue using `gradle run` on Mac ARM.
* Resolved a crash that could occur if the `jlink` command line got too long.
* Improved UNIX flavor detection. Don't delete static Linux binaries from the package due to thinking they're not for Linux.

!!! note 
    For older release notes please use the version picker in the top bar.
