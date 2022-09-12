# 4. Build un-packaged apps

At this point you could run `conveyor  make site` to immediately generate the full set of packages, update metadata and download page. Still, it's a good idea to try running the unpackaged version of your app first, just to double check it's working OK.

Create a self-contained but unpackaged app directory for your current platform:

```
# Windows:
conveyor make windows-app

# Linux:
conveyor make linux-app

# One of the following for Intel/Apple Silicon Macs respectively:
conveyor -Kapp.machines=mac.amd64 make mac-app
conveyor -Kapp.machines=mac.aarch64 make mac-app
```

These commands will create an unzipped, un-packaged app in the `output` directory.

* [ ] Run the generated program directly in the usual manner for your operating system to check it works.

The command for macOS is different to those for Windows and Linux because Conveyor supports two CPU architectures for macOS, so you have to disambiguate which you want. The `-K` switch sets a key in the config file for the duration of that command only. Here we're setting the `app.machines` key which controls which targets to create packages for.

Note that when using the C++ app template, both ARM and Intel Mac packages will actually contain fat binaries that work on either. You can point users at a single download, or keep them separate so the download sizes can be reduced later. For Java apps each package is specific to one CPU architecture as this reduces download sizes for your end users.

!!! tip
    * You don't have to run these commands normally. Conveyor understands task dependencies so running `make site` will automatically build everything. The commands above let you inspect the results of some of the intermediate tasks.
    * The `make` command makes use of a local file cache, downloading for any external files only once, and re-using them in subsequent project builds. Thus, a new download of the same file will be triggered only in case of events like cache or cache content removal, cache file system location change, etc. [Learn more about the disk cache](../running.md#the-cache).
    * The generated project configuration file uses a small subset of the options available for your project configuration. For a detailed review of the configuration file structure and advanced configuration options please visit the [Writing config files](../configs/index.md) section.


??? tip "Error messages"
    **Electron projects.** If you forget to run `npm install` you will get an error about an inability to fully resolve the config. Just run `npm install` in the same directory as the `package.json` file to fix this.

    **JVM projects.** Forgetting to run the `./gradlew jar` command in the previous step will result in the following warning, however the build will proceed and you may therefore get an inconsistent build.
    
    ```
    🔔 There are no JAR files in your inputs. Did you forget to add some?
    ```

<script>var tutorialSection = 4;</script>
