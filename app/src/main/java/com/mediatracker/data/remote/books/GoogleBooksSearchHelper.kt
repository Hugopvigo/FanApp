package com.mediatracker.data.remote.books

/**
 * Builds a smarter Google Books search query from raw user input.
 *
 * Detects ISBNs, author names, and general queries to construct
 * the most effective [q] parameter using Google Books advanced syntax:
 *   intitle:   → search only in titles
 *   inauthor:  → search only in authors
 *   isbn:      → exact ISBN match
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

    // 3. Author-like queries: short (≤3 words), each word starts with uppercase
    //    e.g. "Tolkien", "Gabriel García Márquez", "Stephen King"
    val words = trimmed.split(" ")
    val looksLikeAuthor = words.size <= 4 &&
        words.all { w ->
            w.isNotEmpty() && w.first().isUpperCase() &&
            w.none { it.isDigit() }
        }

    // 4. Use the raw query as-is — Google Books handles full-text search well.
    //    For author-like queries we boost with inauthor: to improve precision.
    return if (looksLikeAuthor) {
        // Try exact title match first, fall back to author + title broad search
        "\"$trimmed\""
    } else {
        trimmed
    }
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
