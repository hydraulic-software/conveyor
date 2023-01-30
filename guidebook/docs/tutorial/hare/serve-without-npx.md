!!! note "Serving localhost"
    Above we use `npm` and the JavaScript `serve` module as a simple localhost web server. You can use any web server that properly supports
    HTTP Content-Range requests. **Some servers have bugs** therefore we recommend using `npx serve .` even though this isn't a JavaScript project.
    You can also use [Caddyserver](https://caddyserver.com/) by running `caddy file-server --browse --listen :3000`. In particular
    **don't use the built in Python web server**. It won't work correctly for Windows installs.
