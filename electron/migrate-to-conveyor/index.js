const assert = require('assert');
const {app, dialog} = require('electron');
const debug = require('debug')('migrate-to-conveyor');
const fs = require('fs');
const path = require('path');
const { spawn, execFileSync } = require('child_process');
const isDev = require('electron-is-dev');

function executeInstaller(installer, output) {
  // Copy the file in case it is packed inside an ASAR.
  fs.copyFileSync(installer, output);
  execFileSync(output, [], { detached: true });
}

function uninstallApp() {
  const updateExe = path.resolve(path.dirname(process.execPath), '..', 'Update.exe');
  spawn(updateExe, ['--uninstall'], { detached: true });
}

function formatError(error) {
  var result = `${error.name}: ${error.message}`;
  if (error.cause) {
    result += `\nCaused by: ${formatError(error.cause)}`;
  }
  return result;
}

module.exports = function(opts) {
  // Electron sets process.windowsStore to true if the app is packaged via MSIX, Even when not in the Windows store.
  if (process.windowsStore) {
      debug('Already migrated.');
      return false;
  }

  // Make sure to finish any Squirrel lifecycle actions when migrating.
  // Paradoxically, we run this before checking for the presence of Squirrel because those checks are very cheap.
  if (require('electron-squirrel-startup')) return true;

  if (isDev) {
    debug('Running in development, ignoring the migration.');
    return false;
  }
  if (process.env.npm_lifecycle_event) {
    debug('Running from npm phase "%s", ignoring the migration.', process.env.npm_lifecycle_event);
    return false;
  }
  // This module is only meant for the migration from Squirrel on Windows, and has no effect on macOS.
  // To migrate from Squirrel on macOS, see https://conveyor.hydraulic.dev/latest/migrating-electron-apps/
  if (process.platform != 'win32') {
    debug('Not running on Windows.');
    return false;
  }
  // If the user is trying out the Conveyor Windows ZIP distribution, the MSIX check above wouldn't trigger, so we check for the presence
  // of "squirrel.exe" before triggering the migration.
  // This ensures that the same code containing the call to this module can be shipped with both Squirrel and Conveyor.
  if (!fs.existsSync(path.resolve(path.dirname(process.execPath), 'squirrel.exe'))) {
    debug('Not using Squirrel.');
    return false;
  }

  try {
    const {windowsInstaller, outputFileName = 'migrate.exe'} = opts;
    assert(windowsInstaller, 'Missing required option: "windowsInstaller".');
    debug('Migrating to Conveyor with Windows installer: %s', windowsInstaller);

    executeInstaller(windowsInstaller, outputFileName);
    uninstallApp();
  } catch (error) {
    dialog.showErrorBox('Error initializing app', formatError(error));
  }
  app.quit();
  return true;
}