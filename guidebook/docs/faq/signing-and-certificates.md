# Signing and certificates

## 1. Do I have to buy certificates?

No, but it'll be harder for your users to install your app if you don't. They'll be instructed either to copy/paste a command into their terminal or to use Apple's "magic right click menu" trick, so in practice this is only feasible if you're distributing code to people who are comfortable with that (i.e. developers).  Additionally Windows anti-virus engines may interfere with your execution, especially if you use the file system or network a lot.

*Don't* use the self-signing feature for regular consumer apps. Code signing does actually raise the bar for malware developers; they often put a lot of risky effort into stealing code signing keys rather than revealing their identities: [example](https://threatpost.com/nvidias-stolen-code-signing-certs-sign-malware/178784/), [another example](https://www.computerworld.com/article/3048346/malware-authors-quickly-adopt-sha-2-through-stolen-code-signing-certificates.html).

If you're asking this because you're an open source developer and working on a GUI app for regular users, consider the following options:

1. Asking your user community to donate the funds for the keys. When people know what their money will be used for they're much more likely to donate, especially if it will help grow the user community.
2. Contacting the [SignPath Foundation](https://about.signpath.io/product/open-source), who sign open source Windows apps for free.

## 2. I don't have a Mac/Windows machine.

You don't need one. Conveyor can sign and notarize apps for any OS on every OS. You also don't need one to acquire signing certificates from Apple or Microsoft, because that's done via a web browser using standard data formats. Conveyor makes the needed CSR files for you.

## 3. Which CA should I use for Windows?

Any CA that issues Authenticode certificates will work. We've had good experiences with DigiCert.

## 4. What type of certificate do I need for macOS?

An Apple Distribution certificate. That's the modern type that works on both macOS and iOS. You may have an Apple Developer ID certificate if you've distributed Mac software before - that also works fine. Only Apple sells these, other code signing certificates won't work.

## 5. What's the difference between a normal and EV certificate?

The distinction only matters for Windows. An Extended Validation certificate costs more money, is harder to get and must be protected by a hardware security module (HSM), normally in the form of a USB token that's physically mailed to you. Conveyor supports HSMs via the standard PKCS#11 interface, which they all support.

In return for this you're purchasing some initial reputation with the Windows SmartScreen download filter. [SmartScreen checks downloaded apps](https://docs.microsoft.com/en-us/windows/security/threat-protection/microsoft-defender-smartscreen/microsoft-defender-smartscreen-overview) against a database to find out how often the app vendor is seen. Vendors whose apps are downloaded very rarely yield warning screens suggesting caution (though the app can still be run by accepting the warning). The intuition here is that viruses are often polymorphic and constantly rewrite themselves to evade detection, so a program that's brand new and rarely seen might be a virus.

Reputations are associated with the certificate you use and build over time as Windows observes users running apps from a new vendor without them being reported as viruses. Both types of certificate accrue reputation, but an EV certificate will get over the needed threshold for warnings to disappear much quicker.

In short: if you're new to distributing Windows software and want your users to have the best experience possible, buy an EV certificate and go through the verification process.

## 6. Does my program still work after my certificate expires?

Yes. Conveyor doesn't just sign your files, it timestamps them too. Timestamping servers sign the hash of your file along with a timestamp, and operating systems use that time when evaluating certificate expiry.

## 7. What is notarization?

It's what Apple calls their pre-approval process for non app store apps. Notarization is basically like running a malware scanner over an app and then signing it to say it's been checked. It moves the work of malware detection to the server side so the operating system doesn't have to do that itself, only check for revocation.

Notarization normally requires tools that only work on macOS, but Conveyor understands the protocol and can notarize apps from any platform.

## 8. Why is signing / notarization so slow?

It's slow using native tools too. Part of the delay is the timestamping process described above, which requires making a call to a remote server for every file being signed. Apple's notarization process is slow because it's a batch process that statically analyzes your program (e.g. it finds and extracts zips), and the protocol involves polling to find out when it's completed.

## 9. Don't M1 Macs refuse to run unsigned code?

Sort of but not exactly. The macOS kernel on ARM laptops insists that code is signed, but as well as accepting standard Developer ID signatures it also accepts self-signed certificates and so-called "ad hoc" signatures (which are machine-specific whitelistings of binary hashes). The reason for requiring a signature is to ensure all programs have an identity that other subsystems can use to identify them. Apple can't actually forbid all unsigned code from macOS even if they wanted to, because otherwise developers would have no way to compile software anymore.

## 10. Why use self-signed packages instead of just unsigned?

Signing (with anything) establishes a long term identity for a piece of code that survives updates. Although self-signed code isn't well liked by the Mac/Windows subsystems tasked with keeping the user safe, other parts simply want the ability to remember an app across multiple versions. For example, remembering granted permissions or passwords stored in the keychain rely on knowing the stable identity of the program. Self signing provides this, as well as securing the update process. Fully unsigned packages do not.

## 11. Are Linux packages signed?

GPG signatures are provided for Debian packages and apt repositories. The package installs the certificate to the keychain as part of the regular package installation process, so apt doesn't need to be specifically configured.

There is no signature or hash generated for the tarball because it's not necessary. The user will be downloading the tarball over HTTPS anyway, which already provides integrity protection.
