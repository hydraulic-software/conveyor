# OS integration

## Introduction

Operating systems expose a lot of integration points. Some are directly supported by Conveyor. For others you can 
extend the generated metadata files from your config, meaning you don't need direct support.

This section discusses how to achieve common integrations and how to develop your own.

## URL handlers

Although Conveyor doesn't have direct support for URL handlers in the current release, you can use the following config snippet to do it
using the [custom integrations](#custom-integrations) feature:

```hocon
// Will register my-app:// as the URL scheme.
url-protocol = my-app

app {
  windows.manifests.msix.extensions-xml = """
  <uap:Extension Category="windows.protocol">
      <uap:Protocol Name=""""${url-protocol}"""">
        <uap:DisplayName>URL Test</uap:DisplayName>
      </uap:Protocol>
  </uap:Extension>
  """

  mac.info-plist.CFBundleURLTypes = [
    {
      CFBundleTypeRole = Viewer
      CFBundleURLName = ${app.rdns-name}
      CFBundleURLSchemes = [ ${url-protocol} ]
    }
  ]

  linux.desktop-file."Desktop Entry" {
    Exec = ${app.linux.install-path}/bin/${app.fsname} %u
    MimeType = x-scheme-handler/${url-protocol}
  }
}
```

If users won't directly see your links then a reasonable choice for the URL scheme is your app's `rdns-name`, because that's meant to be globally unique, and only contains characters valid for URL schemes.

**Important!** This only registers your app for that URL scheme. You still have to receive and handle open requests from the
OS. This differs by platform and runtime. On macOS the OS delivers the request via your Cocoa app delegate and no work is required for
single instance mode. On Linux and Windows the request is delivered by executing a new process with the URL as the command line parameter.
It's up to you to locate the other instance of the app if there is one and relay the request to it. For Electron apps this is built in. For
JVM apps you can use a library like [unique4j](https://github.com/prat-man/unique4j) on Windows/Linux and [java.awt.Desktop.setOpenURIHandler](https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/java/awt/Desktop.html#setOpenURIHandler(java.awt.desktop.OpenURIHandler)) on macOS.

## Custom integrations

Conveyor isn't an installer generator; it builds packages that declare how to integrate apps with the OS using static metadata. Each
OS has its own metadata file controlling the integration:

* [`AppxManifest.xml` on Windows.](https://learn.microsoft.com/en-us/uwp/schemas/appxpackage/appx-package-manifest)
* [`Info.plist` on macOS.](https://developer.apple.com/library/archive/documentation/General/Reference/InfoPlistKeyReference/Introduction/Introduction.html)
* [`app-name.desktop` on Linux](https://specifications.freedesktop.org/desktop-entry-spec/desktop-entry-spec-latest.html) and related files.

For Linux and macOS the underlying file types can be cleanly mapped to the JSON data model and therefore set via config keys. For Windows,
you can control the generated XML using a mix of keys for common settings and the `extensions-xml` key:

```hocon
app.windows.manifests.msix.extensions-xml = """
  <XML/>
"""
```

See the [Windows](windows.md), [macOS](mac.md) and [Linux](linux.md) sections for details on how you can override custom keys.

!!! note "Windows AppX manifest"
    In legacy Windows app deployment, programs are deployed using a dedicated installer EXE that (un)integrates the app by modifying the
    registry. In modern Windows app deployment, programs are installed using the built in package manager and desired integrations are
    expressed using an XML file interepreted by the OS. This approach has many advantages, for example, uninstallation is always clean.
    The "AppX Manifest" is usable by any kind of app packaged using MSIX. Therefore, instead of asking "what registry key do I need for 
    this", you can look up the relevant portion of the XML schema instead.
