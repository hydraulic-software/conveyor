# 5. Serve the download site

* [ ] Request a full build of the download / repository site:

```
conveyor make site
```

The previous contents of the output directory will be replaced. You'll now find there packages for each OS in multiple formats, some update metadata files and a download page ([details](../../package-formats.md)). You can use whatever files you wish: there is no requirement to use them all.

The default generated `conveyor.conf` file tells each package to look for updates on `localhost:8899`. This is good enough for testing and Conveyor doesn't require a license for localhost projects, so we can just serve that directory. We recommend using `npx serve` or [Caddyserver](https://caddyserver.com/). **Do not use the Python built in web server** because it doesn't support HTTP Content-Range requests. If using Caddy you can just run `caddy file-server --browse --listen :8899` from inside the output directory.

!!! warning
    * Make sure to include the `metadata.properties` file, as it's checked by the Windows installer EXE.
    * Don't re-build and re-upload a package with the same version as previous. Conveyor will check for this and show an error if you try.  
    * Don't edit the Windows `.appinstaller` file yourself. It is constructed in a special way to work around bugs in older versions of Windows, and editing the file by hand can break those mitigations. You should never have a need to edit this file as everything in it can be controlled using config.

<script>var tutorialSection = 5;</script>
