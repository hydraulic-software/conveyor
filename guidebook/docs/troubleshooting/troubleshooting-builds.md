# Troubleshooting

Stuck? Can't find what you need here? If you're a commercial user then you can write to [contact@hydraulic.software](mailto:contact@hydraulic.software). Otherwise feel free to post in [the GitHub discussion forum](https://github.com/hydraulic-software/conveyor/discussions).

## My public keys don't match

If you get an error like this:

```
The private key (foo.key) doesn't match the public key found in the certificate file bar.crt.
```

then either:

1. You have multiple keys/certificates and are mixing them up, or
2. Your CA has given you a `.crt` file containing _intermediate_ certificates, i.e. _their_ certificates and then supplied yours in a 
   separate file. If that's the case and you have more than one `.crt` file, create a combined file like this: 
   `cat smaller.crt bigger.crt >combined.crt` and then set `app.windows.certificate = combined.crt`. Remember that these file paths
   are interpreted relative to the config file that contains them, so place the combined crt file next to `defaults.conf` if that's where
   you're setting these keys.

## I get an error about site consistency checks

Conveyor will probe your download site to catch certain types of mistakes. If it can't do this probe e.g. because the site isn't online yet then you'll get an error. You can convert these errors to warnings by setting `app.site.consistency-checks = warn`. AWS S3 can cause problems because it can return 403 Permission Denied for files that don't exist instead of the more normal 404 Not Found. You can add the `s3:ListBucket` permission to the bucket policy to solve this.

Conveyor currently does the following checks:

1. Ensures you aren't building a version that was already uploaded when targeting Windows. Overwriting already released versions is never a good idea but can especially cause problems for Windows 10 (Windows 11 is more robust).
2. Ensures you aren't changing the signing identity of Windows packages. Changing your signing identity e.g. due to a corporate renaming, change of HQ location or change of certificate type will break updates by making Windows think it's a totally separate app from a different vendor. It requires a special process that Conveyor doesn't currently directly support to establish continuity of identity.
3. Checks whether your site is using "flat" layout or not. This check can usually be ignored, as it exists for backwards compatibility.

In the future it will do other checks to catch other kinds of issues.

## Use good config style

Conveyor has a large default configuration that is included before your own. Following two simple rules will help avoid problems:

1. If you assign new values to lists you may overwrite any default values already there. Sometimes that's what you want, but usually your intent is to extend the defaults, not replace them. Good style is therefore to add to lists using `+=` or by writing `list = ${list} [ ... new items ... ] `. If you deviate from that and assign lists directly (`list = [a, b]`) then add a comment explaining why overriding the defaults is intended.
2. Defining your own keys is of course fine, and can be useful for substituting into other values. But define them either at the top level or group them into a top level block, rather than placing them next to where they're being used. Conveyor might add new config settings in future and if you pick the same name you'll end up overriding it, even if today it works.

## My passphrase is wrong

Two possibilities:

1. Make sure that if you change your HSM passphrase you also run `conveyor keys passphrase` to change the passphrase on the root key. See [changing your passphrase](../configs/keys-and-certificates.md#passphrases) for more information.
2. If you've copied your root key words somewhere else e.g. an environment variable in CI, make sure you re-copy it. Changing the passphrase changes the words in your default `app.signing-key` key, so you'll need to update any places you duplicated the original value. 

## Viewing logs and rerunning tasks

If Conveyor isn't working or you aren't sure why something is happening, try running it with the `--show-log` parameter. You should see logs from your last execution. Run with the `LOGGING=trace` environment variable to get lots more detail. You can also write logs to the terminal as it runs by using the `-v` and for even more detail the `-vv` flag.

If a change you're making doesn't seem to be picked up on a re-run of your chosen task, you might have found a caching bug. We aren't aware of any at this time but if you find one please report it to us and then use the `--rerun` flag to force a complete rebuild of the intermediate files used in packaging.

## Increase cache size limit if tasks rerun unnecessarily

The default cache size limit is 10 gigabytes, which is a reasonable size to contain all intermediate files for a single app. If you're building multiple different apps with Conveyor then the cache may start evicting entries more often than convenient. If you see tasks re-running and can't figure out why, try passing `--cache-limit=20.0` to give it more room.
