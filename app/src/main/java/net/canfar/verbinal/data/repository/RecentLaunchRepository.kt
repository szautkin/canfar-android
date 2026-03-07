package net.canfar.verbinal.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.canfar.verbinal.data.model.RecentLaunch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentLaunchRepository
@Inject
constructor(
    @ApplicationContext context: Context,
) {
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    private val file = File(context.filesDir, "recent_launches.json")
    private var launches: MutableList<RecentLaunch> = mutableListOf()

    init {
        launches = readFromDisk().toMutableList()
    }

    fun save(launch: RecentLaunch) {
        val existing = launches.indexOfFirst { it.type == launch.type && it.image == launch.image }
        if (existing >= 0) {
            val updated =
                launches[existing].copy(
                    launchedAt = launch.launchedAt,
                    name = launch.name,
                )
            launches.removeAt(existing)
            launches.add(0, updated)
        } else {
            launches.add(0, launch)
        }

        if (launches.size > MAX_ENTRIES) {
            launches = launches.take(MAX_ENTRIES).toMutableList()
        }
        writeToDisk()
    }

    fun remove(launch: RecentLaunch) {
        launches.removeAll {
            it.type == launch.type && it.image == launch.image && it.launchedAt == launch.launchedAt
        }
        writeToDisk()
    }

    fun load(): List<RecentLaunch> = launches.sortedByDescending { it.launchedAt }

    fun clear() {
        launches.clear()
        writeToDisk()
    }

    private fun readFromDisk(): List<RecentLaunch> = try {
        if (file.exists()) {
            json.decodeFromString<List<RecentLaunch>>(file.readText())
        } else {
            emptyList()
        }
    } catch (_: Exception) {
        emptyList()
    }

    private fun writeToDisk() {
        try {
            file.writeText(json.encodeToString(launches.toList()))
        } catch (_: Exception) {
            // Silently fail
        }
    }

    companion object {
        private const val MAX_ENTRIES = 10
    }
}
