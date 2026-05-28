package com.mediatracker.data.csvimport

import com.mediatracker.domain.model.ItemStatus
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object GoodreadsCsvParser {

    private const val COLUMN_TITLE = "Title"
    private const val COLUMN_AUTHOR = "Author"
    private const val COLUMN_PAGES = "Number of Pages"
    private const val COLUMN_READ = "Exclusive Shelf"
    private const val COLUMN_RATING = "My Rating"
    private const val COLUMN_DATE_READ = "Date Read"
    private const val COLUMN_ISBN = "ISBN"

    fun parse(inputStream: InputStream): ImportPreview {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val lines = reader.readLines()
        if (lines.size < 2) return ImportPreview(0, 0, 0, 0, 0, emptyList())

        val headers = parseCsvLine(lines[0])
        val titleIdx = headers.indexOf(COLUMN_TITLE).takeIf { it >= 0 } ?: return ImportPreview(0, 0, 0, 0, 0, emptyList())
        val shelfIdx = headers.indexOf(COLUMN_READ).takeIf { it >= 0 } ?: -1

        val items = mutableListOf<ImportItem>()
        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue
            val cols = parseCsvLine(line)
            val title = cols.getOrNull(titleIdx)?.trim() ?: continue
            if (title.isBlank()) continue
            val shelf = if (shelfIdx >= 0) cols.getOrNull(shelfIdx)?.trim()?.lowercase() ?: "" else ""

            val status = when (shelf) {
                "read" -> ItemStatus.COMPLETED
                "currently-reading" -> ItemStatus.IN_PROGRESS
                "to-read" -> ItemStatus.WATCHLIST
                "did-not-finish" -> ItemStatus.ABANDONED
                else -> ItemStatus.WATCHLIST
            }

            items.add(
                ImportItem(
                    title = title,
                    year = "",
                    mediaType = "BOOK",
                    status = status,
                    originalLine = i + 1,
                )
            )
        }

        return ImportPreview(
            totalItems = items.size,
            completed = items.count { it.status == ItemStatus.COMPLETED },
            watchlist = items.count { it.status == ItemStatus.WATCHLIST },
            inProgress = items.count { it.status == ItemStatus.IN_PROGRESS },
            abandoned = items.count { it.status == ItemStatus.ABANDONED },
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
