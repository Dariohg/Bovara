package com.example.bovara.statistics.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TotalStatItem(
    title: String,
    value: Int,
    previousValue: Int? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )

        // Mostrar cambio respecto al respaldo anterior
        previousValue?.let {
            val change = value - it
            val changeText = if (change > 0) "+$change" else "$change"
            val changeColor = when {
                change > 0 -> Color(0xFF4CAF50) // Verde
                change < 0 -> Color(0xFFE53935) // Rojo
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Text(
                text = changeText,
                style = MaterialTheme.typography.bodySmall,
                color = changeColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}