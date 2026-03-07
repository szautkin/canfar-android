package net.canfar.verbinal.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import net.canfar.verbinal.data.remote.ApiEndpoints
import net.canfar.verbinal.data.remote.AuthApi
import net.canfar.verbinal.data.remote.AuthInterceptor
import net.canfar.verbinal.data.remote.AuthTokenProvider
import net.canfar.verbinal.data.remote.SkahaApi
import net.canfar.verbinal.data.remote.StorageApi
import net.canfar.verbinal.data.remote.UserApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenProvider: AuthTokenProvider): OkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(AuthInterceptor(tokenProvider))
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            },
        ).build()

    @Provides
    @Singleton
    fun provideAuthApi(): AuthApi = Retrofit
        .Builder()
        .baseUrl(ApiEndpoints.LOGIN_BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(okHttpClient: OkHttpClient): UserApi = Retrofit
        .Builder()
        .baseUrl(ApiEndpoints.AC_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(UserApi::class.java)

    @Provides
    @Singleton
    fun provideSkahaApi(okHttpClient: OkHttpClient): SkahaApi = Retrofit
        .Builder()
        .baseUrl(ApiEndpoints.SKAHA_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(SkahaApi::class.java)

    @Provides
    @Singleton
    fun provideStorageApi(okHttpClient: OkHttpClient): StorageApi = Retrofit
        .Builder()
        .baseUrl(ApiEndpoints.STORAGE_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(StorageApi::class.java)
}
