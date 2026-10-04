# snug-javafx-demo

A minimal JavaFX 25 application used as a demo target for [**snug**](https://github.com/synapticloop/snug) — the snug exe creator.

It exists to give `snug` something real (and small) to package into a native Windows executable, so the packaging toolchain can be exercised end-to-end on a self-contained desktop app.

## What it does

A single window with the snug logo and a "Hello!" button:

- **Click the button** → the logo plays a short wave animation (3 logo↔wave cycles) and the welcome label updates.
- **Click the logo on the coffee cup** → the welcome label says "Mmmmm - coffee".
- **Click the logo anywhere else** → it briefly switches to a surprised "ooh" variant, then snaps back to the logo after a second of no clicks.
- **The "Snug JavaFX Demo" menu** → an About dialog (logo, version), and Quit.

  Quit is bound to the platform's normal modifier key plus `Q` — `⌘Q` on macOS,
  `Ctrl+Q` elsewhere. The accelerator is declared once in the FXML as
  `accelerator="Shortcut+Q"`; JavaFX resolves and renders `Shortcut` per
  platform, so there is no per-OS code.

  On macOS the bar moves out of the window and into the Apple menu bar
  (`MenuBar.setUseSystemMenuBar(true)`, set from `HelloController.initialize()`
  behind an OS check). JavaFX converts the items to a native `NSMenu`,
  accelerators included, so `⌘Q` is a real key equivalent. Windows and Linux
  have no system menu bar in Glass, so they keep the in-window bar. The FXML
  must stay on `MenuItem` / `SeparatorMenuItem` — JavaFX silently ignores the
  hand-over if a `CustomMenuItem` is ever added.

## Project layout

The app follows a thin MVVM split. Base package is `synapticloop.snug.demo.javafx`:

```
src/main/java/synapticloop/snug/demo/javafx/
  Launcher.java                          // plain main() that calls Application.launch
  HelloApplication.java                  // Application entry; wires Model → ViewModel → View, owns shutdown
  model/
    SnugModel.java                       // domain state + rules (logo state, welcome text, wave frame)
    LogoState.java                       // LOGO | WAVE | OOH
    ModelListener.java                   // Model → ViewModel callback surface
  viewmodel/
    HelloViewModel.java                  // JavaFX properties + animation timing (Timeline / PauseTransition)
  controller/
    HelloController.java                 // View glue: @FXML bindings, coffee-rect hit-test, menu actions
src/main/resources/
  assets/images/                         // snug-logo.png, snug-logo-wave.png, snug-logo-ooh.png
  synapticloop/snug/demo/javafx/
    hello-view.fxml                      // UI layout, including the menu bar and its Shortcut+Q accelerator
    styles.css
```

The menu bar is declared in `hello-view.fxml` — layout stays in the view file.
Its two actions live in `HelloController`: About opens a modal `Alert` parented
to the stage, and Quit hands off to the same `HelloApplication.shutdown()` the
window's close button uses, so there is one exit path rather than two.

## Build & run

Requires JDK 25 (the Gradle toolchain will provision it).

```sh
./gradlew run              # run from source
./gradlew shadowJar        # build build/libs/snug-javafx-demo-all.jar
java -jar build/libs/snug-javafx-demo-all.jar
```

## About snug

This project is a smoke-test fixture for [snug](https://github.com/synapticloop/snug), a tool for wrapping JVM applications as standalone `.exe` installers. See the snug repo for the actual packaging instructions.
