# 3. Compile the app

## JVM

* [ ] Run `./gradlew jar` to download the Gradle build tool and compile the app to a portable JAR.

**You'll need to re-run this command any time you change the app's code. At this time Conveyor won't do it for you.**

## Electron

* [ ] Run `npm install` to download Electron for development purposes.

In a real project you would probably use `webpack`, `vite` or some other packer tool to do tree shaking and reduce the number of files. Because there are so many different ways to set up JavaScript projects, this template gives you the most minimal setup possible and lets you configure the rest as you prefer. The generated `conveyor.conf` will import any files matching `*.{js,json,css,html}` at the top level of your project. 

## Native

Native apps can be written in any language that produces a binary, but the template uses C++. Native apps must be compiled for each OS you wish to target. Getting access to different build machines is more work than necessary for this tutorial, so you should now edit `conveyor.conf` and edit the `machines = [ ... ]` line, changing the contents of the list to reflect which platform(s) you're using and will compile the app for.

Instructions for how to build are in the `README.md` file. It's a conventional CMake build system of the kind widely used in the C++ ecosystem, so we won't go into details here.

* [ ] Edit the machines key in the `conveyor.conf` file.
* [ ] Follow the build instructions in `README.md` to create the binaries.

**You'll need to re-compile the app any time you change the app's code. At this time Conveyor won't do it for you.**

??? note "Linker flags for macOS"
    On macOS Conveyor uses the Sparkle framework to implement online updates. You don't have to start up Sparkle yourself because Conveyor will inject code into the binary at build time. To do this there has to be some empty space for additional Mach-O headers to be written in. Apple's linker can be given a flag to add such space, and the CMake build system in the native app sample does so. If you're using other languages or toolchains you'll need to pass the `-headerpad 0xFF` flag to Apple's `ld` via whatever mechanism is normal for your compiler. 

??? note "Icons and manifests"
    Windows programs contain embedded metadata like icon files, XML manifests and whether it's a console app. Conveyor will edit the EXE to reflect settings in your config, so you don't need to set these things up in your build system. Any icons or manifests already there will be replaced. [Learn how to control the binary manifest](../configs/windows.md#manifest-keys).

<script>var tutorialSection = 3;</script>
