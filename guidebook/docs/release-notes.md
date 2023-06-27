# Release notes

## New features

* [Windows escape hatch feature](configs/escape-hatch.md) allows for emergency repair of installs in cases where the usual update mechanism can't be used. 
* When you generate a project with a reverse DNS name of the form `io.github.your_username.your_repo_name`, the project is automatically set up for GitHub pushes and releases. 

## Bug fixes

* Conveyor can now load PEM elliptic curve private keys that lack the public key part.

## Other changes

* The `app` task now builds the executable app directory for the current host OS, not the prior behavior of the single machine named in the
  `app.machines` key.

!!! note 
    For older release notes please use the version picker in the top bar.
