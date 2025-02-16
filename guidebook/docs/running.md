# Running Conveyor

Conveyor is a build system. You give it a configuration file (`-f=conveyor.conf`) that declaratively defines what you want, and then run tasks. Tasks create files or file trees corresponding to various stages in the production of the packages, and these trees are stored in a disk cache (see below). Tasks run in parallel and at the end of the build the output directory (`--output-dir=output`) will contain a copy of whatever the requested task produced.

??? warning "Output overwrite modes"
    By default Conveyor will replace the contents of the output directory if that directory was created by Conveyor itself and the contents haven't been changed. If the output directory already exists but either wasn't created by Conveyor itself, or you changed something inside it, then the tool won't proceed.
    
    You can use `--overwrite-mode=HARD_REPLACE` to replace any files the build produced but leave other files alone, but be aware that this may result in old files hanging around in the output directory, and any read only files will cause an error.
    
    `--overwrite-mode=STOP` can be useful in scripts: it will prevent the tool from proceeding if the output directory already exists, even if it was created by Conveyor.

## Initial setup and default config

The first time you use Conveyor you'll be asked to set a passphrase on a newly generated "root key". This is used to derive any keys you don't explicitly provide. It's written to your per-user defaults file, which can be found here:

* **Windows:** `%USERPROFILE%\Hydraulic\Conveyor\defaults.conf`
* **Linux:** `~/.config/hydraulic/conveyor/defaults.conf`
* **macOS:** `~/Library/Preferences/Hydraulic/Conveyor/defaults.conf`

Config placed in those paths will be merged into every build file. There's nothing special about the signing related config options - anything can be put here.

## Template projects

Conveyor has a command that creates GUI projects with Conveyor configuration:

```sh
# Generate an Electron, C++, Jetpack Compose or JavaFX app.
conveyor generate {electron,cmake,compose,javafx} \
                          --output-dir=path/to/my-project \
                          --display-name="My Amazing Project" \
                          com.example.my-project
```

To learn more see the [tutorial](tutorial/new.md).

!!! tip "GitHub projects"
    If you pick a reverse DNS name of the form `io.github.your_username.your_repo_name` then the project will be set up for releasing 
    with GitHub Releases and putting the download page on GitHub Sites. 

## Common tasks

**Create a packaged app directory/bundle and execute your program from it:**

```bash
conveyor run

# Run without progress output
conveyor --silent run
```

**Do the same but faster:**

```bash
conveyor -Kapp.sign=false run
```

**Put that app directory into `output`**

```bash
conveyor make app
```

**Build a download site for all available platforms in a directory called `output`:**

```bash
conveyor make site
```

**Adjust a configuration key for one build only:**

```bash
conveyor -Kapp.revision=2 make site
conveyor -Kapp.sign=false make site

# In Windows PowerShell you have to quote the argument:
conveyor "-Kapp.sign=false" make site

# You can specify lists:
conveyor "-Kapp.inputs=[a,b,c]"
```

**Show all invokable task names, using a different config file to the default:**

```bash
conveyor make
```

Tasks labelled as "ambiguous" apply to more than one machine. You can run them by temporarily narrowing the machines your config supports by setting the `app.machines` key, e.g. by passing `-Kapp.machines=mac.amd64` on the command line.  The machines you can target are named using simple hierarchical identifiers that look like `mac.amd64` or `linux.aarch64.glibc`. You can pick the machines you wish to build for with the `app.machines` key. [Learn more](configs/index.md#target-platforms).

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

!!! note "Memory usage"
    If using a VM or container you should allocate at least 4GB of RAM. With less Conveyor may stall or trigger the kernel out-of-memory killer.

## Viewing task dependencies

The `task-dependencies` command takes a task command-line name and prints all the dependencies. Try running `conveyor task-dependencies site` to see how the site is made up. Dimmed out tasks are being hidden either because they appeared elsewhere in the tree already (it's really a graph), or because the task is disabled for some reason, which will be explained next to the task's entry.

## The cache

Conveyor makes heavy use of caching to enable fast iteration, both locally and in CI. Tasks work in individual cached directories and the results are copied to the output directory at the end. The disk cache can be found here:

* **Windows:**  `%LOCALAPPDATA%\Hydraulic\Conveyor\Cache`
* **macOS:** `~/Library/Caches/Hydraulic/Conveyor`
* **Linux:** `~/.cache/hydraulic/conveyor/cache`

You can change this location using the `--cache-dir` flag and the maximum number of gigabytes it's allowed to consume using `--cache-limit`. It's safe to delete this directory whenever you like, or any of the individual subdirectories. Entries are stored under a hashed key, and an English description in Markdown of what's in each entry can be found in the `key` file within it, so it's easy to explore if you're curious. You can also find what cache keys are being used by viewing the logs.

To view the contents of the cache, you can use the `print-cache-entries` command:

```bash
conveyor print-cache-entries
```

This command displays a table with information about each cache entry, including the hashed key, the first line of the actual key, size, build time, entry cost, and age. You can customize the output with the following options:

- `--top` or `-t`: Limit the output to the top N entries (e.g., `--top 10`)
- `--sort-by` or `-s`: Sort the entries by a specific column (key, size, buildTime, entryCost, or age)

For example, to view the top 5 entries sorted by size:

```bash
conveyor print-cache-entries --top 5 --sort-by size
```

The "Entry Cost" column is an arbitrary value reflecting a combination of age, size and how long it took to create the entry. It's used to bias the cache towards erasing entries that take up a lot of space but which were quick to create and weren't accessed for a long time. You can also view the contents of specific cache entries by providing their hashed keys:

```bash
conveyor print-cache-entries <hashed_key1> <hashed_key2>
```

This will display the full content of the specified cache entries in markdown format.

If you hit a caching bug you can forcibly re-run tasks. If you find you need this command, please let us know as there are no known correctness issues with the cache implementation:

```bash
conveyor make linux-app --rerun
```

There's no way to clear the cache from the CLI. You can just delete the cache directory yourself if you want to free up the space it uses.

If you want to understand why some tasks are missing in the cache, set `LOG_CACHE_MISS_DIFFS=true` in the environment, run a build and then run `conveyor --show-log` to view the results.

## Viewing logs

If anything goes wrong or you are just curious to see what was done, use the `--show-log` flag. On Windows, you get Notepad. On UNIX it will display the last execution's log file in a pager, highlighted and colored. By default lines aren't wrapped, so you can scroll left and right with the arrows. If you'd like to enable wrapping, perhaps to copy some long path or URL, type `-S` (that is, `-` followed by Shift-S). As always, you can press `q` to quit.

Logs are kept for more than just the last execution. At the top of each log file is the path where logs are kept. You can view log files by process ID in that directory.
