# Download Conveyor

[ :material-download-circle: Download for your OS](https://downloads.hydraulic.dev/conveyor/download.html){ .md-button .md-button--primary }

After downloading:

* **On macOS**, open the app and click "Add to shell path" so you can use the `conveyor` command from the terminal. You'll need to re-open terminal tabs to apply the `$PATH` change.
* **On Windows**, make sure you are using [Windows Terminal](https://apps.microsoft.com/store/detail/windows-terminal/9N0DX20HK701). The legacy default Windows console has some rendering bugs. Conveyor will appear in your `%PATH%` automatically so you don't need to restart any terminals.  

Now run `conveyor --help` to check it's ready to go and move on to the next section.

!!! warning
    **Conveyor is currently in private beta**. To go beyond the tutorial you need a license key (it looks like `abcd-efgh`). As long as your download site is set to localhost you don't need anything. To try it for a real app email [contact@hydraulic.software](mailto:contact@hydraulic.software) and request a key.

!!! note "Automatic updates"
    Conveyor is packaged with itself, but automatic updates are switched off for Windows and macOS to give you full control. To update Conveyor, run it from the GUI and then click "Check for updates". On Debian you can control updates in the usual manner and for other distros, the tarball never updates itself.

## Create a root key

**Run `conveyor keys generate` from a terminal.** You'll be asked for an optional passphrase and a `defaults.conf` file will be created in your home directory. The contents will look like this:

```
app.signing-key = "vicious noble apart total march unit veteran kangaroo recipe plastic unit pottery awkward exhibit curve laugh envelope super shadow primary sword ginger sustain century/2022-01-26T15:03:11Z"
```

You don't need any certificates to make apps with Conveyor. By default, your apps will be self-signed using the root key and users will be asked to use a special procedure to install. 

If you'd like to code sign your apps, read about [keys and certificates](keys-and-certificates.md).

## Next steps

1. Please read the [release notes](release-notes.md)!
1. Learn [what Conveyor produces](outputs.md).
1. Take [the tutorial](tutorial.md).
1. Learn more about how to [run the tool](running.md).
1. Learn [how to customize the results](configs/index.md).
