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

val compileExtensionsForRelease by tasks.registering(DefaultTask::class) {
    doLast {

    }
}

val compileExtensionsForDebug by tasks.registering(DefaultTask::class) {
    doLast {

    }
}
