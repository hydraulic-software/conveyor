# Uploading packages

Conveyor can upload your generated packages and update files after they are built. Use `conveyor make copied-site` in combination with
a configured hosting service. 

## Publishing through object storage

Conveyor can upload to any Amazon S3 compatible object storage system.

[Read our guide on configuring S3 and CloudFront for download sites](https://hydraulic.dev/blog/16-aws-static-website.html){ .md-button .md-button--primary }

To configure Conveyor to upload your site to an Amazon S3 bucket, set the `copy-to` and `s3` keys:

```
app {  
  site {
    copy-to = "s3:my-bucket/path/to/site"
    
    s3 {
      // Your bucket region.
      region = "us-east-1"

      // Optional: access credentials.      
      access-key-id = ...
      secret-access-key = ...
      
      // Optional: override endpoint if using S3 from a different provider:
      endpoint = "s3.us-west-002.backblazeb2.com"
      // Optional: If you're using an S3 impl which uses https://endpoint/bucket-name e.g. minIO
      force-path-style = true  
      
      // Optional, 3 is the default.
      retries = 3      
    }
  }
}
```

1. Set key `app.site.copy-to` to `s3:$bucket/$path`. If your `app.site.base-url` has a host ending with `.s3.amazonaws.com`, you don't need to set the value of `app.site.copy-to`, as Conveyor can infer the correct value.
2. Set `app.site.s3.region` to the appropriate region for your S3 bucket.
3. (Optional) Set `app.site.s3.access-key-id` and `app.site.s3.secret-access-key` with the details of your [AWS programmatic access key](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_access-keys.html#Using_CreateAccessKey). If you don't provide credentials, they will be [read from your environment as described here](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-chain.html#credentials-default).
4. (Optional) If you're using an S3 provider that isn't AWS, set `app.site.s3.endpoint` to their endpoint and possibly `app.site.s3.force-path-style = true`.

Now running `conveyor make copied-site` will build and upload the app to your S3 bucket.

!!! important "Bucket permissions"
    Your bucket should either be public, or (better) be behind a public CDN. Installed clients require public, unauthenticated access to your site URL and Conveyor does not embed your S3 keys into installations as they could be easily extracted and would thus no longer be secret.

!!! warning "S3 URLs"
    Windows updates will fail when using the default static serving endpoint (e.g. `https://bucketname.s3-website-us-east-1.amazonaws.com/`). Use the object URL that ends in `.s3.amazonaws.com` instead (e.g. `https://bucketname.s3.amazonaws.com/`).

    For unclear reasons, Amazon don't advertise the `Accept-Range: bytes` header when using the first form and Windows requires this as part of its optimized download system, where it won't download files the user already has.

## Publishing through GitHub

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

## Publishing via SFTP

If you set `app.site.copy-to` to a string like `//hostname/directory` then you can use the `copied-site` task to both build the downloads
and upload the resulting download site to a remote server. The directory should be absolute, and you can specify both username and password
in the machine part (it's basically an `ssh://` url but minus the scheme). Public/private keys and the `known_hosts` file will be read from
the usual locations as used by OpenSSH, but Conveyor won't log in to a server it doesn't recognize, so if you would get a new key warning
from regular `ssh` you will need to log in first, to ensure the key goes into the `known_hosts` file.

## Publishing to the App Stores

Conveyor currently supports submitting your app to the [Microsoft Store](https://apps.microsoft.com/). In this case you don't use the `copied-site`
command, since the "site" would actually be the store. Instead, you use the `ms-store-release` command. For details, check the [Store config documentation](../configs/windows.md#release-to-the-microsoft-store).

## Configuring Fastly as a CDN

A CDN can offload bandwidth and improve install times by caching binaries close to the user. This can be especially important for fast
Windows installs and rapid startup when using aggressive updates mode.

[Fastly](https://www.fastly.com) is a good CDN provider that offers many features, and the first $50/month of usage is free. Fastly doesn't
store your files so you'll need to upload the files somewhere else, but it provides a free domain and SSL certificate with easy setup.

Configuring Fastly for Conveyor sites is fairly straightforward but requires a custom step to enable serving of files over 20MB:

1. Sign up for an account.
2. Configure the CDN as you would normally, pointing the origin URL at wherever your files are actually being stored.
3. Edit the configuration for your CDN service.
4. Click "VCL Snippets" and create a new snippet titled "Enable segmented caching".
5. Accept the default settings (`within subroutine recv (vcl_recv)`) and then set the content of the script to `set req.enable_segmented_caching = true;` to enable this feature for all files.
6. Click update and apply the new configuration.

If you don't care what your update site domain is, you're done. You can link to the generated files from anywhere so you always have the option of putting the generated `download.html` on some other site, or making your own download links. If you want to use a custom domain, you'll now need to [verify ownership in order to get TLS certificates issue](https://docs.fastly.com/en/guides/setting-up-tls-with-certificates-fastly-manages) and alter your DNS records.

## Remote site checks

Conveyor makes a number of checks to the published download site (if it's been published before) to ensure that users will be able to update
to new versions of your software. The way detected issues are surfaced is controlled by key `app.site.consistency-checks`. The possible
values are `error` and `warn`.

The following checks are made:

* That the Windows signing certificate matches the one used to upload packages previously. This is useful to catch the case where a certificate identity has changed e.g. due to a switch from OV to EV, change of company name and so on.
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
  This has to be used with Conveyor's [escape hatch mechanism](../configs/escape-hatch.md).
* Finally, the `download.html` page and the icons will be updated to point everything to the new location, so even users coming to the old site
  will be pointed to links into the new site.
