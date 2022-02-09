# Download pages

The `site` task will generate the online update repository along with a simple static download page for your software. The download page is entirely optional and can just be ignored if you don't want it, alternatively it can also be customized. The HTML has these features:

* Detects the user's operating system and (on Chrome) CPU architecture, then presents them with the appropriate download button.
* Shows your app logo.
* Templated using Thymeleaf, so you can easily customize the appearance and logic.
* Access to both primary package formats for each platform, and alternatives like raw zips or tarballs.

![Download page screenshot](download-page-screenshot.png)

## Synopsis

```
app.site {
  display-name = Title of the web page.

	# Where the installed packages can check for online updates.
  base-url = somesite.com/downloads

  # Import an HTML template or additional files to the generated site.
  inputs += "my-template-dir/*" -> .
  
  # Change the set of images used for the logo, as found in the resolved inputs.
  icons = "my-icons-*.png"
}
```

## Templates

The download site is generated from a set of input files. If none are specified then a default template is used, but you can replace it and add your own files too. The template should be called `download.html` and use [Thymeleaf](https://www.thymeleaf.org/) to parameterize it with data from your configuration.

The `app.site.icons` key works the same way as the other icon keys do (in the `windows`, `mac` and `linux` sections respectively). The default `app.site.inputs` list contains the value of `${app.site.icons}` so you can use alternative file names by just setting the image name here. The icons need to be found in the root of the resolved inputs. If you have a file named `icon.svg` as an input, it'll be used as the icon instead of the rendered files.

Here's an example of how to import icons from a different directory than the one containing the config and use it at the top of the generated HTML.

```
icons-path = ../../packaging/images
round-icons = "hydraulic-icon-rounded-*.png"

app.site {
  base-url = downloads.hydraulic.dev/${channel}/compose-sample
  inputs = [ ${icons-path}${round-icons} -> . ]
  icons = ${round-icons}
}

```

### Thymeleaf in 20 seconds

This section is not meant to be a comprehensive tutorial in Thymeleaf syntax. It only explains the syntax used in the default template.

Thymeleaf is designed to ensure that templates are always viewable without being processed, which is convenient for rapid iteration and prototyping. When an attribute is prefixed with `th:` the value will be evaluated and set the value of that attribute at evaluation time. That means you can specify dummy values for attributes and the template engine will replace them automatically. This also applies inside JavaScript. When code like this appears `/*[[#{download.win}]]"/` inside a script tag with the  `th:inline="javascript"` attribute, everything from the end of the comment to the semicolon is deleted, and replaced with the evaluation of the expression (`#{download.win}` in this case. That means JavaScript can also contain dummy values.

There are multiple kinds of substitution syntax. Use `${}` for variables and `#{}` for localized messages, which are stored in a Java properties bundle next to the template. The `*{}` substitution simply evaluates the expression in the context of whatever the nearest enclosing `th:object` expression was i.e. it's a form of "with" operator. Please refer to the Thymeleaf manual for a full explanation. You have access to a `data` variable and anything reachable via that object, which includes:

* `data.displayName` - the name of the site as taken from the config.
* `data.downloadUrls` - a map of package types (`appinstaller`, `winzip`, `mac`, `debian`, `tarball`) to objects defining the download. Each object contains a `url` field which is a *relative* URL to the file the user should be given. In Thymeleaf syntax you'd therefore access it like this: `data.downloadUrls.winzip.url` to get e.g. the download URL of a Windows ZIP.
* `data.latestVersion` - set to whatever the version of the app being built and installed to the repository is.
* `data.generatorName` - a string like "Hydraulic Conveyor 1.0"

## Translations

In the inputs you may specify `.properties` files for different languages. To add a language, take the `download.properties` file as a base and translate each string. Call the result `download_pl.properties` if for example you're translating to Polish, then add the language code to the `site.download-page.languages` list in your site config. Re-run the `site` task and you should now have a translated version of the page.

TODO(high): Once Conveyor is self-packaged, update this section to show how to find the default templates and properties files.
