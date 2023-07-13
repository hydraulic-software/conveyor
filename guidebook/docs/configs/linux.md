# Linux

## Synopsis

```
# Set the prefix. App files will be placed in /usr/local/lib/${app.long-fsname-dir}
# and a symlink to the launcher in /usr/local/bin
app.linux.prefix = /usr/local

# Set the location where app files will be placed, overriding the prefix.
app.linux.install-path = /opt/myapp

# Place files elsewhere in the filing system.
app.linux.root-inputs += extra.conf -> /etc/extra.conf

# Change the menu categories, or any other key in the .desktop file.
app.linux.desktop-file."Desktop Entry".Categories = "GTK;Gnome;Game"

# Add a default SystemD service file which runs the app as a non-root dynamic user.
# The key "server" is an arbitrary name here - you can define as many unit files as
# you like as long as they have unique file names.
app.linux.services.server = {
  include "/stdlib/linux/service.conf"
}

# Customize the name of the service and contents of the file:
app.linux.services.server {
  include "/stdlib/linux/service.conf"

  file-name = whateverd.service
  Service {
    ExecStop = ${app.linux.install-path}/bin/my-server-script stop
  }
}

# Add a service and append two command line arguments, one of which has a space in it.
app.linux.services.server {
  include "/stdlib/linux/service.conf"

  Service {
    ExecStart = ${app.linux.services.server.Service.ExecStart} --server --message "\"Hello World\""
  }
}

# Generate sample nginx and Apache site configs.
app.server.http.port = 12345

# Disable generation of nginx config leaving only Apache.
app.server.http.nginx = null

# Add an extra Debian dependency.
app.linux.debian.control.Depends = "postgresql (>= 12)"

# Configure the target distribution.
app.linux.debian.distribution {
  name = "jammy"
  mirrors = ["http://archive.ubuntu.com/ubuntu/"]  
}
```

## Keys

These keys under `app.linux` control Linux specific packaging aspects.

**`prefix`** The part of the directory hierarchy to install under. Defaults to `/usr`. 

!!! warning 
    Anything _other_ than `/usr` will break various kinds of integrations due to Linux desktop environments and system software not reliably supporting other prefixes. Some things will work and others won't if you change this.

**`install-path`** A directory where the input files will be placed. Defaults to `${app.linux.prefix}/lib/${app.long-fs-dir}` so (by default) the name is dependent on whether there's a vendor or not. If no vendor is specified it'll be something like `/usr/lib/fooapp`. Other defaults are all relative to this. The directory should be empty rather than a directory the user will already have.

**`root-inputs`** An inputs list that allows files to be placed relative to `/` instead of the prefix or install path. Also available: `app.linux.{amd64,aarch64}.{glibc,muslc}.root-inputs`. When adding files to this list you should always set the destination location, otherwise you'll end up with files being added to the root directory. Example:

```
app {
    linux {
        root-inputs += packaging/firewall-rules.txt -> /etc/ufw/applications.d/${app.fsname}
    }
}
```

**`conf-dir`** Defaults to `conf`. Files in this subdirectory of the install directory will be marked as "conf files" in Debian packaging, meaning they'll be symlinked from `/etc/$long-fsname-dir` and  on upgrades if the user has edited these files the package manager will offer to do a merge. This is convenient because it means you can change the config file over time and upgrades will treat the files as if they are in a simple form of version control. When the user hasn't edited the part of the file that's changing, the difference will be smoothly applied. Note that if packaging a JVM or Electron app you will need to override this key because the input files are relocated to a subdirectory of the overall install. For JVM apps use `app.linux.conf-dir = lib/app/conf` and place your config files in the `conf` sub-directory (e.g. `app.inputs += something.txt -> conf/something.txt`). For Electron apps use `app.linux.conf-dir = resources/app/conf`. You can of course name the relevant subdirectory whatever you want. 

!!! note "Config directories"
    The config directory is implemented by symlinking `/etc/${app.long-fsname-dir} to the conf directory in your app files. If this directory already exists when a package is installed it will be left alone. Therefore, if your users may have already created files in this directory, the conf-dir mechanism won't be useful as the files expected to be there won't be added. The way this feature works may change in future.  

**`symlinks`** A list of strings, where each string consists of two parts separated by ` -> `. The first part is the location of a symlink, the second part its destination. If the location isn't absolute it's relative to the install path. The second part is just copied to the symlink directly, so can be absolute or relative. By default the list looks like this:

```
symlinks = [
  ${app.linux.prefix}/bin/${app.fsname} -> ${app.linux.install-path}/bin/${app.fsname}
]
```

Later entries override earlier entries and if the target is blank the link won't be created at all, so you can edit this list by adding
items to it. The default means that the launcher will be symlinked into `bin`.

??? note "Older compatibility levels"
    In configs with `conveyor.compatibility-level < 7` there are some additional symlinks added to paths in `/var`. This was meant for servers and is of little practical use in most apps so it was removed from the defaults starting from compatibility level 7.

**`main-binary`** The path relative to the package contents which is treated as the primary entrypoint. This is used for the symlink placed in `/usr/bin` and the `.desktop` file. It defaults to `bin/${app.fsname}` but can be set to something else if your input layout doesn't follow this convention.

**`debian.control`** Keys and values copied into the control file. The defaults here are normally fine but you can add others if you want
more precise control. See below for an example.

**`debian.{postinst,postrm,preinst,prerm}`** Maintainer/package scripts invoked by the package management system when the package is installed, upgraded or uninstalled. The default values should normally be left alone. They take care of registering and starting systemd services, desktop files for icons and other metadata, and so on. They also print out advice to system administrators using the command line when HTTP server config files are installed.

If you want to run extra code as root at install time, you can append a fragment of shell script to the postinst string.

**`appstream.{file-name,content}`** These keys control the content of the generated [AppStream XML file](https://www.freedesktop.org/software/appstream/docs/). This controls how your app appears in the Software Center/app store apps that some Linux desktop environments and distributions use (e.g. the GNOME Software / Snap Store tools utilize this metadata). You don't normally need to alter this.

**`services`** A list of SystemD service definitions (see below).

**`contact-email`** This is needed for package, repository and PGP metadata which all have some notion of a maintainer. Defaults to `${app.contact-email}`.

**`signing-key`** See [signing keys](index.md#signing).

## Desktop integration

**`desktop-file`** An INI file object. The `file-name` subkey controls what the file is called (defaulting to `${app.long-fsname}.desktop`). Then the `"Desktop Entry"` subkey defines the contents of that section with keys and values being mapped as appropriate.

You should only rarely need to configure the .desktop file directly. The main entry you may need to change is `StartupWMClass` which may need to be altered if you aren't seeing your icon get associated with your app in the taskbar. To find out what it should be you can use the `lg` command from GNOME Shell "Run Command" dialog (press alt-f2), or `xprop WM_CLASS` if not using Wayland, and then select the window of your running app.

You may also wish to set the `app.linux.desktop-file."Desktop Entry".Categories` key as on some desktop environments that key controls the menus where the app appears. Please refer to the [registered categories list](https://specifications.freedesktop.org/menu-spec/latest/apa.html#main-category-registry) for what you can place there. Metadata that appears in the Software Center apps some desktop environments have is a combination of data from the .desktop entry file and an AppStream XML file, which will also be generated for you.

## SystemD units

**Conveyor provides [pre-packaged snippets for common systemd use cases](../stdlib/systemd.md) in the standard library.**

SystemD is the standard service manager on Linux. It manages background services, the dependencies between them and also timers/cron jobs. It has many powerful features and you may wish to configure it specifically
for your app. 

SystemD is configured via INI files called units. Unit files are defined by creating an object in the `app.linux.services` hierarachy with any arbitrary key name (it's not used for anything). The structure of the config object maps directly to the structure of the underlying unit file. The `file-name` key controls what the unit file will be called. Other keys are objects for each section. Inside each section values can be strings or arrays of strings. Comments above a key are copied into the systemd file. The INI syntax systemd uses happens to be HOCON compatible so you can write systemd configs in the same style you'd find in the final file, but it's not mandatory.

### Defining a timer / cron job

Here's how to define a timed job that runs weekly. A cron job requires two units - a service unit that defines how to run the program, and a timer unit that controls how often to run it. To learn more about this topic try reading the [Arch Linux Wiki](https://wiki.archlinux.org/title/Systemd/Timers) which has a good page on this, or the [man pages](https://www.freedesktop.org/software/systemd/man/systemd.timer.html).

```
app.linux.services {
	# Define a timer unit that runs the cleanup service.
	timer {
		file-name = ${app.long-fsname}-maintenance.timer
		
		Unit {
			Description=Triggers routine maintenance for ${app.display-name}
		}
		
		Timer {
			OnCalendar=weekly
			Persistent=true
		}
		
		Installer {
			WantedBy=timers.target
		}
	}
	
	# Define a service unit that runs the app with a special maintenance flag.
	cleanup {
		file-name = ${app.long-fsname}-maintenance.service
		
		Unit {
			Description=Runs routine maintenance for ${app.display-name}
		}
		
		Service {
			ExecStart=${app.linux.install-path}/bin/my-program --run-maintenance
		}
	}
}
```

### Defining a server

[The Conveyor standard library has config snippets for servers](../stdlib/systemd.md). The resulting package will have these features:

* Automatically started when the package is installed, stopped and restarted across upgrades (so your app doesn't have to handle files changing out from underneath it), and restarted on reboot.

* Lightly sandboxed by running it as a dynamically allocated UNIX user and group, meaning it won't have read/write access to anywhere except the standard directory locations for UNIX servers.
* Directories under `/var` will be allocated for logs, cache and persistent data in the OS-appropriate places. You could hard-code these paths in your software but a better way is to examine the values of the environment variables `STATE_DIRECTORY` (for storing persistent data), `CACHE_DIRECTORY` (for transient data) and `LOGS_DIRECTORY` (for logs).

## Exposing web servers

If the `app.server.http.port` key is set to a non-zero value then Linux packages will contain example virtual host configs for the popular [nginx](https://nginx.org/en/docs/) and [Apache](https://httpd.apache.org/docs/current/) reverse HTTP proxies. They will be placed in `/etc/{nginx,apache2}/sites-available/${app.long-fsname}.conf`  so the administrator can activate it by making a symlink or running `a2ensite`. The package will print instructions the first time it's installed (if installed from the command line).

Unfortunately due to limitations in both servers the configs cannot be completely automatically usable out of the box. The admin will need to edit them to set what the desired host name should be.

The config files themselves come from the `app.server.http.{nginx,apache}` keys. If you don't like the defaults you can replace them with your own. For Apache, the key `app.server.http.apache-extras` will be added to the end of the config but inside the `VirtualHost` block.

!!! tip
    Set up a default command line argument in your systemd or JVM app settings blocks that substitutes the `app.server.http.port` key. That way if you want to change the default it can be all set in one place, and the server configs will automatically follow along.

## Package dependencies

Conveyor generates Debian packages with dependency metadata by scanning any ELF binaries or shared libraries to find their library dependencies,
and then scanning the Debian package index to find which packages contain those libraries.

**`app.linux.debian.control.Depends`** A list of _additional_ dependencies to add to the automatically determined set. If you wish to remove
an automatically determined dependency, you can add an entry that starts with a `-`. You can also give a single comma separated string for
convenience. Examples:

```
app.linux.debian.control {
  Depends: [ "postgresql (>= 12)", postgresql-client-common, -libavcodec12 ]
}
```

The syntax is the same as a regular Debian control file.

**`app.linux.ignore-dangling-dependencies`** If your package contains shared libraries that have dependencies which can't be found in the target distribution, a warning will be generated during the build. You can add the names of the needed shared libraries here (e.g. `[libfoo{,-extras}.so.2]`) to silence these warnings.

**`app.linux.debian.distribution.name`** Short name of the distro to target, defaults to  `jammy` for [*Jammy Jellyfish*](https://releases.ubuntu.com/jammy/). Before compatibility level 10 the default was `focal`. This controls how ELF library names are mapped to packages.

**`app.linux.debian.distribution.mirrors`** List of mirrors of the distro, defaults to `["http://archive.ubuntu.com/ubuntu/"]`.

Most of the time you won't need to change these. They matter more for apps that rely heavily on libraries expected to come with the distribution.
