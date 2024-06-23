# Build performance

## Introduction

When used effectively Conveyor can do incremental builds very fast, sometimes within seconds. When used ineffectively your package builds
may take many minutes and if you use pay-per-signature cloud code signing, may consume more signatures than necessary.

Here are some tips for improving build efficiency.

## Ensure the disk cache is persistent

!!! tip
    Ensure [the cache directory](running.md#the-cache) is kept between builds.

Conveyor is implemented on top of a parallel, incremental build system with a content-addressable disk cache. The intermediate outputs of 
tasks are preserved and can be reused by the next build, even if that build is for a different app version, configuration or entirely 
unrelated project.

Some CI systems don't supply persistent disks to jobs. The top way people kill their Conveyor packaging speed is by erasing the disk cache
at the end of every run. This will force Conveyor to re-download files and recompute data that hasn't changed, wasting bandwidth and CPU time.
The location on each platform can be found [here](running.md#the-cache). Good CI systems will let you either backup/restore this cache
between runs or (best) keep it on a persistent disk.

## Ensure the cache is sized properly

!!! tip
    Use the `--cache-limit` and `--cache-free-space-limit` flags to give Conveyor more disk. It defaults to 10 GB.

Depending on things like the size of your app and how many variant builds you produce, the cache may not be big enough. Builds of app
variants (different branches, dev vs prod) can reuse each other's work to speed things up. An implication of this is that the cache will
expand to fill whatever space you give it before starting to clear out entries to stay under the limit.

The limit can be controlled in two ways: via a max usage, and a min free disk space. Please note that cache eviction is asynchronous so 
disk usage may temporarily go above the max usage limit during a build. If you have a dedicated disk for Conveyor (e.g. in a container)
then you can set the limit to be higher than the disk size, and use `--cache-free-space-limit` to cause eviction when the OS starts to
run out of space.

The cache is not an LRU cache and will take into account the "cost" of entries when clearing them, where cost is currently defined as 
a function of how long it took to create the entry originally and how much disk space it uses.

## Adjust parallelism

!!! tip
    Use the `--parallelism` flag to run more or fewer tasks at once.

Conveyor defaults to running four tasks in parallel. This is tuned for high-end laptops with fast SSDs. You _may_ get higher performance
with more parallelism, however it's not guaranteed because most build tasks are themselves able to use multiple cores. To learn if you can
exploit more parallelism run a build with `conveyor make site --rerun` whilst watching a CPU monitor. If there are long periods where not
all your cores are utilized, try bumping up to 5 or even 6. Conversely, trying to do too much can lower your performance.

Reducing build parallelism will also reduce the memory requirements of the build. This may help if your CI workers are constrained.

## Build on Linux

Conveyor can run on Windows for the convenience of developers who wish to release or iterate on config from their developer machine. 
However, Conveyor builds are bottlenecked by disk IO and the Windows disk subsystem is much slower than on any other OS. If you can,
run builds on a dedicated Linux VM or bare metal machine. If you can't, consider using WSL2 and doing the build inside that.

macOS is fairly well optimized and has acceptable performance.

## Use fast SSDs

!!! tip
    The `--cache-dir` flag controls where work is done.

Conveyor spends most of its time transforming, moving and generating files. It can issue many file system operations simultaneously even
when only one task is running. This yields huge speeds when using high quality SSDs. Try to hold your disk cache (where all the work is done) 
on an SSD or ramdisk.

!!! note
    Your project directory isn't touched except to read input files, and at the very end when the results are copied to the output directory, so it's OK if your source tree is on a remote network drive. Only the disk cache directory matters for performance. 

## JVM apps: Hard-code module lists

!!! tip
    Set the `app.jvm.modules` list to the modules you know you need.

JVM builds can be slow because Conveyor will scan all the JARs using `jdeps` to figure out what JDK modules are needed. This shrinks your
final redistributable significantly, but takes a lot of CPU time.

To speed this up you can hard code the module list in your config. You'll need to be careful when adjusting your app's dependencies as
a new library or library version might start using a part of the JDK you weren't previously relying on, but this will eliminate the `jdeps`
step entirely and yield time savings.

1. Run `conveyor -Kapp.machines=$MACHINE make processed-jars` for each machine you're targeting. The output directory after each `make` 
   will contain a file called `required-jdk-modules.txt` with a list of the JDK modules that were auto-detected using jdeps.
2. Collect the lists together and remove duplicates.
3. Add them to your config.

Like this:

```
app {
    jvm {
        modules = [
            java.logging
            java.desktop
            // etc
        ]
    }
}
```

The default list contains only one entry, `detect`, which is what triggers the use of `jdeps`. By overwriting the list so it no longer
contains `detect` the scanning step is avoided. Repeat the process when you think your dependencies on the JDK may have changed.

## Reduce the number of deltas created for macOS

The Sparkle update engine used on macOS can use binary patches to significantly speed up the update process. We **strongly recommend** you use this mechanism, as it will not only improve performance but also the rate at which users update, as Sparkle cannot currently resume downloads that were interrupted by the program quitting.

By default patches are created from the prior five released versions. This means delta patching can still work even for users who didn't use your app for a while. Unfortunately the process of computing the patches is slow and uses a lot of RAM. If you need faster builds, if you run out of RAM on your build machine or if you know your users will run the app regularly, try setting `app.mac.deltas` to something lower like 1 or 2.

For dev/test builds where you know your users will be able to redownload the app from scratch you can set it to zero to disable patch generation (e.g. from the command line `conveyor -Kapp.mac.deltas=0 make site`).
