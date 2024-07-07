const os = require('os');
const fs = require('fs');
const path = require('path');
const { execFile } = require('child_process');
const https = require('https');

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
        return `Version{ver='${this.version}', revision=${this.revision}}`;
    }

    static ComparableVersion = class {
        constructor(version) {
            this.value = version;
            this.items = this.parseVersion(version);
        }

        parseVersion(version) {
            return version.toLowerCase().split(/[-.]/).map(item => {
                if (/^\d+$/.test(item)) {
                    return parseInt(item, 10);
                }
                return item;
            });
        }

        compareTo(other) {
            const len = Math.max(this.items.length, other.items.length);
            for (let i = 0; i < len; i++) {
                const a = this.items[i];
                const b = other.items[i];
                if (a === undefined) return -1;
                if (b === undefined) return 1;
                if (typeof a !== typeof b) {
                    return typeof a === 'number' ? -1 : 1;
                }
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

class OnlineUpdater {
    constructor() {
        this.isWindows = process.platform === 'win32';
        this.isLinux = process.platform === 'linux';
        this.isMac = process.platform === 'darwin';
        this.repoUrl = process.env.APP_REPOSITORY_URL;
        this.fsname = process.env.APP_FSNAME;
        this.appDir = process.env.APP_DIR;
    }

    getCurrentVersion() {
        const ver = process.env.APP_VERSION;
        const revision = parseInt(process.env.APP_REVISION) || 0;
        return ver ? new Version(ver, revision) : null;
    }

    triggerUpdateCheckUI() {
        const availability = this.canTriggerUpdateCheckUI();
        if (availability !== 'AVAILABLE') {
            throw new Error(`Update checks unavailable: ${availability}`);
        }

        if (this.isWindows) {
            const updateExePath = this.getUpdateExePath();
            execFile(updateExePath, ['--update-check'], (error) => {
                if (error) {
                    console.error('Error triggering update check:', error);
                }
                process.exit(0);
            });
        } else {
            // For non-Windows platforms, we'll need to implement the native check later
            console.log('Update check triggered (native implementation needed)');
        }
    }

    getCurrentVersionFromRepository() {
        return new Promise((resolve, reject) => {
            const url = new URL(this.repoUrl);
            url.pathname = path.join(url.pathname, 'metadata.properties');

            https.get(url, (res) => {
                let data = '';
                res.on('data', (chunk) => data += chunk);
                res.on('end', () => {
                    const props = this.parseProperties(data);
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

    canTriggerUpdateCheckUI() {
        if (this.isWindows) {
            const updateExePath = this.getUpdateExePath();
            return fs.existsSync(updateExePath) ? 'AVAILABLE' : 'UNSUPPORTED_PACKAGE_TYPE';
        } else if (this.isLinux) {
            return 'UNIMPLEMENTED';
        } else if (this.isMac) {
            const sparkleFrameworkPath = path.join(this.appDir, '..', 'Frameworks', 'Sparkle.framework');
            return fs.existsSync(sparkleFrameworkPath) ? 'AVAILABLE' : 'UNSUPPORTED_PACKAGE_TYPE';
        } else {
            return 'UNIMPLEMENTED';
        }
    }

    getUpdateExePath() {
        return path.join(this.appDir, '..', 'updatecheck.exe');
    }

    parseProperties(data) {
        return data.split('\n').reduce((acc, line) => {
            const [key, value] = line.split('=').map(s => s.trim());
            if (key && value) acc[key] = value;
            return acc;
        }, {});
    }
}

module.exports = {
    OnlineUpdater,
    Version
};
