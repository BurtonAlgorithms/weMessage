# weServer

### Compilation and Debugging

weServer is compiled through a series of Gradle tasks. Inside Android Studio, create a new Gradle run configuration, set the task for ``:server:build`` and under arguments use the following parameter: ``jar dependencyJar proguard``. This will build the dependency.jar and weServer.jar

Everything inside the ``scripts`` folder will also have to be compiled. The AppleScripts can only be compiled on a Mac computer. For best results, the any JavaScript files under this directory should also be compiled on a macOS environment. You can do this by using the Script Editor application to save and export these scripts as ``.scpt`` and ``.js`` files.

By default, weServer's folder structure looks like this:

```
  weServer/
    ├── assets/
    │   ├── AppLogo.png
    ├── bin/
    │   ├── ffmpeg/
    │   │   ├── ffmpeg //A FFmpeg binary for macOS
    │   ├── dependencies.jar
    ├── scripts/
    │   ├── AddParticipant.scpt
    │   ├── ContactSync.scpt
    │   ├── CreateGroup.scpt
    │   ├── Handlers.scpt
    │   ├── Helpers.scpt
    │   ├── JSSendMessage.js
    │   ├── LeaveGroup.scpt
    │   └── ...
    ├── run.command
    └── weServer.jar
```

You can copy any files that are not included in the source code from the weServer distributable (zip file) hosted on our website, such as ``AppLogo.png`` or the ffmpeg binary.

ProGuard minification is enabled by default. In addition, the ``.scpt`` files included in the zip distributable have been saved with the "Run-only" option, which is why they are not viewable or editable via Script Editor.

For debugging any errors in weServer, turn off ProGuard minification (remove the proguard flag from the Gradle run configuration).

By default, crash reports are uploaded to [Sentry](https://sentry.io). You can disable this, or change your Sentry DSN provider in the ``weMessage.java`` file. In addition, notification processing and version checking is done through Firebase Cloud Functions, which is hosted on our own Firebase instance. You can change the URLs for these functions in the ``weMessage.java`` file as well.