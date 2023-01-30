Your users must follow annoying instructions to install the app. That's because it's being self-signed, not signed by a
recognized certificate authority. Let's fix that.

When you ran your first command Conveyor announced it had generated a "root key" and that you should back it up. It also generated
two `.csr` files and printed the paths to them.

The root key is stored in your `defaults.conf` file and looks like this:

```
app.signing-key = "little peace follow cave drive pluck pony rebel grant barrel mammal skate devote skate amateur abandon shaft farm relax cousin few initial olive catch/2023-01-27T16:28:23Z"
```

!!! important "Backups"
    * [x] Make a backup of your root key.

    **You must always back up your root key, even if you will later import signing keys you already have.** The root key is also used for signing Mac update feeds, Linux packages and apt repositories.

    It's represented as words so you can write it down with pen and paper for quick and safe offline backups. Remember to include the generation date! 

All the different keys you need can be deterministically derived from this one root key. 

??? info "How to buy new certificates"

    A _certificate request file_ (CSR) can be uploaded to a certificate authority like Apple, DigiCert or ssl.com to get back a
    _certificate_. The certificate links your public key to a verified personal or corporate identity and is included inside the app
    binaries on Windows and macOS along with the signatures. 

    === ":simple-apple: Apple"

        * [x] Log in using an Apple ID to the [Apple developer programme](https://developer.apple.com/programs/). Joining will require a credit card payment. 
        * [x] Request a "Developer ID Application" certificate using the [Apple Developer console](https://developer.apple.com/account/). You can do this with any web browser and operating system, but you must be the account holder.
        * [x] Upload the `apple.csr` file that was created next to your `defaults.conf` file when you created your root key above. 
    
        You'll get a `.cer` file back immediately. There is no review or approval process because the verification is linked to your credit card details.
    
    === ":simple-windows: Windows"

        * [x] Pick a certificate authority that sells Authenticode certificates. [DigiCert](https://www.digicert.com/dc/code-signing/microsoft-authenticode.htm) is a good choice. Please refer to [this FAQ section](../../faq/signing-and-certificates.md#5-whats-the-difference-between-a-normal-and-ev-certificate) for more information on the difference between normal and EV certificates. 
        * [x] Upload the `windows.csr` file that was created next to your `defaults.conf` when you created a root key above. You will need to verify your identity with the CA. 
        * [x] Download the certificate in a format of your choice. Conveyor understands several but PEM works well.

* [x] Place your certificate files next to your [defaults.conf](../../configs/index.md#per-user-defaults). Name them `apple.cer` and `windows.cer`. 
* [x] Add this to `defaults.conf`:
  ```
  app {
    mac.certificate = apple.cer
    windows.certificate = windows.cer
  }
  ```

If you're shipping to macOS you need to configure Apple notarization. [Learn how to set up notarization](../../configs/keys-and-certificates.md#configure-apple-notarization).

* [x] Run `conveyor make site` or build unpackaged apps again. Your apps should now be signed and notarized. 

[ :material-arrow-right-box: Learn more about keys and certificates](../../configs/keys-and-certificates.md){ .md-button .md-button--primary }
