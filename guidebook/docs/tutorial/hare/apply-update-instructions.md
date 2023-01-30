=== ":simple-windows: Windows"

    Run the program you installed in the previous step. You should see a window appear with a progress bar, the update be applied and
    the app should then start, all without any user interaction.

    This happens because the sample `conveyor.conf` file is using the `app.updates = aggressive` key. [Learn more about update modes](../../configs/index.md#update-modes).

=== ":simple-apple: macOS"

    Run the program you installed in the previous step. You should see a window appear with a progress bar, the update be applied and
    the app should then start, all without any user interaction.

    This happens because the sample `conveyor.conf` file is using the `app.updates = aggressive` key. [Learn more about update modes](../../configs/index.md#update-modes).

=== ":simple-linux: Linux"

    If on Debian derived distributions: Run `apt-get update; apt-get upgrade` to get the newest version of your app. Otherwise, there
    is no automatic update supported right now, sorry.
