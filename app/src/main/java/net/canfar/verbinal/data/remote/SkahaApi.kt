package net.canfar.verbinal.data.remote

import net.canfar.verbinal.data.model.RawImage
import net.canfar.verbinal.data.model.SessionContext
import net.canfar.verbinal.data.model.SkahaSessionResponse
import net.canfar.verbinal.data.model.SkahaStatsResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SkahaApi {
    @GET("v1/session")
    suspend fun getSessions(): Response<List<SkahaSessionResponse>>

    @GET("v1/session/{id}")
    suspend fun getSession(
        @Path("id") id: String,
    ): Response<SkahaSessionResponse>

    @FormUrlEncoded
    @POST("v1/session")
    suspend fun launchSession(
        @FieldMap fields: Map<String, String>,
        @Header("x-skaha-registry-auth") registryAuth: String? = null,
    ): Response<String>

    @DELETE("v1/session/{id}")
    suspend fun deleteSession(
        @Path("id") id: String,
    ): Response<Unit>

    @FormUrlEncoded
    @POST("v1/session/{id}")
    suspend fun renewSession(
        @Path("id") id: String,
        @Query("action") action: String = "renew",
        @Field("dummy") dummy: String = "",
    ): Response<Unit>

    @GET("v1/session/{id}")
    suspend fun getSessionEvents(
        @Path("id") id: String,
        @Query("view") view: String = "events",
    ): Response<String>

    @GET("v1/session/{id}")
    suspend fun getSessionLogs(
        @Path("id") id: String,
        @Query("view") view: String = "logs",
    ): Response<String>

    @GET("v1/session")
    suspend fun getStats(
        @Query("view") view: String = "stats",
    ): Response<SkahaStatsResponse>

    @GET("v1/image")
    suspend fun getImages(): Response<List<RawImage>>

    @GET("v1/context")
    suspend fun getContext(): Response<SessionContext>

    @GET("v1/repository")
    suspend fun getRepositories(): Response<List<String>>
}
