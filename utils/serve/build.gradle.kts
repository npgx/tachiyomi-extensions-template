import io.ktor.server.application.call
import io.ktor.server.application.host
import io.ktor.server.application.port
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.application.Application as KtorApplication

plugins {
    base
}

buildscript {
    dependencies {
        classpath(libs.kotlin.ktor)
        classpath("io.ktor:ktor-server-host-common-jvm")
    }
}

val debugRepo by tasks.registering {
    dependsOn(project(":").tasks.named("constructDebugRepo"))

    doLast {
        val repoRoot = rootProject.layout.buildDirectory.dir("repo/debug").get().asFile
        require(repoRoot.exists()) { "Repo does not exist: $repoRoot" }

        val server = embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = { staticRepo(repoRoot) })
        println("Serving $repoRoot over ${server.environment.config.host}:${server.environment.config.port}")
        server.start(wait = false)

        println("Press enter to stop the server")
        readlnOrNull()
        println("Server should stop within 5-15 seconds...")
        server.stop(5000L, 15000L)
    }
}

fun KtorApplication.staticRepo(repoRoot: File) {
    routing {
        get("/") {
            call.respondRedirect("/index.json")
        }
        staticFiles("/", repoRoot)
    }
}
