package com.example.bovara.statistics.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PieChartData(
    val name: String,
    val value: Float,
    val color: Color
)

@Composable
fun PieChartView(
    dataPoints: List<PieChartData>,
    modifier: Modifier = Modifier
) {
    val total = dataPoints.sumOf { it.value.toDouble() }.toFloat()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = minOf(centerX, centerY) * 0.9f

            var startAngle = 0f

            dataPoints.forEach { data ->
                val sweepAngle = (data.value / total) * 360f

                drawArc(
                    color = data.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - radius, centerY - radius),
                    size = Size(radius * 2, radius * 2)
                )

                startAngle += sweepAngle
            }
        }

        // Leyenda mejorada con etiquetas
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            dataPoints.forEach { data ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    // Un círculo del color correspondiente
                    Surface(
                        modifier = Modifier.size(16.dp),
                        shape = CircleShape,
                        color = data.color
                    ) {}

                    Spacer(modifier = Modifier.width(8.dp))

                    // Texto con valor y porcentaje
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "${data.name}: ${data.value.toInt()} (${(data.value / total * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}