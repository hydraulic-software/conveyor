# Flutter apps

[Flutter](https://flutter.dev/) is a cross-platform UI development platform for mobile and desktop. Flutter apps are easy to package
with Conveyor.

[View sample app on GitHub](https://github.com/hydraulic-software/flutter-sample/){ .md-button .md-button--primary }

There are no Flutter-specific configuration keys at this time. Just include `flutter.conf` from the Conveyor standard library and go.
You can also use a tool like `yq` to read values out of your `pubspec.yaml` file. To learn about the rest of the config and how it works,
see the [config documentation](index.md).

## Synopsis

```
include required("/stdlib/flutter/flutter.conf")

# Use yq to convert pubspec.yaml to json 
pubspec {
  include required("#!yq -o json pubspec.yaml")
}

ci-artifacts-url = "https://some-ci-system/artifacts"

app {
  windows.amd64.inputs += ${ci-artifacts-url}/build-windows-amd64.zip
  linux.amd64.inputs   += ${ci-artifacts-url}/build-linux-amd64.zip
  mac.amd64.inputs     += ${ci-artifacts-url}/build-macos-amd64.zip
  mac.aarch64.inputs   += ${ci-artifacts-url}/build-macos-aarch64.zip

  # Delete any you don't want to support, or if you're happy with all 
  # 4 delete the entire line to accept the defaults.
  machines = [ windows.amd64, linux.amd64, mac.amd64, mac.aarch64 ]

  display-name = Flutter Demo
  fsname = flutter-demo
  license = MIT
  version = ${pubspec.version}
  
  # Where to look for updates.
  site.base-url = downloads.hydraulic.dev/flutter-demo

  # Generating a simple icon based on a small two letter label.
  icons = {
    label = FL
    gradient = "aqua;navy"
  }
  
  contact-email = "contact@hydraulic.software"
  rdns-name = dev.hydraulic.samples.FlutterDemo
  vendor = YourCompany
}
```

## Importing builds

Flutter requires native toolchains to build, so you'll need to compile on each target platform that you support. Once built you'll run
Conveyor once, either locally or in CI, and it will download and include the native files for each OS and CPU architecture.

See [continuous integration](../continuous-integration.md) for further tips on how to use GitHub Actions and how to ensure fresh downloads 
if your inputs change without the input URL changing.

Input handling has various useful features. [Learn more about inputs here](inputs.md).

!!! note "Importing macOS bundles"
    When importing files on macOS the root of the inputs is placed _inside_ the app bundle directory. In other words, the files you
    import should start with `Contents/`. Conveyor has some convenience features that can help you here. It will automatically detect when a
    tarball or zip has a single root directory and strip it. Therefore if you make an archive with files like `flutter_demo.app/Contents/MacOS/Flutter
    Demo` the right thing will happen automatically, because the root directory (`flutter_demo.app`) will be stripped leaving files that start
    with `Contents/`.

## Importing pubspec.yaml

Conveyor doesn't have direct support for reading `pubspec.yaml`. It has a more general mechanism called hashbang includes instead. This
allows you to import the output of command line tools to your config. To read config out of `pubspec.yaml` like the version we use the
useful [`yq` utility](https://github.com/mikefarah/yq) to convert it to JSON, which is understood natively. 
[Learn more about the Conveyor config language](hocon-spec.md).

## Thinning Mac binaries

Conveyor builds separate downloads for Mac Intel and Mac ARM, then uses Javascript on the download page to decide what CPU the user has.
This lets you shrink downloads and updates to get faster results. However the Flutter build system creates universal/fat apps. That's OK,
it just means the Mac download size will double but both downloads will work on either CPU architecture. But you can reduce their size by
using the `lipo` command after your build like this:

```
lipo build/macos/Build/Products/Release/flutter_demo.app/Contents/MacOS/flutter_demo \
      -output build/macos/Build/Products/Release/flutter_demo.app/Contents/MacOS/flutter_demo \
      -thin x86_64
```

This will replace the compiled binary with one just for Intel. Use `-thin arm64` to get a build just for Apple Silicon.

Future versions of Conveyor will probably do this for you.
