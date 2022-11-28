# Troubleshooting Windows

## Program files

Windows apps installed with Conveyor (MSIX) run inside an app container that prevents modification of the program's installed files. It also prevents listing of the `c:\Program Files\WindowsApps` directory where apps are located. To view the installed program files in Explorer and get other useful packaging related features, grab [MSIX Hero](https://msixhero.net/). Then select your package and use the toolbar to open an Explorer inside the right directory.

Also make sure your app follows these rules: 

- Store transient files like logs, cached data etc in the directory pointed to by the `LOCALAPPDATA` environment variable.
- Store important files that should be backed up and follow the user around, but removed on uninstallation, in the directory pointed to by the `APPDATA` environment variable. On networked Windows computers things stored here will be copied to local storage when the user logs in, so apps can run offline.
- Store user created files *that should not be removed even if the user uninstalls*, like work the user has created, in the user's home directory. You can get this by reading the `user.home` system property or (when packaged) the `HOME` environment variable. **Don't create dot-folders here**, if you're tempted to hide files from the user then probably means you should be storing those files in `LOCALAPPDATA` or `APPDATA` instead.

!!! warning "Always use the environment variables"
    Windows programs you start from your app that aren't a part of your package will _not_ run inside the same container. As a consequence, if you pass redirected `AppData` file paths to them the other program won't be able to find those files, as it's not running in the same filesystem namespace. Using the value of the `APPDATA` and `LOCALAPPDATA` environment variables will fix this, because they'll be set to the _targets_ of the redirects and the targets are visible to all programs.

## println debugging

Windows discards stdout/stderr by default, even when an app is run from the command line. When `app.windows.console = true` running the app will either use the terminal you started it from, or cause a console window to open so you can see the output.

## Error 0x80D05011

Older versions of Windows have a caching bug that will cause install/update failure if you change the size of a file in your download/update site. Conveyor puts version numbers into file names and ensures the size of the `.appinstaller` file is constant, but if you change your package without changing the version number you may encounter install failures. 

The workaround is to always ensure you change your version number for any new upload, even whilst testing.

## PowerShell errors about missing AppX modules

PowerShell 7 isn't backwards compatible with PowerShell 6 (a.k.a. "Windows PowerShell"). When self-signing, the `install.ps1` scripts must
be run from PowerShell 6. This issue is planned to be worked around in a future Conveyor release. 
