package com.mediatracker.di

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
}
