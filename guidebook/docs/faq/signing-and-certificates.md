# Signing and certificates

## 1. Do I need to use special hardware to sign Windows apps?

No. Although using a USB HSM device is common, certificate authorities like SSL.com and DigiCert usually offer two alternatives:

1. A "cloud signing service" in which they hold your private keys and sign files over the internet. Conveyor supports this out of the box.
2. You supply a public key along with evidence that the matching private key was created inside an HSM. CAs will normally support an attestation protocol that lets HSMs run by AWS, Google Cloud Platform etc prove private key ownership to the CA. This lets you hold your keys in you regular cloud service.

## 2. I want to do continuous deployment from CI workers

Conveyor is great for this because it can sign Windows and Mac apps from cheap Linux workers. You don't need to do anything special here, but be aware that your CI worker should have plenty of RAM and internet connectivity. You should also try to ensure that the Conveyor [disk cache](../running.md#the-cache) is preserved between builds as otherwise releases will be much slower, and if you use a cloud signing service it may even cost you money as some charge per signature (see below).

## 3. Which CA should I use for Windows?

Any CA that issues Authenticode certificates will work. We've had good experiences with SSL.com

## 4. What type of certificate do I need for macOS?

An Apple "Developer ID" certificate. Other types e.g. for iOS or "Apple Distribution" won't work. Only Apple sells these.

## 5. What's the difference between a normal and EV certificate?

The distinction only matters for Windows. An Extended Validation certificate costs more money, is harder to get and must be protected by a hardware security module (HSM), normally in the form of a USB token that's physically mailed to you. Conveyor supports HSMs via the standard PKCS#11 interface, which they all support.

In return for this you're purchasing some initial reputation with the Windows SmartScreen download filter. [SmartScreen checks downloaded apps](https://docs.microsoft.com/en-us/windows/security/threat-protection/microsoft-defender-smartscreen/microsoft-defender-smartscreen-overview) against a database to find out how often the app vendor is seen. Vendors whose apps are downloaded very rarely yield warning screens suggesting caution (though the app can still be run by accepting the warning). The intuition here is that viruses are often polymorphic and constantly rewrite themselves to evade detection, so a program that's brand new and rarely seen might be a virus.

Reputations are associated with the certificate you use and build over time as Windows observes users running apps from a new vendor without them being reported as viruses. Both types of certificate accrue reputation, but an EV certificate will get over the needed threshold for warnings to disappear much quicker.

In short: if you're new to distributing Windows software and want your users to have the best experience possible, buy an EV certificate and go through the verification process.

## 6. Does my program still work after my certificate expires?

Yes. Conveyor doesn't just sign your files, it timestamps them too. Timestamping servers sign the hash of your file along with a timestamp, and operating systems use that time when evaluating certificate expiry. Existing packages will continue to be installable after the signing certificates have expired, and existing apps will continue to run.

!!! important "Changing your signing identity"
    Despite that, it's a good idea to renew your certificates with plenty of time because you will need to ensure the new certs have exactly the same subject name (identity) as the old certificates. See question 12 for details.

## 7. What is notarization?

It's what Apple calls their pre-approval process for non app store apps. Notarization is basically like running a malware scanner over an app and then signing it to say it's been checked. It moves the work of malware detection to the server side so the operating system doesn't have to do that itself, only check for revocation.

Notarization normally requires tools that only work on macOS, but Conveyor understands the protocol and can notarize apps from any platform.

## 8. Why is signing / notarization so slow?

It's slow using native tools too. Part of the delay is the timestamping process described above, which requires making a call to a remote server for every file being signed. Apple's notarization process is slow because it's a batch process that statically analyzes your program (e.g. it finds and extracts zips), and the protocol involves polling to find out when it's completed.

## 9. Are Linux packages signed?

Yes. GPG signatures are provided for Debian packages and apt repositories. The package installs the certificate to the keychain as part of the regular package installation process, so apt doesn't need to be specifically configured.

There is no signature or hash generated for the tarball because it's not necessary. The user will be downloading the tarball over HTTPS anyway, which already provides integrity protection.

## 10. Can I change between Windows certificate types/authorities?

Yes, but if the new CA doesn't issue an identical X.500 subject name to your previous CA this will trigger an [escape hatch migration](../configs/windows.md#escape-hatch-mechanism). Background updates will stop until the user next runs the app, and an uninstall/reinstall cycle will be required. The escape hatch must be switched on for this to work; it is enabled by default when `conveyor.compatibility-level` is >= 10. 

## 11. I did a build and my Windows cloud signing service charged me for hundreds of signatures

This is normal if you use a large runtime like the JVM or Electron that may have many native code files, but if you do another build there will be far fewer thanks to the disk cache. Conveyor has to sign the following files to produce a valid Windows package that will be accepted by browsers, the OS and virus scanners:

1. Every EXE or DLL file in your app, including the updater EXE that Conveyor adds to it if you turned on aggressive updates, and (for JVM apps) any native libraries found inside JAR files.
2. The MSIX package file.
3. The installer EXE that the user downloads.

If you change your program and do another build, only the files that changed should be re-signed. Often that will mean only the MSIX package file and the installer.

If you keep being charged, you are probably using a continuous integration system that wipes files between builds. Set it up to preserve the contents of [the disk cache](../running.md#the-cache) between builds and things will get both faster and cheaper. Alternatively, you / your worker machine may be running low on disk space and Conveyor is deleting entries to try and free up space. Ensure there is at least 1GB more free space available than the maximum size of the cache to prevent this from happening, as Conveyor will try to ensure you don't run out of disk.
