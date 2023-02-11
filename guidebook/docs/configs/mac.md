# Mac

## Synopsis

```properties
# Mac specific stuff.
app.mac { 
  # Require at least macOS 10.10, or set any other Info.plist key.
  info-plist.LSMinimumSystemVersion = 10.10

  # Add entitlements (requested permissions).
  entitlements-plist {
    # Allow recording from the microphone
    "com.apple.security.device.audio-input" = true
  
    # Allow native-level debugging with lldb
    "com.apple.security.get-task-allow" = true
  
    # All others can be specified.
  }
  
  # Add file associations.
  file-associations += {
    name = "Text files"     
    uniform-type-identifiers = ["public.plain-text"]  
  }

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
    team-id = 1234567890
    app-specific-password = xxxx-xxxx-xxxx-xxxx
   	apple-id = "your@email.com"
  }
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

## File associations

**`app.mac.file-associations`** A list of objects defining file associations. Each object contains the fields:
  * **`name`** A user readable string describing the association.
  * **`uniform-type-identifiers`** (Mac only) An optional list of Apple's [Uniform Type Identifiers](https://developer.apple.com/documentation/uniformtypeidentifiers) specifying the types of files associated with this app. If an identifier is not pre-defined as a [System declared UTI](https://developer.apple.com/documentation/uniformtypeidentifiers/system-declared_uniform_type_identifiers), you need to [define your data type in the Info.plist](https://developer.apple.com/documentation/uniformtypeidentifiers/defining_file_and_data_types_for_your_app) within either `UTExportedTypeDeclarations` or `UTImportedTypeDeclarations`. See below for examples on how to define types in your Conveyor config.
  * **`file-extensions`** An optional list of file extensions associated with this app. If a file extension is not pre-defined as a [System declared UTI](https://developer.apple.com/documentation/uniformtypeidentifiers/system-declared_uniform_type_identifiers), you need to [define your data type in the Info.plist](https://developer.apple.com/documentation/uniformtypeidentifiers/defining_file_and_data_types_for_your_app) within either `UTExportedTypeDeclarations` or `UTImportedTypeDeclarations`. Here's a short config example of how to specify your own type in your Conveyor config:
    ```properties
    app {
      mac {
        info-plist {
          // Specify the exported type.
          UTExportedTypeDeclarations = [{
            UTTypeIdentifier = "com.hydraulic.config"
            UTTypeConformsTo = ["public.json"]
            UTTypeTagSpecification = {
              "public.filename-extension" = ["conf"]
            }
          }]
        }
    
        // Make the association
        file-associations = {
          name = "Config file"
          file-extensions = [".conf"]
        }
      }
    }
    ```
  * **`mime-types`** An optional list of [MIME types](https://en.wikipedia.org/wiki/Media_type) associated with this app. If a mime type is not pre-defined as a [System declared UTI](https://developer.apple.com/documentation/uniformtypeidentifiers/system-declared_uniform_type_identifiers), you need to [define your data type in the Info.plist](https://developer.apple.com/documentation/uniformtypeidentifiers/defining_file_and_data_types_for_your_app) within either `UTExportedTypeDeclarations` or `UTImportedTypeDeclarations`. Here's a short config example of how to specify an imported type in your Conveyor config:
    ```properties
    app {
      mac {
        info-plist {
          // Specify the imported type.
          UTImportedTypeDeclarations = [{
            UTTypeIdentifier = "com.microsoft.excel.xls"
            UTTypeConformsTo = ["public.composite-content", "public.data"]
            UTTypeTagSpecification = {
              "public.filename-extension" = ["xls"]
              "public.mime-type" = ["application/vnd.ms-excel"]
            }
          }]
        }
    
        // Make the association
        file-associations = {
          name = "Config file"
          mime-types = ["application/vnd.ms-excel"]
        }
      }
    }
    ```

## Entitlements

Entitlements are a certain type of permission request that are baked into an application. They apply to both graphical and command line apps and may be required to enable certain types of operations on macOS. Apple provide [documentation on all available entitlements](https://developer.apple.com/documentation/bundleresources/entitlements).

The default entitlements request the ability to do just in time compilation but nothing else. To attach to your process with `lldb` or Xcode, you may also need to specify the `com.apple.security.get-task-allow` entitlement. However to use this, your app must be unsigned. macOS won't allow signed apps that have this entitlement to start up, as it would allow a workaround for code signing security. You can remove the code signature from an app using `codesign --remove-signature "My App.app"`.

!!! warning
    When specifying entitlements make sure to double quote them to stop them being interpreted as config paths.

!!! tip
    To view the entitlements in a binary you can run (on a Mac) `codesign -d --entitlements :- AppName.app`

## File paths

- Store important files in `$HOME/Library/Application Support/$rdns-name`.
- Store log files in `$HOME/Library/Logs/$rdns-name`.
- Store cache files in `$HOME/Library/Caches/$rdns-name`.
