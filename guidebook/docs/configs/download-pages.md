# Download/update site

The `site` task will generate the online update repository along with a static download page. The `copied-site` task will additionally
copy the results to another directory, which can be on a remote system accessible via SFTP. 

!!! important 
    See [Configuring CDNs](../configuring-cdns.md) for important information about how to serve the generated files. 

The download page is optional and can just be ignored if you don't want it. It has these features:

* Detects the user's operating system. Allows the user to switch between operating systems.
* On Chrome, detects the CPU architecture.
* Access to both primary package formats for each platform, and alternatives like raw zips or tarballs.
* Provides copy/pasteable terminal commands to install the app on Linux.
* Shows your app logo.

[See an example](https://hydraulic-software.github.io/eton-desktop/download.html)

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
  
  # Which keys appear in the metadata.properties file in the generated site.
  export-keys = [ app.version, app.revision ]
  
  # HTML to add into the <head> area.
  extra-header-html = """
  	<script>
  		// etc
  	</script>
  """
  
  # Directory to upload/copy the site to over SFTP.
  copy-to = "//user@myserver.com/var/www/downloads"
  
  # If set, controls whether the Debian packages and apt files go into a 
  # subdirectory or not. If not set this is inferred automatically.
  # You should normally never need to set this. 
  flat = true/false
  
  # Whether to show the "Packaged by Conveyor" badge at the bottom of the download page.
  # Can only be disabled for commercial products. 
  # Open source projects are required to advertise the fact that they're packaged with Conveyor.
  show-conveyor-badge = true
  
  # If you need to move your download site to a different URL, set this so your
  # users automatically get updated to the new location. 
  move-from {
    base-url = old-site.com/downloads
    copy-to = "//user@myserver.com/var/www/old-downloads"
  }
}
```

## Icon

The `app.site.icons` key works the same way as the other icon keys do (in the `windows`, `mac` and `linux` sections respectively). The
default `app.site.inputs` list contains the value of `${app.site.icons}` so you can use alternative file names by just setting the image
name here. However, if you use an SVG file as an input it'll be used as the icon instead of the rendered files.

Here's an example of how to import icons from a different directory than the one containing the config and use it at the top of the
generated HTML.

```
app.site {
  base-url = downloads.hydraulic.dev/${channel}/compose-sample
  icons = "../../packaging/images/icon-{32,64,128,256}.png"
}
```

## Conveyor badge

By default, Conveyor will generate a small badge at the bottom of the download page saying "Packaged with Conveyor". This is convenient
for open source projects, since those are required to advertise the fact that they're packaged with Conveyor.

Commercial projects that have acquired a Conveyor license can remove the badge by setting key `app.site.show-conveyor-badge` to `false`.

## Theme

The `app.site.theme` key allows you to select which theme to be used for the download site. Currently Conveyor supports themes `light` and `dark`. If this is left unset, the theme will be set dynamically according to the user system's theme.
By default, this will follow the value of `app.theme`.

## Exporting to metadata.properties

When you generate a download/update site with `conveyor make site` one of the generated files is called `metadata.properties`. It's a text file containing key=value pairs and by default will look like this:

```properties
#Sun Dec 04 18:41:57 CET 2022
app.long-fsname=vendor-name-product-name
app.revision=0
app.windows.manifests.version-quad=2.1.0.0
app.version=2.1
```

The keys to export are controlled by the `app.site.export-keys` key and the default value is:

```
export-keys = [app.long-fsname, app.version, app.revision, app.windows.manifests.version-quad]
```

You can export any key that maps to a string, number or boolean. Objects and lists can't be exported. Here's an example of exporting a multi-line string:

```

release-notes = """
    New feature: We've integrated AI and blockchain to make your work really fizz!
    Also - bug fixes.
"""
app.site.export-keys += release-notes
```

The resulting `metadata.properties` file will have a key that looks like this:

```properties
release-note=New feature: We've integrated AI and blockchain to make your work really fizz!\nAlso - bug fixes.\n
```

Notice that the newline characters are escaped and the indent/leading whitespace were stripped.
