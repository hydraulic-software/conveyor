# Adapt a server

This sample shows you how to re-package a server app that's already packaged by someone else. In this case we'll use TeamCity by JetBrains,
a CI server.

It demonstrates the following features:

* You'll get a DEB and associated apt repository.
* The package will install a systemd service and start/stop it automatically on install and upgrades.
* The DEB will depend on Postgres 12+
* A sample Apache 2 reverse proxy config will be placed in `/etc/apache2/sites-available`.
* TeamCity will be configured to use whatever port is specified in the Conveyor config, by pre-generated one of its config files (the big
  pile of XML).

Note that although TeamCity is a JVM app we'll be packaging it as if it's a native app, because it already bundles its own JVM and we don't
have the original build system. To us it'll just be a directory of files.

```hocon
app {
  display-name = JetBrains TeamCity
  vendor = JetBrains

  // Download from the JetBrains website.
  inputs += download.jetbrains.com/teamcity/TeamCity-2022.04.1.tar.gz

  // Overwrite the default config file, so we can control the default port.
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

  // Turn off the nginx reverse proxy sample config, but keep the Apache2 equivalent. 
  server.http {
    port = 1051
    nginx = null
  }
  
  linux {
    // TeamCity wants some directories in its install directory, use symlinks to redirect them to standard Linux paths here. 
    symlinks = [
      temp -> /tmp
      logs -> /var/log/private/${app.long-fsname-dir}
      cache -> /var/cache/private/${app.long-fsname-dir}
    ]

    // Set up a systemd service so the server will start on install, stop on uninstall and restart on upgrade.
    services {
      // The name "server" here is arbitrary.
      server {
        // Import a default systemd service file from the Conveyor standard library.
        include "/stdlib/linux/service.conf"
        
        // Enable the DynamicUser feature, to ensure the server doesn't run as root and is lightly sandboxed.
        include "/stdlib/linux/lightweight-service-sandbox.conf"
        
        // TeamCity unfortunately wants to write to its own install directory and this cannot be changed. Let it do so here.
        include "/stdlib/linux/writable-service-install.conf"

        // It depends on Postgresql being started, so add a service dependency. This ensures the DB is started first.
        Unit {
          Requires = postgresql.service
          After = postgresql.service
        }

        // Finally, inform systemd how to start and stop the service. 
        Service {
          ExecStart = ${app.linux.install-path}/bin/teamcity-server.sh run
          ExecStop = ${app.linux.install-path}/bin/teamcity-server.sh stop

          // https://www.jetbrains.com/help/teamcity/installing-and-configuring-the-teamcity-server.html#Setting+Up+Memory+settings+for+TeamCity+Server
          Environment += "TEAMCITY_SERVER_MEM_OPTS=-Xmx2048m"
        }
      }
    }

    // Speed up building the package.
    compression-level = low

    // Add package dependencies for the database and a JDK.
    debian {
      control {
        Depends: "postgresql (>= 12), java-11-amazon-corretto-jdk"
      }
    }
  }
  
  // You will need to point this to somewhere, as all Conveyor packages expect an update site at the moment.
  site.base-url = "localhost:1234"
}
```
