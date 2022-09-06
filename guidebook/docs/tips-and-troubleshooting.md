# Tips and troubleshooting

## Use good config style

Conveyor has a large default configuration that is included before your own. Following two simple rules will help avoid problems:

1. If you assign new values to lists you may overwrite any default values already there. Sometimes that's what you want, but usually your intent is to extend the defaults, not replace them. Good style is therefore to add to lists using `+=` or by writing `list = ${list} [ ... new items ... ] `. If you deviate from that and assign lists directly (`list = [a, b]`) then add a comment explaining why overriding the defaults is intended.
2. Defining your own keys is of course fine, and can be useful for substituting into other values. But define them either at the top level or group them into a top level block, rather than placing them next to where they're being used. Conveyor might add new config settings in future and if you pick the same name you'll end up overriding it, even if today it works.

## Use the correct paths at runtime

All modern operating systems dislike apps writing to their install path. To work well on networked computers and avoid compatibility surprises, your app should store files in particular places:

1. On Windows:
    1. Store transient files like logs, cached data etc in the directory pointed to by the `LOCALAPPDATA` environment variable.
    2. Store important files that should be backed up and follow the user around, but removed on uninstallation, in the directory pointed to by the `APPDATA` environment variable. On networked Windows computers things stored here will be copied to local storage when the user logs in, so apps can run offline.
    3. Store user created files *that should not be removed even if the user uninstalls*, like work the user has created, in the user's home directory. You can get this by reading the `user.home` system property or (when packaged) the `HOME` environment variable. **Don't create dot-folders here**, if you're tempted to hide files from the user then probably means you should be storing those files in `LOCALAPPDATA` or `APPDATA` instead.

2. On Linux:
    1. For desktop apps:
        1. Store transient files like logs, cached data etc in `$XDG_DATA_CACHE/$vendor/$appname` using `$HOME/.cache` as a fallback if `$XDG_DATA_CACHE` isn't set.
        2. Store important files that should be backed up and follow the user around in `$XDG_DATA_HOME/$vendor/$appname` and use `$HOME/.local/share` as a fallback if `$XDG_DATA_HOME` isn't set.

    2. For servers managed by `systemd`:
        1. Store important files that should be backed up in `$STATE_DIRECTORY`.
        2. Store log files in `$LOGS_DIRECTORY` and cache/temporary files in `$CACHE_DIRECTORY`.

3. On macOS:
    1. Store important files in `$HOME/Library/Application Support/$vendor/$appname`.
    2. Store log files in `$HOME/Library/Logs/$vendor/$appname`
    3. Store cache files in `$HOME/Library/Caches/$vendor/$appname`.


!!! warning "Always use the environment variables"
    Don't try and work out where to store files by e.g. concatenating the current username into a hard-coded path. On Windows the paths in the environment variables are changed by the Conveyor launcher once your app is packaged. That's because MSIX packaged apps run inside a lightweight form of app container and Windows will redirect file operations from the user's normal `c:\Users\username\AppData` path to a private, app specific directory. If you're familiar with Linux kernel namespaces and bind/overlay mounts, it's a similar mechanism. 

    Windows programs you start from your app that aren't a part of your package will _not_ run inside the same container. As a consequence, if you pass redirected `AppData` file paths to them the other program won't be able to find those files, as it's not running in the same filesystem namespace. Using the value of the `APPDATA` and `LOCALAPPDATA` environment variables will fix this, because they'll be set to the _targets_ of the redirects and the targets are visible to all programs.

## println debugging on Windows

For GUI apps you will find that good old `println` style debugging doesn't seem to work on Windows. That's because Windows discards stdout/stderr by default, even when an app is run from the command line. Set the `app.windows.console` key to true to make the output show up. But watch out! When this is set, running the app from the UI will also cause a console window to open, which would be ugly for users if you shipped it. Try setting the key from the command line to avoid that mistake: `conveyor "-Kapp.windows.console=true" make windows-app`.

## Viewing logs and rerunning tasks

If Conveyor isn't working or you aren't sure why something is happening, try running it with the `--show-log` parameter. You should see logs from your last execution. Run with the `LOGGING=trace` environment variable to get lots more detail. You can also write logs to the terminal as it runs by using the `-v` and for even more detail the `-vv` flag.

If a change you're making doesn't seem to be picked up on a re-run of your chosen task, you might have found a caching bug. We aren't aware of any at this time but if you find one please report it to us and then use the `--rerun` flag to force a complete rebuild of the intermediate files used in packaging.

## Increase cache size limit if tasks rerun unnecessarily

The default cache size limit is 5 gigabytes, which is a reasonable size to contain all intermediate files for a single app. If you're building multiple different apps with Conveyor then the cache may start evicting entries more often than convenient. If you see tasks re-running and can't figure out why, try passing `--cache-limit=10.0` to give it more room. 

## For JVM apps

### Don't use fat JARs

Use individual JARs as inputs. This will work much better with delta compression (as used by MSIX/AppInstaller on Windows), eliminates compatibility issues the fat JAR process can create, and will make things faster.

### Problems loading native libraries

You should always try to load native libraries using `System.loadLibrary` because by default [Conveyor moves libraries out of JARs](configs/jvm.md#jar-stripping). Don't use custom packing/unpacking code until `loadLibrary` has failed.

### Reading app files

The default configuration assigns the `app.dir` system property to wherever your input files can be found on disk. Thus if you want files to be loose (not bundled into a JAR) and loaded directly from files, you should look them up relative to this directory. 

This is useful for icons. On Windows your UI toolkit may need to be given the window icon explicitly, for example, [this is true for Jetpack Compose apps](https://github.com/JetBrains/compose-jb/tree/master/tutorials/Image_And_Icons_Manipulations).
