# Download/update site

The `site` task will generate the online update repository along with a static download page. The `copied-site` task will additionally
copy the results to another directory, which can be on a remote system accessible via SFTP. 

The download page is optional and can just be ignored if you don't want it. It has these features:

* Detects the user's operating system. Allows the user to switch between operating systems.
* On Chrome, detects the CPU architecture.
* Access to both primary package formats for each platform, and alternatives like raw zips or tarballs.
* Provides copy/pasteable terminal commands to install the app on Linux.
* Shows your app logo.

[See an example](https://downloads.hydraulic.dev/eton-sample/download.html)

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

## Hosting providers

### Publishing through GitHub

```
app {
  # Reference to the GitHub repository where the project is hosted.
  vcs-url = "github.com/user/repo"
  site {    
    github {
      // Token looks like "github_pat_SOME_TOKEN_VALUE"
      oauth-token = ${env.GITHUB_TOKEN}
      
      // Optional: upload the download site to a branch. 
      pages-branch = "gh-pages"
    }
  }
}
```

Conveyor's repository sites are designed to be compatible with GitHub releases. Using them is easy:

1. Set your `app.vcs-url` to point to `github.com/user/repo`. This will automatically set `app.site.base-url` to be `https://github.com/$user/$repo/releases/latest/download`. If you aren't packaging an open source app then don't set `vcs-url` and set the `site.base-url` key to that location manually.
2. In GitHub, set up an OAuth token to allow Conveyor to upload releases to your project:
    1. Create either a [Fine Grained Personal Access Token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-fine-grained-personal-access-token) with *Read and Write access* to your repository contents, or a [Classic personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-personal-access-token-classic) with *public_repo* scope (if it's a private repository, you'll need the whole *repo* scope).
    2. Set `app.site.github.oauth-token` to the token, or use the `${env.VARNAME}` feature to substitute it from an environment variable.
3. (Optional) Set up `app.site.github.pages-branch` to the branch where you want to release the download site. For instance, if you set it to "gh-pages" your download will automatically be available from `https://$user.github.io/$repo/download.html`.
4. Run `conveyor make copied-site`.

That's it! To upgrade your users just run `conveyor make copied-site` on newer versions of your app. The auto-update engines will be checking the metadata files on whatever your latest release is to discover what to download.

Under the hood, releasing to GitHub Releases is controlled by setting key `app.site.copy-to` to a special value `github:$user/$repo`. You could set this key directly to publish to GitHub Releases for any repository you control, though it probably only makes sense to publish to the same repo where your code is. Conveyor will set this key automatically if it detects that `app.site.base-url` points to a GitHub Releases page.

!!! note
    Your users will upgrade to whatever the `/releases/latest` URL points to. Therefore, you shouldn't do beta releases or other forms of pre-release this way. Stick those files somewhere else or use draft releases, etc.

### Publishing through Amazon S3

[Read our guide on configuring S3 and CloudFront for download sites](https://hydraulic.dev/blog/16-aws-static-website.html){ .md-button .md-button--primary }

To configure Conveyor to upload your site to an Amazon S3 bucket, set the `copy-to` and `s3` keys:

```
app {  
  site {
    copy-to = "s3:my-bucket/path/to/site"
    
    s3 {
      // Your bucket region.
      region = "us-east-1"
      access-key-id = ${env.AWS_ACCESS_KEY_ID}
      secret-access-key = ${env.AWS_SECRET_ACCESS_KEY}
    }
  }
}
```

1. Set key `app.site.copy-to` to `s3:$bucket/$path`. If your `app.site.base-url` has a host ending with `.s3.amazonaws.com`, you don't need to set the value of `app.site.copy-to`, as Conveyor can infer the correct value. 
2. Set `app.site.s3.region` to the appropriate region for your S3 bucket.
3. Set `app.site.s3.access-key-id` and `app.site.s3.secret-access-key` with the details of your [AWS programmatic access key](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html#Using_CreateAccessKey).

Now running `conveyor make copied-site` will build and upload the app to your S3 bucket.

!!! warning "S3 URLs"
    Windows updates will fail when using the default static serving endpoint (e.g. `https://bucketname.s3-website-us-east-1.amazonaws.com/`). Use the object URL that ends in `.s3.amazonaws.com` instead (e.g. `https://bucketname.s3.amazonaws.com/`). 

    For unclear reasons, Amazon don't advertise the `Accept-Range: bytes` header when using the first form and Windows requires this as part of its optimized download system, where it won't download files the user already has.

### Publishing via SFTP

If you set `app.site.copy-to` to a string like `//hostname/directory` then you can use the `copied-site` task to both build the downloads
and upload the resulting download site to a remote server. The directory should be absolute, and you can specify both username and password
in the machine part (it's basically an `ssh://` url but minus the scheme). Public/private keys and the `known_hosts` file will be read from
the usual locations as used by OpenSSH, but Conveyor won't log in to a server it doesn't recognize, so if you would get a new key warning
from regular `ssh` you will need to log in first, to ensure the key goes into the `known_hosts` file.

### Publishing to the App Stores

Conveyor currently supports submitting your app to the [Microsoft Store](https://apps.microsoft.com/). In this case you don't use the `copied-site`
command, since the "site" would actually be the store. Instead, you use the `ms-store-release` command. For details, check the [Store config documentation](windows.md#release-to-the-microsoft-store).

## Remote site checks

Conveyor makes a number of checks to the published download site (if it's been published before) to ensure that users will be able to update
to new versions of your software. The way detected issues are surfaced is controlled by key `app.site.consistency-checks`. The possible 
values are `error` and `warn`.

The following checks are made:

* That the Windows signing certificate matches the one used to upload packages previously. This is useful to catch the case where a certificate identity has changed e.g. due to a switch from OV to EV, change of company name and so on. Changing certificate identity without disrupting updates is not currently supported by Conveyor. If you need this feature please [let us know](mailto:contact@hydraulic.dev).  
* That you aren't overwriting a pre-existing MSIX file. Older versions of Windows contain bugs that cause updates to fail until the next reboot if a package file is overwritten.


## Relocating your download site

Conveyor supports automatically moving your download site to a new location, by making the necessary changes so that update artifacts in your old
site will point to the new location, redirecting users when they update your app. 

To configure a site move, use the following keys:

* **`app.site.move-from.base-url`**: The value of `app.site.base-url` for the old site.
* **`app.site.move-from.copy-to`**: The value of `app.site.copy-to` for the old site, same value as per instructions above.
* **`app.site.move-from.s3`**: The value of `app.site.s3` containing the Amazon S3 credentials for the old site, if it was served from AWS.
* **`app.site.move-from.github`**: The value of `app.site.github` containing the GitHub credentials for the old site, if it was served from GitHub.

For example, if you're moving your site from GitHub to AWS, the configuration should look something like this:

```hocon
app {
  site {
     // Your new site, backed by an Amazon S3 bucket. 
    base-url = "https://my-download-site.com/path/to/site"
    
    // Path to your new site within your S3 bucket.
    copy-to = "s3:my-bucket/path/to/site"
    
    s3 {      
      region = "us-east-1"
      access-key-id = ${env.AWS_ACCESS_KEY_ID}
      secret-access-key = ${env.AWS_SECRET_ACCESS_KEY}
    }
    
    move-from {
      // Your old site on GitHub
      base-url = "https://github.com/user/repo/releases/latest/download"
      
      github {        
        oauth-token = ${env.GITHUB_TOKEN}
        
        // Optional: upload the new download page with links to the new site to the old location.
        pages-branch = "gh-pages"        
      }
    }
  }
}
```

The `app.site.move-from` config key doesn't need to be permanent; after your users have moved and no app is checking the old site for updates,
as you phase it out you can remove that config.

The way it works is by making a transitory release into the old site, modifying specific artifacts for the transition:

* On macOS, the `appcast-*.rss` files will point to the package in the new site.
* On Linux, the Debian package is set up so the next installation will update the URI set in `/etc/apt/sources.list.d` to point to the new site.
* On Windows, the `metadata.properties` file will contain the instructions for reinstalling the app pointing to the new location of the AppInstaller file.
  This has to be used with Conveyor's [escape hatch mechanism](escape-hatch.md).
* Finally, the `download.html` page and the icons will be updated to point everything to the new location, so even users coming to the old site
  will be pointed to links into the new site.
