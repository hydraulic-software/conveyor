=== "Open source projects"

    * [x] Set the `app.license` key to the name of your software license e.g. `Apache 2`, `GPL-3` etc. Use [SPDX codes](https://spdx.org/licenses/) if you aren't sure what to put here.
    * [x] Upload your project source code to GitHub. 
    * [x] In your config set this key: `app.vcs-url = "https://github.com/you/your-project"`. This automatically sets `app.site.base-url` to point at the latest GitHub Release.
    * [x] Create a [Fine Grained Personal Access Token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token#creating-a-fine-grained-personal-access-token) with *Read and Write access* to your repository *Contents*.
    * [x] Set the `app.site.github.oauth-token` key to the value of the token created above.
    * [x] (optional) Set the `app.site.github.pages-branch` key to `gh-pages`.
    * [x] Run `conveyor make copied-site`.
    
    Your installs will update to whatever the latest release is.

    You don't have to use GitHub. If you want to upload your site elsewhere make sure `app.vcs-url` is set to the URL of your source 
    repository and set `app.site.base-url` to the URL where the generated site will be uploaded to. 

=== "Commercial projects"

    When your `app.site.base-url` key is set to localhost or a domain that ends in `.local` Conveyor is in testing mode and you can use 
    it for free. Once you set `app.site.base-url` to a real website you will be asked to pay and granted three license keys. Each key can 
    be used with one site URL. If you want different update channels (e.g. beta, testing) then you'll need to different site URLs and one 
    key for each.

    * [x] Pick a site URL and set `app.site.base-url` to point to it, e.g. `app { site.base-url = "https://downloads.example.com/myapp" }`
    * [x] Set the `conveyor.billing-email` key to the email address we can use to contact you for billing purposes. 
    * [x] Run `conveyor make site`.
    * [x] You'll be asked to visit a payment URL where you can enter credit card data, and the `conveyor.license-key` key will be set to
          a short random code. This key is linked to your chosen download site URL.
    * [x] Rerun `conveyor make site`. You should now get files that can be uploaded to your chosen site URL.

To release an update you just re-upload/overwrite the files at the site URL.

!!! tip "Upload using SFTP"
    If your download site is accessible using SSH Conveyor can upload the site for you.
    Set `app.site.copy-to` to something like `"sftp://example.com/var/www/example.com/downloads"` i.e. the URL you'd use with `sftp`.
    Then use `conveyor make copied-site` to build the site and upload it all in one step. Also, remember the deploy workflow created in
    GitHub actions? You can run it to start a build/package/release sequence automatically.


[ :material-arrow-right-box: Learn more about download sites](../../configs/download-pages.md){ .md-button .md-button--primary }
