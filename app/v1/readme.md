In order to make the app compile you need to add the opencv library on this level. Then add the lib as a module on Android studio and make it a dependency of the app.

This post has clear instructions of the process: https://stackoverflow.com/a/27421494

When the setup is finished yoou should hace a folder named *openCVLibrary330* with opencv's lib sources inside. Your `settings.gradle` file should also include a reference to the lib.
