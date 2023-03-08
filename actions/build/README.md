# Build action

The build action downloads, installs and executes Conveyor. It caches both the installation and the Conveyor cache itself,
so that subsequent runs can go faster.

## Usage
```yaml
- uses: hydraulic-software/conveyor/actions/build@v1
  with:
    # Which conveyor command to execute. For example, 'make copied-site'.
    # Default: 'make site'
    command: ''
    
    # The root signing key for your application, corresponding to config key 'app.signing-key'.
    # This is required to properly sign your app. We recommend storing your signing key in
    # your GitHub repository secrets as in the example below.
    signing_key: ${{ secrets.SIGNING_KEY }}
    
    # Which version of the Conveyor End User License Agreement you agree to.
    # You must agree to the EULA to run Conveyor: https://hydraulic.software/eula.html
    # The current version is 1.
    agree_to_license: 1
    
    # Additional flags to pass to Conveyor.
    extra_flags: ''
    
    # Optional cache key used to store the Conveyor installation and task cache.
    # Default: 'conveyor'
    cache_key: ''
    
    # Optional path to the location where Conveyor will be installed and keep the task cache.
    # Default: '.conveyor'
    cache_path: ''
    
    # Version of Conveyor to run.
    # Default: 7.1
    conveyor_version: ''
```


