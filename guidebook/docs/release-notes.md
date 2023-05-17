# Release notes

## New features

* [Automatic releasing to the Microsoft App Store](configs/windows.md#release-to-the-microsoft-store). This provides an alternative to 
  distribution via your own websites and avoids the need for a Windows signing key.
* The root key can now be stored in the macOS key ring. This protects it from other applications on the computer, regardless of sandboxing.

## Usability improvements

* Inputs can now be made optional. When the `conveyor.compatibility-level` >= 9, inputs are required by default. This makes it harder to
  accidentally build packages that are missing files.
* A new `app.compression-level` key allows you to disable compression for Windows and Mac zips, as well as the Linux packages. Set it to
  `none` to speed up iteration when doing local testing.
* Conveyor now verifies that the min macOS version required by individual binaries is consistent with the min version advertised in the
  `Info.plist` file. This can catch mistakes where you think you are targeting a certain older macOS version but incorporate code that
  has a higher requirement. Note that this check only applies to code shipped as individual files, not code inside zips.
* A crash reporter was added.
* Certificates that are about to expire trigger a warning.
* Non-colored terminal output is now less verbose.
* The `-K` command line flag can now be given string lists e.g. `conveyor "-Kapp.machines=[mac,windows]" make site`.
* Missing signing keys now yield an error rather than a warning.
* JVM: App JARs are now always placed on the classpath unless listed explicitly in the `app.jvm.modules` list. Previously, Conveyor would
  try to identify the module sub-graphs that could be optimized by jlink; unfortunately there are too many broken module graphs in the wild
  and this feature has now been removed. This change should make no noticeable difference to the execution of most apps but resolves bugs
  that could cause build failures when using some third party libraries.
* JVM: Better icon loading code in the Compose sample.

## Bug fixes

* The Windows installer now detects if admin privileges are necessary to install and elevates as necessary. Normally MSIX packages don't
  require admin access to install, but this situation can occur on managed Windows networks where the IT admins have disabled this 
  capability for non-admins via a registry key. We recommend rebuilding packages with a higher revision number as a consequence.
* Input definitions with globs and placement arrows now treat the right hand side as a directory instead of a file to overwrite.
* A few minor UX bugs in the `conveyor keys export` command were resolved.
* Don't try to unlock keys for machines that are disabled by the `app.machines` key.
* Don't fail if the user deletes the working directory inside the cache.
* Abort early if there's no home directory or it's not writeable (this can occur inside some kinds of Docker containers).
* JVM: Version 1.6 of the Gradle plugin has been released. This fixes some bugs that could occur when working with machine specific 
  dependency graphs that contained conflicting library versions. Additionally, it's no longer needed to add an extra Maven repository.
  We recommend all projects to upgrade.
* JVM: JavaFX apps are now marked as supporting both integrated and discrete graphics on macOS, speeding up startup and avoiding an
  annoying screen flash.
* JVM: Don't fail if there are spaces in the system username.

!!! note 
    For older release notes please use the version picker in the top bar.
