plugins {
    kotlin("jvm") version libs.versions.kotlin.gradleCompatible
    kotlin("plugin.serialization") version libs.versions.kotlin.gradleCompatible
}

group = "local.buildsrc"
version = "0.1.0"

dependencies {
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())

    api(libs.kotlin.json)
    api("de.undercouch.download:de.undercouch.download.gradle.plugin:5.5.0")

    compileOnly("org.jetbrains.kotlin.multiplatform:org.jetbrains.kotlin.multiplatform.gradle.plugin:${libs.versions.kotlin.target.get()}")
    compileOnly("com.android.library:com.android.library.gradle.plugin:${libs.versions.android.gradlePlugin.get()}")
}
