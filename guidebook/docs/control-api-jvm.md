# Control API JVM

The Control API provides access to the software update engine controlling your packaged application. This guide will walk you through the main features and usage of the `SoftwareUpdateController` interface.

## Getting Started

To use the Software Update Controller, you first need to obtain an instance:

```java
SoftwareUpdateController controller = SoftwareUpdateController.getInstance();
```

This method returns an implementation of the interface, or `null` if the app isn't running inside a Conveyor package.

## Checking for Updates and Triggering the Update Process

To check for available updates and trigger the update process correctly, follow these steps:

1. Get the current version:

```java
SoftwareUpdateController.Version currentVersion = controller.getCurrentVersion();
if (currentVersion == null) {
    // Handle the case where current version is not available
    return;
}
```

2. Check for the latest version from the repository:

```java
try {
    SoftwareUpdateController.Version latestVersion = controller.getCurrentVersionFromRepository();
    if (latestVersion == null) {
        // Handle the case where latest version information is not available
        return;
    }

    // Compare versions using the compareTo method
    if (latestVersion.compareTo(currentVersion) > 0) {
        // A newer version is available
        if (controller.canTriggerUpdateCheckUI() == SoftwareUpdateController.Availability.AVAILABLE) {
            // Make sure to save all user data before calling this method
            controller.triggerUpdateCheckUI();
        }
    } else {
        // No update available or current version is newer
    }
} catch (SoftwareUpdateController.UpdateCheckException e) {
    // Handle exception
}
```

This method makes an HTTP request, so it should be called on a background thread.

Remember to handle exceptions and check for null values where appropriate. Also, make sure to save all user data before calling `triggerUpdateCheckUI()`, as it may cause the process to shut down.

## Availability of Update Checks

Check if update triggering is available on the current platform and package:

```java
SoftwareUpdateController.Availability availability = controller.canTriggerUpdateCheckUI();
switch (availability) {
    case AVAILABLE:
        // Update checks can be triggered
        break;
    case UNIMPLEMENTED:
    case UNSUPPORTED_PACKAGE_TYPE:
    case NON_GUI_APP:
    case OTHER:
        // Handle cases where update checks can't be triggered
        break;
}
```

Remember to handle exceptions and check for null values where appropriate. This API allows you to integrate software updates seamlessly into your application's workflow.
