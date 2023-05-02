# Troubleshooting Windows

Stuck? Can't find what you need here? If you're a commercial user then you can write to [contact@hydraulic.dev](mailto:contact@hydraulic.dev). Otherwise, feel free to post in [the GitHub discussion forum](https://github.com/hydraulic-software/conveyor/discussions).

## Web server doesn't support HTTP Range requests

Windows installs MSIX packages incrementally using the Windows [Background Intelligent Transfer Service](https://learn.microsoft.com/en-us/windows/win32/bits/background-intelligent-transfer-service-portal). This requires the web server to advertise support for byte range HTTP requests. Almost all web servers do, but watch out for these exceptions:

* The Python built in web server. Consider using `npx serve` instead.
* AWS S3 when using the default static serving endpoint, which looks like  `https://bucketname.s3-website-us-east-1.amazonaws.com/package.msix`. Use the object URL that ends in `.s3.amazonaws.com` instead (e.g. `https://bucketname.s3.amazonaws.com/package.msix`). For unclear reasons, Amazon don't advertise the `Accept-Range: bytes` header when using the first form.

## Where to store files

Windows apps installed with Conveyor (MSIX) run inside an app container that prevents modification of the program's installed files. It also prevents listing of the `c:\Program Files\WindowsApps` directory where apps are located. To view the installed program files in Explorer and get other useful packaging related features, grab [MSIX Hero](https://msixhero.net/). Then select your package and use the toolbar to open an Explorer inside the right directory.

Also make sure your app follows these rules: 

- Store transient files like logs and cached data in the directory pointed to by the `LOCALAPPDATA` environment variable.
- Store important files that should be backed up and follow the user around, but removed on uninstallation, in the directory pointed to by the `APPDATA` environment variable. On networked Windows computers things stored here will be copied to local storage when the user logs in, so apps can run offline.
- Store user created files *that should not be removed even if the user uninstalls*, like work the user has created, in the user's home directory. You can get this by reading the `user.home` system property or (when packaged) the `HOME` environment variable. **Don't create dot-folders here**, if you're tempted to hide files from the user then probably means you should be storing those files in `LOCALAPPDATA` or `APPDATA` instead.

!!! warning "Always use the environment variables"
    Windows programs you start from your app that aren't a part of your package will _not_ run inside the same container. As a consequence, if you pass redirected `AppData` file paths to them the other program won't be able to find those files, as it's not running in the same filesystem namespace. Using the value of the `APPDATA` and `LOCALAPPDATA` environment variables will fix this, because they'll be set to the _targets_ of the redirects and the targets are visible to all programs.

## println debugging

Windows discards stdout/stderr by default, even when an app is run from the command line. When `app.windows.console = true` running the app will either use the terminal you started it from, or cause a console window to open, so you can see the output.

## "The application has experienced an internal error and must quit."

This error dialog from JVM apps on Windows indicates that an exception escaped the main method. The first lines of the exception are in the message box and the full stack trace is printed to `stdout`. You won't be able to see that by default due to the way Windows separates CLI and GUI apps. See [println debugging](#println-debugging) above for how to view the full stack trace. 

## Missing `vcruntime`/`msvcp140` DLLs

If you get errors about missing runtime Visual C++ runtime DLLs you may need to [bundle them with your app](../stdlib/index.md#microsoft-visual-c-redistributables).

## PowerShell error about missing AppX modules

PowerShell 7 isn't backwards compatible with PowerShell 6 (a.k.a. "Windows PowerShell"). When self-signing, the `install.ps1` scripts must
be run from the default PowerShell that comes with Windows. This issue is planned to be worked around in a future Conveyor release.

## My HSM PIN has expired

Some CAs configure their HSMs to require frequent passphrase changes. If you're using a hardware device to hold your signing key and get a message about "PIN expiry" then you need to follow the instructions found in the [HSM section](../configs/keys-and-certificates.md#hardware-security-modules) of the docs to change your HSM passphrase and then also your software passphrase. They must be kept aligned.

## Error 0x80D05011

Old versions of Windows have a caching bug that will cause install/update failure if you change the size of a file in your download/update site. Conveyor puts version numbers into file names and ensures the size of the `.appinstaller` file is constant, but if you change your package without changing the version number you may encounter install failures. Conveyor will error out if you try this, but you can override that error and if you do this problem may occur. 

The workaround is to always ensure you change your version number for any new upload, even whilst testing. Alternatively you can reboot Windows to clear the buggy cache. 
