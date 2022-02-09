# Linux

## Synopsis

```properties
# Make building test packages faster by disabling compression.
app.linux.compression-level = none

# Set the prefix. App files will be placed in /usr/local/lib/${app.long-fsname-dir}
# and a symlink to the launcher in /usr/local/bin
app.linux.prefix = /usr/local

# Set the location where app files will be placed, overriding the prefix.
app.linux.install-path = /opt/myapp

# Change the menu categories, or any other key in the .desktop file.
app.linux.desktop-file.Categories = "GTK;Gnome;Game"

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
```

## Keys

These keys under `app.linux` control Linux specific packaging aspects.

**`prefix`** The part of the directory hierarchy to install under. Defaults to `/usr`. 

!!! warning 
    Anything _other_ than `/usr` will break various kinds of integrations due to Linux desktop environments and system software not reliably supporting other prefixes. Some things will work and others won't if you change this.

**`install-path`** A directory where the input files will be placed. Defaults to `${app.linux.prefix}/lib/${app.long-fs-dir}` so (by default) the name is dependent on whether there's a vendor or not. If no vendor is specified it'll be something like `/usr/lib/fooapp`. Other defaults are all relative to this. The directory should be empty rather than a directory the user will already have.

**`compression-level`** One of `none`, `low`, `medium` or `high`. Higher compression levels are much, much slower to build than lower levels but yield faster downloads for your users. When experimenting it can be convenient to set this to none.

**`conf-dir`** Defaults to `conf`. Files in this subdirectory of the working directory will be marked as "conf files" in Debian packaging, meaning they'll be symlinked from `/etc/$long-fsname-dir` and  on upgrades if the user has edited these files the package manager will offer to do a merge. This is convenient because it means you can change the config file over time and upgrades will treat the files as if they are in a simple form of version control. When the user hasn't edited the part of the file that's changing, the difference will be smoothly applied.

**`desktop-file`** An INI file object. The `file-name` subkey controls what the file is called (defaulting to `${app.long-fsname}.desktop`). Then the `"Desktop Entry"` subkey defines the contents of that section with keys and values being mapped as appropriate. 
You should only rarely need to configure the .desktop file directly, but, you may wish to set the `app.linux.desktop-file."Desktop Entry".Categories` key as on some desktop environments that key controls the menus where the app appears. Please refer to the [registered categories list](https://specifications.freedesktop.org/menu-spec/latest/apa.html#main-category-registry) for what you can place there. Metadata that appears in the Software Center apps some desktop environments have is a combination of data from the .desktop entry file and an AppStream XML file, which will also be generated for you.

**`symlinks`** A list of strings, where each string consists of two parts separated by ` -> `. The first part is the location of a symlink, the second part its destination. If the location isn't absolute it's relative to the install path. The second part is just copied to the symlink directly, so can be absolute or relative. By default the list looks like this:

```
symlinks = [
  ${app.linux.prefix}/bin/${app.fsname} -> ${app.linux.install-path}/bin/${app.fsname}
  logs -> /var/log/${app.long-fsname-dir}
  data -> /var/lib/${app.linux.var-lib-dir}
  cache -> /var/cache/${app.long-fsname-dir}
]
```

Later entries override earlier entries and if the target is blank the link won't be created at all, so you can edit this list by adding items to it. The defaults mean that the launcher will be symlinked into `bin`.

**`debian.control`** Keys and values copied into the control file. The defaults here are normally fine but you can add others if you want more precise control. See below for an example.

**`debian.{postinst,postrm,preinst,prerm}`** Maintainer/package scripts invoked by the package management system when the package is installed, upgraded or uninstalled. The default values should normally be left alone. They take care of registering and starting systemd services, desktop files for icons and other metadata, and so on. They also print out advice to system administrators using the command line when HTTP server config files are installed.

If you want to run extra code as root at install time, you can append a fragment of shell script to the postinst string.

**`ignore-dangling-dependencies`** If your package contains shared libraries that have dependencies which can't be found in the target distribution, a warning will be generated during the build. You can add the names of the needed shared libraries here (e.g. `[libfoo{,-extras}.so.2]`) to silence these warnings.

**`appstream.{file-name,content}`** These keys control the content of the generated [AppStream XML file](https://www.freedesktop.org/software/appstream/docs/). This controls how your app appears in the Software Center/app store apps that some Linux desktop environments and distributions use (e.g. the GNOME Software / Snap Store tools utilize this metadata). You don't normally need to alter this.

**`services`** A list of SystemD service definitions (see below).

**`contact-email`** This is needed for package, repository and PGP metadata which all have some notion of a maintainer. Defaults to `${app.contact-email}`.

**`signing-key`** See [signing keys](index.md#signing).

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

## Altering package dependencies

You can edit the Debian control file used to define package dependencies like this:

```
app.linux.debian.control {
  Depends: "postgresql (>= 12)"
}
```

The syntax is the same as a regular Debian control file. Note that dependencies determined from shared libraries in your package will always be appended to this list.
