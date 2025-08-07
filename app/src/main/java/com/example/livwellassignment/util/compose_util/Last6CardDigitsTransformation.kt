import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class Last6CardDigitsTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text.take(6)
        val maskedPrefix = "•••• •••• ••"  // length 12, no trailing space

        val formattedInput = buildString {
            input.forEachIndexed { index, c ->
                append(c)
                if (index == 1 && input.length > 2) {
                    append(' ')
                }
            }
        }


        val transformed = maskedPrefix + formattedInput

        val maskedLength = maskedPrefix.length
        val formattedLength = formattedInput.length
        val transformedLength = transformed.length


        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 1 -> maskedLength + offset
                    offset in 2..input.length -> maskedLength + offset + 1 // +1 for space
                    else -> transformedLength
                }.coerceAtMost(transformedLength)
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= maskedLength) return 0

                val relative = offset - maskedLength

                return when {
                    relative <= 1 -> relative
                    relative in 2..formattedLength -> relative - 1
                    else -> input.length
                }.coerceAtMost(input.length)
            }
        }

        return TransformedText(AnnotatedString(transformed), offsetMapping)
    }
}