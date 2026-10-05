plugins {
    java
    application
    id("com.gradleup.shadow") version "9.6.1"
}

group = "synapticloop"
version = "1.0.0"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"
val javafxVersion = "25"

// Host OS, read once. The Gradle JVM runs on the machine doing the build,
// so this is the platform the artifacts are being produced for.
val osName = System.getProperty("os.name").lowercase()
val osArch = System.getProperty("os.arch")

// JavaFX classifier matching gradle-osdetector's naming (was provided by the
// removed org.openjfx.javafxplugin via its `javafx.platform.classifier` extension).
val javafxClassifier: String = when {
    osName.contains("win") -> "win"
    osName.contains("mac") -> if (osArch == "aarch64") "mac-aarch64" else "mac"
    osArch == "aarch64" -> "linux-aarch64"
    else -> "linux"
}

// Coarser OS family, used to name the shadow jar. A build already carries the
// host's natives (glass.dll / libglass.dylib / libglass.so) inside it, so the
// file name records which platform it was actually built for. Deliberately
// coarser than javafxClassifier, which additionally encodes the CPU
// architecture — aarch64 and x64 jars both say "macos" here.
val shadowJarOs: String = when {
    osName.contains("win") -> "windows"
    osName.contains("mac") -> "macos"
    else -> "linux"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    // Promote classpath deps to the module path so module-info's
    // `requires javafx.controls` resolves. Required when JavaFX deps are
    // declared as plain `implementation(...)` rather than via the
    // (now removed) org.openjfx.javafxplugin.
    modularity.inferModulePath = true
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("synapticloop.snug.demo.javafx")
    mainClass.set("synapticloop.snug.demo.javafx.HelloApplication")
    // JavaFX 25 + JDK 22+ (JEP 454): javafx.graphics's NativeLibLoader calls
    // System::load, which is restricted. Grant the module native access so the
    // JVM stops printing the warning and, eventually, stops blocking the call.
    applicationDefaultJvmArgs = listOf("--enable-native-access=javafx.graphics")
}

// JavaFX dependencies declared directly (the org.openjfx.javafxplugin is
// unmaintained and incompatible with Gradle 9 — uses removed VersionNumber API).
// The :${javafxClassifier} classifier is REQUIRED — without it you get the parent
// POM only, not a real jar (this is what the openjfx plugin used to inject).
dependencies {
    implementation("org.openjfx:javafx-base:${javafxVersion}:${javafxClassifier}")
    implementation("org.openjfx:javafx-graphics:${javafxVersion}:${javafxClassifier}")
    implementation("org.openjfx:javafx-controls:${javafxVersion}:${javafxClassifier}")
    implementation("org.openjfx:javafx-fxml:${javafxVersion}:${javafxClassifier}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Fat/uber jar — `./gradlew shadowJar` → build/libs/snug-javafx-demo-<os>.jar
// (e.g. snug-javafx-demo-windows.jar, snug-javafx-demo-macos.jar,
// snug-javafx-demo-linux.jar; the OS comes from the build host).
//
// Notes on JPMS + fat-jar:
//   - Each JavaFX module jar carries its own module-info.class. Flattening
//     them all into one jar with the app's module-info would create a
//     single-module jar that says `requires javafx.controls` but has those
//     classes inlined at the root — which JPMS cannot resolve.
//   - So the fat jar is a CLASSPATH jar (no module-info at root). JavaFX
//     21+ runs fine on classpath: its NativeLibLoader extracts the
//     classified natives (glass.dll / libglass.so / libglass.dylib) from
//     inside the jar to a temp dir on first launch.
//   - When running the fat jar directly (java -jar ...), pass
//     --enable-native-access=javafx.graphics to suppress the JEP 454 warning
//     from NativeLibLoader calling System::load.
//
// The source stays modular (module-info.java still compiles against real
// JavaFX modules on the module path during dev). Only the distribution
// shape is classpath.
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    // Name the artifact after the platform it was built for:
    // snug-javafx-demo-windows.jar / -macos.jar / -linux.jar.
    //
    // The version is dropped because a project property is not something the
    // documentation wants to restate; an empty archiveVersion is what makes
    // Gradle omit the segment and its separator. The OS goes in the classifier
    // slot so the file name stays derived from the project name rather than
    // being hard-coded.
    archiveVersion.set("")
    archiveClassifier.set(shadowJarOs)
    mergeServiceFiles()
    exclude("**/module-info.class")
    manifest {
        attributes["Main-Class"] = "synapticloop.snug.demo.javafx.Launcher"
    }
}
