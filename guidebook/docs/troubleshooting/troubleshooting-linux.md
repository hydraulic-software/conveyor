# Troubleshooting Linux

Stuck? Can't find what you need here? If you're a commercial user then you can write to [contact@hydraulic.dev](mailto:contact@hydraulic.dev). Otherwise feel free to post in [the GitHub discussion forum](https://github.com/hydraulic-software/conveyor/discussions).

## No icon appears in the applications menu

This can happen when the binary that your .desktop file points to doesn't exist. If your main binary isn't named after the `app.fsname` key and placed in the `bin` sub-directory of your package inputs then this can happen. You can fix it by specifying the correct location of your entrypoint:

```
app.linux.desktop-file."Desktop Entry".Exec = ${app.linux.install-path}/my_binary_name
```

## The application menu has an icon but when started the app window doesn't

Firstly, make sure you're testing by starting the app from the GUI. If you start it from the terminal then the icon won't appear, this is a Linux limitation.

If that doesn't fix it then you may need to alter your `StartupWMClass` `.desktop` file key to match whatever your app is using or setting. To find out the right setting start your app and then run `xprop WM_CLASS` and move the mouse over your app's window. The cursor should change to a crosshair and on clicking, the correct WM_CLASS string should be printed. If that doesn't work you may be packaging a Wayland app. You can find the WM_CLASS by pressing Alt-F2 in GNOME, typing `lg` (for Looking Glass), clicking Windows in the top right and then finding the right window.

Once you know the right WM_CLASS for your application, set it in your config:

```
app.linux.desktop-file."Desktop Entry".StartupWMClass = my_binary
```
