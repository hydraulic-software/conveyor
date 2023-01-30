# Electron

{!tutorial/hare/start.md!}

## Create a sample project

* [x] Run the following commands.

```shell
conveyor generate electron com.example.my-project
cd my-project
npm install
```

## Create the unpackaged app

{!tutorial/hare/create-unpackaged-app.md!}

## Serve the download site

{!tutorial/hare/serve-with-npx.md!}

## Release an update

In another terminal tab:

* [x] Edit `package.json` and change the version field to `2.0`.
* [x] Run `npm install` to update `package-lock.json`.
* [x] Run `conveyor make site` to regenerate the download site.

{!tutorial/hare/apply-update-instructions.md!}

## Read the config

It looks like this:

```
include required("/stdlib/electron/electron.conf")

// Import metadata from your package.json file and 
// which version of Electron to use.
package-json {
  include required("package-lock.json")
}

app {
  display-name = "My Project"
  rdns-name = com.example.my-project
  site.base-url = "localhost:5000"
  updates = aggressive
  icons = icons/icon.svg
}
```

{!tutorial/hare/hocon-tip.md!}

## Change the icon

The current icon is the Electron logo, which isn't a great choice for your app. Conveyor can draw icons for you.

* [x] Delete the line that says `icons = icons/icon.svg`.
{!tutorial/hare/generate-icons.md!}

## Upload a real update site

{!tutorial/hare/upload-real-site.md!}

## Signing

{!tutorial/hare/signing.md!}

## Become a 🐢 tortoise

{!tutorial/hare/become-a-tortoise.md!}

<script>var tutorialSection = 100;</script>
