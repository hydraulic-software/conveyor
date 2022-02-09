# Signing and certificates

## 1. Do I have to buy certificates?

No, but it'll be much harder for your users to install your app if you don't. On Windows you'll lose online updates and virus scanners may interfere with the operation of your app (e.g. you may find file accesses randomly fail). On modern Apple Silicon Macs you'll be confined to running in Rosetta emulation and users need to do a [special dance](https://lapcatsoftware.com/articles/unsigned.html) to open the app. Thus in practice this is only feasible if you're distributing code to developers who are comfortable with the command line and doing extra work.

If you're asking this because you're an open source developer, consider asking your user community to acquire a signing key for you, or to donate the funds for the keys. When people know what a donation will be used for they're much more likely to donate.

## 2. I don't have a Mac/Windows machine.

You don't need one. Conveyor can sign and notarize apps for any OS on every OS. You also don't need one to acquire certificates in the first place, because that's done via a web browser using standard data formats. Conveyor makes the needed CSR files for you.

## 3. Which CA should I use for Windows?

Any CA that issues Authenticode certificates will work. We've had good experiences with DigiCert.

## 4. What type of certificate do I need for macOS?

An "Apple Distribution" certificate. That's the modern type that works on both macOS and iOS. You may have an Apple "Developer ID" certificate if you've distributed Mac software before - that also works fine. Only Apple sells these, other code signing certificates won't work.

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
