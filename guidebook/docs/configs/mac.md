# Mac

## Synopsis

```hocon
# Mac specific stuff.
app.mac { 
  info-plist {
    # Require Monterey or higher.
    LSMinimumSystemVersion = 12.0
  }

  # Credentials for the GateKeeper servers.
  notarization {
    issuer-id = 12345678-1234-1234-1234-123456789012
    key-id = ABCDEF1234
    private-key = path/to/private/key/AuthKey_ABCDEF1234.p8
  }

  # Request permissions.
  entitlements-plist {
    # Allow recording from the microphone
    "com.apple.security.device.audio-input" = true
  }
  
  # Add file associations.
  file-associations += ".txt"

  # Add files: for JVM or Electron apps these are app resources, for native 
  # apps it's the same as bundle-extras.
  inputs += fat-file
  amd64.inputs += intel-mac-file
  aarch64.inputs += apple-silicon-file

  # Inputs merged into the Contents/ directory. Useful for adding
  # Mac specific stuff into JVM/Electron apps.
  bundle-extras += extra-stuff/embedded.provisionprofile
  amd64.bundle-extras += extra-stuff/amd64/Foo.framework -> Frameworks/Foo.framework
  aarch64.bundle-extras += extra-stuff/aarch64/Foo.framework -> Frameworks/Foo.framework
}
```

## App files

### `app.mac{.amd64,.aarch64,}.inputs`

An input hierarchy for Mac specific inputs. You can also add to `app.mac.amd64.inputs` and `app.mac.aarch64.inputs`.
How these files are treated depends on the type of app. For JVM and Electron apps these files are added to the `Contents/Resources` 
directory in the bundle, unless they are native files, in which case they're added to the Frameworks directory. Files may be processed to
extract native code of the right architectures. For native apps these files are simply added to the bundle inside the `Contents`
directory.

### `app.mac{.amd64,.aarch64,}.bundle-extras`

Lists of [input definitions](inputs.md). Allows adding extra files directly to the `Contents` directory.. This is only useful for Electron and JVM apps, where the root input files are placed inside a sub-directory that keeps them separate from the runtime.

CPU-neutral data files can be added to `app.mac.bundle-extras`, CPU specific files should go in `app.mac.amd64.bundle-extras` or `app.mac.aarch64.bundle-extras`.

### `app.mac.icons`

An [input list](inputs.md) containing square icons of different sizes. Defaults to whatever `app.icons` is set to, which is `icons-*.png` by default.

??? warning "macOS bug with icons at small sizes"
    Some versions of macOS / the Finder have a bug which will display white noise for small app icons if you don't provide those sizes. To avoid this, make sure to render 16x16 and 32x32 icons and supply them in your inputs. Future versions of Conveyor may work around this bug automatically.

### `app.mac{.amd64,.aarch64,}.info-plist`

Keys to be incorporated into the app's Info.plist file. You can also add to `app.mac.amd64.info-plist` and `app.mac.aarch64.info-plist` to specify different keys per CPU.
Keys are converted to Apple's PList XML format, which provides application metadata on macOS. You normally don't need to alter this, but if you want to add entries to the `Info.plist` file you can do so here. [Consult Apple's reference for more information on what keys are available](https://developer.apple.com/library/archive/documentation/General/Reference/InfoPlistKeyReference/Introduction/Introduction.html).

## Online updates

### `app.mac.updates`

When to update. See [update modes](update-modes.md).

### `app.mac.deltas`

Maximum number of previous versions of your app to generate delta updates for. Note that delta updates aren't generated unless `conveyor.compatibility-level >= 11`. When the compatibility level is high enough, this key defaults to 5.

### `app.mac.sparkle-options`

An object whose values are put in the `Info.plist` that controls Sparkle's behavior. [See here for a reference guide](https://sparkle-project.org/documentation/customization/). You should normally leave this alone unless you want precise behavioral control.

### `app.mac.sparkle-framework`

A single [input definition object](inputs.md) that points to a release of the [Sparkle 2 update framework](https://sparkle-project.org/). You can normally leave this at the default unless you want to use a custom fork of Sparkle for some reason. Note that some Conveyor features, like aggressive updates, aren't supported by upstream Sparkle and thus the default value of this key points to a Conveyor-specific fork. Changing this key to use an upstream release will therefore need careful testing and this key is intended for advanced users only. The default value looks like this:

```
# An input definition for where to get the Sparkle framework.
sparkle-framework {
  from = downloads.hydraulic.dev/sparkle/Sparkle-2.5.1-hydraulic.tar.xz
  remap = [
    "Sparkle.framework/**"
    "-Sparkle.framework/Headers"
    "-Sparkle.framework/PrivateHeaders"
    "-Sparkle.framework/Modules"
    "-Sparkle.framework/Versions/B/Headers/**"
    "-Sparkle.framework/Versions/B/PrivateHeaders/**"
    "-Sparkle.framework/Versions/B/Modules/**"
    "-Sparkle.framework/Versions/B/_CodeSignature/**"
    "-Sparkle.framework/Versions/B/Updater.app/_CodeSignature/**"
    "-Sparkle.framework/Versions/B/XPCServices/org.sparkle-project.Downloader.xpc/Contents/_CodeSignature/**"
    "-Sparkle.framework/Versions/B/XPCServices/org.sparkle-project.InstallerLauncher.xpc/Contents/_CodeSignature/**"
    "-**"
  ]
  to = "Frameworks"
}
```

It's often easier to customize Sparkle by overwriting files rather than replacing the whole framework. This technique can be useful for customizing the default strings and translations. Here's how:

1. Use `make mac-app` to get an extracted and bundled copy of the Sparkle framework, which you can find in the `Contents/Frameworks` directory of your app bundle. 
2. You can then modify the strings in the `Sparkle.framework/Resources` directory and add an entry to the `app.mac.sparkle-framework` key to point to your modified version. Sparkle uses the macOS translation framework, so the files are key/value mappings. These are stored in binary, so let's take a look using `plutil` (this procedure requires macOS, see below if you don't have it):
   ```bash
   $ plutil -p Sparkle.framework/Versions/B/Resources/Base.lproj/Sparkle.strings | less
   ```
3. In the base project keys and values are the same; this is so the strings in the Sparkle source code are in English instead of arbitrary identifiers. Let's imagine we wish to change the message "An important update to %@ is ready to install". We need to replace it in the base project (this won't update the string in the translations, you can use ChatGPT to redo those and then use the same procedure):

    ```bash
    $ cp output/MyApp.app/Contents/Frameworks/Sparkle.framework/Versions/B/Resources/Base.lproj/Sparkle.strings Sparkle.strings
    $ plutil -convert xml1 Sparkle.strings
    
    # Now edit the file in your favorite text editor, or from the CLI:
    $ plutil -replace "An important update to %@ is ready to install" -string "An important update to %@ is ready to install, please click OK to install it now" Sparkle.strings
   
    $ plutil -convert binary1 Sparkle.strings
    ```
    
4. Now we have our modified `Sparkle.strings` file we can overwrite the default one in the Sparkle framework. We can then add an entry to the `app.mac.sparkle-framework` key to point to our modified version. Here's how to do it:

    ```hocon
    app.mac.bundle-extras += {
      from = "path/to/modified/Sparkle.strings"
      to = "Frameworks/Sparkle.framework/Versions/B/Resources/Base.lproj/Sparkle.strings"
    }
    ```

If you don't have access to `plutil`, the same task can be achieved using Python's `biplist` library, and there are also third-party tools available for Windows and Linux that can handle PList files.

```python
import biplist

try:
    plist = biplist.readPlist('path/to/your/file.plist')  # Load the binary plist file
    plist['YourKey'] = 'NewValue'  # Modify or add values
    biplist.writePlist(plist, 'path/to/your/file.plist')  # Write back as binary plist
except (biplist.InvalidPlistException, biplist.NotBinaryPlistException) as e:
    print("Error processing plist file:", e)
```

### `app.mac.check-binary-versions`

If true (the default) then native binaries that declare a minimum required macOS version in their headers will have that be checked against the `LSMinimumSystemVersion` key in the `Info.plist` file. If you are trying to ship native binaries that require a newer macOS version than the declared minimum for your app bundle, Conveyor will stop with an error. Please note that this check does not recurse into JARs or other archives, so requires `app.jvm.extract-native-libraries` to be true to be effective for JVM apps. The declared version header can be viewed using the command `otool -l foobar.dylib | grep -A3 LC_BUILD_VERSION`.

## Signing

### `app.mac.sign`

Controls whether signing is done after bundling. Defaults to the value of `app.sign`. The value could be one of `true` (meaning regular signing done by Conveyor is enabled), `false` (disabling signing altogether), or an object to allow specifying custom scripts for signing your app. 

Here's an example of how to use a custom signing script:

```hocon
app {
  mac {
    sign {
      scripts {
        // Custom script for signing the bundle.
        app = "./my-signing-script.sh bundle $ENTITLEMENTS $BUNDLE"
        
        // Custom script for signing the individual Mach-O binary files - OPTIONAL.
        binary = "./my-signing-script.sh file $ENTITLEMENTS $FILE $IDENTIFIER"
      }
    }
  }
}
```

And here's what your script could look like if it were to use the standard Apple codesign tool:

```bash
#!/usr/bin/env bash

echo "signing: $@"
name="Your Name Here"

if [[ "$1" == "bundle" ]]; then
	codesign --sign "$name" --deep --force --verbose --entitlements "$2" "$3"
else
	codesign --sign "$name" --force --verbose --entitlements "$1" -i "$3" "$2"
fi
```

This script does the same thing Conveyor normally would but in a non-portable way. Still, it's a good starting point for a more complex script that might upload the bundle to a signing server, sign it there, and then download it again.

[You can also configure a custom script for notarization](keys-and-certificates.md#via-custom-notarization-script).

#### `app.mac.sign.scripts.app`

Defines a custom script to be used for signing your macOS app bundle. It will be a command line run from the working directory where Conveyor is executed.
The following replacements are made when running the command:

   * `$BUNDLE`: will get replaced with the full path to the bundle directory that must be signed by the provided script. You don't need quoting, as Conveyor will do that for you.
   * `$ENTITLEMENTS`: will get replaced with the full path to an Apple Property List file containing the macOS entitlements selected for your app. You also don't need quoting for this substitution either.

The script should sign the bundle located at the `$BUNDLE` directory **in-place**. Correctly signing a bundle can be tricky, please refer to [Apple's documentation on how to properly sign items in the bundle](https://developer.apple.com/forums/thread/701514).

!!! important "Caching"
    For speed reasons Conveyor will cache the signed MSIX produced by the given custom script. The cache key contains both the configured
    command line and a fingerprint (hash) of the script itself. If you change the script or the command line, the signing operation will be rerun.
    However, if you change a *dependency* of your script then that won't be detected, as only the command itself is fingerprinted. In that
    case you will need to modify the command too e.g. by altering a version number in a header comment.

#### `app.mac.sign.scripts.binary`

Defines a custom script to be used for signing individual macOS Mach-O binary files. This is typically needed for runtimes that extract
shared libraries from archives. If you build Electron apps for example it isn't necessary, if you build JVM apps without [native library
extraction](jvm.md#native-code) it will be more useful. 

It will be a command line run from the working directory where Conveyor is executed. The following replacements are made when running the command:

* `$FILE`: will get replaced with the full path to a Mach-O binary file that must be signed by the provided script. You don't need quoting, as Conveyor will do that for you.
* `$ENTITLEMENTS`: will get replaced with the full path to an Apple Property List file containing the macOS entitlements selected for your app. You don't need quoting, as Conveyor will do that for you.
* `$IDENTIFIER`: will get replaced with a string with the suggested binary identifier to use for this file.

The script should sign the file located at the `$FILE` path **in-place**. It will be called once for each Mach-O binary located inside your JAR files. 

This script is *only* called if you also specify an `app` script, to avoid mixing up of credentials when signing different parts of the app.

!!! important "Caching"
    For speed reasons Conveyor will cache the signed MSIX produced by the given custom script. The cache key contains both the configured
    command line and a fingerprint (hash) of the script itself. If you change the script or the command line, the signing operation will be rerun.
    However, if you change a *dependency* of your script then that won't be detected, as only the command itself is fingerprinted. In that
    case you will need to modify the command too e.g. by altering a version number in a header comment.
    

### `app.mac.signing-key`, `app.mac.certificate`

See [signing keys](index.md#signing).

### `app.mac.notarization`

Credentials used to send a notarization request to Apple. See [instructions for configuring notarization](keys-and-certificates.md#configure-apple-notarization).

### `app.mac.entitlements-plist`

A set of boolean key/value pairs. Defaults to requesting support for just-in-time compilation.

Entitlements are a certain type of permission request that are baked into an application. They apply to both graphical and command line apps
and may be required to enable certain types of operations on macOS. Apple provide [documentation on all available
entitlements](https://developer.apple.com/documentation/bundleresources/entitlements).

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
    To attach to your process with `lldb` or Xcode you need to specify the `com.apple.security.get-task-allow` entitlement. However, to use this, your app must be unsigned. macOS won't allow signed apps that have this entitlement to start up, as it would allow a workaround for code signing security. You can remove the code signature from an app using `codesign --remove-signature "My App.app"`.


## File associations

### `app.mac.file-associations`

A list of strings or objects defining file associations. Defaults to the value of `app.file-associations` so you should only rarely need to set this key unless you want to use Apple's proprietary UTI system (see below). Normally you should use the top level multi-platform key instead.  

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

## File paths

- Store important files in `$HOME/Library/Application Support/$rdns-name`.
- Store log files in `$HOME/Library/Logs/$rdns-name`.
- Store cache files in `$HOME/Library/Caches/$rdns-name`.
