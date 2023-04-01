# Release notes

## New features

* A new `app.compression-level` key allows you to disable compression for Windows and Mac zips, as well as the Linux packages. Set it to
  `none` to speed up iteration when doing local testing.

## Bug fixes

* Input definitions with globs and placement arrows now treat the right hand side as a directory instead of a file to overwrite.
* Windows: resolve a user report of a Windows API bug affecting their machine when installing self-signed apps. 
* JVM: JavaFX apps are now marked as supporting both integrated and discrete graphics on macOS, speeding up startup and avoiding an annoying screen flash.

!!! note 
    For older release notes please use the version picker in the top bar.
