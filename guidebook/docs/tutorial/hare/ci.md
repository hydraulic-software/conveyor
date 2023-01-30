Compiling your app on every supported OS is annoying. Let's use GitHub Actions to compile our app for us. You can use any CI system
of course, the procedure will be similar.

- [x] Upload your sample app to GitHub.
- [x] Go to [nightly.link](https://nightly.link) and request URLs for your project. You may have to authorize the app to access to your GitHub account.
- [x] At the top level of your config, outside the `app {}` block, add:
  ```
  ci-artifacts-url = nightly.link/<github user>/<github project>/workflows/build/master
  ```
- [x] Delete the inputs section of your config and replace it with this:
  ```
  windows.amd64.inputs += ${ci-artifacts-url}/build-windows-amd64.zip

  linux.amd64.inputs += {
    from = ${ci-artifacts-url}/build-linux-amd64.zip
    extract = 2
  }

  mac.amd64.inputs += {
    from = ${ci-artifacts-url}/build-macos-amd64.zip
    extract = 2
  }

  mac.aarch64.inputs += {
    from = ${ci-artifacts-url}/build-macos-aarch64.zip
    extract = 2
  }
  ```
- [x] Add a workflow file to `.github/workflows` and commit/push it. It should look like [this example](https://github.com/hydraulic-software/flutter-demo/blob/master/.github/workflows/build.yml). 

!!! note
    - Conveyor can download and extract archives for you, including from servers that are behind authentication.
    - [nightly.link](https://nightly.link) is a service that lets us get direct download URLs for artifacts exported by GitHub Actions.
      With GitHub alone you unfortunately can't get simple download URLs straight from a CI job. 
    - The `extract = 2` line is needed to work around another limitation of GitHub Actions.
    - Explore the [Conveyor/Flutter demo repository](https://github.com/hydraulic-software/flutter-demo/blob/master/.github/workflows/build.yml)
      to see how it all fits together.

[ :material-arrow-right-box: Learn more about using continuous integration](../../continuous-integration.md){ .md-button .md-button--primary }
