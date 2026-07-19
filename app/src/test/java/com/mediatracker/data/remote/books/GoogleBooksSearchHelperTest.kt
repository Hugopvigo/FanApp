package com.mediatracker.data.remote.books

import org.junit.Assert.assertEquals
import org.junit.Test

class GoogleBooksSearchHelperTest {

    @Test
    fun `isbn13 with hyphens becomes isbn query`() {
        assertEquals("isbn:9788445077528", buildGoogleBooksQuery("978-84-450-7752-8"))
    }

    @Test
    fun `isbn10 becomes isbn query`() {
        assertEquals("isbn:8445077521", buildGoogleBooksQuery("8445077521"))
    }

    @Test
    fun `isbn prefix is stripped`() {
        assertEquals("isbn:9788445077528", buildGoogleBooksQuery("ISBN: 978-84-450-7752-8"))
    }

    @Test
    fun `author-like query is sent as-is, not exact phrase`() {
        assertEquals("Stephen King", buildGoogleBooksQuery("Stephen King"))
    }

    @Test
    fun `capitalized title query is sent as-is`() {
        assertEquals("La Sombra Del Viento", buildGoogleBooksQuery("La Sombra Del Viento"))
    }

    @Test
    fun `lowercase query is sent as-is`() {
        assertEquals("el nombre del viento", buildGoogleBooksQuery("el nombre del viento"))
    }

    @Test
    fun `blank query returns empty`() {
        assertEquals("", buildGoogleBooksQuery("   "))
    }
}
