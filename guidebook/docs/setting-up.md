# Getting started

**You will need:** 

1. A JVM based application. Conveyor will gain support for non-JVM apps soon!
1. Somewhere that serves static files over HTTP.
2. If code signing, an Apple Developer ID and Windows signing certificate. They can be obtained with only a web browser and credit card, on any platform. For creating unsigned packages, see below.

## Download and install Conveyor

!!! warning
    **Conveyor is currently in private beta**. This build will expire at the end of May.

[ :material-download-circle: Download for your OS](https://downloads.hydraulic.dev/conveyor/download.html){ .md-button .md-button--primary }

**Updates.** Conveyor is packaged using itself. To avoid surprises, automatic background updates are switched off for Windows and macOS. Updates can be checked for by running Conveyor from the GUI instead of the command line, and then clicking "Check for updates". On Linux you'll be using either the apt repository or a tarball, so can apply or delay updates in the usual manner.

## Keys and certificates

You need to generate a root key even if you don't plan to buy Mac or Windows code signing certificates because keys are used for signing Linux packages, macOS update metadata files and self-signing.

**[Set up keys and certificates](keys-and-certificates.md)**, then proceed to the next steps.

## Next steps

1. Read about [what Conveyor produces](outputs.md).
1. Please read the [release notes](release-notes.md)!
1. Take [the tutorial](tutorial.md).
1. Learn more about how to [run the tool](running.md).
1. Learn [how to customize the results](configs/index.md).
