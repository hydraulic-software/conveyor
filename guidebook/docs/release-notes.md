# Release notes

## Conveyor 19 (July 2025)

## :fontawesome-brands-windows: Windows

Microsoft's [Azure Trusted Signing (ATS) certificate authority is now supported](configs/keys-and-certificates.md#azure-trusted-signing). ATS is cheaper than most other Windows CAs, with a more Apple-like pricing structure. It can be driven entirely from CI without needing any hardware keys, and access can be controlled using the standard Azure mechanisms.

## :simple-apple: macOS

The macOS update user experience has been simplified. When the control API is used to trigger an update and one is available, the user is no longer prompted to accept the update or trigger the restart. Instead, the update is downloaded, applied and the program is restarted immediately.

## :simple-linux: Ubuntu

You can specify multiple Ubuntu-based distributions to target for your apt repository using the new [`app.linux.debian.distributions`](configs/linux.md#applinuxdebiandistributions) key. This can be used to work around situations in which you know your app is binary compatible with more than one distribution, but the package names have changed in a backwards incompatible way. Such a situation can arise with JVM apps due to the 64 bit `time_t` transition. If you want to generate one package that works on both Ubuntu 24 and 22 you can add the following to your config:

```
app.linux.debian.distributions += {
    name = "jammy"
    mirrors = ["http://archive.ubuntu.com/ubuntu/", "http://ports.ubuntu.com/ubuntu-ports/"]
}
```

## Other

Other changes include a refresh of the JDK set in the config library, a refresh of the Compose Desktop template, and various bug fixes.