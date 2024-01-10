import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.jvm.optionals.getOrDefault

fun Project.setupTachiyomiMultiSrcConfiguration(
    @Suppress("UNUSED_PARAMETER") vararg useParameterNames: Unit = emptyArray(),
    catalog: VersionCatalog = extensions.getByName<VersionCatalogsExtension>("versionCatalogs").named("libs"),
    compileSdk: Int = catalog.findVersion("sdk_compile").map { it.toString().toIntOrNull() }.getOrDefault(null) ?: error("Not found: libs.versions.sdk.compile"),
    minSdk: Int = catalog.findVersion("sdk_min").map { it.toString().toIntOrNull() }.getOrDefault(null) ?: error("Not found: libs.versions.sdk.min"),
    multiSrc: MultiSrc,
    libs: Set<TachiyomiLibrary> = setOf(),
) {
    extensions.getByName<LibraryExtension>("android").apply {
        this.compileSdk = compileSdk

        defaultConfig {
            this.minSdk = minSdk
        }

        namespace = "eu.kanade.tachiyomi.lib.${multiSrc.identifier}"
    }

    dependencies {
        "compileOnly"(catalog.findBundle("extension_compile").get())

        libs.forEach { lib ->
            "implementation"(project(":lib-${lib.identifier}"))
        }
    }

    project(":").tasks.named("compileMultiSrcKotlin") {
        it.dependsOn(project.tasks.named("compileDebugKotlin"), project.tasks.named("compileReleaseKotlin"))
    }

    tasks.withType<KotlinCompile>().configureEach {
        it.kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            apiVersion = catalog.findVersion("kotlin_api").map { it.toString() }.getOrDefault(null) ?: error("Not found: libs.versions.kotlin.api")
            languageVersion = catalog.findVersion("kotlin_language").map { it.toString() }.getOrDefault(null) ?: error("Not found: libs.versions.kotlin.language")
            freeCompilerArgs += "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        }
    }
}
