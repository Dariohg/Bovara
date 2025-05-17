package com.example.bovara.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bovara.core.presentation.DatabaseBackupDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var showBackupDialog by remember { mutableStateOf(false) }

    // Si se muestra el diálogo de respaldo
    if (showBackupDialog) {
        DatabaseBackupDialog(
            onDismissRequest = { showBackupDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            CategoryHeader(title = "Datos y almacenamiento")

            // Opción de respaldo y restauración
            SettingsItem(
                icon = Icons.Outlined.Backup,
                title = "Respaldo y restauración",
                subtitle = "Crea y restaura respaldos locales de tus datos",
                onClick = { showBackupDialog = true }
            )

            Divider(modifier = Modifier.padding(start = 72.dp))

            // Opción de exportar a CSV
            SettingsItem(
                icon = Icons.Outlined.ImportExport,
                title = "Exportar a CSV",
                subtitle = "Exporta tus datos en formato CSV",
                onClick = { /* TODO: Implementar exportación a CSV */ }
            )

            Divider(modifier = Modifier.padding(start = 72.dp))

            // Más opciones de configuración...
            CategoryHeader(title = "Apariencia")

            // Opción de tema oscuro
            SettingsItem(
                icon = Icons.Outlined.DarkMode,
                title = "Tema oscuro",
                onClick = { /* TODO: Implementar cambio de tema */ }
            )

            Divider(modifier = Modifier.padding(start = 72.dp))

            CategoryHeader(title = "Acerca de")

            // Información de la app
            SettingsItem(
                icon = Icons.Outlined.Info,
                title = "Acerca de Bovara",
                subtitle = "Versión 1.0.0",
                onClick = { /* TODO: Mostrar información de la app */ }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}