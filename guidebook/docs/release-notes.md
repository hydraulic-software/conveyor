# Release notes

## New features

* A new `app.compression-level` key allows you to disable compression for Windows and Mac zips, as well as the Linux packages. Set it to
  `none` to speed up iteration when doing local testing.

## Bug fixes

* Input definitions with globs and placement arrows now treat the right hand side as a directory instead of a file to overwrite.

!!! note 
    For older release notes please use the version picker in the top bar.
