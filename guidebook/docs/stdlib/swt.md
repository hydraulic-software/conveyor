# SWT

SWT is the GUI toolkit used in Eclipse. It maps directly to native widgets, making it small and fast. Using it is simple:

```
include "/stdlib/jvm/17/openjdk.conf"
include "/stdlib/jvm/swt.conf" 1️⃣

eclipse.swt {  2️⃣
  version = "4.21"
  build-id = "202109060500"
}

app {
  ....
}
```

1️⃣ Include the SWT standard library snippet.

2️⃣ Select your requested version and build ID.

That's it! [Check out a full sample config](../samples/swt-file-explorer.md#swt-file-explorer).
