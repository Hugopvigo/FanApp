package com.mediatracker.domain.usecase

import com.mediatracker.data.csvimport.GoodreadsCsvParser
import com.mediatracker.data.csvimport.ImportItem
import com.mediatracker.data.csvimport.ImportPreview
import com.mediatracker.data.csvimport.ImportResult
import com.mediatracker.data.csvimport.LetterboxdCsvParser
import com.mediatracker.domain.model.ItemStatus
import com.mediatracker.domain.model.MediaType
import com.mediatracker.domain.repository.MediaRepository
import com.mediatracker.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.InputStream
import javax.inject.Inject

enum class ImportSource { LETTERBOXD, GOODREADS }

class ImportUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val userRepository: UserRepository,
) {
    suspend fun preview(inputStream: InputStream, source: ImportSource): ImportPreview =
        withContext(Dispatchers.IO) {
            when (source) {
                ImportSource.LETTERBOXD -> LetterboxdCsvParser.parse(inputStream)
                ImportSource.GOODREADS -> GoodreadsCsvParser.parse(inputStream)
            }
        }

    suspend fun import(items: List<ImportItem>, onProgress: (Int, Int) -> Unit = { _, _ -> }): ImportResult =
        withContext(Dispatchers.IO) {
            var imported = 0
            var duplicates = 0
            var failed = 0
            val errors = mutableListOf<String>()
            val existingItems = try {
                userRepository.getUserItemsFlow().first()
            } catch (e: Exception) {
                Timber.e(e, "Failed to load existing items for duplicate check")
                emptyList()
            }

            val existingTitles = existingItems.map { it.title.lowercase().trim() }.toMutableSet()

            for ((index, item) in items.withIndex()) {
                onProgress(index + 1, items.size)
                try {
                    if (item.title.lowercase().trim() in existingTitles) {
                        duplicates++
                        continue
                    }

                    val mediaType = when (item.mediaType) {
                        "MOVIE" -> MediaType.MOVIE
                        "SERIES" -> MediaType.SERIES
                        "BOOK" -> MediaType.BOOK
                        else -> continue.also { failed++ }
                    }

                    val searchResults = mediaRepository.search(item.title, mediaType).getOrDefault(emptyList())
                    val matched = findBestMatch(searchResults, item)

                    if (matched != null) {
                        val status = if (item.status == ItemStatus.ABANDONED) ItemStatus.WATCHLIST else item.status
                        val added = userRepository.addUserItem(
                            mediaType = matched.mediaType,
                            apiId = matched.id.removePrefix("${matched.mediaType.name.lowercase()}_"),
                            title = matched.title,
                            posterUrl = matched.posterUrl,
                            status = status,
                        )
                        if (added.isSuccess) {
                            imported++
                            existingTitles.add(item.title.lowercase().trim())
                        } else {
                            failed++
                        }
                    } else {
                        userRepository.addUserItem(
                            mediaType = mediaType,
                            apiId = "import_${System.currentTimeMillis()}_$index",
                            title = item.title,
                            posterUrl = null,
                            status = if (item.status == ItemStatus.ABANDONED) ItemStatus.WATCHLIST else item.status,
                        )
                        imported++
                        existingTitles.add(item.title.lowercase().trim())
                        errors.add("Linea ${item.originalLine}: '${item.title}' — no se encontro en la API, se agrego sin poster")
                    }
                } catch (e: Exception) {
                    failed++
                    errors.add("Linea ${item.originalLine}: '${item.title}' — ${e.message}")
                    Timber.e(e, "Import failed for item: ${item.title}")
                }
            }

            ImportResult(
                totalAttempted = items.size,
                imported = imported,
                duplicates = duplicates,
                failed = failed,
                errors = errors,
            )
        }

    private fun findBestMatch(
        searchResults: List<com.mediatracker.domain.model.MediaItem>,
        importItem: ImportItem,
    ): com.mediatracker.domain.model.MediaItem? {
        if (searchResults.isEmpty()) return null
        val exactMatch = searchResults.find {
            it.title.equals(importItem.title, ignoreCase = true)
        }
        if (exactMatch != null) return exactMatch
        if (importItem.year.isNotBlank()) {
            val yearMatch = searchResults.find {
                it.releaseDate.startsWith(importItem.year)
            }
            if (yearMatch != null) return yearMatch
        }
        return searchResults.first()
    }
}
