/**
 * Type declarations for Electron applications built with Typescript.
 */
declare module '@hydraulic/conveyor-control' {
    export class Version {
        version: string;
        revision: number;

        constructor(version: string, revision?: number);

        compareTo(other: Version): number;

        toString(): string;

        static ComparableVersion: {
            new (version: string): {
                value: string;
                items: number[];
                parseVersion(version: string): number[];
                compareTo(other: InstanceType<typeof Version.ComparableVersion>): number;
                toString(): string;
            };
        };
    }

    /**
     * Object that lets you interact with Conveyor. Not all platforms are supported; call `canTriggerUpdateCheckUI` first.
     */
    export class OnlineUpdater {
        isWindows: boolean;
        isLinux: boolean;
        isMac: boolean;
        updateSiteURL: string;
        appDir: string;

        /**
         * Create an OnlineUpdater.
         * @param {string} updateSiteURL - The base URL of the update site (same as `app.site.base-url` in your Conveyor config).
         */
        constructor(updateSiteURL: string);

        /**
         * Get the current version of the application by reading the `metadata.properties` file in your update site.
         * @returns {Version} The current version.
         */
        getCurrentVersion(): Version;

        /**
         * Triggers the update process. If there is an update available it will be applied automatically (on Windows) or prompt the user
         * (on macOS), and the app will be restarted if the update is applied. You should ensure you're in a position to restart without the
         * user losing data before calling this.
         *
         * @throws {Error} If update checks are unavailable.
         */
        triggerUpdateCheckUI(): void;

        /**
         * Get the current version from the repository.
         * @returns {Promise<Version>} A promise that resolves with the current version from the repository.
         */
        getCurrentVersionFromRepository(): Promise<Version>;

        /**
         * Check if the update check UI can be triggered.
         * @returns {string} 'AVAILABLE' if the update check UI can be triggered, 
         *                   'UNSUPPORTED_PACKAGE_TYPE' if the package type is not supported,
         *                   'UNIMPLEMENTED' if not implemented for the current platform.
         */
        canTriggerUpdateCheckUI(): 'AVAILABLE' | 'UNSUPPORTED_PACKAGE_TYPE' | 'UNIMPLEMENTED';

        private getUpdateExePath(): string;

        private parseProperties(data: string): { [key: string]: string };
    }

    export const isMacOS: boolean;
    export const fs: typeof import('fs');
    export const path: typeof import('path');
    export const execFile: typeof import('child_process').execFile;
    export const https: typeof import('https');
    export const http: typeof import('http');
}
