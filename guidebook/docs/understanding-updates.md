# Understanding updates

The main reason people use Conveyor is that it makes your app automatically update without needing code changes, and it works for any kind
of app regardless of language or framework.

## Update modes

Conveyor supports background updates (Chrome style) and what we call "aggressive updates", which is more like on the web - your app
will update synchronously (without starting up) on every single launch. [Learn more about update modes](configs/update-modes.md).

## Update site

The [download site](configs/download-pages.md) that you get from `conveyor make site` contains various metadata files as well as packages 
and a simple HTML download page (which is optional). You should upload **all** the files, but can skip `download.html` if you don't want it.

Learn more about [the files that are generated](package-formats.md) and [how to configure your download servers](configuring-cdns.md).

## Testing updates

If you use [aggressive updates](configs/update-modes.md#aggressive-updates) then testing the update procedure is easy. Release your app for
the first time using `conveyor make site` or `conveyor make copied-site` and install/run it. Now tweak your code somehow so you can tell an
update has occurred, re-release the site and restart your app. You should see it update and start up (Windows/Mac only at this time).

If you use background updates, testing is a little harder and will depend on platform.

=== ":simple-windows: Windows"

    1. By default Windows checks for app updates every eight hours. [Follow these instructions to make the update checks more frequent](configs/windows.md#appwindowsupdates). Unfortunately the highest frequency Windows offers is once an hour, but this doesn't necessarily mean you have to wait an hour. Windows batches update checks together for efficiency, so you may wait a lot less than that. In our testing we often only had to wait ten minutes.
    1. Whilst the app is running, tweak your app and release a new version of it.
    1. Open the Event Viewer app from the start menu. Make sure to run it as administrator, not a regular user. Navigate to `\Applications and Services Logs\Microsoft\Windows\AppXDeployment-Server\Operational` and watch the logs (you must manually refresh using the sidebar in this app). Go get lunch, come back. Assuming you had a proper "working lunch" with beers involved you should now be able to find log entries talking about updating your app.
    1. Restart your app and notice that it's now the new version.

=== ":simple-apple: macOS"
      
    Release your app, install it, start it, re-release it. You now have two options:

    1. Run your app from the command line like this: `FORCE_UPDATE_CHECK=1 /Applications/Your App.app/Contents/MacOS/Your App`. You won't see anything different at first, but give it a minute and look in `~/Library/Caches/${app.rdns-name}/org.sparkle-project.Sparkle/Installation`, where `${app.rdns-name}` comes from your config file. You can also find your bundle ID/RDNS name by looking in your `Info.plist` (it is a reverse DNS name). You should see that Sparkle has staged the update into this directory, and when you quit the app it will be copied over your existing app.
    2. Set `app.mac.sparkle-options.SUScheduledCheckInterval = 60` to make Sparkle check for updates every minute. Run your app. Re-release it. Wait for the app to notice there's an update and apply it. Restart, notice the app is now at the new version. Put the update check interval back at something more reasonable (3600 is the default, i.e. check once an hour).

=== ":simple-linux: Linux"

    On Debian/Ubuntu, run `apt-get update; apt-get upgrade` and verify your program has been updated. If you're using the systemd support
    verify that your service was restarted automatically.

## How to know if an update has been staged

In background mode an update is downloaded, verified and unpacked in the background. The new version will take effect on app restart. Currently, figuring out if the new version is downloaded is a bit awkward. A future version of Conveyor will offer an API that exposes this in a more natural manner. For now, you can do it like this:

=== ":simple-windows: Windows"

    1. Poll the `metadata.properties` file on the update site URL to find out when a new version is available, and/or receive a message from your server.
    2. Parse out these keys: `windows.package-family-name` and `app.windows.manifests.version-quad`. Call them N and V
    3. Split N by underscore to get N1 and N2.
    4. Recombine into a string like this: `c:\Program Files\WindowsApps\${N1}_${V}_x64__${N2}`
    5. Poll to see when this directory has appeared.

=== ":simple-apple: macOS"

    Poll or register a file watch in `~/Library/Caches/${app.rdns-name}/org.sparkle-project.Sparkle/Installation`, where `${app.rdns-name}` comes from your config file. You can also find your bundle ID/RDNS name by looking in your app's `Info.plist` (it is a reverse DNS name). If an update has been staged it'll be found in this directory.

=== ":simple-linux: Linux"

    Ship a file with your app that contains the version number, and register a file watch on disk for it. If the user's package manager updates the app the file will be overwritten. You should probably update ASAP if you spot this happening, because the files in the package are now of the new version and your old code may not expect that.

## Delta updates

When Conveyorized apps update themselves on Windows and macOS, they don't download the full app from scratch. The user will 
download only the differences between the installed and latest version. This can give large savings in bandwidth and update time.

Additionally, if a Windows user already has an app installed via the Microsoft Store (or that was built with Conveyor), then the first
time install of a new app can copy any of the files or data chunks needed from those other installed apps. This works regardless of vendor.
For example, if the user has any Electron app installed this way then installing a second app won't redownload Electron, only the app
specific files. This can drastically improve time-to-first-run and reduce download abandonment.

### Example savings

A single line change to the JS of the ["hello world" Electron scaffold app](tutorial/hare/electron.md) yields an approximately 31 KB update on macOS and 115 KB
on Windows. This compares to a full download of around 100 MB, or savings of nearly 1000x.

In practice the size of the delta update may depend on somewhat arbitrary factors. The use of packers/code optimizers can cause a small 
source code change to trigger large changes in the output, especially if your packer isn't deterministic. JVM apps currently generate
larger deltas than necessary (each changed JAR is re-downloaded) because Conveyor can't currently track files that change both name and
contents simultaneously, and JAR files usually have version numbers in their names.

Still, the savings are normally large.

??? info "Want more optimizations?"
    If delta updates for your app are larger than you'd like, you'd like us to do more optimizations, and you're a commercial customer then 
    please [get in touch](mailto:contact@hydraulic.dev). There is some low hanging fruit we can harvest if there's sufficient commercial 
    interest.

### Generating delta updates

You don't need to take any action to get delta updates with Conveyor. Windows can already delta update from anything to anything. For Mac
packages delta patch files will be generated for you automatically and deposited as part of the site files. You can control [how many
previous versions will have delta patches generated](configs/mac.md#appmacdeltas); it defaults to five.

!!! tip "metadata.properties"
    You must upload this file along with the rest of the output files to your download site URL, otherwise neither updates nor delta
    generation will work correctly.
    
### Measuring delta update size

You can see how big delta updates are on macOS by looking at the size of the `*.delta` files generated in the output directory.

For Windows computing the delta is a bit harder. We may add support for showing this in a future version of Conveyor.

### Learn more

If you'd like to learn about delta updates in more detail we have a [blog post that goes in depth](https://hydraulic.dev/blog/20-deltas-diffed.html).
