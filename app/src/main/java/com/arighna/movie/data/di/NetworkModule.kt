package com.arighna.movie.data.di

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

class NoConnectivityException : java.io.IOException()

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OfflineInterceptor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkInterceptor

/**
 * Hilt module that provides singleton instances for the application's networking layer,
 * including OkHttp, Retrofit, and the Movie API implementation.
 * It also configures a caching strategy to improve performance and provide offline support.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCache(app: Application): Cache {
        return Cache(app.cacheDir, com.arighna.movie.common.Constants.CACHE_SIZE_BYTES)
    }

    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    /**
     * Provides an interceptor that adds a "Cache-Control" header to requests when the device is offline.
     * This tells OkHttp to retrieve data from the cache, even if it's stale.
     * If the request fails because the data is not in the cache, it throws a NoConnectivityException.
     */
    @Provides
    @Singleton
    @OfflineInterceptor
    fun provideOfflineInterceptor(connectivityManager: ConnectivityManager): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()
            if (!isNetworkAvailable(connectivityManager)) {
                val maxStale = TimeUnit.DAYS.toSeconds(com.arighna.movie.common.Constants.CACHE_MAX_STALE_DAYS).toInt()
                request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                    .build()

                val response = chain.proceed(request)

                // If the offline request fails because the data is not in the cache, throw our custom exception
                if (response.code == 504) {
                    throw NoConnectivityException()
                }
                return@Interceptor response
            }
            chain.proceed(request)
        }
    }

    /**
     * Provides a network interceptor that rewrites the "Cache-Control" header of the response.
     * This ensures that all successful network responses are cached for a specific duration.
     */
    @Provides
    @Singleton
    @NetworkInterceptor
    fun provideNetworkInterceptor(): Interceptor {
        return Interceptor { chain ->
            val response = chain.proceed(chain.request())
            val maxAge = TimeUnit.MINUTES.toSeconds(com.arighna.movie.common.Constants.CACHE_MAX_AGE_MINUTES).toInt()
            response.newBuilder()
                .header("Cache-Control", "public, max-age=$maxAge")
                .removeHeader("Pragma")
                .build()
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cache: Cache,
        @OfflineInterceptor offlineInterceptor: Interceptor,
        @NetworkInterceptor networkInterceptor: Interceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(offlineInterceptor)
            .addNetworkInterceptor(networkInterceptor)
            .connectTimeout(com.arighna.movie.common.Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(com.arighna.movie.common.Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(com.arighna.movie.common.Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Checks if the device has an active internet connection.
     * Note: This function uses deprecated APIs for simplicity, as the modern callback-based APIs
     * would add significant complexity for this use case.
     */
    @Suppress("DEPRECATION")
    internal fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.arighna.movie.common.Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideMovieApi(retrofit: Retrofit): com.arighna.movie.data.remote.MovieApi {
        return retrofit.create(com.arighna.movie.data.remote.MovieApi::class.java)
    }
}