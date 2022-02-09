# JetBrains TeamCity

This sample shows you how to re-package a server app that's already packaged by someone else. In this case we'll use TeamCity by JetBrains,
a CI server. 

It demonstrates the following features:

* You'll get a DEB and associated apt repository.
* The package will install a systemd service and start/stop it automatically on install and upgrades.
* The DEB will depend on Postgres 12+
* A sample Apache 2 reverse proxy config will be placed in `/etc/apache2/sites-available`.
* TeamCity will be configured to use whatever port is specified in the Conveyor config, by pre-generated one of its config files (the big
  pile of XML).

Unlike in most of the other samples, here we don't need to include a JDK file from the standard library, because TeamCity already bundles its
own JVM.

```hocon
app {
  display-name = JetBrains TeamCity
  vendor = JetBrains

  # Download the latest version from the JetBrains site.
  inputs += download.jetbrains.com/teamcity/TeamCity-2021.2.tar.gz

  # Disable the nginx default sample reverse proxy config and leave the default Apache 2 reverse proxy config, pointing a localhost 1051.
  server.http {
    port = 1051
    nginx = null
  }

  # Not a desktop app.
  icons = []
  
  linux {
    # TeamCity expects some directories in its working directory to point to where you want logs and cache. Direct it to the standard place.
    # These directories are created and managed by systemd.
    symlinks = [
      temp -> /tmp
      logs -> /var/log/private/${app.long-fsname-dir}
      cache -> /var/cache/private/${app.long-fsname-dir}
    ]

    # Define a systemd service so the OS will start and supervise the server.
    services {
      server {
        Service {
          ExecStart = ${app.linux.install-path}/bin/teamcity-server.sh run
          ExecStop = ${app.linux.install-path}/bin/teamcity-server.sh stop

          # This will meet TeamCity's need for its whole directory tree to be writable even though that's not conventional for Linux.
          # The ! mark here makes the script run as root but after uid/gid allocation is done.
          ExecStartPre = "!/bin/sh -c \"chown -R --preserve-root -h" ${app.long-fsname}":"${app.long-fsname} ${app.linux.install-path}"\""
          # Punch a hole in the sandbox so the server can write to its own directory.
          ReadWritePaths = "+"${app.linux.install-path}

          # https://www.jetbrains.com/help/teamcity/installing-and-configuring-the-teamcity-server.html#Setting+Up+Memory+settings+for+TeamCity+Server
          Environment += "TEAMCITY_SERVER_MEM_OPTS=-Xmx2048m"
        }
      }
    }
    
    # Bandwidth is plentiful, my patience is not.
    compression-level = low

    debian {
      control {
      	# TeamCity really wants to use a proper RDBMS.
        Depends: "postgresql (>= 12)"
      }
    }
  }

  # This is where the apt repository will be hosted.
  site.base-url = downloads.hydraulic.dev/jb-packages/${part.fsname}

  # Override the default server config to configure the port automatically.
  inputs += {
    to = conf/server.xml
    content = """
<?xml version='1.0' encoding='utf-8'?>
<Server port="8105" shutdown="SHUTDOWN">
  <Listener className="org.apache.catalina.startup.VersionLoggerListener" />
  <Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />
  <GlobalNamingResources>
    <Resource name="UserDatabase" auth="Container"
              type="org.apache.catalina.UserDatabase"
              description="User database that can be updated and saved"
              factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
              pathname="conf/tomcat-users.xml" />
  </GlobalNamingResources>

  <Service name="Catalina">
    <Connector port=""""${app.server.http.port}"""" protocol="org.apache.coyote.http11.Http11NioProtocol"
               connectionTimeout="60000"
               socket.txBufSize="64000"
               socket.rxBufSize="64000"
               useBodyEncodingForURI="true"
               tcpNoDelay="1"
    />
    <Engine name="Catalina" defaultHost="localhost">
      <Realm className="org.apache.catalina.realm.LockOutRealm">
        <Realm className="org.apache.catalina.realm.UserDatabaseRealm" resourceName="UserDatabase"/>
      </Realm>

      <Host name="localhost"  appBase="webapps"
            unpackWARs="true" autoDeploy="true">
        <Valve className="org.apache.catalina.valves.RemoteIpValve" protocolHeader="x-forwarded-proto" />
        <Valve className="org.apache.catalina.valves.ErrorReportValve"
               showReport="false"
               showServerInfo="false" />
      </Host>
    </Engine>
  </Service>
</Server>
    """
  }
}
```
