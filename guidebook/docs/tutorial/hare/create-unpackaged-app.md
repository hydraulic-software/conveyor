* [x] Create a self-contained directory and run the app from there:

  ```
  conveyor run
  ```

* [x] Get the app into the `output` directory and take a look.

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
