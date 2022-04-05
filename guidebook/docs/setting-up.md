# Getting started

**You will need:** 

1. A JVM based application. Conveyor will gain support for non-JVM apps soon!
1. Somewhere that serves static files over HTTP.
2. If code signing, an Apple Developer ID and Windows signing certificate. They can be obtained with only a web browser and credit card, on any platform. For creating unsigned packages, see below.

## Download and install Conveyor

!!! warning
    **Conveyor is currently in private beta**. You will need a password to use the button below.

[ :material-download-circle: Download for your OS](https://downloads.hydraulic.dev/conveyor/download.html){ .md-button .md-button--primary }

!!! note 
    **Conveyor is packaged using itself**. To avoid surprises, automatic background updates are switched off for Windows and macOS. Updates can be checked for by running Conveyor from the GUI instead of the command line, and then clicking "Check for updates". On Linux you'll be using either the apt repository or a tarball, so can apply or delay updates in the usual manner.

## Keys and certificates

Conveyor has built in support for everything needed to sign your downloads. It can also help you obtain code signing certificates. 

You need to generate keys even if you don't plan to use a code signing certificates, because they're used for signing Linux packages and macOS update metadata files.

**[Set up keys and certificates](keys-and-certificates.md)**, then proceed to the next steps.

## Next steps

1. Read about [what Conveyor produces](outputs.md).
2. Take [the tutorial](tutorial.md).
3. Learn more about how to [run the tool](running.md).
4. Learn [how to customize the results](configs/index.md).
