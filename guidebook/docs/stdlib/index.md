# Standard library

Conveyor ships with some pre-canned config snippets to help you configure your package for popular frameworks and libraries. 

* [Electron apps](../configs/electron.md)
* [SystemD](systemd.md) for Linux services.
* JVM apps:
    * Import [JDKs](jdks.md) from various vendors
    * [JavaFX](javafx.md) specific configuration.

## Microsoft Visual C++ redistributables

Windows apps sometimes require DLLs like `msvcp140.dll`, `vcruntime140.dll` etc. These don't come with Windows and most toolchains won't
bundle them with your app either, expecting you to do it at packaging time. 

To bundle these DLLs, write:

```
include required("/stdlib/windows/msvc.conf")

msvc.input.remap = [
  # Name the DLLs you need here:
  msvcp140.dll
  vcruntime140.dll
  vcruntime140_1.dll
  
  # And drop the rest.
  "-**"
]
```

If you don't specify `msvc.input.remap` then all the DLLs will be included. This should always be safe but can bloat your package. The
following files are available:

- `msvcp140.dll`
- `msvcp140_1.dll`
- `msvcp140_2.dll`
- `msvcp140_atomic_wait.dll`
- `msvcp140_codecvt_ids.dll`
- `vcamp140.dll`
- `vccorlib140.dll`
- `vcomp140.dll`
- `vcruntime140.dll`
- `vcruntime140_1.dll`

The `msvc.conf` stdlib file looks like this:

```
msvc.version = 14.0.30704.0
msvc.redist-url = "https://downloads.hydraulic.dev/msvc-redist/msvc-redist-"${msvc.version}".zip"
msvc.input = { from = ${msvc.redist-url} }
app.windows.amd64.inputs += ${msvc.input}
```

Normally Microsoft don't distribute these DLLs in zip form. We extract the DLLs from their VC++ runtime packages and host them for you,
so you can include only what you need. Normally your app framework will tell you what DLLs are required but if you don't know or are writing
a native app you can use [Dependency Walker](https://www.dependencywalker.com/) to find out. Our hosted redistributables include these
DLLs at version `14.0.30704.0`.

!!! note
    - It may appear at first that you don't need these DLLs, because it's common for Windows apps to install them to `c:\windows\system32`.
      However if you don't bundle them then your app may break on clean/fresh Windows installs where nothing else has provided them. 
    - HotSpot based JVMs already have these DLLs included.
