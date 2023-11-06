# HOCON

The _Human Optimized Config Object Notation_ is syntax sugar on top of JSON that makes it more convenient to write config files. Conveyor
configs are written in an extended form of HOCON.

Let's learn HOCON by tidying up a simple JSON document.

{!configs/hocon-tutorial.html!}

## Seeing the transform

You can run `conveyor json` to see the JSON equivalent of your config, including the default configuration that's been merged in. Secrets
will be redacted from this output so it's safe to share.

## Tips

There are a few common mistakes that people sometimes make when writing HOCON:

1. **String concatenation.** Although there is a `+=` operator, there's no `+` operator. Strings are concatenated directly. To add a word to the end of a variable therefore you can write `${foo} bar`, or with quoting `${foo}" bar"`, but not `${foo} + " bar"` as you might expect from other languages.
2. **Object merge vs replacement.** Writing `a = { b = "c" }` is different to `a { b = "c" }`. The former _replaces_ the value of `a` with a new object consisting of a single key. The latter _extends_ the value of `a` by merging with it, thus preserving any keys in the pre-existing `a` object.
3. **Interpolation does not take effect inside quoted strings.** You can't write this: `a = "foo bar ${key} baz"` as that will interpret `${key}` literally. You have to close the quoting, interpolate, then re-open the quoting: `a = "foo bar "${key}" baz"`. 

## Editor plugins

* [JetBrains IDEs](https://plugins.jetbrains.com/plugin/10481-hocon)
* [Visual Studio Code](https://marketplace.visualstudio.com/items?itemName=sabieber.HOCON)
* [Sublime Text](https://packagecontrol.io/packages/HOCON%20Syntax%20Highlighting)

## Spec

[HOCON has a formal specification](hocon-spec.md) which describes its base features and rules in a more rigorous way.

## Extending the defaults

!!! tip "Lists"
    It's good style to always append to lists rather than overwriting them. If you assign directly, importing other configs won't work as you'll overwrite the values they place in shared lists. In other words write `key += value` rather than `key = [ value ]`. The first form adds to whatever `key` currently holds, the second replaces it.

## Conveyor Extensions

Conveyor adds the following features to base HOCON:

1. Smart string lists.
2. Including output from external commands.
3. Access to environment variables.
4. A temporary scratch object.

### Smart string lists

Any key that requires a list of strings, files or URLs will have brace expansion applied to it recursively. For example:

```
a = [ thing, "foo-{{,extra-}utils,core}" ]
```

is equivalent to 

```
a = [ thing, foo-utils, foo-extra-utils, foo-core ]
```

Additionally it's valid in Conveyor to set a string list property to be just a string - it'll be interpreted as a list containing that single element.

### Including the output of external commands

You can generate config dynamically by using a hashbang include statement paired with an external program, like this:

```
// Assume my-external-program produces JSON or HOCON, include it.
include "#!my-external-program --flag 'an argument with spaces.txt'"

// Set the foobar key to the stdout of my-external-program.
include "#!=foobar my-external-program"

// You can assign each line of output to a list by adding [] to the key name.
include "#!=foobar[] my-external-program" 

// Setting environment variables also works.
include "#!ENV_VAR=VALUE my-external-program"
```

The command line will be evaluated in the same way on all operating systems including Windows, so if you want this to be portable you may want to ensure there's both a `.bat` version of your command as well as an extension-less UNIX version.

### Environment variables

Environment variables can be accessed by writing `${env.FOO}`.

```
app.mac.signing-key = ${env.HOME}/keys/apple.p12
```

The $HOME environment variable is available also on Windows even though it's not normally set, which can be convenient for pointing to files you don't want to store with your project. This happens because Conveyor is packaged with itself and therefore benefits from [the custom launcher](jvm.md#launcher-features).

### Temporary object

The top level `temp` object is deleted before config is printed out by the `json` command. It's useful to put keys here that have no real meaning and are only intended for concatenation with other keys.
