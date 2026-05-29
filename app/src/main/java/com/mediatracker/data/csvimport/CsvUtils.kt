package com.mediatracker.data.csvimport

internal fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var current = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
        val char = line[i]
        when {
            char == '"' && !inQuotes -> inQuotes = true
            char == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                current.append('"')
                i++
            }
            char == '"' && inQuotes -> inQuotes = false
            char == ',' && !inQuotes -> {
                result.add(current.toString())
                current = StringBuilder()
            }
            else -> current.append(char)
        }
        i++
    }
    result.add(current.toString())
    return result
}
