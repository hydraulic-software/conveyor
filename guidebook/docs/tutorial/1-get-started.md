# 1. Get started

In this tutorial we'll generate a fresh application using the templates built in to Conveyor. Then we'll generate a download site for it containing packages for every supported platform. Finally we'll take a look at how things are wired up to learn how to package a pre-existing project.

This tutorial doesn't cover all the features Conveyor has. Read through the rest of this guidebook to learn about the full range of possibilities.

!!! tip
    You can tick the checkmarks on these pages to mark your progress. Their state is stored in a cookie.

* [x] [Download Conveyor](../download-conveyor.md). On macOS, make sure it's added to your path by opening the app from the GUI and clicking the relevant button.

You don't need to have any code signing certificates to use Conveyor or follow this tutorial. Nonetheless, Conveyor always needs cryptographic keys so it can at least self-sign your app. To get started we'll use self-signing, which is good enough for testing, internal apps and distributing software to developers. The final steps of the tutorial show you how to use real code signing keys.

* [x] Run `conveyor keys generate` from a terminal.

This command will create a new root private key, convert it to a series of words and write it to a config file in your home directory.

!!! tip "Key derivation"
    You can back this generated file up in any way you like, even by writing down the words with a pen (include the timestamp). Each type of key Conveyor needs will be derived from this one root, unless you supply a custom key for specific platforms.

<script>var tutorialSection = 1;</script>
