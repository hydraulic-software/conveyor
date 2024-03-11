# Inputs

Inputs define a set of files that will be used to assemble the final package. Inputs can be:

* Files, including [globs](https://en.wikipedia.org/wiki/Glob_(programming)).
* Directories.
* HTTP(S) URLs.

Zips or tarball inputs will be extracted by default.

## Where inputs are placed

Inputs can be specified at different places in the config, but the most important is the top level `app` input hierarchy. The final location of files here depends on the type of app:

* For native apps, inputs make up the raw package contents:
    * The contents of the program installation directory on Windows.
    * The contents of the `Contents` directory inside the bundle on macOS.
    * The contents of the `/usr/lib/$vendor/$fsname` directory on Linux.
* For JVM apps inputs should contain your app JARs, any other data files. They will be placed in:
    * The `app` sub-directory of your install directory on Windows.
    * The `Resources` directory of your app bundle on macOS.
    * The `/usr/lib/$vendor/$fsname/lib/app` directory on Linux.
* For Electron apps, inputs should contain your app JS/HTML/CSS/package.json files. They will be placed in the standard locations:
    * The `resources\app` directory inside your install directory on Windows.
    * The `Contents/Resources/app` directory inside the bundle on macOS.
    * The `/usr/lib/$vendor/$fsname` directory on Linux.

For JVM apps any native libraries added in the app inputs (or found in the JARs) will be moved next to the other shared libraries of the JVM (requires compatibility level >= 4).

## Synopsis

```properties
# Add a file or directory for every platform. They will be placed into 
# the working directory at the same location. Globs are supported.
app.inputs += path/to/a/file
app.inputs += path/to/a/directory
app.inputs += /opt/foo/bar/*.sh

# You can change where the file is placed.
app.inputs += a/file -> other-dir/file

# That's shorthand for this longer form:
app.inputs += { 
  from = a/file
  to = other-dir/file
}

# URLs are downloaded. The https:// prefix is implied if missing.
app.inputs += foobar.com/some-file
app.inputs += "data:IyEvYmluL2Jhc2gKZWNobyBTYW1wbGUgc2NyaXB0Lg== -> sample-script.sh"

# You can specify http headers for URL inputs.
app.inputs += {
  from = foobar.com/path/to/file
  http-headers {
    # Environment variables get expanded automatically. 
    Authentication: Basic ${env.AUTH_TOKEN}
  }
}

# Raw content can be embedded in the config. It will be de-indented for you.
app.inputs += {
  content = """
    #!/bin/sh
    echo Hello world!
  """
  to = bin/sample.sh
}

# You can specify that some inputs are optional.
app.inputs += { 
  from = an/optional/file
  optional = true  
}

# Archives are extracted by default (but not JARs).
#
# Any input spec may use brace expansion with nesting. {} are special in HOCON so you
# must quote the string.
#
# If the files in an archive are under a single root directory, the contents 
# of that dir are extracted instead of the root.
app.inputs += "files/vendor-{core,utils,extras{,-2}}-1.2.zip -> vendor-stuff"
app.inputs += foobar.tar.{gz,bz2,xz,Z,ztd}

# Archives can have content be remapped or dropped during extraction.
app.inputs += {
  from = foobar.zip
  remap = [
    "**"                       # Include everything.
    "samples/*.conf -> conf"   # But move files ending in .conf to the conf 
                               # directory (and subdirs as appropriate).
    "-**/*.unwanted"           # Don't extract files ending in .unwanted, anywhere in the tree
    "-.git/**"                 # or anything under .git
  ]
}

# OS / CPU / libc specific inputs, see below for full list of paths you can use.
app.linux.aarch64.glibc.inputs += linux-arm-natives.tar.gz -> lib

# Add a JDK and it will be jlinked and used as the application runtime.
# You must use the OS/CPU/libc specific inputs for this as a JVM is inherently platform specific.
app.jdk.linux.amd64.inputs += example.com/jdk-linux-17.tar.gz
```

**`app.inputs`** An array of input definitions (see below). Each input is copied or extracted into the working directory one after the other, with later inputs overwriting files from previous inputs. You can add a new input to the end of the array by using the `+=` HOCON operator. You can append an object, but when you append a string specification it is parsed and treated as shorthand for an object with `from` and `to` fields, in which the `from` field is the URL/path of the input. Brace expansion is applied to the string form to create multiple inputs.

Certain keys are derived from the name of the first input if not specified, which works well if you use the convention that the first input contains the core software of the app (i.e. not a dependency).

Paths should always use the UNIX path separator (`/`) and are interpreted relative to the location of the config file containing the input definition (i.e. if you include a file that specifies inputs, relative paths are relative to the *included* file, not the *including* file).

## Object syntax

The string syntax is shorthand for the object syntax, they can always be treated identically. Objects may consist of these keys:

**`from`** A string interpreted as either a file/directory if it exists relative to the config file, or a URL if the file isn't found
missing. An https prefix is optional. The string is brace expanded and may contain glob characters when it refers to a path on the file
system, thus a single specified input may be expanded to multiple actual inputs that share the same `to` field.

You can also specify a path inside a zip using this syntax:

```
app.inputs += "zip:example.zip!/content.txt"
app.inputs += "zip:https://example.com/path/to/file.zip!/path/in/zip/content.txt"
```

In the second case the `content.txt` file will be downloaded _without_ downloading the rest of the zip. This is possible because zip files
are seekable.

**`content`** A string that will be placed in the given destination file. It's de-indented for you, so you can place the content at the
right offset to look good in the config. On UNIX if it starts with a shebang line it will be marked executable automatically. The
HOCON `"""` multi-line string literal syntax is useful here but remember that if you want to include HOCON substitutions, you have to do
that _outside_ the string, so you'll need to write something like this:

```
my-var = "Substitution example"
app.inputs += {
  to = "example.txt"  
  content = """     
     Content: ["""${my-var}"""]
  """
}
```

**`to`** The location in the staging area where the input should be placed or extracted to. By default this is the root.

**`remap`** A list or multi-line string of remap rules. See below for details. If not specified the default is `[ ** ]` which means "copy
everything to the same location" in the staging area.

**`extract`** The number of archives (within archives) to extract. If this is set to 1 (or true), the input is assumed to be an archive 
and extraction of the contents will occur to whatever the `to` destination path is. If zero or false, the input won't be extracted. If more
than one then the archive is assumed to contain another archive and that will in turn be extracted - this can be useful when retrieving
artifacts from GitHub Actions which insists on wrapping everything in a zip, but can't preserve UNIX permissions or symlinks properly. This
can necessitate putting a tarball inside the zip. If not set then a heuristic is used: it will be extracted if the input is a zip or 
tarball, otherwise it won't be (i.e. file formats based on zip like JARs won't be extracted).

**`http-headers`** A map of key-value pairs that contains additional HTTP headers to be sent when downloading a URL.

**`optional`** A boolean that specifies whether the input is optional. Conveyor will normally throw an error if inputs are missing, but for
inputs marked as optional a warning is shown and execution proceeds.

## Remap rules

Remap rules allow you to selectively drop, rename or move files as they are being copied. They can be used both when using a directory as an
input, and also when extracting a zip or tarball.

When an input has a list of remap rules every file being copied or extracted is tested against each item in the list in order. A rule
defines a set of files that match and optionally, a location in the destination to place them. At least one rule must match for the file to
be included, which means if you specify your own remap list then it will by default match nothing. Each rule is written as:

`[-]path/to/files/** [-> some/location]`

If no location is given, it's the same as the location of the matched file. Files can be excluded by prefixing a more specific rule with `-`.

**Precedence.** The rule that applies is the most specific, defined as the rule for which the pattern part matches the least. The *pattern* is the file path part of the rule, and the match is the part from the first glob. For example given `foo/bar/**` tested against `foo/bar/baz.txt` the pattern matched part is `baz.txt`. As a consequence, `**` is the least specific rule (because it matches everything) and can be overridden by any other. In case of ties, the last rule in the list is used.

A tricky case is when you have a rule which starts with a glob. Because the pattern matched part is defined as the part of the string following the first pattern, the rules:

```
-*/foo/*.bar
**
```

will simply include everything i.e. the first rule will be ignored, because both rules are considered to pattern match the whole path of every file giving them equal precedence. Because in a tie the last rule wins you can invert the ordering to fix it:

```
**
-*/foo/*.bar
```

The files ending in `.bar` in the `foo` directory will now be correctly excluded.

!!! important
    Remap rules **are not prefixes:** they are tested against _files_, not directories. Therefore specifying a directory as a source won't work, you _must_ use globs to do that. For example `lib` won't copy anything out of the lib directory, but `lib/**` will. For the same reason, a rule such as `-foo/` to drop a directory won't work, but `-foo/**` will.

**Root directories.** When extracting a zip that has exactly one root directory entry the remap rules are applied to the files *within* that directory, but when extracting a tarball the fact that there's a single root is only known at the end of the extraction process, so the rules then are applied to the root of the tarball, thus a single root directory must be taken into account in the remap rules.

## Primary input

If you don't specify all needed fields the file name of the first input will be parsed and some missing values derived from it. Each one of these can be overridden if you don't like the derived values. The rules are:

1. The file extension is dropped.
2. The file name is split *on* `-` and `_` characters. The first component that starts with a number starts the version, which is placed in **`app.version`**. The name before the version is assigned to **`app.fsname`** and is used as the base for anywhere a name is needed on the filesystem. It is conventionally all lower case and uses `-` to split words.

## Machine specific inputs

Conveyor calls the combination of an operating system, CPU architecture and (for Linux) libc a *machine*. A machine is identified by a dot separated triple `os.cpu[.libc]`. You can specify which machines to build packages for using the `app.machines` key. A machine name is made of:

* OS: Can be `linux`, `windows` or `mac`. Some common alternative names are also supported (e.g. `win`, `osx`, `macos` and `darwin`)
* CPU: Can be `amd64` or `aarch64`. Again some common alternatives are supported.
* LibC: (Linux only) Can be `glibc` or `muslc` and again, alternatives are recognized.

When an app is built the actual list of inputs to use comes from the `app[.jdk].$machine.inputs` paths, so for example when making a standard package for Linux the inputs will be taken from `app.linux.amd64.glibc.inputs`. The default configuration aliases each set of inputs along the hierarchy, meaning any files you add further up will be propagated. This lets you put data files used by any type of machine in `app.inputs`, data files used only on macOS in `app.macos.inputs` and binaries compiled specifically for Apple's M1 chip in `app.macos.aarch64.inputs`.

## Symlink handling

All platforms Conveyor supports implement symlinks, but on Windows they are called junction points and cannot point to directories. Packages need to be "hermetic", meaning that there are no symlinks inside them that point outside them. On Linux nothing enforces this and in some cases it can be reasonable to include such symlinks, but Apple treats such symlinks as a security violation. For this reason when files, directories or archives are being copied into an input staging area, symlinks are followed unless they point inside the directory tree or archive. The rules are:

* If an input is a symlink it's followed.
* If an input is a directory it's copied recursively, or if it's an archive it's extracted, and all symlinks within:
    * That point to a path outside that directory are followed, with the symlink being replaced by the file or directory tree it points to.
    * That point to a path inside that directory are left as is and will be packaged as symlinks.
