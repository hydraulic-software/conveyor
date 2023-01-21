# Managing servers

Conveyor can build and push servers to remote Linux machines. It can be a simpler alternative to Docker, Kubernetes or Ansible for some use cases.

## Getting started

Here's how to ship a JVM server that uses the Gradle build system.

```
include "#!./gradlew -q printConveyorConfig"

app {
    fsname = frontend-server
    vendor = MegaCorp

    jvm.constant-app-arguments = --port 8080
        
    linux.services.server {
        include "/stdlib/linux/service.conf"
    }
}

deploy {
    to = [ 
        "frontend{1,2,3,4,5,6,7,8,9,10}.example"
    ]
    via = ssh
    remote-user = root
}
```

Now run:

```bash
conveyor push
```

Conveyor will now:

1. Create a self-contained Linux install directory with a bundled JVM using `jlink`, minified using `jdeps` and configured as described in [JVM config](../configs/jvm.md). The version is taken from your Gradle build.
2. Log in to each machine as specified in the `deploy.to` key and check that it's a Debian/Ubuntu derivative (the result of this check is cached).
3. Copy the app to `/usr/lib/${long-fsname}/versions/${version}` via sftp.
4. Create a tiny stub .deb package. This little package integrates the app with the system but doesn't directly contain the app files.
5. Upload the stub deb, install it, delete it. This will in turn:
   1. Update the `/usr/lib/${long-fsname}/current` symlink to point to the new version.
   2. Refresh systemd
   3. Restart the service with the new version.
   4. Do any other tasks you added to your `app.linux.debian.postinst` scripts (and postrm etc).

This process will be done in parallel for each server. If any server fails to start up or pass health checks it's rolled back. At the end
a report of what's been done is available in the `output` directory and will be printed to the console.
