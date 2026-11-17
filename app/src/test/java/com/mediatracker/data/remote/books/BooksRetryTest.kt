package com.mediatracker.data.remote.books

import com.mediatracker.data.repository.withBooksRetry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class BooksRetryTest {
    private fun httpError(code: Int): HttpException =
        HttpException(Response.error<Any>(code, okhttp3.ResponseBody.create(null, "")))

    @Test
    fun `reintenta tras 429 y devuelve el resultado`() = runTest {
        var attempts = 0
        val result = withBooksRetry(delayMs = 0) {
            attempts++
            if (attempts < 3) throw httpError(429) else "ok"
        }
        assertEquals("ok", result)
        assertEquals(3, attempts)
    }

    @Test
    fun `no reintenta errores que no son 429`() = runTest {
        var attempts = 0
        try {
            withBooksRetry(delayMs = 0) {
                attempts++
                throw httpError(500)
            }
        } catch (_: HttpException) {
            // esperado
        }
        assertEquals(1, attempts)
    }
}
