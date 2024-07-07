# Control API JVM

The Control API provides access to the software update engine controlling your packaged application. This guide will walk you through the main features and usage of the `SoftwareUpdateController` interface.

## Getting Started

To use the Software Update Controller, you first need to obtain an instance:

```java
SoftwareUpdateController controller = SoftwareUpdateController.getInstance();
```

This method returns an implementation of the interface, or `null` if the app isn't running inside a Conveyor package.

## Checking for Updates

To check for available updates, use the `getCurrentVersionFromRepository()` method:

```java
try {
    SoftwareUpdateController.Version latestVersion = controller.getCurrentVersionFromRepository();
    // Compare latestVersion with current version
} catch (SoftwareUpdateController.UpdateCheckException e) {
    // Handle exception
}
```

This method makes an HTTP request, so it should be called on a background thread. You can get the current version of your application like this:

```java
SoftwareUpdateController.Version currentVersion = controller.getCurrentVersion();
if (currentVersion != null) {
    String versionString = currentVersion.getVersion();
    int revision = currentVersion.getRevision();
}
```

## Triggering Updates

If a new version is available, you can trigger the update process:

```java
if (controller.canTriggerUpdateCheckUI() == SoftwareUpdateController.Availability.AVAILABLE) {
    controller.triggerUpdateCheckUI();
}
```

Make sure to save all user data before calling this method, as it may cause the process to shut down.

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
