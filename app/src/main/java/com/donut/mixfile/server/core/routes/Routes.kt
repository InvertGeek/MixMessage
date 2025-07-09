package com.donut.mixfile.server.core.routes

import com.donut.mixfile.server.core.MixFileServer
import com.donut.mixfile.server.core.routes.api.getAPIRoute
import com.donut.mixfile.server.core.utils.extensions.paramPath
import com.donut.mixfile.server.core.utils.parseFileMimeType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.jvm.javaio.toByteReadChannel

fun MixFileServer.getRoutes(): Routing.() -> Unit {

    return {
        get("{param...}") {
            val file = paramPath.ifEmpty {
                "index.html"
            }
            val fileStream =
                getStaticFile(file) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respondBytesWriter(
                contentType = file.parseFileMimeType()
            ) {
                fileStream.toByteReadChannel().copyAndClose(this)
            }
        }

        route("/api", getAPIRoute())
    }
}

