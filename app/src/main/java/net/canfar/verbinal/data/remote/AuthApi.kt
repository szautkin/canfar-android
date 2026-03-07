package net.canfar.verbinal.data.remote

import net.canfar.verbinal.data.model.UserInfo
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): Response<String>

    @GET("whoami")
    suspend fun whoami(
        @Header("Authorization") authHeader: String,
    ): Response<String>
}

interface UserApi {
    @GET("users/{username}")
    suspend fun getUserInfo(
        @Path("username") username: String,
    ): Response<UserInfo>
}
