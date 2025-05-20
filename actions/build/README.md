# Build action

The build action downloads, installs and executes Conveyor. It caches both the installation and the Conveyor cache itself,
so that subsequent runs can go faster.

If the instructions here aren't enough, [please see the user guide](https://conveyor.hydraulic.dev/latest/continuous-integration/#forcing-re-downloads-of-artifacts).

It needs to be run from a Linux runner (e.g. `ubuntu-latest`).

## Usage

```yaml
# IMPORTANT: Look up the latest Conveyor version and use it here!
# https://conveyor.hydraulic.dev/latest/
- uses: hydraulic-software/conveyor/actions/build@v18.1
  with:
    # The root signing key for your application, corresponding to config key 'app.signing-key'.
    # This is required to properly sign your app. We recommend storing your signing key in
    # your GitHub repository secrets as in the example below.
    signing_key: ${{ secrets.SIGNING_KEY }}
    
    # Which version of the Conveyor End User License Agreement you agree to.
    # You must agree to the EULA to run Conveyor: https://hydraulic.dev/eula.html
    # The current version is 1.
    agree_to_license: 1
    
    # Additional flags to pass to Conveyor.
    extra_flags: ''

    # Which conveyor command to execute. For example, 'make copied-site'.
    # Default: 'make site'
    command: ''

    # Optional cache key used to store the Conveyor installation and task cache.
    # Default: 'conveyor'
    cache_key: ''
    
    # Optional path to the location where Conveyor will be installed and keep the task cache.
    # Default: '.conveyor'
    cache_path: ''
    
    # Version of Conveyor to run. Defaults to the same version as the Action itself.
    conveyor_version: ''
```

## Example

This is an example of how to deploy a Flutter app to a private SSH repository using the Build action:

```yaml
# This file can be stored in your repository under .github/workflows/deploy.yml, for example.
name: Deploy
on: [workflow_dispatch]
jobs:
  build:
    # Here we expect a build.yml similar to the one in 
    # https://github.com/hydraulic-software/flutter-demo/blob/master/.github/workflows/build.yml
    uses: ./.github/workflows/build.yml
    
  deploy:
    needs: [build]
    
    # Important: must be run from Linux.
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v3
        
      # The artifacts in this example are expected to be in the layout from the build.yml mentioned above.
      - name: Download macOS ARM build
        uses: actions/download-artifact@v3
        with:
          name: build-macos-aarch64
          path: ./artifacts
          
      - name: Download macOS AMD64 build
        uses: actions/download-artifact@v3
        with:
          name: build-macos-amd64
          path: ./artifacts
          
      - name: Download Windows build
        uses: actions/download-artifact@v3
        with:
          name: build-windows-amd64
          # Windows is not in a tarball, so we extract to a separate directory.
          path: ./artifacts/windows
          
      - name: Download Linux AMD64 build
        uses: actions/download-artifact@v3
        with:
          name: build-linux-amd64
          path: ./artifacts          
        
      - name: Set up SSH
        uses: shimataro/ssh-key-action@v2
        with:
          # Use secrets for sensitive data.
          key: ${{ secrets.SSH_KEY }}
          known_hosts: ${{ secrets.KNOWN_HOSTS }}
          
      - name: Run Conveyor     
        uses: hydraulic-software/conveyor/actions/build@v18.1
        with:
          command: make copied-site
          signing_key: ${{ secrets.SIGNING_KEY }}          
          agree_to_license: 1
          # If your SSH address is sensitive, you can hide it behind a secret as well.
          # Otherwise, you can just add it directly to your 'conveyor.conf' file under key 'app.site.copy-to' 
          extra_flags: -Kapp.site.copy-to="${{ secrets.COPY_TO }}"
```

This workflow works in conjunction with a `conveyor.conf` file like below:

```hocon
# To work with the workflow above, this file should be in the root of your repository.
include required("/stdlib/flutter/flutter.conf")

pubspec {
  include required("#!yq -o json pubspec.yaml")
}

app {
  version = ${pubspec.version}
   
  display-name = Flutter Demo
  fsname = flutter-demo
  contact-email = "contact@example.com"
  rdns-name = com.example.FlutterDemo
  vendor = Example Inc
  
  site {
    // Your download site.  
    base-url = downloads.example.com/flutter-demo
  }

  // The artifacts as laid out by the Deploy workflow above.
  windows.amd64.inputs += artifacts/windows
  linux.amd64.inputs += artifacts/build-linux-amd64.tar
  mac.amd64.inputs += artifacts/build-macos-amd64.tar
  mac.aarch64.inputs += artifacts/build-macos-aarch64.tar  
}

conveyor.compatibility-level = 11
```
