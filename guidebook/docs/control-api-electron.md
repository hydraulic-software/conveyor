# Electron Online Updater API

!!! tip
    To see a working example of using this API run `conveyor generate electron update-api-demo`.

```
npm install @hydraulic/conveyor-control koffi
```

The `OnlineUpdater` class provides functionality for checking and managing updates in Electron applications. This API is designed to work across Windows and macOS, and integrates with platform-specific update mechanisms. You need to install the `koffi` module as well if you want to control updates on macOS, it won't be installed automatically.

```
const {OnlineUpdater, Version} = require('@hydraulic/conveyor-control')
```

## Class: OnlineUpdater

### Constructor

```javascript
new OnlineUpdater(updateSiteURL)
```

- `updateSiteURL` {string} The URL of the update site.

### Methods

#### getCurrentVersion()

Returns the current version of the application as a `Version` object (see below).

#### triggerUpdateCheckUI()

The behavior depends on the platform:

* :simple-windows: **Windows.** Triggers a restart and update cycle. Your program will shut down so make sure to have saved any user data. It will restart after the update. Don't call this until you've checked there is actually an update available, as otherwise you'll just restart the program for no reason.
* :simple-apple: **macOS.** If an update is available, shows an update UI prompt that lets the user decide whether to update or not.
* :simple-linux: **Linux.** Throws an error/Unimplemented.

Throws an error if update checks are unavailable for some reason, e.g. because on Windows the user is running the zip version rather than the EXE/MSIX version or because `app.updates = none`.

#### getCurrentVersionFromRepository()

Returns a Promise that resolves with the latest version available in the repository as a `Version` object. This works by reading the `metadata.properties` file at the site URL.

#### canTriggerUpdateCheckUI()

Returns a string indicating whether the update check UI can be triggered. Possible return values:

- `'AVAILABLE'`: Update checks can be triggered.
- `'UNSUPPORTED_PACKAGE_TYPE'`: The current package type doesn't support update checks.
- `'UNIMPLEMENTED'`: Update checks are not implemented for the current platform.

## Class: Version

Represents a version number with optional revision.

### Constructor

```javascript
new Version(version, revision = 0)
```

- `version` {string} The version string (e.g., "1.0.0").
- `revision` {number} (Optional) The revision number. Defaults to 0.

### Methods

#### compareTo(other)

Compares this version to another Version object.

- `other` {Version} The Version object to compare against.

Returns:
- Negative number if this version is lower than the other.
- 0 if the versions are equal.
- Positive number if this version is higher than the other.

#### toString()

Returns the string representation of the version.

## Example Usage

```javascript
const updater = new OnlineUpdater('https://example.com/updates');

// Check if updates are available
const updateStatus = updater.canTriggerUpdateCheckUI();
if (updateStatus === 'AVAILABLE') {
  updater.triggerUpdateCheckUI();
} else {
  console.log(`Update checks unavailable: ${updateStatus}`);
}

// Get current and latest versions
const currentVersion = updater.getCurrentVersion();
updater.getCurrentVersionFromRepository()
  .then(latestVersion => {
    if (currentVersion.compareTo(latestVersion) < 0) {
      console.log('A new version is available!');
    } else {
      console.log('You are on the latest version.');
    }
  })
  .catch(error => console.error('Error checking for updates:', error));
```
