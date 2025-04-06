package com.example.bovara.statistics.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class DetailItem(
    val name: String,
    val value: Int,
    val previousValue: Int? = null
)

@Composable
fun DetailCard(
    title: String,
    color: Color,
    infoText: String,
    items: List<DetailItem>
) {
    var showInfo by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(color, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Icono de información
                IconButton(
                    onClick = { showInfo = !showInfo },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = Color.Gray.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Mostrar información si se hace clic en el icono de info
            AnimatedVisibility(visible = showInfo) {
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de detalles
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.value.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        // Mostrar cambio si hay valor previo
                        item.previousValue?.let {
                            val change = item.value - it
                            val arrowIcon = if (change > 0) {
                                Icons.Default.TrendingUp
                            } else if (change < 0) {
                                Icons.Default.TrendingDown
                            } else {
                                null
                            }

                            val changeColor = when {
                                change > 0 -> Color(0xFF4CAF50) // Verde
                                change < 0 -> Color(0xFFE53935) // Rojo
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            if (arrowIcon != null) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = arrowIcon,
                                    contentDescription = null,
                                    tint = changeColor,
                                    modifier = Modifier.size(16.dp)
                                )

                                Text(
                                    text = if (change > 0) "+$change" else "$change",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = changeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}