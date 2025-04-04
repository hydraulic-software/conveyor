# Release notes

## Conveyor 18 (release date TBD)

* The output printed when there's no terminal has been improved, which should make Conveyor's output more pleasant in CI log viewers. It now uses color and has clearer formatting.
* You can control the use of color and animation explicitly using the new `--console` flag, which is useful if you're redirecting Conveyor's output to a file.

### :simple-windows: Windows

* Conveyor now longer uses the keychain to create new root keys. Existing root keys will continue to be read from the keychain.

### :simple-apple: macOS

### :simple-linux: Linux

### :simple-electron: Electron

### :fontawesome-brands-java: JVM

* The Jetpack Compose template app has been updated to be a full Multiplatform app using the current standard build system, with Android and iOS versions. 
