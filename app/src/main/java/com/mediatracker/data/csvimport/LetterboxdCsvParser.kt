package com.mediatracker.data.csvimport

import com.mediatracker.domain.model.ItemStatus
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object LetterboxdCsvParser {

    private const val COLUMN_NAME = "Name"
    private const val COLUMN_YEAR = "Year"
    private const val COLUMN_WATCHED = "Watched"
    private const val COLUMN_WATCHLIST = "In Watchlist"

    fun parse(inputStream: InputStream): ImportPreview {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val lines = reader.readLines()
        if (lines.size < 2) return ImportPreview(0, 0, 0, 0, 0, emptyList())

        val headers = parseCsvLine(lines[0])
        val nameIdx = headers.indexOf(COLUMN_NAME).takeIf { it >= 0 } ?: return ImportPreview(0, 0, 0, 0, 0, emptyList())
        val yearIdx = headers.indexOf(COLUMN_YEAR).takeIf { it >= 0 } ?: -1
        val watchedIdx = headers.indexOf(COLUMN_WATCHED).takeIf { it >= 0 } ?: -1
        val watchlistIdx = headers.indexOf(COLUMN_WATCHLIST).takeIf { it >= 0 } ?: -1

        val items = mutableListOf<ImportItem>()
        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue
            val cols = parseCsvLine(line)
            val title = cols.getOrNull(nameIdx)?.trim() ?: continue
            if (title.isBlank()) continue
            val year = if (yearIdx >= 0) cols.getOrNull(yearIdx)?.trim() ?: "" else ""
            val watched = if (watchedIdx >= 0) cols.getOrNull(watchedIdx)?.trim()?.lowercase() == "true" else false
            val inWatchlist = if (watchlistIdx >= 0) cols.getOrNull(watchlistIdx)?.trim()?.lowercase() == "true" else false

            val status = when {
                watched -> ItemStatus.COMPLETED
                inWatchlist -> ItemStatus.WATCHLIST
                else -> ItemStatus.WATCHLIST
            }

            items.add(
                ImportItem(
                    title = title,
                    year = year,
                    mediaType = "MOVIE",
                    status = status,
                    originalLine = i + 1,
                )
            )
        }

        return ImportPreview(
            totalItems = items.size,
            completed = items.count { it.status == ItemStatus.COMPLETED },
            watchlist = items.count { it.status == ItemStatus.WATCHLIST },
            inProgress = 0,
            abandoned = 0,
            items = items,
        )
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        for (char in line) {
            when {
                char == '"' && !inQuotes -> inQuotes = true
                char == '"' && inQuotes -> inQuotes = false
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())
        return result
    }
}
