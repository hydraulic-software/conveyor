# 3. Compile the app

!!! important 
    You'll need to re-invoke your build system any time you change the app's code. At this time Conveyor won't do it for you.

=== "JVM"
    * [ ] If packaging a template project, run `./gradlew jvmJar` or `./gradlew jar` to download the Gradle build tool and compile the app to a portable JAR. Otherwise, invoke your build system of choice.

=== "Electron"

    * [ ] Run `npm install` to download Electron for development purposes.
    
    In a real project you would probably use `webpack`, `vite` or some other packer tool to do tree shaking and reduce the number of files. Because there are so many different ways to set up JavaScript projects, this template gives you the most minimal setup possible and lets you configure the rest as you prefer. The generated `conveyor.conf` will import any files matching `*.{js,json,css,html}` at the top level of your project. 

=== "Native"

    [Native apps](../../configs/native-apps.md) can be written in any language that produces a binary, but the template uses C++. Native apps must be compiled for each OS you wish to target. Getting access to different build machines is more work than necessary for this tutorial, so you should now edit `conveyor.conf` and edit the `machines = [ ... ]` line, changing the contents of the list to reflect which platform(s) you're using and will compile the app for.
    
    Instructions for how to build are in the `README.md` file. It's a conventional CMake build system of the kind widely used in the C++ ecosystem, so we won't go into details here.
    
    * [ ] Edit the machines key in the `conveyor.conf` file.
    * [ ] Follow the build instructions in `README.md` to create the binaries.

=== "Flutter"

    * [ ] Use `flutter build {windows,macos,linux} --release` on each platform as appropriate.

<script>var tutorialSection = 3;</script>
