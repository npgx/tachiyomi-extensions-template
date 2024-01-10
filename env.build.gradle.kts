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

val assembleExtensionsForDebug by tasks.registering(DefaultTask::class)
val assembleExtensionsForRelease by tasks.registering(DefaultTask::class)

val compileLibsKotlin by tasks.registering(DefaultTask::class)
val compileMultiSrcKotlin by tasks.registering(DefaultTask::class)
