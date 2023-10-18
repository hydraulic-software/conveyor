const PluginBase = require("@electron-forge/plugin-base")
const assert = require('assert');
const fs = require('fs');

class OverrideZip extends PluginBase.default {
  constructor(config) {
    super(config);
    this.config = config || {}
  }
  getHooks () {
    return {
      postMake: [this.postMake.bind(this)]
    };
  }

  postMake (forgeConfig, results) {
    if (process.platform != 'darwin') return;
    const {zipPaths} = this.config;
    assert(zipPaths, 'No zipPaths specified');

    const oldZips = {}
    for(const result of results) {
      const found = result.artifacts.find((name) => name.endsWith('.zip'));
      if (found) {
        oldZips[result.arch] = found;
      }
    }

    for (const cpu in oldZips) {
      if (cpu in zipPaths) {
        const oldZip = oldZips[cpu];
        const newZip = zipPaths[cpu];
        fs.copyFileSync(newZip, oldZip);
      }
    }
  }
}

module.exports = {
  default: OverrideZip,
}