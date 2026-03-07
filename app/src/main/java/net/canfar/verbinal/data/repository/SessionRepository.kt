package net.canfar.verbinal.data.repository

import android.util.Base64
import kotlinx.serialization.json.Json
import net.canfar.verbinal.data.model.Session
import net.canfar.verbinal.data.model.SessionLaunchParams
import net.canfar.verbinal.data.model.toSession
import net.canfar.verbinal.data.remote.SkahaApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository
@Inject
constructor(
    private val skahaApi: SkahaApi,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getSessions(): List<Session> {
        val response = skahaApi.getSessions()
        if (!response.isSuccessful) throw Exception("Failed to load sessions: ${response.code()}")
        return response.body()?.map { it.toSession() } ?: emptyList()
    }

    suspend fun getSession(id: String): Session? {
        val response = skahaApi.getSession(id)
        return if (response.isSuccessful) response.body()?.toSession() else null
    }

    suspend fun launchSession(params: SessionLaunchParams): String? {
        val fields =
            mutableMapOf(
                "name" to params.name,
                "image" to params.image,
                "type" to params.type,
            )
        if (params.cores > 0) fields["cores"] = params.cores.toString()
        if (params.ram > 0) fields["ram"] = params.ram.toString()
        if (params.gpus > 0) fields["gpus"] = params.gpus.toString()
        if (!params.cmd.isNullOrEmpty()) fields["cmd"] = params.cmd

        val registryAuth =
            if (!params.registryUsername.isNullOrEmpty()) {
                val credentials = "${params.registryUsername}:${params.registrySecret ?: ""}"
                Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            } else {
                null
            }

        val response = skahaApi.launchSession(fields, registryAuth)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: ""
            throw Exception("Launch failed: ${response.code()} ${response.message()} - $errorBody")
        }

        val body = response.body()?.trim() ?: return null
        return if (body.startsWith("[")) {
            val ids = json.decodeFromString<List<String>>(body)
            ids.firstOrNull()
        } else {
            body
        }
    }

    suspend fun deleteSession(id: String): Boolean = skahaApi.deleteSession(id).isSuccessful

    suspend fun renewSession(id: String): Boolean {
        val response = skahaApi.renewSession(id)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: ""
            throw Exception("Renew failed: ${response.code()} ${response.message()} $errorBody")
        }
        return true
    }

    suspend fun getSessionEvents(id: String): String? {
        val response = skahaApi.getSessionEvents(id)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getSessionLogs(id: String): String? {
        val response = skahaApi.getSessionLogs(id)
        return if (response.isSuccessful) response.body() else null
    }
}
