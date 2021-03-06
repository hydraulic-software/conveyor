# 5. Serve the download site

* [ ] Request a full build of the download / repository site:

```
conveyor make site
```

The previous contents of the output directory will be replaced. You'll now find there packages for each OS in multiple formats, some update metadata files and a download page ([details](../outputs.md)). You can use whatever files you wish: there is no requirement to use them all.

The default generated `conveyor.conf` file tells each package to look for updates on `localhost:8899`. This is good enough for testing and Conveyor doesn't require a license for localhost projects, so grab your favourite web server and serve that directory. We recommend [Caddyserver](https://caddyserver.com/). You can just run `caddy file-server --browse --listen :8899` from inside the output directory.

??? tip "Error messages"
    Forgetting to run the `./gradlew jar` command in the previous step will result in the following warning, however the build will proceed and you may therefore get an inconsistent build.

    ```
    🔔 There are no JAR files in your inputs. Did you forget to add some?
    ```

!!! warning
    * There's no way to change the site URL after release because it's included in the packages. Choose wisely!
    * The Python 3 built in web server doesn't support `Content-Range` HTTP requests which is necessary for Windows `.appinstaller` installation to succeed.
    * Don't edit the `.appinstaller` file yourself. It is constructed in a special way to work around bugs in older versions of Windows, and editing the file by hand can break those mitigations. You should never have a need to edit this file as everything in it can be controlled using config.

<script>var tutorialSection = 5;</script>
