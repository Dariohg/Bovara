package com.example.bovara.core.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import com.example.bovara.core.utils.DatabaseBackupUtil
import kotlinx.coroutines.launch
import android.net.Uri

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
    var lastBackupUri by remember { mutableStateOf<Uri?>(null) }
    var showRestoreSuccessDialog by remember { mutableStateOf(false) }

    // Launcher para permisos de almacenamiento (para Android 9 y anteriores)
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si el permiso es concedido, crear el respaldo
            createBackup(
                context = context,
                coroutineScope = coroutineScope,
                onStarted = { backupInProgress = true; operationSuccess = null; operationMessage = "Creando respaldo..." },
                onCompleted = { success, message, uri ->
                    backupInProgress = false
                    operationSuccess = success
                    operationMessage = message
                    lastBackupUri = uri
                }
            )
        } else {
            // Si el permiso es denegado, mostrar mensaje
            backupInProgress = false
            operationSuccess = false
            operationMessage = "Se requiere permiso de almacenamiento para crear respaldos en la carpeta Descargas"
        }
    }

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

                if (success) {
                    showRestoreSuccessDialog = true
                } else {
                    operationSuccess = false
                    operationMessage = "Error al restaurar la base de datos. Verifique que el archivo es un respaldo válido."
                }
            }
        }
    }

    // Dialog principal
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
                        // Verificar si se necesita solicitar permiso (solo en Android 9 o inferior)
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            // No se necesita permiso o ya está concedido
                            createBackup(
                                context = context,
                                coroutineScope = coroutineScope,
                                onStarted = { backupInProgress = true; operationSuccess = null; operationMessage = "Creando respaldo..." },
                                onCompleted = { success, message, uri ->
                                    backupInProgress = false
                                    operationSuccess = success
                                    operationMessage = message
                                    lastBackupUri = uri
                                }
                            )
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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

                            // Botón de compartir (solo si hay un respaldo exitoso)
                            if (operationSuccess == true && lastBackupUri != null) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/zip"
                                            putExtra(Intent.EXTRA_STREAM, lastBackupUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Compartir respaldo"))
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Compartir Respaldo")
                                }
                            }
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

    // Dialog de restauración exitosa
    if (showRestoreSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                // No hacer nada al tocar fuera del diálogo
            },
            title = {
                Text("Restauración Exitosa")
            },
            text = {
                Text("Base de datos restaurada exitosamente. La aplicación se reiniciará para aplicar los cambios.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Código para reiniciar la aplicación
                        val packageManager = context.packageManager
                        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(intent)

                        // Finalizar la actividad actual
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }
                ) {
                    Text("Aceptar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            iconContentColor = MaterialTheme.colorScheme.primary
        )
    }
}

private fun createBackup(
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onStarted: () -> Unit,
    onCompleted: (Boolean, String, Uri?) -> Unit
) {
    onStarted()

    coroutineScope.launch {
        val backupUri = DatabaseBackupUtil.backupDatabase(context)

        if (backupUri != null) {
            onCompleted(
                true,
                "Respaldo creado exitosamente en la carpeta Descargas/Bovara",
                backupUri
            )
        } else {
            onCompleted(false, "Error al crear el respaldo", null)
        }
    }
}