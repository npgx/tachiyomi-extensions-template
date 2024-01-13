plugins {
    base
}

val template by tasks.registering(Zip::class) {
    from(fileTree(rootProject.layout.projectDirectory))

    val includedExtensions = setOf("kt", "kts", "java", "png", "xml", "yaml")

    val excludeTopLevel = setOf(".idea", ".git", "extensions")
    val excludeRecursive = setOf("build", ".gradle", ".idea")

    include { it.isDirectory }
    include { it.file.extension in includedExtensions }
    include { arrayOf("tachiyomi.versions.toml").contentEquals(it.relativePath.segments) }

    exclude { it.relativePath.segments.getOrNull(0) in excludeTopLevel }
    exclude { it.relativePath.lastName in excludeRecursive }

    includeEmptyDirs = false

    destinationDirectory.set(rootProject.layout.buildDirectory)
    archiveBaseName.set("stripped-template")
}
