=== ":simple-windows: Windows"
    ```
    conveyor make windows-app
    ```

=== ":simple-apple: macOS"
    One of the following for Intel/Apple Silicon Macs respectively:

    ```
    conveyor -Kapp.machines=mac.amd64 make mac-app
    conveyor -Kapp.machines=mac.aarch64 make mac-app
    ```

=== ":simple-linux: Linux"
    ```
    conveyor make linux-app
    ```

* [x] Open the `output` directory and run the app you find inside.

The unpackaged app won't update. We'll fix that now by creating a download/update site.
