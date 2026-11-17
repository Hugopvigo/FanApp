package com.mediatracker.data.remote.books

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Marker interceptor for the Google Books client.
 *
 * Rate limiting and 429 backoff used to live here as `Thread.sleep(...)` calls,
 * which blocked an OkHttp dispatcher thread for up to 14 seconds. Retries now
 * happen in `withBooksRetry` with coroutine `delay`, so they never block a thread.
 */
class GoogleBooksRateLimiter : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
}
