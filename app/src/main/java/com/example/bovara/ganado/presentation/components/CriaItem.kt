package com.example.bovara.ganado.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bovara.core.utils.DateUtils
import com.example.bovara.ganado.data.model.GanadoEntity

/**
 * Componente para mostrar la información de una cría en una lista
 * @param cria La entidad de GanadoEntity que representa a la cría
 * @param onClick Función a ejecutar cuando se hace clic en el elemento
 */
@Composable
fun CriaItem(
    cria: GanadoEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono según el tipo
        Icon(
            imageVector = when (cria.tipo) {
                "becerro", "torito" -> Icons.Default.Male
                "becerra", "vaca" -> Icons.Default.Female
                else -> Icons.Default.Pets
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Información de la cría
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cria.apodo ?: "Sin nombre",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Arete: ${cria.numeroArete}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = "Tipo: ${cria.tipo.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            cria.fechaNacimiento?.let { fecha ->
                Text(
                    text = "Fecha de nacimiento: ${DateUtils.formatDate(fecha)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Flecha para navegar al detalle
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Ver detalle",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}