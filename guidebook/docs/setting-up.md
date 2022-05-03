# Getting started

You will need:

- [x] **An application to package.** Conveyor can generate a hello world app if you don't have one.
- [x] **A static web server.** GitHub Releases works OK.
- [x] **A fully energized terminal.** On Windows, please use [Windows Terminal](https://apps.microsoft.com/store/detail/windows-terminal/9N0DX20HK701) instead of the default console.

!!! important
    Currently only JVM applications are supported. This is a temporary limitation that will be lifted soon.

## Download Conveyor

!!! warning
    **Conveyor is currently in private beta**. This build will expire at the end of May.

[ :material-download-circle: Download for your OS](https://downloads.hydraulic.dev/conveyor/download.html){ .md-button .md-button--primary }

**Updates.** Conveyor is packaged using itself. To avoid surprises, automatic background updates are switched off for Windows and macOS. To update run it from the GUI instead of the command line and then click "Check for updates". On Linux you can control updates in the usual manner (the tarball never updates itself).

## Add it to your path

* **On Windows**: do nothing. The app is already in your path after installation and you don't need to restart any terminal sessions either. This is a feature of MSIX.
* **On macOS**: open the app and click "Add to shell". You may need to re-exec your shell to get the `$PATH` update.
* **On Linux**: you don't need instructions :wink:

Now run `conveyor --help` to check it's ready to go.

## Create a root key

**Run `conveyor keys generate` from a terminal.** You'll be asked for an optional passphrase and a `defaults.conf` file will be created in your home directory. The contents will look like this:

```
app.signing-key = "vicious noble apart total march unit veteran kangaroo recipe plastic unit pottery awkward exhibit curve laugh envelope super shadow primary sword ginger sustain century/2022-01-26T15:03:11Z"
```

You don't need any certificates to make apps with Conveyor. By default your apps will be self-signed using the root key and users will be asked to copy/paste commands to their terminal to install. If you buy certificates then Conveyor will sign and notarize your packages, giving users the standard experience with no security errors.

If you already have certificates you'd like to use, read about [keys and certificates](keys-and-certificates.md).

## Next steps

1. Read about [what Conveyor produces](outputs.md).
1. Please read the [release notes](release-notes.md)!
1. Take [the tutorial](tutorial.md).
1. Learn more about how to [run the tool](running.md).
1. Learn [how to customize the results](configs/index.md).
