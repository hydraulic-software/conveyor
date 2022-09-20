# 3. Compile the app

!!! important 
    You'll need to reinvoke your build system any time you change the app's code. At this time Conveyor won't do it for you.

=== "JVM"
    * [ ] Run `./gradlew jar` to download the Gradle build tool and compile the app to a portable JAR.

=== "Electron"

    * [ ] Run `npm install` to download Electron for development purposes.
    
    In a real project you would probably use `webpack`, `vite` or some other packer tool to do tree shaking and reduce the number of files. Because there are so many different ways to set up JavaScript projects, this template gives you the most minimal setup possible and lets you configure the rest as you prefer. The generated `conveyor.conf` will import any files matching `*.{js,json,css,html}` at the top level of your project. 

=== "Native"

    [Native apps](../configs/native-apps.md) can be written in any language that produces a binary, but the template uses C++. Native apps must be compiled for each OS you wish to target. Getting access to different build machines is more work than necessary for this tutorial, so you should now edit `conveyor.conf` and edit the `machines = [ ... ]` line, changing the contents of the list to reflect which platform(s) you're using and will compile the app for.
    
    Instructions for how to build are in the `README.md` file. It's a conventional CMake build system of the kind widely used in the C++ ecosystem, so we won't go into details here.
    
    * [ ] Edit the machines key in the `conveyor.conf` file.
    * [ ] Follow the build instructions in `README.md` to create the binaries.
    
    ??? note "Linker flags"
        On macOS and Linux extra linker flags are required for correct operation in the package environment. These flags don't affect operation when running from the development build tree, so can be always added. See the [native apps](../configs/native-apps.md) section to learn more.

    ??? note "Icons and manifests"
        Windows programs contain embedded metadata like icon files, XML manifests and whether it's a console app. Conveyor will edit the EXE to reflect settings in your config, so you don't need to set these things up in your build system. Any icons or manifests already there will be replaced. [Learn how to control the binary manifest](../configs/windows.md#manifest-keys).

<script>var tutorialSection = 3;</script>
