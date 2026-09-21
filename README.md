# snug-javafx-demo

A minimal JavaFX 25 application used as a demo target for [**snug**](https://github.com/synapticloop/snug) — the snug exe creator.

It exists to give `snug` something real (and small) to package into a native Windows executable, so the packaging toolchain can be exercised end-to-end on a self-contained desktop app.

## What it does

A single window with the snug logo and a "Hello!" button:

- **Press** the button → the logo swaps to the waving variant.
- **Release** → it swaps back.
- **Click** → the welcome label updates.

## Project layout

```
src/main/java/synapticloop/snugjavafxdemo/
  HelloApplication.java   // JavaFX Application entry point
  HelloController.java     // button + image-swap handlers
  Launcher.java
src/main/resources/
  assets/images/           // snug-logo.png, snug-logo-wave.png
  synapticloop/snugjavafxdemo/
    hello-view.fxml        // UI layout
    styles.css
```

## Build & run

Requires JDK 25 (the Gradle toolchain will provision it).

```sh
./gradlew run              # run from source
./gradlew shadowJar        # build build/libs/snug-javafx-demo-all.jar
java -jar build/libs/snug-javafx-demo-all.jar
```

## About snug

This project is a smoke-test fixture for [snug](https://github.com/synapticloop/snug), a tool for wrapping JVM applications as standalone `.exe` installers. See the snug repo for the actual packaging instructions.