# 2. Create or adapt a project

## Pick a reverse DNS name

* [ ] A reverse DNS name identifies your app. If you can't think of one, use `com.example.my-project`.

Every project needs a reverse DNS (RDNS) name. RDNS names are used by some operating systems to keep apps separate even if they share a brand name. Domain name ownership isn't checked by anything. 

If you don't have a website consider creating a [GitHub](https://www.github.com) account and then using `io.github.youruser.yourproject`, which will ensure no naming conflicts with anyone else.  Use `names-with-dashes` when separating words, not `camelCase`, as that way you'll get smarter defaults.

## Generating a starter project

This is the quickest way to try Conveyor without getting distracted by details of your actual app.

=== "Electron"

    * [ ] `conveyor generate electron com.example.my-project`
    * [ ] `cd my-project`
    * [ ] `npm install`
    * [ ] `conveyor make site`

=== "Native"

    * [ ] Install [CMake](https://cmake.org/) and the compiler toolchain for each platform you will target.
    * [ ] `conveyor generate cmake com.example.my-project`
    * [ ] `cd my-project`
    * [ ] Read the README to learn how to compile the project.
    * [ ] `conveyor make site`.

=== "JVM"

    * [ ] Install a JDK 17 or higher from any vendor e.g. [Amazon Corretto](https://aws.amazon.com/corretto) is a good choice.
    * [ ] `conveyor generate compose com.example.my-project` <br>or<br> `conveyor generate javafx com.example.my-project`
    * [ ] `cd my-project`
    * [ ] `./gradlew jar`  - this step will download Gradle if you don't already have it.
    * [ ] `conveyor make site`

You now have a demo project with a `conveyor.conf` file in it, and possibly some build system integration.

## Packaging a project with Conveyor

There are three steps:

1. Writing a `conveyor.conf` file.
1. Preparing the build system.
1. Running Conveyor to generate packages or update sites.

`conveyor.conf` defines your packages and is defined using a superset of JSON called [HOCON](../configs/hocon-spec.md) with a few [Conveyor-specific extensions](../configs/hocon-extensions.md). This makes it much more pleasant to express configuration whilst preserving the simple JSON data model. It also means you can import JSON into your config directly, including config generated dynamically by other programs.

Don't worry about the details of `conveyor.conf` too much right now. Most projects don't need many settings.

!!! tip "Packaging a server app for Linux with systemd integration"
    You can also package servers with Conveyor. The resulting packages get systemd integration, can be sandboxed, and contain pre-canned Apache/nginx configs. Learn how to [adapt a server](2-adapt-a-server.md).

!!! tip
    Click the + icons next to code lines to see further explanations.

=== "Native"

    Most of the `conveyor.conf` for a native app is straightforward. However, look at the lines defining the [`inputs`](../configs/inputs.md). They are defining where the binaries should be found on local disk, and where they should be placed in the package for each OS.

    ```javascript title="conveyor.conf"
    app {
      display-name = Template App // (1)!
      fsname = template-app // (2)!
      rdns-name = org.some-org.some-product
      version = 1
      site.base-url = "localhost:8899" // (3)!
    
      machines = [ 
        windows.amd64, linux.amd64.glibc, mac.amd64, mac.aarch64 // (4)!
      ]  
      
      icons = "icons/icon-rounded*" // (5)!
      windows.icons = "icons/icon-square*"
    
      mac.inputs = [ // (6)!
        build/mac/installation/bin -> Contents/MacOS
        build/mac/installation/lib -> Contents/Frameworks
      ]
    
      windows.amd64.inputs = build/win/installation/bin
      linux.amd64.inputs = build/linux/installation
    }
    ```
    
    1. The display name is the natural language name of the project as it appears to the user. It's initialized with a guess based on de-dashifying the `fsname` key.
    2. The `fsname` is the name of the project as it appears on disk, e.g. in file names.
    3. This is a directory on a web server where packages will look for update files.
    4. You can restrict which platforms you support. See ["Machines"](../configs/index.md#machines) for details.
    5. The templates come with pre-made icons. You should replace these files with your own. Conveyor will take care of converting to native formats and embedding the icon into the Windows EXE file.
    6. The CMake build system produces an install directory that uses non-Mac UNIX conventions. Here, we adapt it to a Mac bundle layout. If your build system produces a `.app` bundle already you can just provide the path of the bundle directory. See Apple's document ["Placing content in a bundle"](https://developer.apple.com/documentation/bundleresources/placing_content_in_a_bundle).

    A native app will require a few tweaks before being packaged. A concrete example can be found in the native OpenGL sample which uses the CMake build system. The most important change required is adding linker flags:

    1. On UNIX you need to set an `rpath` so the program can find any bundled shared libraries. It should be set to `@executable_path/../Frameworks` on macOS, and `$ORIGIN/../lib` on Linux.
    2. On macOS you will additionally need to pass `-headerpad 0xFF` to the linker. This allows Conveyor to inject some code into the program's startup sequence which initializes the Sparkle update framework.

    ??? note "Code injection on macOS"
        Windows and Linux have built-in package managers that can update software automatically, but macOS does not. The only Apple provided way to ship software updates to Mac users is via the App Store. Conveyor doesn't go this route. Instead, it uses the popular [Sparkle Framework](https://sparkle-project.org/) to give your app the ability to update itself. Sparkle is a de-facto standard used across the Mac software ecosystem.
    
        For Sparkle to work it must be initialized at app startup. To avoid you needing to write Mac specific code in Objective-C or Swift, Conveyor will edit the Mach-O headers of your binary when it builds the bundle to inject a shared library that starts up Sparkle for you. This happens automatically for any app that links against Cocoa or AppKit and not Sparkle. This feature is particularly useful for apps that aren't written in C++, as long as they provide sufficient header padding space. The amount of space left for adding headers can be controlled using Apple's `ld` linker with the `-headerpad` flag. If your language toolchain doesn't support header padding, this technique won't work and you'll have to link against `Sparkle.framework` yourself.

    ??? note "Icons and manifests on Windows"
        Windows programs contain embedded metadata like icon files, XML manifests and whether it's a console app. Conveyor will edit the EXE to reflect settings in your config, so you don't need to set these things up in your build system. Any icons or manifests already there will be replaced. [Learn how to control the binary manifest](../configs/windows.md#manifest-keys).

    The `CMakeLists.txt` file of the OpenGL sample project contains commands with comments explaining what they do. This build system demonstrates importing a third party library from a source zip, compiling it, dynamically linking against it, and passing the right linker flags to produce binaries that will work with Conveyor. It looks like this:
    
    ```javascript
    cmake_minimum_required(VERSION 3.16.3)
    project(gl_cmake)
    
    set(CMAKE_CXX_STANDARD 17) // (1)!
    set(CMAKE_OSX_ARCHITECTURES arm64;x86_64) // (2)!
    set(CMAKE_INSTALL_PREFIX ${CMAKE_BINARY_DIR}/installation) // (3)!
    
    if (APPLE) // (4)!
        set(CMAKE_INSTALL_RPATH "@executable_path/../Frameworks")
        set(CMAKE_EXE_LINKER_FLAGS 
            ${CMAKE_EXE_LINKER_FLAGS} "-Wl,-headerpad,0xFF")  // (5)!
    elseif (UNIX)
        set(CMAKE_INSTALL_RPATH "\$ORIGIN/../lib")
    endif()
    
    include_directories("include")
    add_executable(gl_cmake
            src/main.cpp
            src/gl.c
    )
    
    if (APPLE)
        target_link_libraries(gl_cmake PRIVATE "-framework Cocoa") // (6)!
    endif()
    
    include(ImportGLFW.txt) // (7)!
    target_link_libraries(gl_cmake PRIVATE glfw)
    
    install(TARGETS gl_cmake)
    ```
    
    1. Use the C++17 standard.
    2. When compiling on macOS, cross-compile for Intel and Apple Silicon. The result is a universal fat binary.
    3. "Install" to a directory tucked neatly into the build directory. Conveyor will pick up the binaries from this directory later.
    4. Ensure libraries can be found. On UNIX systems libraries are stored in a different directory to the executable, and a header in the binary file tells the linker where to look. Unfortunately they are not set by default, so we must instruct the linker to add these headers here. The syntax varies between macOS and Linux. It isn't necessary for Windows where the convention is to put DLLs and EXEs in the same directory.
    5. On macOS Conveyor will inject its own library into the binary to initialize the update system. Ensure there is sufficient empty space in the headers to make this possible.
    6. On macOS we should also depend on Apple's Cocoa GUI framework, because otherwise Conveyor will think we're not a GUI app and not initialize Sparkle. In this case all the GUI work is being done by GLFW but that's rare, and only because this is such a simple example. Normally you'd need this regardless.
    7. Import and compile an open source library. Delete these lines if you don't want to use OpenGL.

=== "JVM"

    It's possible to package JVM apps with no code changes. Nonetheless:
    
    1. The `app.version` and `app.dir` system properties may prove useful. You can [set any other system properties you like in the config](../configs/jvm.md).
    1. Compose and JavaFX apps can load their window icons from the packaged files. See the Gradle page or the code of the template apps to get this snippet.

    How to integrate a JVM project depends on your build system. Please select:

    * [Gradle](2-gradle.md)
    * [Maven](2-maven.md)
    * [Other](2-other-jvm-builds.md)

    ??? warning "Uber-jars"
        Don't use an uber/fat-jar for your program unless you're obfuscating. It'll reduce the efficiency of delta download schemes like the one used by Windows. It also means modular JARs won't be encoded using the optimized `jimage` scheme. Use separate JARs for the best user experience.


=== "Electron"

    The build system of Electron apps doesn't need any adaptation for Conveyor. The `conveyor.conf` file should look like this:
    
    ```hocon
    include required("/stdlib/electron/electron.conf") 
    
    package-json {  
      include "package.json"
    }
    
    app {
      display-name = "Electro Thing"
      rdns-name = com.example.electro-thing
      site.base-url = "localhost:8899"
      icons = "icons/icon-*.png"
    }
    ```
    
    The configuration is straightforward. The first line activates Electron support by importing configuration from the standard library.

    The next few lines take advantage of the fact that HOCON is a superset of JSON, and thus all valid JSON is also valid HOCON. It imports
    the `package.json` file into your Conveyor config, so values in it are available for substitution. Inside the 
    `/stdlib/electron/electron.conf` file that comes with Conveyor is code like this (see [full version](../configs/electron.md#stdlib-config)):
    
    ```hocon
    app {
      // Read core metadata from the package.json file.
      fsname = ${package-json.name}
      version = ${package-json.version}
      electron.version = ${package-lock.packages.node_modules/electron.version}
    
      // Import typical files that make up the app.
      inputs = ${app.inputs} [

        "*.{json,js,css,html}"
    
        {
          from = node_modules
          to = node_modules
          remap = ["-electron/dist/**"]
        }

      ]
    }
    ```
    
    As you can see, HOCON syntax lets us copy data out of the `package.json` file and assign to the right place in the Conveyor config
    schema. You can also read the `package-lock.json` file, which can be useful because the lockfile has the specific version
    of Electron that you've chosen to use, whereas `package.json` may contain a version range.
    
    The default inputs will copy JavaScript, JSON, CSS and HTML files from the project root into the app along with the `node_modules` directory,
    whilst excluding the `dist` sub-directory of the `electron` module (which contains a complete Mac app bundle that isn't needed). 
    
    **Although these defaults will work, they will create a bloated package.** A better approach would be to integrate a bundler like `webpack`
    or `vite` so the `node_modules` directory isn't shipped. Because the JavaScript ecosystem changes so quickly, and the way this is done
    varies between projects, setting this up is left as an exercise for the reader.

    !!! note
        Conveyor doesn't currently generate [ASAR files](https://github.com/electron/asar), which Electron can use to speed startup. Your
        build system will need to generate those itself.

<script>var tutorialSection = 2;</script>
