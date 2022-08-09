# Continuous integration

Conveyor works well with CI platforms like TeamCity, GitHub Actions etc. When building in CI you should supply your signing credentials in a different way than using the `defaults.conf` file. A simple approach is to create a separate file next to your main `conveyor.conf` file that looks like this:

```
include required("conveyor.conf")

app {
    sign = true
    signing-key = ${env.SIGNING_KEY}

    mac.certificate = apple.cer
    windows.certificate = windows.cer
    
    mac.notarization {
        app-specific-password = ${env.APPLE_ASP}
        team-id = 6MD7Z8H86K
        apple-id = "you@user.host"
    }
}
```

Call it something like `ci.conveyor.conf`. Copy your `.cer`/`.pem` files to be next to this file (or adjust the paths). Now place your root key and Apple notarization app-specific password into secret environment variables in your CI configuration called `SIGNING_KEY` and `APPLE_ASP` respectively. Finally, invoke conveyor like this: `conveyor -f ci.conveyor.conf make site`. Your main `conveyor.conf` file can set `app.sign = false` so signing doesn't get in the way during development.

An alternative approach is to set a passphrase, then put the encrypted `app.signing-key` value into your main app config that gets checked into version control. You can then put the passphrase into an environment variable and specify it on the command line with `--passphrase=env:PASSPHRASE`.

!!! important "Caching Conveyor downloads"
    Please be careful that your CI/build system doesn't download Conveyor over and over again. If you can't pre-install it on your workers for some reason, make sure the download is cached locally. IP addresses that seem to be re-downloading Conveyor on every build may be throttled or blocked.