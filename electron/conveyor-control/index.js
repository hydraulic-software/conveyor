const fs = require('fs');
const path = require('path');
const { execFile } = require('child_process');
const https = require('https');
const http = require('http');
const { app } = require('electron');

class Version {
    constructor(version, revision = 0) {
        this.version = version;
        this.revision = revision;
    }

    compareTo(other) {
        const v1 = new Version.ComparableVersion(this.version);
        const v2 = new Version.ComparableVersion(other.version);
        const comparison = v1.compareTo(v2);
        if (comparison === 0) {
            return Math.sign(this.revision - other.revision);
        }
        return comparison;
    }

    toString() {
        return `${this.version}${this.revision > 0 ? '.' + this.revision : ''}`;
    }

    static ComparableVersion = class {
        constructor(version) {
            this.value = version;
            this.items = this.parseVersion(version);
        }

        parseVersion(version) {
            return version.split('.').map(item => parseInt(item, 10) || 0);
        }

        compareTo(other) {
            const len = Math.max(this.items.length, other.items.length);
            for (let i = 0; i < len; i++) {
                const a = this.items[i] || 0;
                const b = other.items[i] || 0;
                if (a !== b) {
                    return a < b ? -1 : 1;
                }
            }
            return 0;
        }

        toString() {
            return this.value;
        }
    }
}

const isMacOS = process.platform === 'darwin';
let koffi = null;
let libconveyor = null;
let conveyor_check_for_updates = null;

/**
 * Object that lets you interact with Conveyor. Not all platforms are supported; call `canTriggerUpdateCheckUI` first.
 */
class OnlineUpdater {
    /**
     * Create an OnlineUpdater.
     * @param {string} updateSiteURL - The base URL of the update site (same as `app.site.base-url` in your Conveyor config).
     */
    constructor(updateSiteURL) {
        this.isWindows = process.platform === 'win32';
        this.isLinux = process.platform === 'linux';
        this.isMac = process.platform === 'darwin';
        this.updateSiteURL = updateSiteURL
        this.appDir = path.dirname(app.getPath('exe'));
    }

    /**
     * Get the current version of the application by reading the `metadata.properties` file in your update site.
     * @returns {Version} The current version.
     */
    getCurrentVersion() {
        const ver = app.getVersion();
        const parts = ver.split('.');
        const revision = parts.length >= 4 ? parseInt(parts[3], 10) : 0;
        return new Version(parts.slice(0, 3).join('.'), revision);
    }

    /**
     * Triggers the update process. If there is an update available it will be applied automatically (on Windows) or prompt the user
     * (on macOS), and the app will be restarted if the update is applied. You should ensure you're in a position to restart without the
     * user losing data before calling this.
     *
     * @throws {Error} If update checks are unavailable.
     */
    triggerUpdateCheckUI() {
        const availability = this.canTriggerUpdateCheckUI();
        if (availability !== 'AVAILABLE') {
            throw new Error(`Update checks unavailable: ${availability}`);
        }

        if (this.isWindows) {
            const updateExePath = this.#getUpdateExePath();
            execFile(updateExePath, ['--update-check'], (error) => {
                if (error) {
                    console.error('Error triggering update check:', error);
                }
                process.exit(0);
            });
        } else if (this.isMac) {
            if (this.canTriggerUpdateCheckUI() === 'AVAILABLE' && conveyor_check_for_updates) {
                conveyor_check_for_updates();
            } else {
                console.log('Native update check function not available');
            }
        } else {
            console.log('Update check triggered (native implementation needed)');
        }
    }

    /**
     * Get the current version from the repository.
     * @returns {Promise<Version>} A promise that resolves with the current version from the repository.
     */
    getCurrentVersionFromRepository() {
        return new Promise((resolve, reject) => {
            const url = new URL(this.updateSiteURL);
            url.pathname = path.join(url.pathname, 'metadata.properties');

            const protocol = url.protocol === 'https:' ? https : http;

            protocol.get(url, (res) => {
                let data = '';
                res.on('data', (chunk) => data += chunk);
                res.on('end', () => {
                    const props = this.#parseProperties(data);
                    if (!props['app.version']) {
                        reject(new Error('Cannot find app.version key in download site metadata.properties'));
                    } else {
                        const ver = props['app.version'];
                        const revision = parseInt(props['app.revision']) || 0;
                        resolve(new Version(ver, revision));
                    }
                });
            }).on('error', reject);
        });
    }

    /**
     * Check if the update check UI can be triggered.
     * @returns {string} 'AVAILABLE' if the update check UI can be triggered, 
     *                   'UNSUPPORTED_PACKAGE_TYPE' if the package type is not supported,
     *                   'UNIMPLEMENTED' if not implemented for the current platform.
     */
    canTriggerUpdateCheckUI() {
        if (this.isWindows) {
            const updateExePath = this.#getUpdateExePath();
            return fs.existsSync(updateExePath) ? 'AVAILABLE' : 'UNSUPPORTED_PACKAGE_TYPE';
        } else if (this.isLinux) {
            return 'UNIMPLEMENTED';
        } else if (this.isMac) {
            const sparkleFrameworkPath = path.join(this.appDir, '..', 'Frameworks', 'Sparkle.framework');
            if (!fs.existsSync(sparkleFrameworkPath)) {
                console.error('Failed to locate Sparkle framework at path:', sparkleFrameworkPath);
                return 'UNSUPPORTED_PACKAGE_TYPE';
            }
            
            if (!koffi) {
                try {
                    koffi = require('koffi');
                } catch (error) {
                    console.error('Failed to load koffi:', error);
                    return 'UNSUPPORTED_PACKAGE_TYPE';
                }
            }

            if (!libconveyor) {
                try {
                    libconveyor = koffi.load(path.join(app.getAppPath(), '..', '..', 'Frameworks', 'libconveyor.dylib'));
                    conveyor_check_for_updates = libconveyor.func('void conveyor_check_for_updates()');
                } catch (error) {
                    console.error('Failed to load libconveyor:', error);
                    return 'UNSUPPORTED_PACKAGE_TYPE';
                }
            }

            return 'AVAILABLE';
        } else {
            return 'UNIMPLEMENTED';
        }
    }

    #getUpdateExePath() {
        return path.join(this.appDir, 'updatecheck.exe');
    }

    #parseProperties(data) {
        return data.split('\n').reduce((acc, line) => {
            line = line.trim();
            if (line && !line.startsWith('#')) {
                const [key, value] = line.split('=').map(s => s.trim());
                if (key && value) acc[key] = value;
            }
            return acc;
        }, {});
    }
}

module.exports = {
    OnlineUpdater,
    Version
};
