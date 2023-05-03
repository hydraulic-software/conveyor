# Release notes

## New features

* [Automatic releasing to the Microsoft App Store](configs/windows.md#release-to-the-microsoft-store). This provides an alternative to 
  distribution via your own websites and avoids the need for a Windows signing key.
* The root key can now be stored in the macOS key ring. This protects it from other applications on the computer, regardless of sandboxing.
* A new `app.compression-level` key allows you to disable compression for Windows and Mac zips, as well as the Linux packages. Set it to
  `none` to speed up iteration when doing local testing.

## Usability improvements

* Inputs can now be made optional. When the `conveyor.compatibility-level` >= 9, inputs are required by default. This makes it harder to
  accidentally build packages that are missing files.
* Conveyor now verifies that the min macOS version required by individual binaries is consistent with the min version advertised in the
  `Info.plist` file. This can catch mistakes where you think you are targeting a certain older macOS version but incorporate code that
  has a higher requirement. Note that this check only applies to code shipped as individual files, not code inside zips.
* A crash reporter was added.

## Bug fixes

* Input definitions with globs and placement arrows now treat the right hand side as a directory instead of a file to overwrite.
* JVM: JavaFX apps are now marked as supporting both integrated and discrete graphics on macOS, speeding up startup and avoiding an annoying screen flash.
* A few minor UX bugs in the `conveyor keys export` command were resolved.

!!! note 
    For older release notes please use the version picker in the top bar.
