# 1. Get started

## tl;dr

[ :material-download-circle: Download Conveyor](https://downloads.hydraulic.dev/conveyor/download.html){ .md-button .md-button--primary }

```shell
$ conveyor keys generate
$ conveyor generate {electron,compose,javafx,cmake} org.your-site.your-app-name
$ cd your-app-name
$ conveyor make site
# Your download site is here, ready for serving on localhost.
$ ls output
# Set the final download URL and rebuild.
$ echo 'app.site.base-url = downloads.your-site.org'
$ conveyor make site
# Copy up to the server.
$ cd output; scp * www@your-site.org:/var/www/downloads/  
```

## Welcome!

!!! tip
    You can tick the checkmarks on these pages to mark your progress. Their state is stored in a cookie.

In this tutorial you will package one of:

* A test app generated using the built-in templates (native, JVM or Electron)
* Your pre-existing app.

You will end up with a directory that contains:

* Windows, Mac and Linux packages.
* Update repository metadata.
* Scripts that users can pipe to a shell to launch from the command line.
* A `download.html` page that gives your users a big green download button:
    * [Signed app example](https://downloads.hydraulic.dev/eton-sample/download.html) 
    * [Self-signed app example](https://downloads.hydraulic.dev/eton-sample/selfsigned/download.html)

At the end you'll learn how to do code signing, and how Conveyor helps you obtain certificates if needed. 

This tutorial doesn't cover all the features Conveyor has. Read through the rest of this guidebook to learn about the full range of possibilities.

----

Let's go!

* [x] [Download Conveyor](../download-conveyor.md). On macOS, make sure it's added to your path by opening the app from the GUI and clicking the "Add to path" button.

You don't need code signing certificates to use Conveyor. Nonetheless, you always need cryptographic keys so it can at least self-sign your app. Self-signing is good enough for learning, testing, internal apps and distributing software to developers. The final steps of the tutorial show you how to use real code signing keys to get a smoother download user experience.

* [x] Run `conveyor keys generate` from a terminal.

This command will create a new root private key, convert it to a series of words and write it to a config file in your home directory.

!!! tip "Key derivation"
    You can back this generated file up in any way you like, even by writing down the words with a pen (include the timestamp). Each type of key Conveyor needs will be derived from this one root, unless you supply a custom key for specific platforms.

<script>var tutorialSection = 1;</script>
