# Control API (native)

The JVM and Electron APIs are simple and you don't have to use them. To interact with the control API from native code, you can do the following.

## Reading metadata

Download the `metadata.properties` file from the base of your update site and parse out the `app.version` key to discover the currently
published version. Ensure your app knows its own version and then compare it using your own string parsing and comparison routines.

If you need to, you can create a file in your app distribution containing the version as specified in `conveyor.conf` like this:

```
app.inputs += {
    to = current-version.txt
    content = ${app.version}
}
```

## Windows

In the root of the generated package there will be a file called `updatecheck.exe`. To perform an update+restart, run `updatecheck.exe --update-check` and then immediately quit. It's important that you quit because the updater will otherwise force your app to shut down and then give it 30 seconds to do, but your app probably don't need that much time. It will therefore be a much better experience if you quit immediately after executing the checker, as by the time the updater has downloaded the update and needs to apply it your program will have already quit and the delay is avoided. So, make sure you only invoke `updatecheck.exe` when your user won't lose any work.

After the update applies your app will be restarted at the new version. If no update is actually available then your app will just be immediately restarted at the same version.

## macOS

Look up the `conveyor_check_for_updates` symbol using dlsym and invoke it. It'll be available in your packaged app, and this will trigger Sparkle's update prompt. The user can then choose whether to install the update or not. There's no way currently to bypass the GUI dialog, so the user always has an option. Your app will be restarted after the update is applied.
