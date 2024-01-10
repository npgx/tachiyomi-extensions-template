#!/usr/bin/env kotlin

@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.9.0")
@file:Suppress("PropertyName")

import io.github.typesafegithub.workflows.actions.actions.CheckoutV4
import io.github.typesafegithub.workflows.actions.actions.SetupJavaV4
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV2
import io.github.typesafegithub.workflows.actions.gradle.WrapperValidationActionV1
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.JobOutputs
import io.github.typesafegithub.workflows.domain.Mode
import io.github.typesafegithub.workflows.domain.Permission
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile

workflow(
    name = "Compile Release",
    on = listOf(WorkflowDispatch(), Push(branches = listOf("master"), tags = listOf("build_release"), pathsIgnore = listOf("**.md"))),
    sourceFile = __FILE__.toPath(),
    concurrency = Concurrency(group = expr { github.workflow }, cancelInProgress = true),
    permissions = mapOf(Permission.Contents to Mode.Write),
    env = linkedMapOf(),
) {

    job(
        id = "compile_release",
        name = "Compile Extensions for release",
        runsOn = UbuntuLatest,
        env = linkedMapOf(),
        outputs = object : JobOutputs() {}
    ) {
        uses(name = "Clone repo", action = CheckoutV4())
        uses(name = "Validate Gradle Wrapper", action = WrapperValidationActionV1())
        uses(name = "Set up JDK", action = SetupJavaV4(javaVersion = "21", distribution = SetupJavaV4.Distribution.Adopt))
        uses(name = "Setup Gradle", action = GradleBuildActionV2())

        run(name = "Prepare signing key", command = "echo ${expr { secrets["KEY_STORE"]!! }} | base64 -d > ${expr { secrets["KEY_FILE_NAME"]!! }}")
        run(
            name = "Compile for release",
            command = "./gradlew :assembleExtensionsForRelease",
            env = linkedMapOf(
                "KEY_FILE_NAME" to expr { secrets["KEY_FILE_NAME"]!! },
                "KEY_STORE_PASSWORD" to expr { secrets["KEY_STORE_PASSWORD"]!! },
                "KEY_STORE_ALIAS" to expr { secrets["KEY_STORE_ALIAS"]!! },
                "KEY_PASSWORD" to expr { secrets["KEY_PASSWORD"]!! },
            )
        )

        run(name = "Clean up CI files", command = "rm ${expr { secrets["KEY_FILE_NAME"]!! }}")
    }

}.writeToFile()
