Compiling your app on every supported OS is annoying. Let's use GitHub Actions to compile our app for us. You can use any CI system
of course, the procedure will be similar.

- [x] Upload your sample app to GitHub.
- [x] Delete the inputs section of your config and replace it with this:
  ```
  windows.amd64.inputs += artifacts/windows
  linux.amd64.inputs += artifacts/build-linux-amd64.tar
  mac.amd64.inputs += artifacts/build-macos-amd64.tar
  mac.aarch64.inputs += artifacts/build-macos-aarch64.tar
  ```
- [x] Add a build workflow file to `.github/workflows` and commit/push it. It should look like [the `build.yml` file on this example](https://github.com/hydraulic-software/flutter-demo/blob/master/.github/workflows/build.yml).
- [x] Locate the [`defaults.conf` file in your system](https://conveyor.hydraulic.dev/7.1/configs/#per-user-defaults) and copy the value of the `app.signing-key` config key.
- [x] Create a [GitHub Encrypted Secret](https://docs.github.com/en/actions/security-guides/encrypted-secrets) named SIGNING_KEY, and paste the value copied above (without the surrounding quotes).
- [x] Add a deploy workflow file to `.github/workflows` and commit/push it. It should look like [the `deploy-to-gh.yml` file on this example](https://github.com/hydraulic-software/flutter-demo/blob/master/.github/workflows/deploy-to-gh.yml).

!!! note
    - This deploy workflow in this example will release your app to GitHub Releases. To deploy via SSH to a private server instead, you can use a workflow like [the `deploy-to-ssh.yml` file on this example](https://github.com/hydraulic-software/flutter-demo/blob/master/.github/workflows/deploy-to-ssh.yml). Take note of the additional secrets necessary for that workflow.
    - Explore the [Conveyor/Flutter demo repository](https://github.com/hydraulic-software/flutter-demo/blob/master/.github/workflows/build.yml)
      to see how it all fits together.

[ :material-arrow-right-box: Learn more about using continuous integration](../../continuous-integration.md){ .md-button .md-button--primary }
