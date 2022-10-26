# 1. Get started

## tl;dr

[ :material-download-circle: Download Conveyor](https://downloads.hydraulic.dev/conveyor/download.html){ .md-button .md-button--primary }

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

Now set where clients will look for updates.  

```shell
# Set the update repository URL.
echo 'app.site.base-url = downloads.your-site.org' >>conveyor.conf

# Set an upload URL if your web server is accessible with SFTP.
echo 'app.site.copy-to = "sftp://user@your-site/var/www/downloads"' >>conveyor.conf

# Rebuild and upload (use `make site` if not using sftp).
conveyor make copied-site
```

----

## Welcome!

!!! tip
    You can tick the checkmarks on these pages to mark your progress. Their state is stored in a cookie.

In this tutorial you will learn:

* How to package a generated or pre-existing app.
* How to configure and run Conveyor to build packages and download/update sites. 

You will get a directory that contains:

* Windows, Mac and Linux packages.
* Update repository metadata.
* Scripts that users can pipe to a shell to launch from the command line.
* A `download.html` page that gives your users a big green download button:
    * [Signed app example](https://downloads.hydraulic.dev/eton-sample/download.html) 
    * [Self-signed app example](https://downloads.hydraulic.dev/eton-sample/selfsigned/download.html)

At the end you'll learn how to do code signing, and how Conveyor helps you obtain certificates if needed. 

This tutorial doesn't cover all the features Conveyor has. Read through the rest of this guidebook to learn about the full range of possibilities.

<script>var tutorialSection = 1;</script>
