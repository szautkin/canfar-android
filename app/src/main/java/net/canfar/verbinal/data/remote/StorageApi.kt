package net.canfar.verbinal.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface StorageApi {
    @GET("{username}")
    suspend fun getStorageQuota(
        @Path("username") username: String,
        @Header("Accept") accept: String = "text/xml",
    ): Response<String>
}
