package com.mediatracker.data.csvimport

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvUtilsTest {

    @Test
    fun `parseCsvLine splits simple comma-separated values`() {
        assertEquals(listOf("a", "b", "c"), parseCsvLine("a,b,c"))
    }

    @Test
    fun `parseCsvLine handles quoted field containing a comma`() {
        assertEquals(listOf("hello, world", "value"), parseCsvLine("\"hello, world\",value"))
    }

    @Test
    fun `parseCsvLine handles RFC 4180 escaped double quotes inside quoted field`() {
        assertEquals(listOf("He said \"hello\"", "b"), parseCsvLine("\"He said \"\"hello\"\"\",b"))
    }

    @Test
    fun `parseCsvLine handles empty fields`() {
        assertEquals(listOf("a", "", "c"), parseCsvLine("a,,c"))
    }

    @Test
    fun `parseCsvLine returns single-element list for line without commas`() {
        assertEquals(listOf("simple"), parseCsvLine("simple"))
    }

    @Test
    fun `parseCsvLine handles empty string`() {
        assertEquals(listOf(""), parseCsvLine(""))
    }
}
