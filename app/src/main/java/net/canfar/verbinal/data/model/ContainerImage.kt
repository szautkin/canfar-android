package net.canfar.verbinal.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawImage(
    @SerialName("id") val id: String = "",
    @SerialName("types") val types: List<String> = emptyList(),
)

data class ParsedImage(
    val id: String = "",
    val registry: String = "",
    val project: String = "",
    val name: String = "",
    val version: String = "",
    val label: String = "",
    val types: List<String> = emptyList(),
)

object ImageParser {
    fun parse(raw: RawImage): ParsedImage {
        val id = raw.id
        val parts = id.split("/")
        val registry: String
        val project: String
        val nameWithVersion: String

        when {
            parts.size >= 3 -> {
                registry = parts[0]
                project = parts[1]
                nameWithVersion = parts.drop(2).joinToString("/")
            }
            parts.size == 2 -> {
                registry = ""
                project = parts[0]
                nameWithVersion = parts[1]
            }
            else -> {
                registry = ""
                project = ""
                nameWithVersion = parts[0]
            }
        }

        val colonIdx = nameWithVersion.lastIndexOf(':')
        val name: String
        val version: String
        if (colonIdx > 0) {
            name = nameWithVersion.substring(0, colonIdx)
            version = nameWithVersion.substring(colonIdx + 1)
        } else {
            name = nameWithVersion
            version = "latest"
        }

        return ParsedImage(
            id = id,
            registry = registry,
            project = project,
            name = name,
            version = version,
            label = "$name:$version",
            types = raw.types,
        )
    }

    fun groupByTypeAndProject(rawImages: List<RawImage>): Map<String, Map<String, List<ParsedImage>>> {
        val result = mutableMapOf<String, MutableMap<String, MutableList<ParsedImage>>>()

        for (raw in rawImages) {
            val parsed = parse(raw)
            for (type in parsed.types) {
                val projects = result.getOrPut(type) { mutableMapOf() }
                val images = projects.getOrPut(parsed.project) { mutableListOf() }
                images.add(parsed)
            }
        }

        return result.mapValues { (_, projects) ->
            projects.mapValues { (_, images) ->
                images.sortedByDescending { it.version }
            }
        }
    }
}
