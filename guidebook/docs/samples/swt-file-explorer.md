# SWT File Explorer

SWT is a UI toolkit that maps directly to underlying native widgets. This config will download and package the SWT File Explorer demo.

```hocon
include required("/stdlib/jdk/17/openjdk.conf")
include required("/stdlib/jvm/swt.conf")

app {
  fsname = swt-demos
  display-name = SWT File Explorer
  version = 3.106

  // Download the demo app from the Eclipse website.
  inputs += download.eclipse.org/eclipse/updates/4.20/R-4.20-202106111600/plugins/org.eclipse.swt.examples_3.106.1300.v20210401-1514.jar
  
  // Just grab an icon off the web.
  inputs += www.iconfinder.com/icons/79514/download/png/512 -> icon-512x512.png

  jvm.main-gui-class = org.eclipse.swt.examples.fileviewer.FileViewer

  // Host it off localhost
  site.base-url = "https://localhost/"${app.fsname}
}
```
