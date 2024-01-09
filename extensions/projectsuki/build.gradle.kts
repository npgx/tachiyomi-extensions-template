plugins {
    id("com.android.application") version libs.versions.android.gradlePlugin
    kotlin("android") version libs.versions.kotlin.target
    kotlin("plugin.serialization") version libs.versions.kotlin.target
}

buildscript {
    dependencies {
        @Suppress("GradleDynamicVersion")
        classpath("local.buildsrc:extension-conventions:+")
    }
}

setupTachiyomiExtensionConfiguration(
    compileBundle = libs.bundles.extension.compile,
    compileSdk = libs.versions.sdk.compile.get().toInt(),
    minSdk = libs.versions.sdk.min.get().toInt(),
    namespaceIdentifier = "npgx",
    extName = "Project Suki",
    pkgNameSuffix = "projectsuki",
    extClass = ".ProjectSuki",
    extVersionCode = 4,
    isNsfw = false,
)
