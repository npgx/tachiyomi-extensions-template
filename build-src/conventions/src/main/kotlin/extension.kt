import com.android.build.api.dsl.ApkSigningConfig
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.RegularFile
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.jvm.optionals.getOrDefault

sealed class LibVersion(val versionName: String) {
    data object V4 : LibVersion("1.4")
    data object V5 : LibVersion("1.5")
}

sealed class TachiyomiLibrary(val identifier: String) {
    data object CryptoAES : TachiyomiLibrary("cryptoaes")
    data object DataImage : TachiyomiLibrary("dataimage")
    data object I18n : TachiyomiLibrary("i18n")
    data object RandomUA : TachiyomiLibrary("randomua")
    data object Synchrony : TachiyomiLibrary("synchrony")
    data object TextInterceptor : TachiyomiLibrary("textinterceptor")
    data object Unpacker : TachiyomiLibrary("unpacker")
}

sealed class MultiSrc(val identifier: String) {
    data object A3Manga : MultiSrc("a3manga")
    data object Bakamanga : MultiSrc("bakamanga")
    data object Bakkin : MultiSrc("bakkin")
    data object Bilibili : MultiSrc("bilibili")
    data object ComicGamma : MultiSrc("comicgamma")
    data object EroMuse : MultiSrc("eromuse")
    data object FansubsCat : MultiSrc("fansubscat")
    data object FlixScans : MultiSrc("flixscans")
    data object FMReader : MultiSrc("fmreader")
    data object FoolSlide : MultiSrc("foolslide")
    data object Gattsu : MultiSrc("gattsu")
    data object GigaViewer : MultiSrc("gigaviewer")
    data object Grouple : MultiSrc("grouple")
    data object Guya : MultiSrc("guya")
    data object HeanCms : MultiSrc("heancms")
    data object HentaiHand : MultiSrc("hentaihand")
    data object Kemono : MultiSrc("kemono")
    data object LibGroup : MultiSrc("libgroup")
    data object Madara : MultiSrc("madara")
    data object MadTheme : MultiSrc("madtheme")
    data object MangaBox : MultiSrc("mangabox")
    data object MangaCatalog : MultiSrc("mangacatalog")
    data object Mangadventure : MultiSrc("mangadventure")
    data object MangaHub : MultiSrc("mangahub")
    data object MangaMainac : MultiSrc("mangamainac")
    data object MangaRaw : MultiSrc("mangaraw")
    data object MangaReader : MultiSrc("mangareader")
    data object MangaThemesia : MultiSrc("mangathemesia")
    data object MangaWorld : MultiSrc("mangaworld")
    data object MCCMS : MultiSrc("mccms")
    data object MMRCMS : MultiSrc("mmrcms")
    data object Monochrome : MultiSrc("monochrome")
    data object MultiChan : MultiSrc("multichan")
    data object MyMangaCMS : MultiSrc("mymangacms")
    data object NepNep : MultiSrc("nepnep")
    data object OtakuSanctuary : MultiSrc("otakusanctuary")
    data object Paprika : MultiSrc("paprika")
    data object PizzaReader : MultiSrc("pizzareader")
    data object ReadAllComics : MultiSrc("readallcomics")
    data object ReaderFront : MultiSrc("readerfront")
    data object Senkuro : MultiSrc("senkuro")
    data object SinMH : MultiSrc("sinmh")
    data object Webtoons : MultiSrc("webtoons")
    data object WPComics : MultiSrc("wpcomics")
    data object ZBulu : MultiSrc("zbulu")
    data object ZeistManga : MultiSrc("zeistmanga")
    data object ZManga : MultiSrc("zmanga")
}

fun Project.setupTachiyomiExtensionConfiguration(
    @Suppress("UNUSED_PARAMETER") vararg useParameterNames: Unit = emptyArray(),
    catalog: VersionCatalog = extensions.getByName<VersionCatalogsExtension>("versionCatalogs").named("libs"),
    compileSdk: Int = catalog.findVersion("sdk_compile").map { it.toString().toIntOrNull() }.getOrDefault(null) ?: error("Not found: libs.versions.sdk.compile"),
    minSdk: Int = catalog.findVersion("sdk_min").map { it.toString().toIntOrNull() }.getOrDefault(null) ?: error("Not found: libs.versions.sdk.min"),
    namespaceIdentifier: String,
    extName: String,
    pkgNameSuffix: String,
    extClass: String,
    extFactory: String = "",
    extVersionCode: Int,
    isNsfw: Boolean,
    libVersion: LibVersion = LibVersion.V4,
    readmeFile: RegularFile = project.layout.projectDirectory.file("README.md"),
    changelogFile: RegularFile = project.layout.projectDirectory.file("CHANGELOG.md"),
    kotlinApiVersion: String = catalog.findVersion("kotlin_api").map { it.toString() }.getOrDefault(null) ?: error("Not found: libs.versions.kotlin.api"),
    kotlinLanguageVersion: String = catalog.findVersion("kotlin_language").map { it.toString() }.getOrDefault(null) ?: error("Not found: libs.versions.kotlin.language"),
    libs: Set<TachiyomiLibrary> = setOf(TachiyomiLibrary.RandomUA),
    multisrc: Set<MultiSrc> = setOf(),
    signingConfiguration: (ApkSigningConfig.() -> Unit)? = null,
) {

    extensions.getByName<BaseAppModuleExtension>("android").apply {
        namespace = "eu.kanade.tachiyomi.extension.${namespaceIdentifier}"
        this.compileSdk = compileSdk

        sourceSets {
            named("main") {
                it.manifest.srcFile(project.layout.projectDirectory.file("AndroidManifest.xml"))
                it.java.setSrcDirs(listOf(project.layout.projectDirectory.dir("src")))
                it.kotlin.setSrcDirs(listOf(project.layout.projectDirectory.dir("src")))
                it.assets.setSrcDirs(listOf(project.layout.projectDirectory.dir("assets")))
                it.res.setSrcDirs(listOf(project.layout.projectDirectory.dir("res")))
            }

            named("release") {
                it.manifest.srcFile(project.layout.projectDirectory.file("AndroidManifest.xml"))
            }

            named("debug") {
                it.manifest.srcFile(project.layout.projectDirectory.file("AndroidManifest.xml"))
            }
        }

        defaultConfig {
            this.minSdk = minSdk
            this.targetSdk = compileSdk
            applicationIdSuffix = pkgNameSuffix
            versionCode = extVersionCode
            versionName = "${libVersion.versionName}.${extVersionCode}"

            addManifestPlaceholders(buildMap {
                put("appName", "Tachiyomi: $extName")
                put("extClass", extClass)
                put("extFactory", extFactory)
                put("nsfw", isNsfw)
                put("hasReadme", readmeFile.asFile.exists())
                put("hasChangelog", changelogFile.asFile.exists())
            })
        }

        val signing = signingConfiguration ?: {
            storeFile = System.getenv("KEY_FILE_NAME")?.let { rootProject.rootDir.resolve(it) }
            storePassword = System.getenv("KEY_STORE_PASSWORD")
            keyAlias = System.getenv("KEY_STORE_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }

        signingConfigs {
            create("release", signing)
        }

        buildTypes {
            release {
                signingConfig = signingConfigs.findByName("release")
                isMinifyEnabled = false
            }
        }

        dependenciesInfo {
            includeInApk = false
            includeInBundle = false
        }

        buildFeatures {
            aidl = false
            renderScript = false
            resValues = false
            shaders = false
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        it.kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            apiVersion = kotlinApiVersion
            languageVersion = kotlinLanguageVersion
            freeCompilerArgs += "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
        }
    }

    dependencies {
        "compileOnly"(catalog.findBundle("extension_compile").get())

        libs.forEach { lib ->
            "implementation"(project(":lib-${lib.identifier}"))
        }

        multisrc.forEach { multi ->
            "implementation"(project("multisrc-${multi.identifier}"))
        }
    }

    project(":").tasks.named("assembleExtensionsForDebug") {
        it.dependsOn(project.tasks.named("assembleDebug"))
    }

    project(":").tasks.named("assembleExtensionsForRelease") {
        it.dependsOn(project.tasks.named("assembleRelease"))
    }
}
