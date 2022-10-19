# Making packages

## 1. I need to express something that's awkward in HOCON

Write the config in any language you like and then use a hashbang include. For example, grab [yaml2json](https://github.com/bronze1man/yaml2json) or [jsonnet](https://jsonnet.org/) and then just put something like this in your `conveyor.conf`:

```
include "#!jsonnet conveyor.jsonnet"
```

Conveyor will run `jsonnet conveyor.jsonnet` and treat the output as if it was an included file. Because HOCON is a superset of JSON, anything that produces JSON can be used in this way.

## 2. Can I define URL handlers or file associations?

You can register a URL handler using some custom config, see the [operating system integration section](../configs/os-integration.md). File
type associations are a high priority feature that's already in development.

## 3. Why aren't I seeing icons show up?

Make sure you provide icon images at the full range of sizes. Conveyor won't currently rescale icons if you don't provide enough of them. In particular, note that the biggest icon size Windows supports is 256x256 so if you only provide an image larger than that, you won't get any icons.

## 4. How do I do a new version for every commit?

Something like:

```
app {
    // Get the number of commits in the current branch.
	include "#!=revision git rev-list --count --first-parent HEAD"
}
```

and then just trigger Conveyor from your CI system. This is an example of [a hashbang include](../configs/hocon-extensions.md#including-the-output-of-external-commands).

!!! important "Revision numbers vs commit hashes"
    Note that revision must be a number. You can't set it to be a commit hash. This is because package managers don't generally allow non-integer versions, and for Windows and macOS this is a hard rule.

## 5. How do I display a license agreement at install time?

You can't. User interactivity during installation is idiomatic on macOS, extremely discouraged on Linux and causes problems for network admins on Windows. Ask the user to agree in your app on first run instead.
