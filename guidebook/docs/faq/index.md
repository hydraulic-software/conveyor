# General FAQ

## 1. What does Conveyor do?

It builds packages for desktop/CLI apps targeting Windows, macOS and Linux. These packages self update using the native package management
or Sparkle on macOS, and are fully signed/notarized. You can build these packages from any OS, meaning you can sign and notarize Mac
apps from Linux or Windows, and Windows apps from macOS or Linux. It can also create simple server packages for Debian/Ubuntu that use
systemd as a service manager.

## 2. Is Conveyor free?

During the introductory period it's free for everyone. Once the introductory period ends, it will be free for open source apps and
require a subscription when used with proprietary apps. You can learn what it'll cost on the [pricing page](https://www.hydraulic.software/pricing.html).

Open source apps that use it should advertise the fact that they're packaged with Conveyor somewhere and link to the website. Using the generated download page 
makes this easy, because it contains a "Packaged with Conveyor" badge. If you don't use that HTML then you'll need to provide your own link
in e.g. your download page, website, README file etc.

## 3. Why isn't Conveyor a service?

Services can sometimes be convenient and if you'd like to have Conveyor-as-a-service, please [let us know](mailto:contact@hydraulic.software). 
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

## 4. What kinds of app can you package with it?

It can package any kind of app, it just has some built-in features to make packaging JVM apps easy. The [tutorial](../tutorial/1-get-started.md) 
shows how to package a CMake based C++ OpenGL app, for example.

Although you can package any kind of app, over time Conveyor will gain features to apps using specific frameworks easier to package.

## 5. Is Conveyor only for desktop apps?

Not at all! It also has great support for:

1. Servers that run outside of containers. See the [package a server](../tasks/server.md) task page for an example that demonstrates systemd 
   integration, Apache 2/nginx reverse proxy integration and adding a dependency on Postgres.
2. Command line apps. Conveyor is packaged with itself, in fact.

## 6. Does it support Electron apps?

Working on it! The Electron Mac launcher doesn't currently work with Conveyor's code injection. Support should arrive soon - [follow this issue on GitHub](https://github.com/hydraulic-software/conveyor/issues/6) to know when it launches.

## 7. Why should I make a desktop app?  

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
