# Keys and certificates

Here's what you need to know:

* **Signing capabilities.** Conveyor can fully sign and (for macOS) notarize your software without requiring macOS or Windows. It can also generate keys and certificate signing request files. Those files can be traded for certificates for both platforms using only a browser and credit card.
* **Signing requirements.** Conveyor can make unsigned packages. Be aware that:
    * **Windows:** Microsoft requires MSIX packages to be signed. Conveyor can generate ZIPs with unsigned software but you won't get software updates, start menu integration or containerization. Antivirus products may interfere with your program if it isn't signed, because they will see each version as an independent program and won't learn that it's not malware.
    * **macOS:** Apple Silicon Macs can only run signed code, so unsigned code will be Intel only and run in emulation. Unsigned packages will require intervention from the [Security settings panel](https://support.apple.com/en-us/HT202491) or command line before they can execute.
    * **Linux:** apt repositories are always self-signed (no certificate is necessary). The generated DEBs install the GPG public key and apt sources file for the repository, thus, the user gets your app by just downloading the DEB and installing it with `apt install ./yourapp*.deb`. The generated download page gives the command to use.
* **Key derivation.** Conveyor can create a single private key and derive all the different platform keys from it, so there's only one thing to back up.
* **Using existing keys.** Conveyor can use your existing Windows and Apple code signing keys if you have them. You'll still need to back up your Conveyor root key because it's also used to generate Linux and Mac repository signing keys.
* **Passphrase protection.** Conveyor can encrypt your private keys. [See below](#passphrases).
* **Hardware tokens / HSMs.** Conveyor can use private keys protected by HSMs. [See below](#hardware-security-modules).

??? question "Is signing worth it?"
    On macOS and Windows your signing key must be linked with an identity. Apple uses the name on your credit card and you can obtain personal certificates within a few minutes. For Windows you can use any certificate authority, but they may request to see government issued ID. The effort involved in ID verification depends on whether you are signing as a person or as a company.

    In our view it's worth signing your code if you need full performance, are targeting non-developers, or are writing developer tools that might be blocked by Windows AV engines. For example, Conveyor is fully signed because we want it to be as easy to use as possible, and because otherwise Windows Defender can block filesystem operations (writing lots of JARs to disk triggers malware heuristics). Finally, it's not security theater: signing actually does make malware harder to produce and distribute, which is why modern virus writers put effort into stealing signing keys.
    
    For open source apps all these arguments still apply, but if you don't want to get a certificate you can just distribute unsigned files. Alternatively, ask for volunteers from your user community who will sign them for you. Getting a signing certificate is simple way to contribute back that doesn't require any technical skills. 

??? info "Making unsigned apps easier to use"
    Although Conveyor removes a lot of the technical effort involved in signing, the need for certificates remains. Over time we plan to make software distribution easier through a combination of sandboxing, developer/open source-focused CLI tooling, and better instructions to end users.

## Start here: Create a root key

You need to do this even if you already have Mac/Windows keys and certificates.

Your root private key is an ordinary config setting, and [the `defaults.conf` file in your home directory](configs/hocon-extensions.md#per-user-defaults) is automatically included into every build config. That makes it a good place to store your key.

:octicons-tasklist-24: **Run `conveyor keys generate` from a terminal to create a fresh user defaults file.** The result will look like this:

```
app.signing-key = "vicious noble apart total march unit veteran kangaroo recipe plastic unit pottery awkward exhibit curve laugh envelope super shadow primary sword ginger sustain century/2022-01-26T15:03:11Z"
```

The word list encodes the key and also a checksum. The timestamp is required to deterministically re-generate GPG certificates for Linux. This form is intended to be easy to write down with a pen and paper. Add line breaks and indentation if you wish using triple quote syntax (`"""foo"""`).

There's also a commented out section where you can place your Apple notarization credentials. If you want to sign Mac apps, fill those out.

!!! important
    * Keep your new `defaults.conf` file outside of version control and <u>back it up</u>. 
    * The words come from a pre-selected list, so don't try to create a custom sentence.

## If you have certificates

:octicons-tasklist-24: Export them to `.cer`, `.p12` or `.pfx` files:

* **macOS:** Export the keys from Keychain Access to a .p12 file (see below). Set `app.mac.signing-key` to point to that file. If you have a separate key and certificate file, set `app.mac.certificate` to point to the `.cer` file.
* **Windows:** Set `app.windows.signing-key` to the path of either a .p12/.pfx file containing your private key and certificate, or set `app.windows.signing-key` and `app.windows.certificate` separately.

To learn more about configuring keys and certificates see [signing configuration](configs/index.md#signing). Conveyor can read most common ways to encode keys and certificates, including ASCII format (PEM, meaning `---BEGIN PRIVATE KEY---` style). When using a `.p12` or `.pfx` file, you must supply [a passphrase](#passphrases). 

### Exporting keys from Mac Keychain Access

**Step 1.** Open Keychain Access and locate your developer ID certificate and associated private key:

![Keychain Access](images/keychain-access-1.png)

**Step 2.** Select both, right click and choose "Export 2 items". Save as .p12 format.

**Step 3.** Pick a strong passphrase and enter it. It may have spaces in it. Click OK.

**Step 4.** Enter your login password to unlock the keychain. The export should now complete.

## If you need certificates

When you ran `conveyor keys generate` it also produced two certificate signing request files. These can be uploaded to certificate authorities to get signing certificates.

* If distributing to macOS: 
    * :octicons-tasklist-24:  Log in using an Apple ID in the [Apple developer programme](https://developer.apple.com/programs/). Joining will require a credit card payment. Then request an "Apple Distribution" certificate using the [Apple Developer console](https://developer.apple.com/account/). You can do this with any web browser and operating system. Upload the `apple.csr` file that was created next to your `defaults.conf` file when you created your root key above. You'll get a `.cer` file back immediately; there is no review or approval process because the verification is linked to your credit card details.
* If distributing to Windows:
    * :octicons-tasklist-24:  Pick a certificate authority that sells Authenticode certificates. [DigiCert](https://www.digicert.com) is a good choice.
    * :octicons-tasklist-24:  Upload the `windows.csr` file that was created next to your `defaults.conf` when you created a root key above. You may need to verify your identity with the CA. They will give you back a certificate. Conveyor understands several formats but PEM works well.

The private keys backing the certificate requests aren't written to disk separately. They're all derived on demand from the contents of the `app.signing-key` config value. To export them, see below.

## Configure Apple notarization

Apple requires all apps for macOS to be both signed and uploaded to them for a form of ahead-of-time virus scanning, called notarization. This is different to the app store: approval is automatic with no humans in the loop and takes only a minute or two. 

Conveyor needs to be provided with an Apple ID and credentials for the notarization service. Any Apple ID registered with the developer programme can get these. 

:octicons-tasklist-24: **Add the following to your config** (or your [defaults.config](configs/hocon-extensions.md#per-user-defaults)):

```
app { 
  mac {
  	notarization {
	    team-id = 1234567890
  	  app-specific-password = xxxx-xxxx-xxxx-xxxx
      apple-id = "your@email.com"
    }
  }
}
```

* :octicons-tasklist-24: Generate an app specific password from the security section of your Apple ID account webpage
* :octicons-tasklist-24: The team ID can be found in the [Apple developer console](https://developer.apple.com/account/) under "Membership".

!!! tip
    You can use move your app specific password outside of a per-project config by using an `include` statement.

## Making unsigned packages

Set `app.sign = false`, or separately, `app.mac.sign = false` and `app.windows.sign = false`. 

You will get warnings during the build and some formats like MSIX or ARM Mac won't be available. You'll still need to generate a root key for Linux, but no effort is required beyond backing it up. Use `conveyor task-dependencies site` to see what will be included and what won't.

## Sample defaults.config

Although you can store your signing configuration and certificates with your project, it's better to use your personal `defaults.config` file. Here's what such a file can look like:

```
app.signing-key = "<24 words>/2021-05-27T17:45:47Z"

app.mac.notarization {
  app-specific-password = aaaa-bbbb-cccc-dddd
  team-id = 6MD7Z8H86K
  apple-id = "you@user.host"
}

app.mac.certificate = apple.cer
app.windows.certificate = windows.cer
```

The `apple.cer` and `windows.cer` files are then stored alongside the `defaults.config` file.

## Key derivation and export

For your convenience Conveyor by default derives all needed keys and certificates from the single root private key generated by the `conveyor keys generate` command. The advantage is that rather than backing up several different private keys you only need to back up one, yet each platform has its own unique private key.

To get the platform-specific private keys in formats understood by other programs, use `conveyor keys export`. Use the `--passphrase` flag if necessary (see below). It will create:

* `apple.p12` A DER encoded PKCS#12 key store, encrypted under the password "conveyor" (note: *not* your regular passphrase - to change it you can use `keytool` or `openssl`, or just import it using Keychain Access and then delete it).
* `windows.key` PEM encoded private key, RSA 4096 bits.
* `sparkle.key` Ed25519 private key + public key point, base64 encoded. This format is the one used by the Sparkle `generate_keys` command.
* `gpg.key` A GPG/PGP secret keyring that you can use with `gpg --import`.

## Passphrases

Keys can be encrypted under a passphrase. You'll be asked for one when using `conveyor keys generate` and you can change it with `conveyor keys passphrase`.

Then use the `--passphrase` command line option when invoking commands:

* If specified on its own, you'll be asked to type the passphrase in at the console. 
* If given a value that starts with `env:` then the rest is the name of an environment variable containing the passphrase. This is useful for continuous build systems.
* Otherwise the value is the passphrase.

The same passphrase is used for the root key, any PKCS#12 files (`.p12` or `.pfx`) and hardware security modules. Therefore they must all match. Using different passphrases for different key stores isn't supported at this time.

## Hardware security modules

Conveyor can use keys stored in hardware security modules. This is useful because Extended Validation certificates can buy you some initial reputation with Windows SmartScreen, but they must be held in an HSM/token.

Using a token is simple:

1. Install the drivers for your host platform (it doesn't have to be Windows).
2. Find the path to the PKCS#11 driver library. HSM user guides will often give you this path under instructions for setting up Firefox or Thunderbird.
3. Set the path as the value of `app.windows.signing-key` .

Example for using a SafeNet HSM from MacOS:

```
app.windows.signing-key = /Library/Frameworks/eToken.framework/Versions/Current/libeToken.dylib
```

Then make sure to use the `--passphrase` flag, and you should be set.
