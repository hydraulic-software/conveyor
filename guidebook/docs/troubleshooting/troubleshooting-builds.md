# Troubleshooting

## Use good config style

Conveyor has a large default configuration that is included before your own. Following two simple rules will help avoid problems:

1. If you assign new values to lists you may overwrite any default values already there. Sometimes that's what you want, but usually your intent is to extend the defaults, not replace them. Good style is therefore to add to lists using `+=` or by writing `list = ${list} [ ... new items ... ] `. If you deviate from that and assign lists directly (`list = [a, b]`) then add a comment explaining why overriding the defaults is intended.
2. Defining your own keys is of course fine, and can be useful for substituting into other values. But define them either at the top level or group them into a top level block, rather than placing them next to where they're being used. Conveyor might add new config settings in future and if you pick the same name you'll end up overriding it, even if today it works.

## Viewing logs and rerunning tasks

If Conveyor isn't working or you aren't sure why something is happening, try running it with the `--show-log` parameter. You should see logs from your last execution. Run with the `LOGGING=trace` environment variable to get lots more detail. You can also write logs to the terminal as it runs by using the `-v` and for even more detail the `-vv` flag.

If a change you're making doesn't seem to be picked up on a re-run of your chosen task, you might have found a caching bug. We aren't aware of any at this time but if you find one please report it to us and then use the `--rerun` flag to force a complete rebuild of the intermediate files used in packaging.

## Increase cache size limit if tasks rerun unnecessarily

The default cache size limit is 5 gigabytes, which is a reasonable size to contain all intermediate files for a single app. If you're building multiple different apps with Conveyor then the cache may start evicting entries more often than convenient. If you see tasks re-running and can't figure out why, try passing `--cache-limit=10.0` to give it more room. 
