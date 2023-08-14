# 9. Next steps

## Signing and stores

To get rid of security warnings you can use a code signing certificate. For Windows users you can alternatively upload your app to the Microsoft Store, which doesn't have any signing requirements. At around $19 for an individual or $99 for a company, using the Microsoft Store is cheaper than buying a Windows code signing certificate. 

This step is optional; if you're just experimenting or will be distributing to a network where the admins can install custom root certificates, you can skip this section.

Conveyor can use existing keys, certificates and hardware security modules you may have, and it can also assist you in getting certificates if you don't already have them. 

[Read about keys and certificates](../../configs/keys-and-certificates.md){ .md-button } [Read about the Microsoft Store](../../configs/windows.md#release-to-the-microsoft-store){ .md-button }

## Check for updates in your app

The generated repository site has a file called `metadata.properties` in it, which contains the version and revision of the release. This uses the Java properties format and so can be easily loaded in a few lines of code from any language, without needing any special parser. Just be sure to skip any lines that start with a `# ` as those are comments and to unescape newlines. This data can be used to poll for updates in the background, present update notifications and so on.

## Change the default settings

Not happy with the defaults? There are [lots of settings](../../configs/index.md) available, including settings that expose platform specific metadata and features.

## Explore other types of app

Conveyor also supports servers with full Linux `systemd` integration. Take a look at the [Linux config sections](../../configs/linux.md) to learn more, or see an example of [packaging a server](2-adapt-a-server.md). 

## Get help

Stuck? Try asking in our [Discord chatroom](https://discord.gg/E87dFeuMFc) or our [GitHub Discussions forum](https://github.com/hydraulic-software/conveyor/discussions).

<script>var tutorialSection = 10;</script>
