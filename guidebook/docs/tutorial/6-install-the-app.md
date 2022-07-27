# 6. Install the app

* [ ] [Browse to your new download site](http://localhost:8899/download.html) and take a look at what's there.

You don't have to use this HTML page. It's there purely as a convenience.

The generated download HTML detects the user's computer whilst allowing them to pick a different download
if they want. For properly signed packages you get a standard green download button. For self-signed packages, installation is more complex
and instructions are provided. They require using the terminal (on Windows) or using
[a hidden keyboard shortcut](https://support.apple.com/guide/mac-help/open-a-mac-app-from-an-unidentified-developer-mh40616/mac) on macOS.

On Windows the link will go to a small `.exe` file, and not the `.msix` package directly. When the user opens the
executable Windows will download and install the package whilst also registering it for background updates. Letting Windows do the download makes it faster, because data from other programs on disk will be
re-used. If you download and install the MSIX file directly then those features are unavailable.

<script>var tutorialSection = 6;</script>
