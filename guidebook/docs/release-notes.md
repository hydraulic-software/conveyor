# Release notes

## Conveyor 7

This release focuses on improved icons support. You can now supply an SVG file as your icon instead of PNGs, and it'll be rendered to each platform's native format

* **SVG support:** Conveyor can now render an SVG file to icons using the appropriate style for Windows, macOS and Linux, with a rounded rectangle+drop shadow background compliant with Apple's guidelines being used automatically.
* **Synthetic icons**. Reduces the required effort to quickly package apps like internal tools, dashboards and demos where drawing an icon would be too much effort. Conveyor can generate an icon for you with a one or two character label on a gradient background.

Minor changes:

* Consistency checks against the target upload site, to catch cases where you're changing your signing certificates or where you're overwriting already published packages with different contents. This can help avoid broken updates.
* Tweaked the colors used for code blocks in printed Markdown messages to improve readability on different background colors.
* Windows update metadata no longer allows downgrades when `conveyor.compatibility-level >= 7`. Downgrades aren't allowed by Sparkle on macOS and require admin intervention on Linux, so it's better to be consistent and disallow them on Windows too. This has the advantage that you can distribute packages of a higher version than the update stream it's connected to, e.g. to distribute one-off patched versions to specific users. They will return to the mainline when you next do an upgrade past the version used for the one-off builds.

JVM specific improvements:

* You are now warned if using a JavaFX version lower than 19 on Windows, as earlier versions can fail to start on some Windows installs. 
* Rolled the JVM client enhancements config into the defaults as some are necessary for apps to run successfully when packaged.
* Bugfix: Updated the OpenJFX Gradle plugin to resolve an issue using `gradle run` on Mac ARM.
* Bugfix: Resolved a crash that could occur if the `jlink` command line got too long. 

## Conveyor 6.1

Bugfixes:

* **Important:** Fix a non-determinism in generation of self-signing certificates that would cause upgrade failures for self-signed apps re-built after January 1st 2023.
* Resolved an issue with using HSM drivers on macOS.
* Enforce that the `app.fsname` key is written in `kebab-case`, as this name is transformed into different casing styles for different platforms to match native conventions and thus kebab-case was as previously undocumented assumption.
* Fix a crash that could occur if the Mac finder placed `.DS_Store` files inside the private disk cache area.
* Improved documentation around how to fix missing app icons on Linux.

## Conveyor 6

* To improve compatibility with Amazon S3/CloudFront, the way Debian packages are placed in the generated apt site has been changed.
  The new default "non-flat" layout places the apt and .deb files into a `debian` subdirectory. The previous "flat" layout (where all 
  the files are together in the same directory) continues to be used for projects that have already uploaded a flat site, or for projects 
  targeting GitHub Releases, as GitHub doesn't allow uploads of files in directories. The type of site can be controlled with the new
  `app.site.flat` key.
* Updated JVM template apps to Gradle plugin 1.3, and refreshed the JDK standard library. 
* Bugfix: improved UNIX flavor detection for native libraries in JAR files.
* Bugfix: fixed an issue with restarting the app after a forced update on Windows if the application name has a space in it.
* Bugfix: ensure that `app.version` is always interpreted as a string even if unquoted in HOCON.

## Older

For older release notes please use the version picker in the top bar.
