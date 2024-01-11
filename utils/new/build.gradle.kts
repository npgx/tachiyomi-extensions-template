plugins {
    base
}

val extension by tasks.registering(NewExtension::class)

abstract class NewExtension : DefaultTask() {
    @get:Input
    @set:Option(option = "identifier", description = "name of the extension to generate")
    abstract var identifier: String

    @TaskAction
    fun create() {
        val identifier = identifier
        val extensionsDir = project.rootProject.layout.projectDirectory.dir("extensions").asFile
        val newExtension = extensionsDir.resolve(identifier)
        require(!newExtension.exists()) { "Already exists: $newExtension" }

        newExtension.mkdirs()
        newExtension.resolve("AndroidManifest.xml").apply { createNewFile() }.writeText(
            """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application>
    </application>
</manifest>
            """.trimIndent()
        )

        newExtension.resolve("build.gradle.kts").apply { createNewFile() }.writeText(
            """
plugins {
    id("com.android.application") version libs.versions.android.gradlePlugin
    kotlin("android") version libs.versions.kotlin.target
    kotlin("plugin.serialization") version libs.versions.kotlin.target
}

buildscript {
    dependencies {
        @Suppress("GradleDynamicVersion")
        classpath("local.buildsrc:conventions:+")
    }
}

setupTachiyomiExtensionConfiguration(
    namespaceIdentifier = "changeme", // will appear after eu.kanade.tachiyomi.extension
    extName = "Change Me",            // name of the extension, will appear after "Tachiyomi: "
    pkgNameSuffix = "changeme",       // convention is to use lowercase only letters (or numbers) version of extName
    extClass = ".ChangeMe",           // name of the class that is located in eu.kanade.tachiyomi.extension.{namespaceIdentifier}.{pkgNameSuffix}{extClass}
    // NOTE:    ^ the dot here is needed and intentional
    extVersionCode = 1,               // should increase it after a release
    isNsfw = false,                   // whether or not the extension manages Not Safe For Work content
)

            """.trimIndent()
        )

        newExtension.resolve("src/eu/kanade/tachiyomi/extension/namespaceIdentifier/pkgNameSuffix").mkdirs()
        newExtension.resolve("res").mkdirs()
    }
}
