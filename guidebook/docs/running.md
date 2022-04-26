# Running Conveyor

Conveyor is a build system. You give it a configuration file (`-f=conveyor.conf`) that declaratively defines what you want, and then run tasks. Tasks create files or file trees corresponding to various stages in the production of the packages, and these trees are stored in a disk cache (see below). Tasks run in parallel and at the end of the build the output directory (`--output-dir=output`) will contain a copy of whatever the requested task produced.

??? warning "Output overwrite modes"
    By default Conveyor will replace the contents of the output directory if that directory was created by Conveyor itself and the contents haven't been changed. If the output directory already exists but either wasn't created by Conveyor itself, or you changed something inside it, then the tool won't proceed.
    
    You can use `--overwrite-mode=HARD_REPLACE` to replace any files the build produced but leave other files alone, but be aware that this may result in old files hanging around in the output directory, and any read only files will cause an error.
    
    `--overwrite-mode=STOP` can be useful in scripts: it will prevent the tool from proceeding if the output directory already exists, even if it was created by Conveyor.

## Initial setup

The first time you use Conveyor you will need to run:

```
conveyor keys generate
```

optionally giving this command a `--passphrase`. See [Setting up](setting-up.md) for more information on this command and why it's necessary.

## Template projects

Conveyor has a simple project generation command that creates self-contained GUI projects complete with source code, build system and Conveyor configuration:

````sh
# Generate a JetPack Compose for Desktop app, or a JavaFX app.
conveyor generate {compose,javafx} \
                          --output-dir=path/to/my-project \
                          --site-url=https://mysite.com/downloads \
                          --rdns=com.example.myproject \
                          --display-name="My Amazing Project"
````

To learn more see the [tutorial](tutorial.md).

## Common tasks

**Build a download site for all available platforms in a directory called `output`:**

```bash
conveyor make site
```

**Adjust a configuration key for one build only:**

```bash
conveyor -Kapp.revision=2 make site
conveyor -Kapp.sign=false make site

# In Windows PowerShell you have to quote the argument like this:
conveyor "-Kapp.sign=false" make site
```

**Show all invokable task names, using a different config file to the default:**

```bash
conveyor make
```

Tasks labelled as "ambiguous" apply to more than one machine. You can run them by temporarily narrowing the machines your config supports by setting the `app.machines` key, e.g. by passing `-Kapp.machines=mac.amd64` on the command line.  The machines you can target are named using simple hierarchical identifiers that look like `mac.amd64` or `linux.aarch64.glibc`. You can pick the machines you wish to build for with the `app.machines` key. [Learn more](configs/index.md#machines).

**Render the config to JSON:**

```bash
conveyor json
```

**Create a Mac .app directory for Apple Silicon, an unnotarized zip of it, and a notarized zip for Intel CPUs:**

```bash
conveyor -Kapp.machines=mac.aarch64 make mac-app
conveyor -Kapp.machines=mac.aarch64 make unnotarized-mac-zip
conveyor -Kapp.machines=mac.amd64 make notarized-mac-zip
```

**Create a Windows app as a directory tree, a ZIP and an MSIX package:** 

```bash
conveyor make windows-app
conveyor make windows-zip
conveyor make windows-msix
```

This doesn't need you to set `app.machines` because currently only Intel/AMD64 targets are supported for Windows.

**Create a Linux JVM app as a directory tree, tarball and a Debian package:**

```bash
conveyor make linux-app
conveyor make linux-tarball
conveyor make debian-package
```

## Controlling parallelism

The `--parallelism` flag allows you to control how many tasks run simultaneously. It defaults to four, which works well enough for us. Be aware that setting this too high may not yield performance improvements, or may use too much memory. Experiment a bit and see what works best for you. 

## Viewing task dependencies

The `task-dependencies` command takes a task command-line name and prints all the dependencies. Try running `conveyor task-dependencies site` to see how the site is made up. Dimmed out tasks are being hidden either because they appeared elsewhere in the tree already (it's really a graph), or because the task is disabled for some reason, which will be explained next to the task's entry.

## The cache

Conveyor makes heavy use of caching. Tasks work in individual cached directories and the results are copied to the output directory at the end. The disk cache can be found here:

* **Windows:**  `%LOCALAPPDATA%\Hydraulic\Conveyor\Cache`
* **macOS:** `~/Library/Caches/Hydraulic/Conveyor`
* **Linux:** `~/.cache/hydraulic/conveyor/cache`

You can change this location using the `--cache-dir` flag and the maximum number of gigabytes it's allowed to consume using `--cache-limit`. It's safe to delete this directory whenever you like, or any of the individual sub-directories. Entries are stored under a hashed key, and an English description in Markdown of what's in each entry can be found in the `key` file within it, so it's easy to explore if you're curious. You can also find what cache keys are being used by viewing the logs.

If you hit a caching bug you can forcibly re-run tasks. You shouldn't need this unless you encounter a caching bug, but here it is anyway:

```bash
conveyor --rerun make linux-app
```

## Viewing logs

If anything goes wrong or you are just curious to see what was done, use the `--show-log` flag. On Windows, you get Notepad. On UNIX it will display the last execution's log file in a pager, highlighted and colored. By default lines aren't wrapped, so you can scroll left and right with the arrows. If you'd like to enable wrapping, perhaps to copy some long path or URL, type `-S` (that is, `-` followed by Shift-S). As always, you can press `q` to quit.

Logs are kept for more than just the last execution. At the top of each log file is the path where logs are kept. You can view log files by process ID in that directory.
