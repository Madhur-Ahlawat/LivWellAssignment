package com.example.myandroidproject.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberPad(
    onNumberClick: (String) -> Unit,
    onDelete: () -> Unit,
    onSubmit: () -> Unit
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("⌫", "0", "✓")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    NumberKey(
                        label = key,
                        modifier = Modifier
                            .size(80.dp),
                        onClick = {
                            when (key) {
                                "⌫" -> onDelete()
                                "✓" -> onSubmit()
                                else -> onNumberClick(key)
                            }
                        }
                    )
                }
            }
        }
    }
}
@Composable
fun NumberKey(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF2F2F2),
            contentColor = Color.Black
        ),
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
