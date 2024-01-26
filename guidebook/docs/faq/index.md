# General FAQ

## What does Conveyor do?

It builds packages for desktop/CLI apps targeting Windows, macOS and Linux. These packages self update using the native package management
or Sparkle on macOS, and are fully signed/notarized. You can build these packages from any OS, meaning you can sign and notarize Mac
apps from Linux or Windows, and Windows apps from macOS or Linux. It can also create simple server packages for Debian/Ubuntu that use
systemd as a service manager.

## Is Conveyor free?

Conveyor is free if your update site is set to localhost (i.e. for testing) and for open source projects if you set the `app.vcs-url` key to
where your source repository can be found. Once you set your `app.site.base-url` key to a public website for a commercial project you'll be
asked to pay. [Learn more about pricing](https://www.hydraulic.dev/pricing.html). A subscription includes commercial support.

Open source apps that use it should advertise the fact that they're packaged with Conveyor somewhere and link to the website. Using the
generated download page makes this easy, because it contains a "Packaged with Conveyor" badge. If you don't use that HTML then you'll need
to provide your own link in e.g. your download page, website, README file etc.

## Am I locked in?

No. Conveyor generates [standard package formats](../outputs.md) and on macOS it uses an open source update framework called
[Sparkle](https://www.sparkle-framework.org/). All these can be produced with native toolchains. If you want to stop using Conveyor, you
will just have to set up your own packaging scripts and do the integration work yourself. In other words you'll have spend the time you
saved by using Conveyor, but nothing will break.


## I am an IT admin. How can I control the update schedule of an app that uses Conveyor?

This depends on the platform.

=== ":simple-windows: Windows"

    MSIX is Microsoft's official packaging and deployment system, so it has excellent controls built in to
    Windows. Admins can use standard enterprise IT management tools like InTune to add packages to groups using the `.appinstaller` file, or
    push out specific MSIX versions centrally. 

    Although the installer EXE is useful to work around bugs, especially on old versions of Windows, it isn't obligatory and enterprise
    deployments will generally prefer to bypass it as long as their fleet is fully patched. Alternatively the MSIX file will not update, if
    installed directly bypassing the installer and the `.appinstaller` file. PowerShell can be used to automate this. Please refer to the
    Windows documentation for how to do this.

=== ":simple-apple: macOS"

    The Sparkle framework will by default ask the user if they want to apply automatic updates or not, and whether to ask them again in future.
    Admins can use this command on machines to disable update checking for newly installed apps (it will disable the question users are asked):

    ```
    defaults write -g SUEnableAutomaticChecks -bool NO
    ```

    If you only want to disable updates for a specific app, then you can use the command:

    ```
    app="/Applications/Your App.app"
    defaults write $( plutil -extract CFBundleIdentifier raw $app/Contents/Info.plist ) SUEnableAutomaticChecks -bool NO
    ```

=== ":simple-linux: Linux"

    The tarball version can be placed anywhere and will not update. If you are using a Debian derived distribution
    and installing the deb, you can use `sudo apt-mark hold your-package` to stop `apt` upgrading the package until you release the hold.

## What justifies your claim that Conveyor is simple?

1. No signup process. This is a classic dev tool - no contact forms or account creation, just [download and run](../download-conveyor.md). You can use it for free with open source projects by simply setting the `app.vcs-url` key in your config.
1. You can run it from your dev laptop, a preferred CI machine or wherever else because Conveyor can build packages for any OS from every OS.
1. It generates [everything you need](../outputs.md) in one go: full download site complete with packages, update metadata, a download page and bootstrap scripts.
1. Online updates work out of the box. Unlike with many other tools, with this one no code changes are required, not even if you use the mode that does an update check on every launch.
1. It handles [signing and Apple notarization](../configs/keys-and-certificates.md) for you. It can even help you buy certificates, by generating a CSR for you.
1. The [config syntax](../configs/hocon-spec.md) is clean and lightweight whilst still letting you flexibly compose config together, use substitutions and more. It's a JSON superset so lots of tooling works with it too. 
1. It knows popular app frameworks and can use their build system output directly (JVM, Electron and Flutter).
1. It has lots of little code paths that check for common mistakes and tries to give you high quality errors. Try setting a key like `app { app.url-schemes = [ foo ] }` and Conveyor will notice that you made a copy/paste error.

And loads of other things. We built Conveyor due to experience distributing desktop apps previously, and knowing how many tedious and frustrating details there can be. Making it as easy as possible is the tool's primary focus, above raw feature count. 

## Why isn't Conveyor a service?

Services can sometimes be convenient and if you'd like to have Conveyor-as-a-service, please [let us know](mailto:contact@hydraulic.dev). 
Also, if you work on a code hosting service/forge of some kind and you'd like to incorporate Conveyor's features into your site,
then get in touch and ask for a reseller license.

Being a local tool means you get:

* Ultra-fast turnaround times on rebuilds, so you can quickly experiment and polish your package.
* No outages. A tool is as available as your own hardware.
* Privacy. We never see your code, so you can use Conveyor for internal or sensitive apps without worry.
* Absolute control. When you buy the Source or Unlimited plans you get the source code and can fork it, or even resell it as a feature of your own product. Good luck forking a service!

It has advantages for us too:

* Natural and effortless scaling, because you're using your own hardware so there are no bottlenecks.
* We can't accidentally flip the wrong switch and create an outage. Corollary: no need to be on-call, yielding big savings that we can then
  pass on to you.
* No need to hold people's private keys, which would create a tempting target for attackers.

## What kinds of app can you package with it?

It can package any kind of app, it just has some built-in features to make packaging Electron, JVM and Flutter apps easy. 
The [tutorial](../tutorial/new.md) shows how to package a CMake based C++ OpenGL app, for example.

## Is Conveyor only for desktop apps?

It also has some support for:

1. Servers that run outside of containers. See the [package a server](../tutorial/tortoise/2-adapt-a-server.md) task page for an example that demonstrates systemd 
   integration, Apache 2/nginx reverse proxy integration and adding a dependency on Postgres.
2. Command line (CLI) apps. Conveyor is packaged with itself, in fact.

Support for CLI apps will be improved in future releases.

## Does it support Electron apps?

Yes, you can create packages straight from your Javascript/HTML/CSS files and your `node_modules` directory. Unlike with other Electron
packaging solutions, you don't need special or custom servers and the results don't create problems for network administrators. 

[Here's an in depth comparison of Conveyor vs other tools](../comparisons/electron-comparisons.md).

## Why should I make a desktop app?  

The web was designed for documents and when we use it for applications, the gap causes problems. As developers we like to (ab)use web browsers
to deliver software for several reasons, but a big one is because web browsers make deployment and updates easy. Now with Conveyor we 
can push updates to desktop and CLI apps just as easily as we push changes to a web site. This lets us revisit some of the foundational
assumptions about how we write software, and achieve major simplifications across the stack. We plan to write about this more in future,
but for now consider just a few of the advantages to leaving the browser:

* Use the best language for the job, not just JavaScript and watered down language dialects that transpile to it.
* Properly exploit every CPU core the user has and deliver them results with the lowest latency possible. The multi-core revolution  
  never made it to the web, but that hardware is waiting for you and your users.
* Direct access to the newest, most powerful and most interesting devices, without interference from the sandbox.
* Empower your users with control over their own data by storing it in files. 
    * Let people organize, back up, share, store and work with their data in multiple apps at once.
    * Dodge a whole swathe of awkward privacy problems and regulations.
* Give your users sophisticated features like re-arrangeable windows, complex keyboard shortcuts, hot screen edges and plugins.
* Be always available even in the most challenging conditions. Users will learn to rely on your app everywhere, at any time, whether
  that's in the middle of a storm on an oil rig, on holiday in the middle of the savannah, during a business trip with spotty connectivity  
  or during a delicate hospital operation where a server outage could be a matter of life and death.

What about regular database backed apps that don't need special hardware, multi-core or files? For these apps, leaving the browser can
yield especially large simplifications, especially if your app doesn't need very large numbers of concurrent users (e.g. internal apps)
and if your database supports writing custom SQL functions in your preferred languages, which most do. Instead of making a web app you can 
write the GUI in a language of your choice and connect it directly to your relational database using your language's drivers. 
Each gets an account on the RDBMS and logs in directly using the app, yielding the following benefits:

* No need to provision and run custom web servers at all, you just need somewhere that does static file hosting for the downloads.
* Eliminate the most common security bugs by design:
  * No XSS and XSRF because you aren't using HTML.
  * No SQL injection because the database knows who the user is and ACLs are implemented at the SQL level.
* No need to design or debate how best to use REST (the driver/RDBMS have a protocol for invoking code on the server already)
* No need to define custom JSON schemas to send data to the client (the database protocol can serialize SQL results automatically).
* Transactions available everywhere. No need to painstakingly align REST endpoints with transactions.
* RDBMS protocols often have other useful features, like pub/sub.
* Use real GUI toolkits without the indirection of the HTML DOM. Your users can benefit from productivity enhancers like menu bars, 
  sophisticated printing support, advanced table view controls that let them rearrange columns, sort them, edit in place and other 
  basics that are taken for granted in normal desktop apps but which web apps can't do.
* Use any third party library, not just those able to work in the browser.
