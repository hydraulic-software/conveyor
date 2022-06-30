# Conveyor Extensions

Conveyor adds the following features to base HOCON: 
    

1. Per-user defaults loaded from the home directory.
1. Smart string lists.
2. Including output from external commands.
3. Access to environment variables.

## Per-user defaults

Config placed in the following paths will be merged into every build file:

* **Windows:** `%USERPROFILE%\Hydraulic\Conveyor\defaults.conf`
* **Linux:** `~/.config/hydraulic/conveyor/defaults.conf`
* **macOS:** `~/Library/Preferences/Hydraulic/Conveyor/defaults.conf`

## Smart string lists

Any key that requires a list of strings, files or URLs will have brace expansion applied to it recursively. For example:

```
a = [ thing, "foo-{{,extra-}utils,core}" ]

is equivalent to 

a = [ thing, foo-utils, foo-extra-utils, foo-core ]
```

Additionally it's valid in Conveyor to set a string list property to be just a string - it'll be interpreted as a list containing that single element.

## Including the output of external commands

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

## Environment variables

Environment variables can be accessed by writing `${env.FOO}`. The $HOME environment variable is available also on Windows even though it's not normally set, which can be convenient for pointing to files you don't want to store with your project.

```
app.mac.signing-key = ${env.HOME}/keys/apple.p12
```

This happens because Conveyor is packaged with itself and therefore benefits from the custom launcher.
