package com.brilliant.movie.data.di

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.brilliant.movie.common.Constants
import com.brilliant.movie.data.remote.MovieApi
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit

class NetworkModuleTest {

    private val networkModule = NetworkModule

    @Test
    fun `provideCache returns a valid Cache`() {
        val application = mockk<Application>()
        val cacheDir = File("test-cache")
        every { application.cacheDir } returns cacheDir

        val cache = networkModule.provideCache(application)

        assertEquals(cacheDir, cache.directory)
        assertEquals(Constants.CACHE_SIZE_BYTES, cache.maxSize())
    }

    @Test
    fun `provideConnectivityManager returns ConnectivityManager`() {
        val context = mockk<Context>()
        val connectivityManager = mockk<ConnectivityManager>()
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager

        val result = networkModule.provideConnectivityManager(context)

        assertEquals(connectivityManager, result)
    }

    @Test
    fun `provideMoshi returns a Moshi instance`() {
        val moshi = networkModule.provideMoshi()
        assertNotNull(moshi)
    }

    @Test
    fun `provideRetrofit builds a Retrofit instance`() {
        val okHttpClient = mockk<OkHttpClient>()
        val moshi = networkModule.provideMoshi()
        val retrofit = networkModule.provideRetrofit(okHttpClient, moshi)

        assertEquals(Constants.BASE_URL, retrofit.baseUrl().toString())
        assertEquals(okHttpClient, retrofit.callFactory())
    }

    @Test
    fun `provideMovieApi creates a MovieApi instance`() {
        val retrofit = mockk<Retrofit>()
        val movieApi = mockk<MovieApi>()
        every { retrofit.create(MovieApi::class.java) } returns movieApi
        networkModule.provideMovieApi(retrofit)
        verify { retrofit.create(MovieApi::class.java) }
    }

    @Test
    fun `offlineInterceptor forces cache when network is unavailable`() {
        val connectivityManager = mockk<ConnectivityManager>()
        val chain = mockk<Interceptor.Chain>(relaxed = true)
        val request = Request.Builder().url("https://test.com").build()

        every { chain.request() } returns request
        every { connectivityManager.activeNetwork } returns null

        val interceptor = networkModule.provideOfflineInterceptor(connectivityManager)
        interceptor.intercept(chain)

        val expectedMaxStale = TimeUnit.DAYS.toSeconds(Constants.CACHE_MAX_STALE_DAYS)
        verify {
            chain.proceed(withArg { assertEquals("public, only-if-cached, max-stale=$expectedMaxStale", it.header("Cache-Control")) })
        }
    }

    @Test
    fun `offlineInterceptor forces cache when network capabilities are null`() {
        val connectivityManager = mockk<ConnectivityManager>()
        val chain = mockk<Interceptor.Chain>(relaxed = true)
        val request = Request.Builder().url("https://test.com").build()
        val network = mockk<Network>()

        every { chain.request() } returns request
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns null

        val interceptor = networkModule.provideOfflineInterceptor(connectivityManager)
        interceptor.intercept(chain)

        val expectedMaxStale = TimeUnit.DAYS.toSeconds(Constants.CACHE_MAX_STALE_DAYS)
        verify {
            chain.proceed(withArg { assertEquals("public, only-if-cached, max-stale=$expectedMaxStale", it.header("Cache-Control")) })
        }
    }

    @Test
    fun `offlineInterceptor forces cache when there is no internet capability`() {
        val connectivityManager = mockk<ConnectivityManager>()
        val chain = mockk<Interceptor.Chain>(relaxed = true)
        val request = Request.Builder().url("https://test.com").build()
        val network = mockk<Network>()
        val networkCapabilities = mockk<NetworkCapabilities>()

        every { chain.request() } returns request
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false

        val interceptor = networkModule.provideOfflineInterceptor(connectivityManager)
        interceptor.intercept(chain)

        val expectedMaxStale = TimeUnit.DAYS.toSeconds(Constants.CACHE_MAX_STALE_DAYS)
        verify {
            chain.proceed(withArg { assertEquals("public, only-if-cached, max-stale=$expectedMaxStale", it.header("Cache-Control")) })
        }
    }

    @Test
    fun `offlineInterceptor does nothing when network is available`() {
        val connectivityManager = mockk<ConnectivityManager>()
        val chain = mockk<Interceptor.Chain>(relaxed = true)
        val request = Request.Builder().url("https://test.com").build()

        every { chain.request() } returns request
        val network = mockk<Network>()
        val networkCapabilities = mockk<NetworkCapabilities>()
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true

        val interceptor = networkModule.provideOfflineInterceptor(connectivityManager)
        interceptor.intercept(chain)

        verify { chain.proceed(request) }
    }

    @Test
    fun `networkInterceptor adds cache-control header`() {
        val chain = mockk<Interceptor.Chain>(relaxed = true)
        val request = Request.Builder().url("https://test.com").build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns response

        val interceptor = networkModule.provideNetworkInterceptor()
        val newResponse = interceptor.intercept(chain)

        val expectedMaxAge = TimeUnit.MINUTES.toSeconds(Constants.CACHE_MAX_AGE_MINUTES)
        assertEquals("public, max-age=$expectedMaxAge", newResponse.header("Cache-Control"))
    }

    @Test
    fun `provideOkHttpClient provides a valid OkHttpClient`() {
        val cache = mockk<Cache>()
        val offlineInterceptor = mockk<Interceptor>()
        val networkInterceptor = mockk<Interceptor>()

        val okHttpClient = networkModule.provideOkHttpClient(cache, offlineInterceptor, networkInterceptor)

        assertEquals(cache, okHttpClient.cache)
        assertTrue(okHttpClient.interceptors.contains(offlineInterceptor))
        assertTrue(okHttpClient.networkInterceptors.contains(networkInterceptor))
        assertEquals(TimeUnit.SECONDS.toMillis(Constants.TIMEOUT_SECONDS).toInt(), okHttpClient.connectTimeoutMillis)
        assertEquals(TimeUnit.SECONDS.toMillis(Constants.TIMEOUT_SECONDS).toInt(), okHttpClient.readTimeoutMillis)
        assertEquals(TimeUnit.SECONDS.toMillis(Constants.TIMEOUT_SECONDS).toInt(), okHttpClient.writeTimeoutMillis)
    }
}