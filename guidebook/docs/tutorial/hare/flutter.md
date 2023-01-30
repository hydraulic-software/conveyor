# Flutter

{!tutorial/hare/start.md!}

## Create a sample project

* [x] Create a new Flutter project, either with your IDE or with the CLI (`flutter create my_project`).
* [x] Change the version to remove the build number (the `+1` after the version). Version numbers aren't allowed to have `+` symbols in them in most operating system packaging formats.

Now let's build it:

=== ":simple-windows: Windows"

    ```
    flutter build windows --release
    ```

=== ":simple-apple: macOS"

    ```
    flutter build macos --release
    ```

=== ":simple-linux: Linux"

    ```
    flutter build linux --release
    ```

!!! important
    If you share your source directory between platforms e.g. using a virtual machine mount, you may need to run `flutter clean` when
    switching between different operating systems.

* [x] Install [the `yq` tool](https://github.com/mikefarah/yq) by Mike Farah. We'll use it to import config from the `pubspec.yaml` file.
* [x] Create a file called `conveyor.conf` in the root of your project:

```
include required("/stdlib/flutter/flutter.conf")

pubspec {
  include required("#!yq -o json pubspec.yaml")
}

app {
  # 💥 EDIT THESE NEXT 4 LINES!!! 💥
  display-name = My Project
  fsname = my-project
  rdns-name = com.example.MyProject
  vendor = SuperOrg

  version = ${pubspec.version}
  description = ${pubspec.description}
  
  site.base-url = "localhost:3000"
  updates = aggressive

  windows.amd64.inputs += build/windows/runner/Release
  linux.amd64.inputs += build/linux/x64/release/bundle
  mac.inputs += build/macos/Build/Products/Release/${pubspec.name}.app
}
```

* [x] Edit the four metadata lines at the start of the app object. 

## Create the unpackaged app

{!tutorial/hare/create-unpackaged-app.md!}

## Restrict to your host machine

Conveyor can build packages for every OS from any OS, so normally we'd now compile our test app on Windows, macOS and Linux then get all the binaries onto the same machine. To save time for now we'll restrict Conveyor to building packages only for your current OS. Later we'll see how to use GitHub Actions as a source of binaries for each OS.

=== ":simple-windows: Windows"

    * [x] Inside the `app { .. }` block, add `machines = windows.amd64`

=== ":simple-apple: macOS"

    * [x] Inside the `app { .. }` block, add `machines = mac`

=== ":simple-linux: Linux"

    * [x] Inside the `app { .. }` block, add `machines = linux.amd64.glibc`

{!tutorial/hare/hocon-tip.md!}

## Serve the download site

{!tutorial/hare/serve-with-npx.md!}

{!tutorial/hare/serve-without-npx.md!}

## Release an update

In another terminal tab:

* [x] Edit `pubspec.yaml` and change the version field to `2.0`.
* [x] Run the build command you used in the first step.
* [x] Run `conveyor make site` to regenerate the download site.

{!tutorial/hare/apply-update-instructions.md!}

## Change the icon

Conveyor can set the icons for your app on every platform given some image inputs, or even generate a synthetic icon for you.

{!tutorial/hare/generate-icons.md!}

## Integrate with a continuous build

{!tutorial/hare/ci.md!}

## Upload a real update site

{!tutorial/hare/upload-real-site.md!}

## Signing

{!tutorial/hare/signing.md!}

## Become a 🐢 tortoise

{!tutorial/hare/become-a-tortoise.md!}

<script>var tutorialSection = 400;</script>
