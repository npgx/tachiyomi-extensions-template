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
    namespaceIdentifier = "npgx",
    extName = "Project Suki",
    pkgNameSuffix = "projectsuki",
    extClass = ".ProjectSuki",
    extVersionCode = 4,
    isNsfw = false,
)
