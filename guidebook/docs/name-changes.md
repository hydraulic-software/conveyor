# Name changes

Packages require a variety of names that are used for different purposes. Conveyor will do its best to derive all these names for you from
whatever is provided, however, be aware that changing names can break updates.

## All platforms

Changing the `app.site.base-url` will break updates, because existing installs will still be looking at the old URL. There is currently
no support for changing the update site, nor having more than one update site.

Changing the *`app.fsname`* key can break updates because this is used to identify the program to the operating system's package manager
when there is one. If you aren't specifying an `app.fsname` explicitly, it will be derived from the `app.display-name` and `app.vendor` keys. 
In some cases it is safe to change this name, but it must be done with care.

!!! note "Upcoming features"
    In upcoming releases Conveyor will introduce a mechanism to support moving between update sites.

## Windows

When distributing outside the app store updates may stop working in the following cases: 

* Renaming your organization.
* Changing the location of your organizational headquarters.
* Going from an OV to an EV certificate or vice-versa.
* Changing your personal name, if not using an organizational certificate.
* In some cases, changing from one certificate authority to another.

This is a Windows design issue: it identifies packages by using a hash of the verified identity in the signing certificate. Certificate
authorities refuse to issue certificates to old identities, so once your code signing certificate expires your signing identity will change 
if the underlying legal identity has changed.

Conveyor provides a new [escape hatch mechanism](configs/escape-hatch.md) that allows you to circumvent those issues by forcing
a reinstallation of the app when there's a change in the package family name. That mechanism makes a best effort approach to back up local app data, so the app keeps its state after a reinstallation.
Starting from Conveyor 10, that mechanism is enabled by default. Currently only changes to the verified identity in the signing certificate are supported,
you should keep the previous package identity names.

When changing the `app.fsname` you can keep your previous package identity names by changing the following keys:

* `app.windows.store.identity-name` (when doing in-store distribution)
* `app.windows.manifests.msix.identity-name` (when doing out-of-store distribution)

These keys default to the value of `app.fsname`, re-capitalized to match Windows platform conventions.

!!! note "Upcoming features"
    In upcoming releases the escape hatch mechanism will also support changes to the package identity name.

## macOS

Changing the `app.display-name` key should work as expected, except the name of the app in the file system (the one users will see in Finder) doesn't change after an update. It can be renamed manually.

The `app.rdns-name` key (reverse DNS) controls the bundle identifier. Combined with your signing "team ID" this is how macOS identifies an application. Changing the `rdns-name` will therefore make macOS perceive your app as totally new and it will forget any granted permissions.

Changing your Apple Developer ID can be done as long as you don't change your Conveyor root key at the same time, as that is used to sign the update feeds. This is due to Sparkle's support for [key rotation](https://sparkle-project.org/documentation/#rotating-signing-keys).

## Linux 

The package / fsname can't be changed without breaking updates. Other names are safe to change.
