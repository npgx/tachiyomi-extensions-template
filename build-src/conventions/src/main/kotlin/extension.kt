import com.android.build.api.dsl.ApkSigningConfig
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import de.undercouch.gradle.tasks.download.Download
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.DefaultTask
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
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

private fun Project.getAAPT2Command(): Provider<String> {
    return extensions.getByName<ApplicationAndroidComponentsExtension>("androidComponents")
        .sdkComponents
        .aidl
        .map { it.executable.get() }
        .map { it.asFile.parentFile }
        .map { it.resolve("aapt2") }
        .map { it.absoluteFile.path }
}

private fun Boolean.as1or0() = if (this) 1 else 0

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
    useDefaultManifest: Boolean = true,
    includeStdLibInApk: Boolean = true,
    aapt2Command: Provider<String> = getAAPT2Command(),
    versionName: String = "${libVersion.versionName}.${extVersionCode}",
    archivesBaseName: String = "tachiyomi-$pkgNameSuffix-v${versionName}",
    includeInBatchDebug: Boolean = true,
    includeInBatchRelease: Boolean = includeInBatchDebug,
    signingConfiguration: (ApkSigningConfig.() -> Unit)? = null,
) {

    this.archivesName.set(archivesBaseName)

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
            this.versionName = versionName

            addManifestPlaceholders(buildMap {
                put("appName", "Tachiyomi: $extName")
                put("extClass", extClass)
                put("extFactory", extFactory)
                put("nsfw", isNsfw.as1or0())
                put("hasReadme", readmeFile.asFile.exists().as1or0())
                put("hasChangelog", changelogFile.asFile.exists().as1or0())
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
                this.isShrinkResources
                isMinifyEnabled = false
                isDebuggable = false
            }

            debug {
                isMinifyEnabled = false
                isDebuggable = true
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

        if (useDefaultManifest) {
            "implementation"(project(":default"))
        }

        if (includeStdLibInApk) {
            "implementation"(catalog.findLibrary("kotlin_stdlib").get())
        } else {
            "compileOnly"(catalog.findLibrary("kotlin_stdlib").get())
        }

        libs.forEach { lib ->
            "implementation"(project(":lib-${lib.identifier}"))
        }

        multisrc.forEach { multi ->
            "implementation"(project("multisrc-${multi.identifier}"))
        }
    }

    afterEvaluate { extension ->

        val variants = when {
            includeInBatchDebug && includeInBatchRelease -> listOf("debug", "release")
            includeInBatchDebug -> listOf("debug")
            includeInBatchRelease -> listOf("release")
            else -> emptyList()
        }

        // jitpack doesn't work
        val downloadInspectorTask: TaskProvider<Download> = tasks.register<Download>("downloadInspector") {
            val url = URL("""https://api.github.com/repos/tachiyomiorg/tachiyomi-extensions-inspector/releases/latest""")
            val data = Json.parseToJsonElement(String(url.readBytes()))
            val inspectorURL = data.jsonObject["assets"]!!.jsonArray[0].jsonObject["browser_download_url"]!!.jsonPrimitive.content
            src(inspectorURL)
            dest(layout.buildDirectory.file("inspector.jar"))
            overwrite(false)
            onlyIfModified(true)
        }

        fun File.resolveSiblingWithExtension(ext: String) = resolveSibling("${nameWithoutExtension}.${ext}")

        variants.forEach { variant ->
            val capitalVariant = variant.replaceFirstChar { it.uppercase() }

            val assembleTask = tasks.named("assemble${capitalVariant}")
            val assembleOutputDir = layout.buildDirectory.dir("outputs/apk/$variant").get()

            val constructRepoTask = project(":").tasks.named<DefaultTask>("construct${capitalVariant}Repo")

            val apkFile = assembleOutputDir.file("${archivesBaseName}-${variant}.apk").asFile
            constructRepoTask.configure { it.inputs.file(apkFile) }

            val inspectorOutputFile = apkFile.resolveSiblingWithExtension("inspector.json")
            val inspectTask = tasks.register<Exec>("inspect${capitalVariant}") {
                dependsOn(assembleTask)
                dependsOn(downloadInspectorTask)

                outputs.file(inspectorOutputFile)

                workingDir(assembleOutputDir)

                executable("java")
                args(
                    "-jar",
                    downloadInspectorTask.get().outputs.files.singleFile.absolutePath,
                    apkFile.name,
                    inspectorOutputFile.name,
                    "${apkFile.nameWithoutExtension}-inspector",
                )

                doFirst {
                    println("Running: $commandLine")
                    println("In: $workingDir")
                    println("Contents: ${workingDir.listFiles()?.map { it.name }}")
                }
            }

            val jsonDataFile = apkFile.resolveSiblingWithExtension("json")
            constructRepoTask.configure { it.inputs.file(jsonDataFile) }

            val pngFile = apkFile.resolveSiblingWithExtension("png")
            constructRepoTask.configure { it.inputs.file(pngFile) }

            val createRepoDataTask = tasks.register<Exec>("create${capitalVariant}RepoData") {
                dependsOn(assembleTask)
                dependsOn(inspectTask)

                val aapt2BAOS = ByteArrayOutputStream()
                standardOutput = aapt2BAOS

                workingDir(assembleOutputDir)
                executable(aapt2Command.get())
                args(
                    "dump",
                    "badging",
                    "--include-meta-data",
                    apkFile.name,
                )

                doFirst {
                    println("Running: $commandLine")
                    println("In: $workingDir")
                    println("Contents: ${workingDir.listFiles()?.map { it.name }}")
                }

                doLast {
                    val (repoData: RepositoryJsonExtension, aapt2Output: AAPT2Output) = createRepoData(
                        aapt2Output = String(aapt2BAOS.toByteArray()),
                        inspectorOutput = inspectTask.get().outputs.files.singleFile.readText(),
                        baseName = apkFile.nameWithoutExtension,
                    )

                    jsonDataFile.apply { createNewFile() }.writeText(Json.encodeToString(repoData))

                    val extractionDir = apkFile.resolveSibling(apkFile.nameWithoutExtension).apply { mkdir() }
                    copy { cp ->
                        cp.duplicatesStrategy = DuplicatesStrategy.INCLUDE
                        cp.from(zipTree(apkFile))
                        cp.into(extractionDir)
                    }

                    val icLauncherRelativePath: String = aapt2Output.icons.icon320
                    val icLauncherFile = extractionDir.resolve(icLauncherRelativePath)
                    require(icLauncherFile.exists()) {
                        "$icLauncherRelativePath not found:\n${
                            fileTree(extractionDir).files.joinToString("\n") {
                                it.relativeTo(extractionDir).invariantSeparatorsPath
                            }
                        }"
                    }

                    copy { cp ->
                        cp.duplicatesStrategy = DuplicatesStrategy.INCLUDE
                        cp.from(icLauncherFile)
                        cp.rename { pngFile.name }
                        cp.into(pngFile.parentFile)
                    }

                    val packageIconFile = pngFile.resolveSibling("${aapt2Output.pack.name}.png")
                    constructRepoTask.configure { it.inputs.file(packageIconFile) }

                    copy { cp ->
                        cp.from(pngFile)
                        cp.rename { packageIconFile.name }
                        cp.into(pngFile.parentFile)
                    }

                    println("Contents After: ${workingDir.listFiles()?.map { it.name }}")
                }
            }

            constructRepoTask.configure {
                it.dependsOn(assembleTask)
                it.dependsOn(inspectTask)
                it.dependsOn(createRepoDataTask)
            }
        }
    }
}
