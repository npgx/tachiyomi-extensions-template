import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


plugins {
    base
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = """8.6-rc-1"""
        distributionSha256Sum = """7f95f484b97c07afc9e4dbca18d9b433155747a462857c7a7620694c6e20a58d"""
    }
}

buildscript {
    dependencies {
        classpath(libs.kotlin.json)
        classpath(libs.nanoHTTPD)
    }
}

val repo: Directory = rootProject.layout.buildDirectory.dir("repo").get()
val prettyJson = Json { prettyPrint = true }

val cleanseRootBuildDir by tasks.registering(Delete::class) {
    delete(project.layout.buildDirectory)
}

val cleanseAll by tasks.registering(Delete::class)

gradle.projectsEvaluated {
    allprojects {
        cleanseAll.configure {
            delete(layout.buildDirectory)
        }
    }
}

val compileLibsKotlin: TaskProvider<DefaultTask> by tasks.registering(DefaultTask::class)
val compileMultiSrcKotlin: TaskProvider<DefaultTask> by tasks.registering(DefaultTask::class)

listOf("debug", "release").forEach { variant ->
    val capitalVariant = variant.replaceFirstChar { it.uppercase() }

    tasks.register<DefaultTask>("construct${capitalVariant}Repo") {
        dependsOn(cleanseRootBuildDir)

        val repo = repo.dir(variant)
        outputs.dir(repo)

        doLast {
            val allJson = inputs.files
                .filter { it.extension == "json" }
                .joinToString(",", "[", "]") { json -> json.readText() }

            inputs.files
                .filter { it.extension == "apk" }
                .forEach { apk ->
                    copy {
                        duplicatesStrategy = DuplicatesStrategy.FAIL
                        from(apk)
                        into(repo.dir("apk"))
                    }
                }

            inputs.files
                .filter { it.extension == "png" }
                .forEach { apk ->
                    copy {
                        duplicatesStrategy = DuplicatesStrategy.FAIL
                        from(apk)
                        into(repo.dir("icon"))
                    }
                }

            val index = Json.parseToJsonElement(allJson)

            repo.file("index.min.json").asFile
                .apply { createNewFile() }
                .writeText(Json.encodeToString(index))

            repo.file("index.json").asFile
                .apply { createNewFile() }
                .writeText(prettyJson.encodeToString(index))

            println("$capitalVariant repo (${repo.asFile.invariantSeparatorsPath}) contents:")
            fileTree(repo).forEach { file ->
                println(file.relativeTo(repo.asFile).invariantSeparatorsPath)
            }
        }
    }
}
