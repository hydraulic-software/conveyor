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

  # Disable signing even if keys are available.
  sign = false
  
  # A file only shipped in Mac packages.
  inputs += mac-file
  
  # Override the default set of icon images pulled from the inputs
  icons = "mac-icons-*.png"
  
  # Add files specific to Intel Mac or Apple Silicon builds.
  amd64.inputs += intel-mac-file
  aarch64.inputs += apple-silicon-file
  
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

**`app.mac.icons`** An [input list](inputs.md) containing square icons of different sizes. Defaults to whatever `app.icons` is set to, which is `icons-*.png` by default.

??? warning "macOS bug with icons at small sizes"
    Some versions of macOS / the Finder have a bug which will display white noise for small app icons if you don't provide those sizes. To avoid this, make sure to render 16x16 and 32x32 icons and supply them in your inputs. Future versions of Conveyor may work around this bug automatically.

**`app.mac.info-plist`**  Keys are converted to Apple's PList XML format, which provides
application metadata on macOS. You normally don't need to alter this, but if you want to add entries to the `Info.plist` file you can do so here. [Consult Apple's reference for more information on what keys are available](https://developer.apple.com/library/archive/documentation/General/Reference/InfoPlistKeyReference/Introduction/Introduction.html).

**`app.mac.entitlements-plist`** A set of boolean key/value pairs that request privileges from the operating system. See below for more information. Defaults to requesting support for just-in-time compilation.

**`app.mac.sparkle-inputs`** An input definition that points to a release of the [Sparkle 2 update framework](https://sparkle-project.org/). You can normally leave this at the default unless you want to use a custom fork of Sparkle for some reason.

**`app.mac.updates`** If "background" (the default), then the app will check for updates on a schedule. The first time an update is available the user will be asked to apply it, and given the option to apply updates silently in future. If "aggressive" then an update check is triggered on every app start, and the user will be notified that there's an update available within a few seconds of startup. They'll be given an option to skip it, be reminded later or apply it immediately. If "none" then update functionality isn't included. The exact UX implied by these names may change in future releases. 

**`app.mac.sparkle-options`** An object whose values are put in the `Info.plist` that controls Sparkle's behavior. [See here for a reference guide](https://sparkle-project.org/documentation/customization/). You should normally leave this alone unless you want precise behavioral control.

**`app.mac.sign`** Controls whether signing is done after bundling. Defaults to the value of `app.sign`. You should normally leave this set to true unless you want to speed up the build temporarily. It can be true even if you don't have a Developer ID certificate because the app will be self-signed.

**`app.mac.signing-key`**, **`app.mac.certificate`** See [signing keys](index.md#signing).

## Entitlements

Entitlements are a certain type of permission request that are baked into an application. They apply to both graphical and command line apps and may be required to enable certain types of operations on macOS. Apple provide [documentation on all available entitlements](https://developer.apple.com/documentation/bundleresources/entitlements).

The default entitlements request the ability to do just in time compilation but nothing else. To attach to your process with `lldb` or Xcode, you may also need to specify the `com.apple.security.get-task-allow` entitlement. However to use this, your app must be unsigned. macOS won't allow signed apps that have this entitlement to start up, as it would allow a workaround for code signing security. You can remove the code signature from an app using `codesign --remove-signature "My App.app"`.

!!! warning
    When specifying entitlements make sure to double quote them to stop them being interpreted as config paths.

!!! tip
    To view the entitlements in a binary you can run (on a Mac) `codesign -d --entitlements :- AppName.app`
