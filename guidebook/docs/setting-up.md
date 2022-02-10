# Getting started

**You will need:** 

1. A JVM based application. Conveyor will gain support for non-JVM apps soon!
1. Somewhere that serves static files over HTTP.
2. If code signing, an Apple Developer ID and Windows signing certificate. They can be obtained with only a web browser and credit card, on any platform. For creating unsigned packages, see below.

## Download and install Conveyor

[ :material-download-circle: Download for your OS](https://downloads.hydraulic.dev/conveyor/download.html){ .md-button .md-button--primary }

!!! note "Repackaging"
    Conveyor is distributed using itself, but if you want a version that doesn't auto update or wish to re-package it, you can just use the ZIP/tarball version.

## Set up code signing

Conveyor has built in support for everything needed to sign your downloads. It can also help you obtain keys. You need to generate a key at least for Linux, even if you won't sign your Mac/Windows files with a verified identity. [Set up code signing](keys-and-certificates.md) and then come back to this page.

## Write your config

A package is defined using a build [config](configs/index.md). Configs are written in a superset of JSON called [HOCON](configs/hocon-spec.md) that adds a more convenient syntax.

Look at the [samples](samples/index.md) page and the [standard library](stdlib/index.md) to see if there are any pre-canned configs for your type of app. If so, copy/paste and customize. Otherwise [read the tutorial](tutorial.md) to learn how to write your own configs. You should end up with one or more `conveyor.conf` files.

## Build your packages

You're ready! Run `conveyor make site` and look in the directory called `output`. Now copy this directory to your web server using a tool like `scp`, `rsync` or however else you normally publish static web files.

To release a new version just adjust the version number in the first file name, rerun `conveyor make site` and re-upload. The build is incremental so you can rapidly iterate. You can also run `mac-app`, `windows-app` and `linux-app` to get the executable tree unpackaged.

## Testing it

A quick way to test things are working is to create and upload a site, then immediately upload another with a higher revision (which turns into a higher version number), like this:

```
# Make an initial version.
conveyor make site

# Copy it to the server.
rsync -avz output/ www.server.com:public_html/downloads

# Open the site in your browser and try downloading, installing the package.
open "https://www.server.com/downloads/download.html"

# Make a version that's identical but with a higher version number.
conveyor -Kapp.revision=1 make site

# Sync it again.
rsync -avz output/ www.server.com:public_html/downloads
```

Now check if updates work:

* On Debian/Ubuntu try `apt-get update; apt-get upgrade` and see if your package is detected as updated.
* On Windows, open up PowerShell and run `Get-AppxPackageAutoUpdateSettings -ShowUpdateAvailability` to see if Windows recognizes that there's an update available. You can also try re-opening the AppInstaller XML file; it should have an "Update" button.
* On macOS, run the app from the command line whilst setting the `FORCE_UPDATE_CHECK=1` environment variable, e.g. run `FORCE_UPDATE_CHECK=1 /Applications/YourApp.app/Contents/MacOS/YourApp` from a terminal. You should get an update notification.

Didn't work? Need more than that? Or you didn't get quite what you wanted? Don't worry - start by looking at the rest of this user guide. You can change many settings to get better results. If you're still not sure, then [join our chat room](https://gitter.im/hydraulic-software/community) or [post to the forums](https://github.com/hydraulic-software/conveyor/discussions).

## Next steps

Read about [what Conveyor produces](outputs.md), how to [run the tool](running.md), and then learn [how to customize the results](configs/index.md).
