# 3. Compile the app

## JVM

* [ ] Run `./gradlew jar` to download the Gradle build tool and compile the app to a portable JAR.

**You'll need to re-run this command any time you change the app's code. At this time Conveyor won't do it for you.**

## Native / C++

Native apps can be written in any language that produces a binary, but the template uses C++. Native apps must be compiled for each OS you wish to target. Getting access to different build machines is more work than necessary for this tutorial, so you should now edit `conveyor.conf` and edit the `machines = [ ... ]` line, changing the contents of the list to reflect which platform(s) you're using and will compile the app for.

Instructions for how to build are in the `README.md` file. It's a conventional CMake build system of the kind widely used in the C++ ecosystem, so we won't go into details here.

* [ ] Edit the machines key in the `conveyor.conf` file.
* [ ] Follow the build instructions in `README.md` to create the binaries.

**You'll need to re-compile the app any time you change the app's code. At this time Conveyor won't do it for you.**

??? note "Icons and manifests"
    Windows programs contain embedded metadata like icon files, XML manifests and whether it's a console app. Conveyor will edit the EXE to reflect settings in your config, so you don't need to set these things up in your build system. Any icons or manifests already there will be replaced. [Learn how to control the binary manifest](../configs/windows.md#manifest-keys).

<script>var tutorialSection = 3;</script>
