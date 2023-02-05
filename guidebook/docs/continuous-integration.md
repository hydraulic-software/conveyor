# Continuous integration

!!! important "Caching Conveyor downloads"
    Please be careful that your CI/build system doesn't download Conveyor over and over again. If you can't pre-install it on your workers for some reason, make sure the download is cached locally. IP addresses that seem to be re-downloading Conveyor on every build may be throttled or blocked.

Conveyor works well with CI platforms like TeamCity, GitHub Actions etc. When building in CI you should supply your signing credentials in a different way than using the `defaults.conf` file. A simple approach is to create a separate file next to your main `conveyor.conf` file that looks like this:

```
include required("conveyor.conf")

app {
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

Call it something like `ci.conveyor.conf`. Copy your `.cer`/`.pem` files to be next to this file (or adjust the paths). Now place your root key and Apple notarization app-specific password into secret environment variables in your CI configuration called `SIGNING_KEY` and `APPLE_ASP` respectively. Finally, invoke conveyor like this: `conveyor -f ci.conveyor.conf make site`. Your main `conveyor.conf` file will fall back to the generated defaults for self-signing.  

An alternative approach is to set a passphrase, then put the encrypted `app.signing-key` value into your main app config that gets checked into version control. You can then put the passphrase into an environment variable and specify it on the command line with `--passphrase=env:PASSPHRASE`.

To get Conveyor onto your build agents either download the Linux tarball or pre-install it on your agents. You can get a link for the current version from the [download page](https://downloads.hydraulic.dev/conveyor/download.html), which will look like this: `https://downloads.hydraulic.dev/conveyor/conveyor-6.3-linux-amd64.tar.gz`.

## Using GitHub Actions

GitHub Actions has a couple of limitations that require workarounds:

1. No direct download links for artifacts exported from jobs.
2. Can only export files using zips, and those zips don't preserve UNIX file permissions.

An example of how to use GitHub Actions with Conveyor is [the GitHub Desktop package](https://github.com/hydraulic-software/github-desktop/). It does the build of the platform-specific artifacts in Actions on each commit, but expects you to run Conveyor locally when it's time to release.

* [`conveyor.conf`](https://github.com/hydraulic-software/github-desktop/blob/conveyorize/conveyor.conf)
* [`ci.yml`](https://github.com/hydraulic-software/github-desktop/blob/conveyorize/.github/workflows/ci.yml)

It solves the above limitations:

1. A direct download link to the output of a CI build job is created using the [`nightly.link`](https://www.nightly.link) service. You give this website the URL of your Actions job YAML, and it gives you back download links that can be used as Conveyor inputs. 
2. UNIX files are wrapped in a tarball which is then in turn exported inside a zip. Windows files are exported directly inside a zip. Conveyor is then told to extract the archives and inner archives to get at the files.

The config for this looks like:

```
ci-artifacts-url = nightly.link/hydraulic-software/github-desktop/workflows/ci/conveyorize

app {
  windows.amd64.inputs = ${ci-artifacts-url}/build-out-Windows-x64.zip
  mac.amd64.inputs = [{
    from = ${ci-artifacts-url}/build-out-macOS-x64.zip
    extract = 2
  }]
  mac.aarch64.inputs = [{
    from = ${ci-artifacts-url}/build-out-macOS-arm64.zip
    extract = 2
  }]
}
```

By defining the inputs as an object and then using the `extract` key, the outer zip and inner tarball can be both unwrapped. This preserves file permissions and other UNIX metadata.

### Doing releases from within GitHub Actions

We don't currently have an example of doing this, but it should be no different to using any other tool. Please remember to cache the download, and be aware that Microsoft plan to phase out non-hardware protected signing keys in future. To release Windows apps with hardware protected keys you have a few options:

1. Run Conveyor locally instead of driving it from CI, with your key HSM plugged in via USB.
2. Provide GitHub or your CI system with a build agent that has the signing key plugged in, and supply the passphrase via a secret environment variable (e.g. `--passphrase=env:SIGNING_PASSPHRASE`).
3. Use a cloud HSM or signing service like [SignPath](https://about.signpath.io/)
