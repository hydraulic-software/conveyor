# 8. Set a real site URL

So far we've been using `localhost` as the download site URL. This is convenient for testing because Conveyor treats this as a sort of trial mode. Once you change the `app.site.base-url` config key to something else, there are two scenarios.

## Open source projects

Set the `app.vcs-url` key to the URL of your version control repository (e.g. `github.com/user/project`). You will be able to use Conveyor for free. Any version control system can be used, it doesn't have to be git. The download site URL you use must be publicly accessible, or become so soon after using Conveyor. If it never becomes accessible, or the downloads don't seem to match the source code, Conveyor will stop working until the issue is rectified.

It's also a good idea to set the `app.license` key to the name of your license, as Linux packages like to have this metadata.

You will probably want to [use GitHub Releases to host your binaries](../../configs/download-pages.md#publishing-through-github). That works fine and your installs will update from the latest GitHub release.

## Proprietary projects

License keys are associated with projects, and projects are defined by the download site URL. Anyone can build a project - Conveyor isn't licensed on a per user basis.

If you don't specify a public source repository then the config will be edited to include a freshly issued license key. Once you set your site URL to a non-localhost address you'll be asked to pay. This lets you overlap development and testing work with purchasing, which can be useful in companies where purchases might take time. You can change your site URL in a forwards direction, i.e. you can change the site URL associated with a key if you make a mistake but can't reset it to one you've used previously. If you need to do this anyway please contact us and we'll sort it out for you.

<script>var tutorialSection = 9;</script>
