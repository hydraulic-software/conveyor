A plain directory can't be installed or updated. Let's fix that by making self-updating packages.

```shell
conveyor make site
cd output
npx serve .
```

* [x] Open [localhost:3000/download.html](http://localhost:3000/download.html) and try installing your new app.

It's self-signed, so you'll need to follow the instructions for how to install it. We'll fix that later.
