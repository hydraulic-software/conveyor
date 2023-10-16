# Understanding delta updates

When Conveyorized apps update themselves on Windows and macOS, they don't download the full app from scratch. The user will 
download only the differences between the installed and latest version. This can give large savings in bandwidth and update time.

Additionally, if a Windows user already has an app installed via the Microsoft Store (or that was built with Conveyor), then the first
time install of a new app can copy any of the files or data chunks needed from those other installed apps. This works regardless of vendor.
For example, if the user has any Electron app installed this way then installing a second app won't redownload Electron, only the app
specific files. This can drastically improve time-to-first-run and reduce download abandonment.

## Example savings

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

## Generating delta updates

You don't need to take any action to get delta updates with Conveyor. Windows can already delta update from anything to anything. For Mac
packages delta patch files will be generated for you automatically and deposited as part of the site files. You can control [how many
previous versions will have delta patches generated](configs/mac.md#appmacdeltas); it defaults to five.

!!! tip "metadata.properties"
    You must upload this file along with the rest of the output files to your download site URL, otherwise neither updates nor delta
    generation will work correctly.
    
## Measuring delta update size

You can see how big delta updates are on macOS by looking at the size of the `*.delta` files generated in the output directory.

For Windows computing the delta is a bit harder. We may add support for showing this in a future version of Conveyor.

## Learn more

If you'd like to learn about delta updates in more detail we have a [blog post that goes in depth](https://hydraulic.dev/blog/20-deltas-diffed.html).
