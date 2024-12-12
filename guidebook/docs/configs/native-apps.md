# Native apps

Conveyor can package any app regardless of runtime. The support for [Electron](electron.md) and [JVM apps](jvm.md) is just a set of convenience features on top of this core support.

Packaging a native app involves:

1. Building the binaries in a Conveyor compatible manner.
2. Importing the binaries for each platform.

## Building compatible binaries

### Windows

For Windows no special steps are required.

### Linux

For Linux the binary should be linked with an rpath of `$ORIGIN/../lib` to ensure the binary looks in the right place for any included
shared libraries. This involves passing flags to `ld` during the linking stage and ensures that the tarball is relocatable, and that your
build system doesn't need to know how Conveyor lays out packages.

### macOS

For macOS the rpath must also be set to `@executable_path/../Frameworks` and additionally, spare space in the binary headers is required.
This enables Conveyor to inject its own dylib which will hook into the startup code path and initialize Sparkle updates for GUI apps that
link against Cocoa (if your app doesn't link against the Mac GUI framework, no updates will occur). To enable this the following flag should
be passed to Apple's linker: `-headerpad 0xFF`.

If using cmake, a snippet like the following will ensure the right linker flags are used on each platform:

```cmake
# Ensure libraries can be found.
if (APPLE)
    set(CMAKE_INSTALL_RPATH "@executable_path/../Frameworks")
    # On macOS Conveyor will inject its own library into the binary to initialize the update system. Ensure there is
    # sufficient empty space in the headers to make this possible.
    set(CMAKE_EXE_LINKER_FLAGS ${CMAKE_EXE_LINKER_FLAGS} "-Wl,-headerpad,0xFF")
elseif (UNIX)
    set(CMAKE_INSTALL_RPATH "\$ORIGIN/../lib")
endif()
```

If using cargo a snippet like this in `cargo.rs` should work:

```rust
if cfg!(target_os = "macos") {
     println!("cargo:rustc-link-arg=-Wl,-rpath,@executable_path/../Frameworks");
     println!("cargo:rustc-link-arg=-Wl,-headerpad,0xFF");
}
```

## Fat Mac binaries

Whether to ship fat binaries is up to you, but Conveyor currently always produces separate downloads for ARM and AMD64. On Chrome the
generated download page can detect the user's CPU but for Safari users this isn't possible due to Apple policy.

Future versions of Conveyor may support a single full-fat download and update stream.

## Importing the binaries

Synopsis:

```
app {
  mac.inputs = [
    # See: https://developer.apple.com/documentation/bundleresources/placing_content_in_a_bundle
    build/mac/installation/bin -> Contents/MacOS
    build/mac/installation/lib -> Contents/Frameworks
  ]

  windows.amd64.inputs = build/win/installation/bin
  linux.amd64.inputs = build/linux/installation
}
```

The above snippet shows how to configure a native app:

* For Windows the EXE/DLL files are assumed to be found in the `build/win/installation/bin` directory relative to the `conveyor.conf` directory, and are placed in the root of the install directory.
* For Linux, the tree found in `build/linux/installation` will be copied into the package under an app specific directory in `/usr/lib`. It should therefore contain a `bin` sub-directory, and possibly `lib`/`share`/`man` directories and so on.
* For macOS, binaries and libraries are assumed to be found in the equivalent directories and must be explicitly laid out in bundle format. See [inputs](inputs.md) for more information on the syntax used here.

## Dependencies

On Windows and macOS, there is no special handling of library dependencies. You are expected to run on the target OS out of the box.

For Debian packages, ELF binaries will be scanned and checked against the Debian package database to turn dynamic library dependencies into package dependencies. Therefore, if a binary links against `libfoo.so.2` and your package doesn't include that library itself, Conveyor will try to find the package containing that file and add a package dependency on it.
