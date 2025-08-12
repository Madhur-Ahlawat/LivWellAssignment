package com.example.myandroidproject.util.compose_util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class ZeroGrayTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder()

        text.forEach { char ->
            if (char == '0') {
                builder.pushStyle(SpanStyle(color = Color(0xFFC1C1C1)))
                builder.append(char)
                builder.pop()
            } else {
                builder.pushStyle(SpanStyle(color = Color.Black))
                builder.append(char)
                builder.pop()
            }
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}