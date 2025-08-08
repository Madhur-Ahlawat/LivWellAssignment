import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class Last6CardDigitsTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rawInput = text.text.take(6) // only last 6 digits
        val maskedPrefix = "•••• •••• ••" // fixed part

        // Fill remaining editable space with placeholder zeros
        val editablePart = buildString {
            rawInput.forEachIndexed { index, c ->
                append(c)
                if (index == 1 && rawInput.length > 2) {
                    append(' ')
                }
            }
            val needed = 6 - rawInput.length
            if (needed > 0) {
                // We insert space after first two if not already filled
                val afterTwo = if (rawInput.length <= 2) 1 else 0
                for (i in 0 until needed) {
                    if ((rawInput.length + i) == 2 && afterTwo == 1) append(' ')
                    append('0')
                }
            }
        }

        val transformed = maskedPrefix + editablePart

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

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}