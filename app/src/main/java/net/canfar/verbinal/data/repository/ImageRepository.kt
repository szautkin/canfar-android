package net.canfar.verbinal.data.repository

import net.canfar.verbinal.data.model.RawImage
import net.canfar.verbinal.data.model.SessionContext
import net.canfar.verbinal.data.remote.SkahaApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository
@Inject
constructor(
    private val skahaApi: SkahaApi,
) {
    private var cachedImages: List<RawImage>? = null
    private var cacheTime: Long = 0
    private val cacheDuration = 5 * 60 * 1000L // 5 minutes

    suspend fun getImages(): List<RawImage> {
        val now = System.currentTimeMillis()
        cachedImages?.let {
            if (now - cacheTime < cacheDuration) return it
        }

        val response = skahaApi.getImages()
        if (!response.isSuccessful) throw Exception("Failed to load images: ${response.code()}")
        val images = response.body() ?: emptyList()
        cachedImages = images
        cacheTime = now
        return images
    }

    suspend fun getContext(): SessionContext? {
        val response = skahaApi.getContext()
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getRepositories(): List<String> {
        val response = skahaApi.getRepositories()
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
}
