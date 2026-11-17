package com.mediatracker.di

import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class NetworkCacheTest {

    @Test
    fun `buildCache usa diez MiB`() {
        val dir = File(System.getProperty("java.io.tmpdir"), "fanapp-cache-test")
        dir.mkdirs()
        val cache = NetworkModule.buildCache(dir)
        assertEquals(NetworkModule.CACHE_SIZE_BYTES, cache.maxSize())
        cache.evictAll()
    }

    @Test
    fun `los tres clientes registran cacheControl como network interceptor`() {
        val dir = File(System.getProperty("java.io.tmpdir"), "fanapp-cache-test")
        dir.mkdirs()
        val cache = NetworkModule.buildCache(dir)
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.NONE }
        val apiKeyInterceptor = Interceptor { chain -> chain.proceed(chain.request()) }

        val clients = listOf(
            NetworkModule.provideTmdbOkHttpClient(logging, apiKeyInterceptor, cache),
            NetworkModule.provideBooksOkHttpClient(logging, cache),
            NetworkModule.provideOpenLibraryOkHttpClient(logging, cache),
        )

        clients.forEach { client ->
            assertEquals(
                "cacheControl debe ser network interceptor para que CacheInterceptor vea la cabecera",
                1,
                client.networkInterceptors.size,
            )
        }
        cache.evictAll()
    }
}
