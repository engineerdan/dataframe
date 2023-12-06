package org.jetbrains.kotlinx.dataframe.io

import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.parser.OpenAPIParser
import java.io.File
import java.net.URL

private val logger = KotlinLogging.logger {}

/** Needs to have any type schemas to convert. */
public fun isOpenApiStr(text: String): Boolean = try {
    val parsed = OpenAPIParser().readContents(text, null, null)
    parsed.openAPI?.components?.schemas != null
} catch (e: Throwable) {
    logger.debug(e) { "Attempt to read input as YAML/JSON OpenAPI specification failed." }
    false
}

public fun isOpenApi(path: String): Boolean = isOpenApi(asURL(path))

public fun isOpenApi(url: URL): Boolean {
    if (url.path.endsWith(".yml") || url.path.endsWith("yaml")) {
        return true
    }
    if (!url.path.endsWith("json")) {
        return false
    }

    return isOpenApiStr(url.readText())
}

public fun isOpenApi(file: File): Boolean {
    if (file.extension.lowercase() in listOf("yml", "yaml")) {
        return true
    }

    if (file.extension.lowercase() != "json") {
        return false
    }

    return isOpenApiStr(file.readText())
}
