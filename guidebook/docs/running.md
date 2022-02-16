# Running Conveyor

Conveyor is a kind of build system. You give it a configuration file (`-f=conveyor.conf`) describing what to build and then run tasks. Tasks create files or file trees, which are stored in a cache directory (see below). Tasks run in parallel and at the end of the build the output directory (`--output-dir=output`) will contain whatever the task produced.

??? warning "Output overwrite modes"
    By default Conveyor will **replace** the contents of the output directory, removing any other files inside it. If the output directory already exists but wasn't created by Conveyor itself then it will refuse to proceed until either a different output directory is given, or the pre-existing path is moved out of the way. 
    
    You can use `--overwrite-mode=OVERWRITE` to replace any files the build produced but leave other files alone, but be aware that this may result in old files hanging around in the output directory, and any read only files will cause an error. 
    
    `--overwrite-mode=STOP` can be useful in scripts: it will prevent the tool from proceeding if the output directory already exists.

## Common tasks

Build a download site plus packages for all available platforms in a directory called `output`.

```bash
conveyor make site
```

Build the app with an adjusted configuration key.

```bash
conveyor -Kapp.revision=2 make site
conveyor -Kapp.sign=false make site
# In Windows PowerShell you have to quote the argument like this:
conveyor "-Kapp.sign=false" make site
```

Show all available tasks for the app defined in a different file.

```bash
conveyor -f myapp.conveyor.conf
```

Render the config to raw JSON.

```bash
conveyor json
```

Create a Mac bundle from a JVM app matching the CPU of the host, an unnotarized zip of it, and 
a notarized version:

```bash
conveyor make mac-app
conveyor make unnotarized-mac-zip
conveyor make notarized-mac-zip
```

Create an Intel Mac app or ARM Mac app specifically:

```
conveyor -m mac.amd64 make mac-app
conveyor -m mac.aarch64 make mac-app
```

Create a Windows JVM app as a directory tree, a ZIP and an MSIX package.

```bash
conveyor make windows-app
conveyor make windows-zip
conveyor make windows-msix
```

Create a Linux JVM app as a directory tree, tarball and a Debian package.

```bash
conveyor make linux-app
conveyor make linux-tarball
conveyor make debian-package
```

Show a dependency tree explaining what tasks will or will not be run.

```bash
conveyor task-dependencies site
```

View logs of the last execution.

```bash
conveyor --show-log
```

Forcibly re-run all tasks, those that don't download inputs, and  just the given task along with any that depended on the changed output.

```bash
conveyor --rerun=all make mac-app
conveyor --rerun=local make mac-app
conveyor --rerun=mac-app make mac-app
```

**Tasks.** A list of available tasks can be listed by just not specifying a task to the `make` command. The selected tasks will generate files or directory trees and put them in the `--output` directory. Intermediate work is cached, so running it again will be instant as the same outputs from last time will be reused. Use `--rerun` if for some reason you need to force a re-execution.

**Configs.** The tool config file can be specified with the `-c` flag or will be taken from `conveyor.conf` in the same directory. The `-K` flag can be used to add/override keys in the configuration.

!!! tip
    The -K flag can be useful during development for turning off features, e.g. try `-Kapp.sign=false` to disable signing temporarily.

**Machines.** Each native target is identified by an identifier formatted like `$operating_system.$cpu[.$libc]`, for example `mac.aarch64` (Apple Silicon Macs), or `windows.amd64` (Intel Windows machines), or `linux.amd64.glibc` for a typical Linux box. When using the `site` task the machines to use are taken from the `app.machines` config key. When using other tasks you can pick the machine you wish to build tasks for by passing it using the `--machine` or `-m` flag. 

## Controlling parallelism

The `--parallelism` flag allows you to control how many tasks run simultaneously. Be aware that setting this too high may not yield performance improvements, or may use too much memory. Experiment a bit and see what works best for you. 

## Viewing task dependencies

The `task-dependencies` command takes a task command-line name and prints all the dependencies. Try running `conveyor task-dependencies site` to see how the site is made up. Dimmed out tasks are being hidden either because they appeared elsewhere in the tree already (it's really a graph), or because the task is disabled for some reason, which will be explained next to the task's entry.

## Running tasks by name

Normally the `site` task is all you need, but sometimes you might just want individual packages or components, either for direct usage or to understand what is going on behind the scenes. Use the `conveyor make` command on its own to see the list of tasks you can run by name, and try specifying a machine using the `-m` flag, e.g.  `conveyor -m mac.amd64 make` to view additional tasks that would otherwise be ambiguous because they're target-able to multiple different machines. 

Note that not every task you see in the `task-dependencies` tree can actually be invoked from the command line. The task tree may show you additional internal tasks that are parameterized in ways not supported by the CLI. Additionally the tree view uses *display names* which can contain additional information about what tasks will do, but the make command takes _command line names_ and flags in order to be unambiguous. In most cases the command line name is just a lowercased version of the display name with dashes added.

??? warning "CLI stability"
    The command line interface of the tool may be changed in future, but we'll try to ensure old scripts continue to work.

## The cache

Conveyor makes heavy use of caching. Tasks work in individual cached directories and the results are copied to the output directory at the end. The disk cache can be found here:

* **Windows:**  `%LOCALAPPDATA%\Hydraulic\Conveyor\Cache`
* **macOS:** `~/Library/Caches/Hydraulic/Conveyor`
* **Linux:** `~/.cache/hydraulic/conveyor/cache`

You can change this location using the `--cache-dir` flag and the maximum number of gigabytes it's allowed to consume using `--cache-limit`. It's safe to delete this directory whenever you like, or any of the individual sub-directories. Entries are stored under a hashed key, and an English description in Markdown of what's in each entry can be found in the `key` file within it, so it's easy to explore if you're curious. You can also find what cache keys are being used by viewing the logs.

## Viewing logs

If anything goes wrong or you are just curious to see what was done, use the `--show-log` flag. On Windows, you get Notepad. On UNIX it will display the last execution's log file in a pager, highlighted and colored. By default lines aren't wrapped, so you can scroll left and right with the arrows. If you'd like to enable wrapping, perhaps to copy some long path or URL, type `-S` (that is, `-` followed by Shift-S). As always, you can press `q` to quit.

Logs are kept for more than just the last execution. At the top of each log file is the path where logs are kept. You can view log files by process ID in that directory.
