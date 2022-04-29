# General FAQ

## 1. What does Conveyor do?

It builds packages for desktop/CLI apps targeting Windows, macOS and Linux. These packages self update using the native package management
or Sparkle on macOS, and are fully signed/notarized. You can build these packages from any OS, meaning you can sign and notarize Mac
apps from Linux or Windows, and Windows apps from macOS or Linux. It can also create simple server packages for Debian/Ubuntu that use
systemd as a service manager.

## 2. Is Conveyor free?

It's free for open source projects, and during the early access period it's free for everyone else too. Once the early access period ends,
it will become a commercial product when used on proprietary apps.

## 3. Why isn't Conveyor a service?

Services can sometimes be convenient and if you'd like to have Conveyor-as-a-service, please [let us know](mailto:contact@hydraulic.software). 
Also, if you work on a code hosting service/forge of some kind and you'd like to incorporate Conveyor's features into your site,
then get in touch and ask for a reseller license.

Conveyor isn't designed as a service because, like many, we feel that the service-ification of the software industry has swung a bit too far. 
Many programs are now offered exclusively as hosted services when it's not technically justifiable, and even when it makes things worse for
users. Being a local tool means you get:

* Ultra-fast turnaround times on rebuilds, so you can quickly experiment and polish your package.
* No outages - a tool is as available as your own hardware.
* Freedom and privacy. We don't know what software you build with Conveyor, and that's as it should be! In an era of sudden and arbitrary 
  cancellations, a tool is something you can rely on.
* Absolute control. When you buy the Unlimited Edition you get the source code and can easily fork it. Good luck forking a service!

It has advantages for us too:

* Natural and effortless scaling, because you're using your own hardware so there are no bottlenecks.
* We can't accidentally flip the wrong switch and create an outage. Corollary: no need to be on-call, yielding big savings that we can then
  pass on to you.
* No need to hold people's private keys, which would create a tempting target for attackers.

## 4. Is Conveyor only for JVM apps?

Currently yes, but that won't be true for long as there are no fundamental reasons for that limitation. In theory you can package any kind of app, Conveyor just happens to have great support for the JVM. It makes sense to integrate support for
the JVM because a cross-platform runtime is when the ability to create packages for other operating systems really shines. You don't need to
mess with cross-compilers or running virtualized operating systems - just develop your app on your preferred platform and then ship to
everyone with a single command.

## 5. Is Conveyor only for desktop apps?

It also has great support for:

1. Deeply system integrated servers. See the [TeamCity sample](../samples/teamcity.md) for an example that demonstrates systemd integration,
   Apache 2 integration and adding a dependency on Postgres.
2. Command line apps. Conveyor is packaged with itself, in fact.

## 6. Does it support Electron or native apps?

It should do, but we don't have any samples or integrated support yet. Working on it!

## 7. Why should I make a desktop app?  

Desktops are a fantastically important and critical part of our infrastructure - they're the beating heart of our economy where work gets
done and things get made. Flexible, comfy, ergonomic and incredibly powerful, the machines sitting in front of us are truly magnificent
marvels of engineering. Yet the web platform barely exploits them, and its roots in hypertext often shine through: HTML5 can struggle to
serve the needs of productivity users. As developers we like it partly because it makes deployment and updates easy, but now with Conveyor
we can push updates to desktop and CLI apps just as easily as we push changes to a web site. It's time to revisit the advantages of being
outside the browser:

* Use the best language for the job, not just JavaScript and watered down language dialects that transpile to it.
* Multi-threading! Saturate every core the user has and deliver them results with the lowest latency possible. The multi-core revolution  
  never made it to the web, but that hardware is waiting for you and your users.
* Direct access to the newest, most powerful and most interesting hardware, without interference from the sandbox.
* Empower your users with control over their own data by storing it in files. 
    * Let people organize, back up, share, store and work with their data in multiple apps at once.
    * Eliminate a whole swathe of awkward privacy problems and regulations.
* Give your users sophisticated features like re-arrangeable windows, complex keyboard shortcuts, hot screen edges and plugins.
* Be always available even in the most challenging conditions. Users will learn to rely on your app everywhere, at any time, whether
  that's in the middle of a storm on an oil rig, on holiday in the middle of the savannah, during a business trip with spotty connectivity  
  or during a delicate hospital operation where a server outage could be a matter of life and death.

If you're interested in these ideas, check out the essay ["Our Glorious Post Web Future"](https://www.hydraulic.software/blog/2-our-glorious-post-web-future.html)
on the Hydraulic blog. We plan to write more about reinvigorating the desktop there, and hopefully organize a podcast with other people doing
interesting work in the post-web space.
