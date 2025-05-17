package com.example.bovara.core.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.bovara.core.utils.DatabaseBackupUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseBackupDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var backupInProgress by remember { mutableStateOf(false) }
    var restoreInProgress by remember { mutableStateOf(false) }
    var operationSuccess by remember { mutableStateOf<Boolean?>(null) }
    var operationMessage by remember { mutableStateOf("") }

    // Launcher para seleccionar archivo de respaldo para restaurar
    val selectBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            restoreInProgress = true
            operationSuccess = null
            operationMessage = "Restaurando base de datos..."

            coroutineScope.launch {
                val success = DatabaseBackupUtil.restoreDatabase(context, it)
                restoreInProgress = false
                operationSuccess = success
                operationMessage = if (success) {
                    "Base de datos restaurada exitosamente. La aplicación se reiniciará para aplicar los cambios."
                } else {
                    "Error al restaurar la base de datos. Verifique que el archivo es un respaldo válido."
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Respaldo de Datos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Desde aquí puedes crear respaldos de todos tus datos o restaurar un respaldo previamente creado.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón para crear respaldo
                Button(
                    onClick = {
                        backupInProgress = true
                        operationSuccess = null
                        operationMessage = "Creando respaldo..."

                        coroutineScope.launch {
                            val backupUri = DatabaseBackupUtil.backupDatabase(context)
                            backupInProgress = false

                            if (backupUri != null) {
                                operationSuccess = true
                                operationMessage = "Respaldo creado exitosamente"

                                // Compartir el archivo
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "application/zip"
                                    putExtra(android.content.Intent.EXTRA_STREAM, backupUri)
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir respaldo"))
                            } else {
                                operationSuccess = false
                                operationMessage = "Error al crear el respaldo"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !backupInProgress && !restoreInProgress
                ) {
                    if (backupInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Crear Respaldo")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para restaurar respaldo
                OutlinedButton(
                    onClick = {
                        selectBackupLauncher.launch("application/zip")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !backupInProgress && !restoreInProgress
                ) {
                    if (restoreInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.RestorePage,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Restaurar Respaldo")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar mensaje de éxito o error
                if (operationSuccess != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (operationSuccess == true)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (operationSuccess == true)
                                    Icons.Default.CheckCircle
                                else
                                    Icons.Default.Error,
                                contentDescription = null,
                                tint = if (operationSuccess == true)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = operationMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (operationSuccess == true)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón para cerrar
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}