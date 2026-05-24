package com.mediatracker.data.remote.books

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

/**
 * OkHttp interceptor that rate-limits Google Books API requests and
 * handles 429 (Too Many Requests) with exponential backoff retry.
 *
 * Google Books free tier: 1,000 requests/day (~1 request/86s sustained).
 * This interceptor:
 *   1. Enforces a minimum 2-second gap between consecutive requests
 *      to avoid burst traffic.
 *   2. On 429: retries up to 3 times with exponential backoff
 *      (2s → 4s → 8s).
 */
class GoogleBooksRateLimiter : Interceptor {

    private var lastRequestTime = 0L
    private val minIntervalMs = 2_000L // 2 seconds between calls

    override fun intercept(chain: Interceptor.Chain): Response {
        rateLimitIfNeeded()
        val request = chain.request()

        val response = chain.proceed(request)

        if (response.code == 429) {
            response.close()
            return retryWithBackoff(chain, request, attempt = 1)
        }

        return response
    }

    private fun rateLimitIfNeeded() {
        val now = System.currentTimeMillis()
        val elapsed = now - lastRequestTime
        if (elapsed < minIntervalMs) {
            val sleepMs = minIntervalMs - elapsed
            Timber.d("Google Books rate limiter: sleeping ${sleepMs}ms")
            Thread.sleep(sleepMs)
        }
        lastRequestTime = System.currentTimeMillis()
    }

    private fun retryWithBackoff(
        chain: Interceptor.Chain,
        request: okhttp3.Request,
        attempt: Int,
    ): Response {
        if (attempt > MAX_RETRIES) {
            Timber.w("Google Books 429: max retries ($MAX_RETRIES) exceeded")
            throw IOException("Google Books API rate limited after $MAX_RETRIES retries")
        }

        val delayMs = INITIAL_BACKOFF_MS * (1 shl (attempt - 1)) // 2s, 4s, 8s
        Timber.w("Google Books 429: retry #$attempt in ${delayMs}ms")
        Thread.sleep(delayMs)

        val retryResponse = chain.proceed(request)
        if (retryResponse.code == 429) {
            retryResponse.close()
            return retryWithBackoff(chain, request, attempt + 1)
        }
        return retryResponse
    }

    companion object {
        private const val MAX_RETRIES = 3
        private const val INITIAL_BACKOFF_MS = 2_000L
    }
}
