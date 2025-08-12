package com.example.myandroidproject.util.compose_util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun filterExpiryDate(input: TextFieldValue): TextFieldValue {
    // Remove non-digit and slash chars
    val digitsOnly = input.text.filter { it.isDigit() }

    // Limit max length to 4 digits (MMYY)
    val maxLength = 4
    val trimmed = if (digitsOnly.length >= maxLength) digitsOnly.substring(0, maxLength) else digitsOnly

    // Insert slash after 2 digits
    val formatted = buildString {
        for ((index, c) in trimmed.withIndex()) {
            if (index == 2) append('/')
            append(c)
        }
    }

    // Calculate new cursor position
    var cursorPosition = input.selection.end

    // Adjust cursor for inserted slash
    if (cursorPosition == 3 && !input.text.contains('/')) {
        cursorPosition++
    }

    // Prevent cursor going beyond text length
    cursorPosition = cursorPosition.coerceIn(0, formatted.length)

    return TextFieldValue(
        text = formatted,
        selection = TextRange(cursorPosition)
    )
}