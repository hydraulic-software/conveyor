# OS integration

## Introduction

Operating systems expose a lot of integration points. Some are directly supported by Conveyor. For others you can 
extend the generated metadata files from your config, meaning you don't need direct support.

This section discusses how to achieve common integrations and how to develop your own.

## URL handlers / deep linking

Having your app launch when the user clicks a URL is easy. If you want to handle `my-app:foo` or `my-app://foo` links then just add it to
the `app.url-schemes` key, like this:

```hocon
app.url-schemes = [ my-app ]
```

If users won't directly see your links then a reasonable choice for the URL scheme is your app's `rdns-name`, because that's meant to be globally unique, and only contains characters valid for URL schemes.

!!! important
    This only registers your app for that URL scheme. You still have to receive and handle open requests from the
    OS. This differs by platform and runtime. On macOS the OS delivers the request via your Cocoa app delegate and no work is required for
    single instance mode. On Linux and Windows the request is delivered by executing a new process with the URL as the command line parameter.
    It's up to you to locate the other instance of the app if there is one and relay the request to it. For Electron apps this is built in. For
    JVM apps you can use a library like [unique4j](https://github.com/prat-man/unique4j) on Windows/Linux and [java.awt.Desktop.setOpenURIHandler](https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/java/awt/Desktop.html#setOpenURIHandler(java.awt.desktop.OpenURIHandler)) on macOS.

## File associations

Having your app launch when the user selects "Open with..." is also very easy. If you want to open files with extension `.foo` then just add it to
the `app.file-associations` key, like this:

```hocon
app.file-associations = [ .foo ]
```

You can even define different file associations for different platforms by using the keys `app.mac.file-assocations`, `app.linux.file-assocations` and `app.windows.file-assocations`. By default, the value of all of those keys is the same as `app.file-associations`.

!!! important
    This only registers your app for that file extension. You still have to receive and handle open requests from the
    OS. This differs by platform and runtime. On macOS the OS delivers the request via your Cocoa app delegate and no work is required for
    single instance mode. On Linux and Windows the request is delivered by executing a new process with the file path as the command line parameter.
    It's up to you to locate the other instance of the app if there is one and relay the request to it. For Electron apps this is built in. For
    JVM apps you can use a library like [unique4j](https://github.com/prat-man/unique4j) on Windows/Linux and [java.awt.Desktop.setOpenFileHandler](https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/java/awt/Desktop.html#setOpenFileHandler(java.awt.desktop.OpenFilesHandler)) on macOS.


### Platform specific details

#### Linux

Linux requires you to specify MIME types for file associations. Conveyor takes care of that for you by generating a MIME type based on your app's `rdns-name`, specifically `application/vnd.${app.rdns-name}.${extension}`. If you prefer to set the MIME yourself, you can do so like this:

```hocon
app.file-associations = [ ".foo application/x-my-mime-type" ]
```

Whenever there are file associations, Conveyor checks the entry under `app.linux.desktop-file."Desktop Entry".Exec` for the presence of one of `%f`, `%F`, `%u` or `%U`, conforming to the [Exec entry spec](https://specifications.freedesktop.org/desktop-entry-spec/latest/ar01s07.html). If none is present, Conveyor appends `%f` to that entry automatically.

#### macOS

There are further optional [customization options for macOS](mac.md#file-associations) to deal with Uniform Type Identifiers, but Conveyor does a lot of inference
for you so that even a simple config like the examples above should work out of the box.

## Windows notifications and COM

To allow the user to trigger app actions from notifications *even after* the app has quit, the Windows notification center requires that you
register with the OS so it can re-start the app and then call back into your code. This requires the registration of COM objects. [See here
for Microsoft's documentation](https://learn.microsoft.com/en-us/windows/apps/design/shell/tiles-and-notifications/send-local-toast-desktop-cpp-wrl#packaged).

Here's how to do it for apps that use the `desktop-notifications` npm module:

```
app {
  windows {
    // Register an object that will be invoked by Windows when the user 
    // interacts with a "toast" notification for things like pull requests 
    // being opened. 
    //
    // See https://learn.microsoft.com/en-us/windows/apps/design/shell/tiles-and-notifications/send-local-toast-desktop-cpp-wrl#packaged
    manifests.msix.extensions-xml = """
        <!-- Register COM CLSID LocalServer32 registry key -->
        <com:Extension Category="windows.comServer">
          <com:ComServer>
            <com:ExeServer Executable=""""${app.display-name}""".exe" DisplayName="Toast activator">
              <com:Class Id="27D44D0C-A542-5B90-BCDB-AC3126048BA2" DisplayName="Toast activator"/>
            </com:ExeServer>
          </com:ComServer>
        </com:Extension>

        <!-- Specify which CLSID to activate when toast clicked -->
        <desktop:Extension Category="windows.toastNotificationActivation">
          <desktop:ToastNotificationActivation ToastActivatorCLSID="27D44D0C-A542-5B90-BCDB-AC3126048BA2" />
        </desktop:Extension>
    """
  }
}
```

For other apps you'll need to change the GUIDs in the XML. A GUID can be generated by your IDE or a tool like `uuidgen`. A full discussion
of COM is unfortunately out of scope for this document; please refer to any Windows programming guide to learn how to work with COM APIs.

To make notifications work in Electron, you will also need to call `app.setAppUserModelId` to your startup code, like this:

```
if (__WIN32__) {
  app.setAppUserModelId('<your id goes here>')
}
```

To find out what goes there, read on ...

## Windows AppUserModelID (AUMID)

Some Windows APIs require you to know your "app user model ID". When packaged with Conveyor this will be a string that looks like this:
`ExampleApp_49jahnq5qzr1m!ExampleApp`.

To discover your AUMID the simplest way is to run `conveyor make app-user-model-id` which will output your AUMID to standard output.
The code in the middle of the AUMID is a hash of your signing certificate, so you may be prompted for your passphrase.

!!! warning
    Because this value depends on your signing certificate, if you use self-signing or test certificates during development _the AUMID
    will change when it comes to release time_ (assuming you're properly code signing, of course). Remember to compute the AUMID for the
    real signing certificate before release!

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
