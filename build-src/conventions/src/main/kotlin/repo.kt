import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.properties.PropertyDelegateProvider

@Serializable
data class RepositoryJsonExtension(
    @SerialName("name")
    val name: String,
    @SerialName("pkg")
    val pkg: String,
    @SerialName("apk")
    val apk: String,
    @SerialName("lang")
    val lang: String,
    @SerialName("code")
    val code: Int,
    @SerialName("version")
    val version: String,
    @SerialName("nsfw")
    val nsfw: Int,
    @SerialName("hasReadme")
    val hasReadme: Int,
    @SerialName("hasChangelog")
    val hasChangelog: Int,
    @SerialName("sources")
    val sources: List<ExtensionSource>,
) {
    @Serializable
    data class ExtensionSource(
        @SerialName("name")
        val name: String,
        @SerialName("lang")
        val lang: String,
        @SerialName("id")
        val id: String,
        @SerialName("baseUrl")
        val baseUrl: String,
        @SerialName("versionId")
        val versionId: Int,
        @SerialName("hasCloudflare")
        val hasCloudflare: Int,
    )
}

private sealed class AAPT2DataExtractor {
    abstract val data: List<String>

    protected fun startingWith(start: Regex): List<String> = data.filter { str -> start.matchesAt(str, 0) }

    class Package(override val data: List<String>) : AAPT2DataExtractor() {
        private val pack = startingWith(packageRegex).first()

        val name by attrValue(nameRegex, pack)
        val versionCode by attrValue(versionCodeRegex, pack)
        val versionName by attrValue(versionNameRegex, pack)
    }

    class Application(override val data: List<String>) : AAPT2DataExtractor() {
        private val app = startingWith(applicationRegex).first()

        val label by attrValue(labelRegex, app)
        val icon by attrValue(iconRegex, app)
    }

    class MetaData(override val data: List<String>) : AAPT2DataExtractor() {
        private val all = startingWith(metaDataRegex)

        val meta: Map<String, String> by lazy {
            buildMap {
                all.forEach { line ->
                    val name by attrValue(nameRegex, line)
                    val value by attrValue(valueRegex, line, "")
                    put(name, value)
                }
            }
        }

        val `class` by lazy { meta["tachiyomi.extension.class"] }
        val factory by lazy { meta["tachiyomi.extension.factory"] }
        val nsfw by lazy { meta["tachiyomi.extension.nsfw"] }
        val hasReadme by lazy { meta["tachiyomi.extension.hasReadme"] }
        val hasChangelog by lazy { meta["tachiyomi.extension.hasChangelog"] }
    }

    private companion object {
        private fun attrValue(use: Regex, `in`: String, default: String? = null): PropertyDelegateProvider<Any?, Lazy<String>> = PropertyDelegateProvider { cls, property ->
            lazy { use.find(`in`)?.groupValues?.get(1) ?: default ?: error("Could not find ${property.name} in ${(cls ?: Unit)::class.simpleName}") }
        }

        private fun attr(labelOverride: String? = null): PropertyDelegateProvider<Any?, Lazy<Regex>> = PropertyDelegateProvider { _, property ->
            val label = labelOverride ?: property.name.substringBeforeLast("Regex", "").takeIf { it.isNotBlank() } ?: error("Invalid name: ${property.name}")
            lazy { """${label}='([^']+)'""".toRegex(RegexOption.IGNORE_CASE) }
        }

        private fun start(startOverride: String? = null): PropertyDelegateProvider<Any?, Lazy<Regex>> = PropertyDelegateProvider { _, property ->
            val start = startOverride ?: property.name.substringBeforeLast("Regex", "").takeIf { it.isNotBlank() } ?: error("Invalid name: ${property.name}")
            lazy { """${start}:""".toRegex(RegexOption.IGNORE_CASE) }
        }

        val packageRegex by start()
        val applicationRegex by start()
        val metaDataRegex by start("meta-data")

        val nameRegex by attr()
        val versionCodeRegex by attr()
        val versionNameRegex by attr()
        val labelRegex by attr()
        val iconRegex by attr()
        val valueRegex by attr()
    }
}

@Serializable
data class InspectionElement(
    val name: String,
    val lang: String,
    val id: Long,
    val baseUrl: String,
    val versionId: Int,
    val hasCloudflare: Int = 0,
)

private typealias ExtensionPackage = String

fun createRepoData(raw: String, inspectorOutput: String, baseName: String): RepositoryJsonExtension {
    val lines = raw.lines()
    val packageData = AAPT2DataExtractor.Package(lines)
    val applicationData = AAPT2DataExtractor.Application(lines)
    val metaData = AAPT2DataExtractor.MetaData(lines)

    val inspectionReport: Map<ExtensionPackage, List<InspectionElement>> = Json.decodeFromString(inspectorOutput)
    val (_, inspectionData) = inspectionReport.entries.singleOrNull() ?: error("inspector report should contain only a single entry!")

    val globalLang = when (inspectionData.size) {
        0 -> error("No sources found!")
        1 -> inspectionData.single().lang
        else -> {
            val distinct = inspectionData.mapTo(HashSet()) { it.lang }
            when {
                "all" in distinct -> "all"
                "other" in distinct -> "other"
                else -> distinct.singleOrNull() ?: "all"
            }
        }
    }

    return RepositoryJsonExtension(
        name = applicationData.label,
        pkg = packageData.name,
        apk = "${baseName}.apk",
        lang = globalLang,
        code = packageData.versionCode.toInt(),
        version = packageData.versionName,
        nsfw = metaData.nsfw?.toIntOrNull() ?: -1,
        hasReadme = metaData.hasReadme?.toIntOrNull() ?: -1,
        hasChangelog = metaData.hasChangelog?.toIntOrNull() ?: -1,
        sources = inspectionData.map { element ->
            RepositoryJsonExtension.ExtensionSource(
                name = element.name,
                lang = element.lang,
                id = element.id.toString(),
                baseUrl = element.baseUrl,
                versionId = element.versionId,
                hasCloudflare = element.hasCloudflare,
            )
        }
    )
}
