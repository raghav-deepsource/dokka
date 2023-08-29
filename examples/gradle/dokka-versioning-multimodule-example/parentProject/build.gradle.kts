import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:versioning-plugin:1.8.20")
    }

    repositories {
        mavenCentral()
    }
}

val currentVersion = "1.0"
val previousVersionsDirectory = project.rootProject.projectDir.resolve("previousDocVersions").invariantSeparatorsPath

// Main configuration for the versioning plugin. It will generate documentation for
// the current version of the application, and look for previous versions of docs
// in the directory defined in previousVersionsDirectory, allowing it to create
// the version navigation dropdown menu.
tasks.dokkaHtmlMultiModule {
    pluginConfiguration<VersioningPlugin, VersioningConfiguration> {
        version = currentVersion
        olderVersionsDir = file(previousVersionsDirectory)
    }
}
