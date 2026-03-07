package net.canfar.verbinal.data.repository

import net.canfar.verbinal.data.model.SkahaStatsResponse
import net.canfar.verbinal.data.remote.SkahaApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformRepository
@Inject
constructor(
    private val skahaApi: SkahaApi,
) {
    suspend fun getStats(): SkahaStatsResponse? {
        val response = skahaApi.getStats()
        if (!response.isSuccessful) {
            throw Exception("Stats returned ${response.code()} ${response.message()}")
        }
        return response.body()
    }
}
