# Conveyor vs alternative JVM tools

## jpackage / Compose Desktop gradle plugin

* It creates packages/installers that can't update themselves.
* It can only build packages for the platform it's being run on, so you'll need a multi-platform CI cluster to make packages for every platform even though the JVM is cross platform. Conveyor can build packages for all supported platforms from your developer laptop regardless of OS. That makes it a lot more convenient to knock out quick, low effort, low maintenance apps.
* It doesn't attempt to simplify code signing in any way. Conveyor can derive all the signing keys you need from a single small piece of root entropy you can write down with a pen and paper, can create certificate signing requests for you and has deep support for self-signing.
* It generates .MSI or installer EXEs on Windows. MSI is a deprecated format that Microsoft no longer develop, and installer EXEs cause headaches for IT departments. Conveyor generates MSIX packages with a small optional EXE that invokes the Windows package management API to trigger the install. This offers [a variety of benefits](../package-formats.md) and Microsoft regularly adds new features to this format.
* It is free, open source and comes with the JDK. Conveyor is free only for open source apps.
* `jpackage` is, as the name implies, only for Java apps. Conveyor works for any kind of app including Electron and native.

## install4j

* [install4J](https://www.ej-technologies.com/products/install4j/overview.html) has been around a lot longer than Conveyor and is essentially an IDE for creating interactive installers. If you have complex installation needs like user interaction or device driver setup, you should check it out. Conveyor builds packages rather than installers and has no plans to enable user interaction.
* install4j is GUI driven and uses XML to store its configuration. Conveyor doesn't have a GUI, so you'll have to read the user guide, but the config syntax is more convenient and powerful than XML. For example you can easily include configuration generated by external programs, include statements are supported, you can import JSON or `.properties` files as config and so on.
* They are both commercial products. Conveyor is cheaper than install4j.
* It is, as the name implies, only for Java apps. Conveyor supports any kind of app including Electron and native.

## jdeploy

* [jdeploy](https://www.jdeploy.com/) yields a non-standard user experience somewhat similar to Java Web Start e.g. with a custom installer app on macOS that then downloads a "JRE" separately. Conveyor provides a relatively standard experience with a small installer EXE on Windows (or MSIX packages for admins), and a bundled / linked JVM.
* Apps are hosted on NPM and must be accessed via the jdeploy website. Conveyor's output doesn't rely on any servers other than your own (or GitHub Releases).
* On macOS the signing infrastructure is effectively bypassed by using a stub that will download and execute anything un-sandboxed.
    * This is convenient for the developer but leads to the risk of jdeploy being permanently revoked if malware ever abuses it. If that were to happen there would be no way to execute any jdeploy-using apps, nor update them.
    * Conveyor follows the spirit of each operating system's rules by making it as easy as possible for developers to sign their apps, whilst also allowing for self signed apps using small CLI scripts that enable non-CA-signed installs.
    * Conveyor may support the execution of sandboxed apps without CA signing in future, similar to how web browsers do it.
* jdeploy is free and open source. Conveyor is free only for open source apps.
* jdeploy is, as the name implies, only for Java apps. Conveyor works for any kind of app including Electron and native.
