# Mac

## Synopsis

```properties
# Mac specific stuff.
app.mac { 
  info-plist { 
    LSMinimumSystemVersion = 12.0
  }

  # Request permissions.
  entitlements-plist {
    # Allow recording from the microphone
    "com.apple.security.device.audio-input" = true
  
    # Allow native-level debugging with lldb
    "com.apple.security.get-task-allow" = true
  
    # All others can be specified.
  }
  
  # Add file associations.
  file-associations += ".txt"

  # Disable signing even if keys are available.
  sign = false

  # Override the default set of icon images pulled from the inputs
  icons = "mac-icons-*.png"
  
  # Add files: for JVM or Electron apps these are app resources, for native 
  # apps it's the same as bundle-extras.
  inputs += fat-file
  amd64.inputs += intel-mac-file
  aarch64.inputs += apple-silicon-file

  # Input definitions merged into the Contents/ directory. Useful for adding
  # Mac specific stuff into JVM/Electron apps.
  bundle-extras += extra-stuff/embedded.provisionprofile
  amd64.bundle-extras += extra-stuff/amd64/Foo.framework -> Frameworks/Foo.framework
  aarch64.bundle-extras += extra-stuff/aarch64/Foo.framework -> Frameworks/Foo.framework

  # Credentials for the GateKeeper servers.
  notarization {
    app-store-connect-api-key {
        issuer-id = 12345678-1234-1234-1234-123456789012
        key-id = ABCDEF1234
        private-key = path/to/private/key/AuthKey_ABCDEF1234.p8
      }
  }
  
  # Maximum number of previous versions of your app to keep track for generating Sparkle delta updates.
  max-previous-versions = 5
}
```

## Keys

**`app.mac.inputs`** An input hierarchy for Mac specific inputs. You can also add to `app.mac.amd64.inputs` and `app.mac.aarch64.inputs`.
How these files are treated depends on the type of app. For JVM and Electron apps these files are added to the `Contents/Resources` 
directory in the bundle, unless they are native files, in which case they're added to the Frameworks directory. Files may be processed to
extract native code of the right architectures. For native apps these files are simply added to the bundle inside the `Contents`
directory.

**`app.mac.{amd64,aarch64,}bundle-extras`** Only relevant for JVM and Electron apps. A list of inputs that are added directly to 
the `Contents` directory.

**`app.mac.icons`** An [input list](inputs.md) containing square icons of different sizes. Defaults to whatever `app.icons` is set to, which is `icons-*.png` by default.

??? warning "macOS bug with icons at small sizes"
    Some versions of macOS / the Finder have a bug which will display white noise for small app icons if you don't provide those sizes. To avoid this, make sure to render 16x16 and 32x32 icons and supply them in your inputs. Future versions of Conveyor may work around this bug automatically.

**`app.mac.info-plist`**  Keys are converted to Apple's PList XML format, which provides application metadata on macOS. You normally don't need to alter this, but if you want to add entries to the `Info.plist` file you can do so here. [Consult Apple's reference for more information on what keys are available](https://developer.apple.com/library/archive/documentation/General/Reference/InfoPlistKeyReference/Introduction/Introduction.html).

**`app.mac.entitlements-plist`** A set of boolean key/value pairs that request privileges from the operating system. See below for more information. Defaults to requesting support for just-in-time compilation.

**`app.mac.updates`** See [update modes](index.md#update-modes).

**`app.mac.sign`** Controls whether signing is done after bundling. Defaults to the value of `app.sign`. You should normally leave this set to true unless you want to speed up the build temporarily. It can be true even if you don't have a Developer ID certificate because the app will be self-signed.

**`app.mac.signing-key`**, **`app.mac.certificate`** See [signing keys](index.md#signing).

**`app.mac.sparkle-options`** An object whose values are put in the `Info.plist` that controls Sparkle's behavior. [See here for a reference guide](https://sparkle-project.org/documentation/customization/). You should normally leave this alone unless you want precise behavioral control.

**`app.mac.sparkle-framework`** An input definition that points to a release of the [Sparkle 2 update framework](https://sparkle-project.org/). You can normally leave this at the default unless you want to use a custom fork of Sparkle for some reason.

**`app.mac.max-previous-versions`** Maximum number of previous versions of your app to keep track for generating Sparkle delta updates.

**`app.mac.check-binary-versions`** If true (the default) then native binaries that declare a minimum required macOS version in their headers will have that be checked against the `LSMinimumSystemVersion` key in the `Info.plist` file. If you are trying to ship native binaries that require a newer macOS version than the declared minimum for your app bundle, Conveyor will stop with an error. Please note that this check does not recurse into JARs or other archives, so requires `app.jvm.extract-native-libraries` to be true to be effective for JVM apps. The declared version header can be viewed using the command `otool -l foobar.dylib | grep -A3 LC_BUILD_VERSION`.

**`app.mac.notarization`** Credentials used to send a notarization request to Apple. See [instructions for configuring notarization](keys-and-certificates.md#configure-apple-notarization).

## File associations

**`app.mac.file-associations`** A list of strings or objects defining file associations. Defaults to the value of `app.file-associations` so you should only rarely need to set this key unless you want to use Apple's proprietary UTI system (see below). Normally you should use the top level multi-platform key instead.  

The simplest way to define a file association is to add a file extension to the list:

```
app.mac.file-associations += .superfile
```

The OS also needs a MIME type. If you don't specify a MIME type then Conveyor will generate one for you in the `application/vnd` hierarchy, but you can control it by just appending it to the string form like this:

```
app.mac.file-associations += .superfile application/vnd.superfile
```

Some file types may have more than one valid extension, e.g. for an image editor you could write:

```
app.mac.file-associations += .jpg .jpeg image/jpeg
app.mac.file-associations += .tif .tiff image/tiff
```

### Uniform Type Identifiers

Apple platforms have their own more powerful equivalent to MIME types called [Uniform Type Identifiers](https://developer.apple.com/documentation/uniformtypeidentifiers). UTIs can be arranged in a hierarchy to express the fact that e.g. a file format is based on JSON but adds additional semantics on top. You can normally ignore this because Conveyor will generate the necessary metadata for you. If you want to customize them then read on.

To set up a UTI you must first use the object form and then create a unique reverse DNS name for the file type, like this:

```
app.mac.file-associations += {
    extensions = [ .superfile ]
    mime-type = application/vnd.superfile
    uti = ${app.rdns-name}.superfile
}
```

Apple distinguishes between exported types (you own the format) and imported types (someone else owns the format). If an identifier is not pre-defined as a [System declared UTI](https://developer.apple.com/documentation/uniformtypeidentifiers/system-declared_uniform_type_identifiers), and not present in `app.mac.info-plist` under `UTExportedTypeDeclaration` or `UTImportedTypeDeclaration`  Conveyor will generate a `UTExportedTypeDeclaration` for you (generate a Mac app and then look at the `Info.plist` to see exactly what it creates).

If you want to define your own UTIs explicitly, you can define the plist items like this:

```
app.mac.info-plist {
  UTExportedTypeDeclarations = [{
    UTTypeIdentifier = "com.example.config"
    UTTypeConformsTo = ["public.json"]
    UTTypeTagSpecification = {
      "public.filename-extension" = ["conf"]
    }
  }]
  
  UTImportedTypeDeclarations = [{
    UTTypeIdentifier = "com.microsoft.excel.xls"
    UTTypeConformsTo = ["public.composite-content", "public.data"]
    UTTypeTagSpecification = {
      "public.filename-extension" = ["xls"]
      "public.mime-type" = ["application/vnd.ms-excel"]
    }
  }]
}
```

and then define a file association object with `uti = com.example.config` to reference it.

## Entitlements

**`app.mac.entitlements-plist`** Entitlements are a certain type of permission request that are baked into an application. They apply to both graphical and command line apps and may be required to enable certain types of operations on macOS. Apple provide [documentation on all available entitlements](https://developer.apple.com/documentation/bundleresources/entitlements).

You can set them like this:

```
app {
  mac {
    entitlements-plist {
      # Allow the app to create and control personal VPN configurations.
      "com.apple.developer.networking.vpn.api" = [ allow-vpn ]
    }
  }
}
```

!!! warning
    Make sure to quote entitlement names to stop them being interpreted as config paths.

You probably won't need to add entitlements when writing cross-platform apps. Conveyor automatically adds any entitlements needed by your chosen app runtime. In the unlikely event you need to ship a provisioning profile with your Mac app, it can be added using the `app.mac.bundle-extras` inputs list.

??? tip "Viewing entitlements"
    To view the entitlements in a binary you can run (on a Mac) `codesign -d --entitlements :- AppName.app`

??? information "Debugging"
    To attach to your process with `lldb` or Xcode you need to specify the `com.apple.security.get-task-allow` entitlement. However to use this, your app must be unsigned. macOS won't allow signed apps that have this entitlement to start up, as it would allow a workaround for code signing security. You can remove the code signature from an app using `codesign --remove-signature "My App.app"`.

## File paths

- Store important files in `$HOME/Library/Application Support/$rdns-name`.
- Store log files in `$HOME/Library/Logs/$rdns-name`.
- Store cache files in `$HOME/Library/Caches/$rdns-name`.
