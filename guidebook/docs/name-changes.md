# Name changes

Packages require a variety of names that are used for different purposes. Conveyor will do its best to derive all these names for you from
whatever is provided, however, be aware that changing names can break updates.

## All platforms

Changing the `app.site.base-url` will break updates, because existing installs will still be looking at the old URL. There is currently
no support for changing the update site, nor having more than one update site.

Changing the *`app.fsname`* key can break updates because this is used to identify the program to the operating system's package manager
when there is one. If you aren't specifying an `app.fsname` explicitly, it will be derived from the `app.display-name` and `app.vendor` keys. 
In some cases it is safe to change this name, but it must be done with care. 

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

When changing the `app.fsname` you can keep your previous package identity names by changing the following keys:

* `app.windows.store.identity-name` (when doing in-store distribution)
* `app.windows.manifests.msix.identity-name` (when doing out-of-store distribution)

These keys default to the value of `app.fsname`, re-capitalized to match Windows platform conventions.

!!! note "Upcoming features"
    We are developing a system to allow forced uninstalls/reinstalls which will launch in a future version of Conveyor. The user will see a progress bar whilst the reinstallation is in progress. Background updates will stop until the user goes through this process. 

## macOS

Currently, changing the `app.display-name` key is not supported and may cause macOS update errors. This can be fixed; if you have an urgent need for this [please get in touch](mailto:contact@hydraulic.software).

The `app.rdns-name` key (reverse DNS) controls the bundle identifier. Combined with your signing "team ID" this is how macOS identifies an application. Changing the `rdns-name` will therefore make macOS perceive your app as totally new and it will forget any granted permissions.

Changing your Apple Developer ID can be done as long as you don't change your Conveyor root key at the same time, as that is used to sign the update feeds. This is due to Sparkle's support for [key rotation](https://sparkle-project.org/documentation/#rotating-signing-keys).

## Linux 

The package / fsname can't be changed without breaking updates. Other names are safe to change.
