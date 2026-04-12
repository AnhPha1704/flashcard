package com.example.flashcard.domain.util

import com.example.flashcard.data.local.entity.Flashcard

object CsvHelper {

    fun parseCsvLine(line: String): Pair<String, String>? {
        if (line.isBlank()) return null
        
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        for (i in line.indices) {
            val char = line[i]
            if (char == '\"') {
                inQuotes = !inQuotes
            } else if (char == ',' && !inQuotes) {
                result.add(current.toString().replace("\"\"", "\"").trim())
                current.clear()
            } else {
                current.append(char)
            }
        }
        result.add(current.toString().replace("\"\"", "\"").trim())
        
        return if (result.size >= 2) {
            Pair(result[0], result[1])
        } else {
            null
        }
    }

    fun toCsvLine(front: String, back: String): String {
        return "${escapeCsvField(front)},${escapeCsvField(back)}"
    }

    private fun escapeCsvField(field: String): String {
        var escaped = field
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            escaped = field.replace("\"", "\"\"")
            return "\"$escaped\""
        }
        return escaped
    }
}
