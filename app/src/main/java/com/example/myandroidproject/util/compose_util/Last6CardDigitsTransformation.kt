package com.example.myandroidproject.util.compose_util
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle

class Last6CardDigitsTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rawInput = text.text.take(6) // Only last 6 digits
        val maskedPrefix = "•••• •••• ••" // Fixed part before editable digits

        // Build editable part (digits typed + placeholder 0s)
        val editablePart = buildString {
            rawInput.forEachIndexed { index, c ->
                append(c)
                if (index == 1 && rawInput.length > 2) {
                    append(' ')
                }
            }
            val needed = 6 - rawInput.length
            if (needed > 0) {
                val afterTwo = if (rawInput.length <= 2) 1 else 0
                for (i in 0 until needed) {
                    if ((rawInput.length + i) == 2 && afterTwo == 1) append(' ')
                    append('0')
                }
            }
        }

        // Apply colors: black for real digits, gray for 0s, black for •
        val transformed = buildAnnotatedString {
            // Masked prefix (always black dots)
            withStyle(SpanStyle(color = Color(0xFFC1C1C1))) {
                append(maskedPrefix)
            }

            // Editable part (check each char)
            editablePart.forEach { c ->
                when {
                    c == '0' -> withStyle(SpanStyle(color = Color(0xFFC1C1C1))) {
                        append(c)
                    }
                    c.isDigit() -> withStyle(SpanStyle(color = Color.Black)) {
                        append(c)
                    }
                    else -> withStyle(SpanStyle(color = Color.Black)) {
                        append(c) // spaces stay black
                    }
                }
            }
        }

        val maskedLength = maskedPrefix.length
        val transformedLength = transformed.length

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 1 -> maskedLength + offset
                    offset in 2..rawInput.length -> maskedLength + offset + 1 // +1 for space
                    else -> maskedLength + editablePart.length
                }.coerceAtMost(transformedLength)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= maskedLength) return 0
                val relative = offset - maskedLength
                return when {
                    relative <= 1 -> relative
                    relative in 2..editablePart.length -> relative - 1
                    else -> rawInput.length
                }.coerceAtMost(rawInput.length)
            }
        }

        return TransformedText(transformed, offsetMapping)
    }
}
