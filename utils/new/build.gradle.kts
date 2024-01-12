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
    namespaceIdentifier = "changeme",
    extName = "$identifier",
    pkgNameSuffix = "$identifier",
    extClass = ".${identifier.replaceFirstChar { it.uppercase() }}",
    // NOTE:    ^ the dot here is needed and intentional
    extVersionCode = 1,
    isNsfw = false,
)

            """.trimIndent()
        )

        newExtension.resolve("src/eu/kanade/tachiyomi/extension").mkdirs()
        newExtension.resolve("res").mkdirs()
    }
}
