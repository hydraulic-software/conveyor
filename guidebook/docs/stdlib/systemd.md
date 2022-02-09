# SystemD

[You can create systemd unit files from config](../configs/linux.md#systemd-units). Some snippets are provided to help you easily define Linux servers. The resulting packages will automatically start the server on installation, restart it on boot, restart it if it terminates abnormally and stop/restart it across package upgrades. You can  also lightly sandbox your server by running it as a non-root user.

## Simple server

To use this snippet `include "/stdlib/linux/service.conf"`, like this:

```
app {
	// ...
	
	// The ".server" part here can be anything - the included snippet will override the file name.
	linux.services.server {
		include "/stdlib/linux/service.conf"
	}
}
```

Assuming you've followed the standard conventions found elsewhere in this guide, this is enough to integrate your app with the service manager. The service will by default be named using the `app.long-fsname`.

Install the package and it should start the server. Try `systemctl status myvendor-awesomeapp` to see if the server is running.

## Adding additional command line arguments

SystemD uses the `ExecStart` key to decide what to run. Therefore, you can add command line arguments that will only be passed when started from the service manager like this:

```
app.linux.services.server {
  include "/stdlib/linux/service.conf"
  Service {
    ExecStart = ${app.linux.services.server.Service.ExecStart} --port 8888 --message "\"Hello World\""
  }
}
```

Note the escaping required to pass a command line argument that contains a space - just using quotes isn't sufficient because it won't be preserved through the config parsing process.

## Setting default environment variables

Default environment variables are easy to add. Just remember to append to the list, not replace it:

```
app.linux.services.server {
  include "/stdlib/linux/service.conf"
  Service {
    Environment += "APP_STUFF=whatever"
  }
}
```

## More than one service

You can define as many systemd units as you like, but if you include the `service.conf` snippet you'll have to pick your own file names for the others to avoid conflicts. Here's an example for a program that can be run in both "frontend" and "backend" mode where they should be started and controlled separately.

```
app.linux.services {
  frontend-server {
    include "/stdlib/linux/service.conf"

    Service {
      ExecStart = ${app.linux.services.server.Service.ExecStart} --port 8888 --message "\"Hello World\""
    }
  }
    
  backend-server {
    include "/stdlib/linux/service.conf"

    file-name = ${app.long-fsname}-backend.service

    Service {
      ExecStart = ${app.linux.services.server.Service.ExecStart} --backend-mode --port 8889
    }	
  }
}
```

## Sandboxing the server

To make the server run as non-root, you can `include "/stdlib/linux/lightweight-service-sandbox.conf"`. This uses systemd's [dynamic user feature](http://0pointer.net/blog/dynamic-users-with-systemd.html) to implement traditional UNIX unprivileged user sandboxing, but without the need to actually create users and groups in the install scripts.

Because the server won't run as root you won't be able to open ports lower than 1024. If your server is an HTTP server consider using a reverse proxy and Conveyor's built in ability to generate canned Apache/nginx reverse proxy configs (see [the Linux section](../configs/linux.md) of the guidebook).

The server will be restricted to only writing to its private directories under `/var`, which can be found in the `STATE_DIRECTORY`, `LOGS_DIRECTORY` and `CACHE_DIRECTORY` environment variables. Pay attention to them to make your software easier to run as a Linux server.

SystemD and the Linux kernel have many advanced sandboxing features which this simple standard library snippet doesn't use, but because you can control the unit files yourself you can define any policy you wish. [Learn more about what keys are available](https://www.freedesktop.org/software/systemd/man/systemd.exec.html).

## Writable server files

Sometimes servers not written for UNIX or which were not designed to be packaged want to write to their own installation directory. It's not a good idea as it requires reducing the security of the server quite considerably (any compromise can be made permanent), but if you need this you can do it by using `include "/stdlib/linux/writable-service-install.conf"`.

## File contents

### service.conf

The default service configuration looks like this:

```
# File/service name of the systemd unit.
file-name = ${app.long-fsname}.service

Unit {
  Description = ${app.display-name}
  Wants = network-online.target
  After = network-online.target
}

Service {
  # Type=exec has more much sensible behaviour than Type=simple. Also the systemd maintainer stated in a talk in 2018 that
  # 'exec' is the right way to go, and opt-in only for compatibility reasons.
  # https://media.ccc.de/v/ASG2018-230-systemd_in_2018#t=491
  Type = exec

  # The command to start the server with. Should block not fork.
  ExecStart = ${app.linux.install-path}/bin/${app.fsname}

  # Store data in the conventional UNIX locations. SystemD will create these for us, and in such a way that other
  # services can't read them. There are symlinks from WorkingDirectory= pointing to these locations, so the software
  # can just use those names relative to the working directory in a portable manner.
  StateDirectory = ${app.linux.var-lib-dir}
  CacheDirectory = ${app.long-fsname-dir}
  LogsDirectory = ${app.long-fsname-dir}
  ConfigurationDirectory = ${app.long-fsname-dir}

  # Some servers want to write to $HOME even though conventionally UNIX services don't have one.
  Environment = [ "HOME=/var/lib/"${app.linux.var-lib-dir} ]

  # Lets the app just resolve relative paths to find things, if it wants to.
  WorkingDirectory = ${app.linux.install-path}

  # Restart the service if it crashes, is OOM killed etc, but not if it quits with a non-zero exit code
  # (which probably implies a bad configuration file or similar).
  Restart = on-abnormal
}

Install {
  WantedBy = default.target
}
```

### writable-service-install.conf

```
Service {
  ExecStartPre += "!/bin/sh -c \"chown -R --preserve-root -h" ${app.long-fsname}":"${app.long-fsname} ${app.linux.install-path}"\""
  ReadWritePaths += "+"${app.linux.install-path}
}
```

### lightweight-service-sandbox.conf

```
Service {
  DynamicUser = yes
  User = ${app.long-fsname}
  Group = ${app.long-fsname}
}
```
