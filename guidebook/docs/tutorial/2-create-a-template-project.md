# 2. Create a template project

Conveyor has three pre-canned "Hello World" project templates, all Apache 2 licensed so they can form the basis of your own apps.

One is a native OpenGL app written in C++, the second is for a GUI JVM app using the reactive [Jetpack Compose for Desktop](https://www.jetbrains.com/lp/compose-desktop/) toolkit, and the last is another JVM GUI app using [JavaFX](https://www.openjfx.io). Generating a project based on these templates is the quickest way to try things out. The JVM apps are easier to play with because you don't need cross-compilers. For the C++ project you'll need to compile it on each OS that you wish to target. It's your choice, this tutorial will guide you through all of them.

* [x] For the native C++ app, install CMake and the compiler toolchain for each platform you will target.
* [x] For a JVM app, install a JDK 11 or higher.
* [x] Run the following command, picking an app type and reverse DNS name as you see fit. There are also `--display-name` and `--output-dir` flags but they are optional.

```
conveyor generate {cmake,compose,javafx} --rdns=com.example.my-project
```

* [x] Change into the output directory you just created.

Identifying an app with a reverse DNS name is required for some platforms like macOS, and other needed values can be derived from it. A directory named after the last component (in this case `my-project`) will be populated with a buildable project. Use `names-with-dashes` when separating words, not `camelCase`, as that way you'll get smarter defaults.

We'll explore what's inside the project directory in a moment. For now, note that there's a `conveyor.conf` file at the top level directory. This is where your packages are configured.

!!! tip "Reverse DNS names"
    RDNS names are just a naming convention meant to keep apps clearly separated. Actual domain name ownership isn't checked by anything. If you don't have a website consider creating a [GitHub](https://www.github.com) account and then using `io.github.youruser.yourproject`, which will ensure no naming conflicts with anyone else.

!!! tip "Cross platform UI"
    Jetpack Compose is the next-gen native UI toolkit on Android and it also runs on Windows/Mac/Linux, making it easy to share code between mobile and desktop. [JavaFX also runs on mobile](https://gluonhq.com/products/mobile/) and [the web](https://www.jpro.one). The native C++ app uses OpenGL and the [GLFW library](https://glfw.org/), which abstracts the operating system's windowing APIs.

<script>var tutorialSection = 2;</script>
