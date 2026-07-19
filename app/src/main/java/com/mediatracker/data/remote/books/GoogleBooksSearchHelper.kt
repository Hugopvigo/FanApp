package com.mediatracker.data.remote.books

/**
 * Builds a smarter Google Books search query from raw user input.
 *
 * Detects ISBNs to construct the most effective [q] parameter using
 * Google Books advanced syntax (isbn: → exact ISBN match); everything
 * else relies on Google Books full-text relevance.
 */
fun buildGoogleBooksQuery(rawQuery: String): String {
    val trimmed = rawQuery.trim()
    if (trimmed.isBlank()) return ""

    // 1. ISBN detection (10 or 13 digits, with or without hyphens)
    val digitsOnly = trimmed.replace("-", "").replace(" ", "")
    if (digitsOnly.matches(Regex("^\\d{10}$|^\\d{13}$"))) {
        return "isbn:$digitsOnly"
    }

    // 2. ISBN with "isbn" prefix (e.g. "isbn 978...", "ISBN:978...")
    val isbnPrefix = Regex("^isbn[:\\s]*", RegexOption.IGNORE_CASE)
    if (isbnPrefix.containsMatchIn(trimmed)) {
        val isbnPart = trimmed.replace(isbnPrefix, "").replace("-", "").replace(" ", "")
        return "isbn:$isbnPart"
    }

    // 3. General query: send as-is. Google Books full-text relevance handles
    //    authors and titles well; forcing an exact phrase ("Stephen King")
    //    restricted results, and no heuristic can tell an author name from a
    //    capitalized title ("La Sombra Del Viento").
    return trimmed
}

/**
 * Returns the appropriate langRestrict for Google Books based on device locale.
 * Falls back to "es" (default for this app's Spanish audience).
 */
fun getBookSearchLang(): String {
    return try {
        val locales = android.content.res.Resources.getSystem().configuration.locales
        val lang = locales.get(0).language
        // Google Books accepts two-letter ISO-639-1 codes
        when (lang) {
            "es", "en", "fr", "de", "it", "pt", "ja", "zh", "ru", "ko" -> lang
            else -> "es" // default to Spanish for unknown locales
        }
    } catch (_: Exception) {
        "es" // fail-safe fallback
    }
}
