* [x] Look inside the `output` directory to see the files you or Conveyor would upload to the distribution site.
* [x] Run the app in packaged form from a temporary directory:

  ```
  conveyor run
  ```

* [x] Change the `output` directory to reflect different build targets.

=== "Your current OS"
    ```
    conveyor make app
    ```

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
