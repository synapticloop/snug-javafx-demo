plugins {
    java
    application
    id("com.gradleup.shadow") version "9.6.1"
}

group = "synapticloop"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"
val javafxVersion = "25"

// JavaFX classifier matching gradle-osdetector's naming (was provided by the
// removed org.openjfx.javafxplugin via its `javafx.platform.classifier` extension).
val javafxClassifier: String = when {
    System.getProperty("os.name").lowercase().contains("win") -> "win"
    System.getProperty("os.name").lowercase().contains("mac") ->
        if (System.getProperty("os.arch") == "aarch64") "mac-aarch64" else "mac"
    System.getProperty("os.arch") == "aarch64" -> "linux-aarch64"
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
    mainModule.set("synapticloop.snugjavafxdemo")
    mainClass.set("synapticloop.snugjavafxdemo.HelloApplication")
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

// Fat/uber jar — `./gradlew shadowJar` → build/libs/snug-javafx-demo-all.jar
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
//
// The source stays modular (module-info.java still compiles against real
// JavaFX modules on the module path during dev). Only the distribution
// shape is classpath.
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("all")
    mergeServiceFiles()
    exclude("**/module-info.class")
    manifest {
        attributes["Main-Class"] = "synapticloop.snugjavafxdemo.HelloApplication"
    }
}
