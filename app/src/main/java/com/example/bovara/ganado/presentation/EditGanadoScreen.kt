package com.example.bovara.ganado.presentation

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bovara.core.utils.DateUtils
import com.example.bovara.core.utils.ImageUtils
import com.example.bovara.di.AppModule
import com.example.bovara.ganado.presentation.components.GenderOption
import com.example.bovara.ganado.presentation.components.GenderOptionReadOnly
import com.example.bovara.ganado.presentation.components.StatusOption
import com.example.bovara.ganado.presentation.components.TypeOption
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGanadoScreen(
    ganadoId: Int,
    onNavigateBack: () -> Unit,
    onGanadoUpdated: (Int) -> Unit
) {
    val context = LocalContext.current
    val ganadoUseCase = AppModule.provideGanadoUseCase(context)

    val viewModel: EditGanadoViewModel = viewModel(
        factory = EditGanadoViewModel.Factory(ganadoId, ganadoUseCase)
    )

    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var showDatePicker by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showStateChangeDialog by remember { mutableStateOf(false) }
    var pendingState by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Guarda la ruta de la imagen en el estado
            val imagePath = ImageUtils.saveImageToInternalStorage(context, it)
            viewModel.onEvent(EditGanadoEvent.ImageUrlChanged(imagePath))
        }
    }
    // Lanzador para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            // Guarda la ruta de la imagen en el estado
            val imagePath = ImageUtils.saveImageToInternalStorage(context, tempImageUri!!)
            viewModel.onEvent(EditGanadoEvent.ImageUrlChanged(imagePath))
            selectedImageUri = tempImageUri
        }
    }

// Lanzador para permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si el permiso es concedido, inicia la cámara
            tempImageUri = ImageUtils.createImageUri(context)
            tempImageUri?.let {
                cameraLauncher.launch(it)
            }
        } else {
            Toast.makeText(context, "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    // Lanzador para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allPermissionsGranted = permissions.entries.all { it.value }
        if (allPermissionsGranted) {
            // Si los permisos son concedidos, abre la galería
            imagePicker.launch("image/*")
        } else {
            Toast.makeText(context, "Se necesitan permisos para acceder a la galería", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.ganado?.imagenUrl) {
        state.ganado?.imagenUrl?.let { url ->
            // Cargar imagen existente en selectedImageUri
            val bitmap = ImageUtils.loadImageFromInternalStorage(context, url)
            bitmap?.let {
                // Convertir bitmap a Uri (esta es una simplificación, necesitarás implementar esto)
                // selectedImageUri = convertBitmapToUri(context, bitmap)
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Animal") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else if (state.ganado == null && !state.isInitialLoading) {
            // Animal no encontrado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(72.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Animal no encontrado",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Volver")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Editar información del animal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    // Mostrar imagen existente o imagen seleccionada
                    if (selectedImageUri != null) {
                        // Mostrar imagen seleccionada
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Imagen del animal",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (state.ganado?.imagenUrl != null) {
                        // Mostrar imagen existente
                        val bitmap = remember(state.ganado?.imagenUrl) {
                            ImageUtils.loadImageFromInternalStorage(context, state.ganado?.imagenUrl!!)
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Imagen del animal",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Mostrar placeholder
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = "Imagen no disponible",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    } else {
                        // Mostrar placeholder
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Sin imagen",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    // Añadir botones para selección de imagen/cámara al pie de la imagen
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Botón de Galería
                        IconButton(
                            onClick = {
                                // Solicitar permisos
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                                } else {
                                    permissionLauncher.launch(arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    ))
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Galería",
                                tint = Color.White
                            )
                        }

                        // Botón de Cámara
                        IconButton(
                            onClick = {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Cámara",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Número de arete (condicional según tipo)
                OutlinedTextField(
                    value = state.numeroArete,
                    onValueChange = { viewModel.onEvent(EditGanadoEvent.NumeroAreteChanged(it)) },
                    label = {
                        Text(
                            text = if (state.tipo == "becerro" || state.tipo == "becerra")
                                "Número de Arete (opcional)"
                            else "Número de Arete*"
                        )
                    },
                    isError = state.numeroAreteError != null,
                    supportingText = {
                        if (state.numeroAreteError != null) {
                            Text(
                                text = state.numeroAreteError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Formato: 07 seguido de 8 dígitos")
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                OutlinedTextField(
                    value = state.apodo,
                    onValueChange = { viewModel.onEvent(EditGanadoEvent.ApodoChanged(it)) },
                    label = { Text("Apodo (opcional)") },
                    placeholder = { Text("Ej: Luna") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )


                // En edición, ya se permite cambiar el sexo
                Text(
                    text = "Sexo*",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GenderOption(
                        title = "Macho",
                        icon = Icons.Rounded.Male,
                        isSelected = state.sexo == "macho",
                        onClick = { viewModel.onEvent(EditGanadoEvent.SexoChanged("macho")) },
                        modifier = Modifier.weight(1f)
                    )

                    GenderOption(
                        title = "Hembra",
                        icon = Icons.Rounded.Female,
                        isSelected = state.sexo == "hembra",
                        onClick = { viewModel.onEvent(EditGanadoEvent.SexoChanged("hembra")) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (state.sexoError != null) {
                    Text(
                        text = state.sexoError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Tipo de animal
                Text(
                    text = "Tipo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Opciones de tipo para machos
                if (state.sexo == "macho") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TypeOption(
                            title = "Toro",
                            isSelected = state.tipo == "toro",
                            onClick = { viewModel.onEvent(EditGanadoEvent.TipoChanged("toro")) },
                            modifier = Modifier.weight(1f)
                        )

                        TypeOption(
                            title = "Torito",
                            isSelected = state.tipo == "torito",
                            onClick = { viewModel.onEvent(EditGanadoEvent.TipoChanged("torito")) },
                            modifier = Modifier.weight(1f)
                        )

                        TypeOption(
                            title = "Becerro",
                            isSelected = state.tipo == "becerro",
                            onClick = { viewModel.onEvent(EditGanadoEvent.TipoChanged("becerro")) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Opciones de tipo para hembras
                else if (state.sexo == "hembra") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TypeOption(
                            title = "Vaca",
                            isSelected = state.tipo == "vaca",
                            onClick = { viewModel.onEvent(EditGanadoEvent.TipoChanged("vaca")) },
                            modifier = Modifier.weight(1f)
                        )

                        TypeOption(
                            title = "Becerra",
                            isSelected = state.tipo == "becerra",
                            onClick = { viewModel.onEvent(EditGanadoEvent.TipoChanged("becerra")) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (state.tipoError != null) {
                    Text(
                        text = state.tipoError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Color del animal
                OutlinedTextField(
                    value = state.color,
                    onValueChange = { viewModel.onEvent(EditGanadoEvent.ColorChanged(it)) },
                    label = { Text("Color*") },
                    placeholder = { Text("Ej: Negro, Pinto, etc.") },
                    singleLine = true,
                    isError = state.colorError != null,
                    supportingText = state.colorError?.let {
                        { Text(text = it, color = MaterialTheme.colorScheme.error) }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FormatColorFill,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Fecha de nacimiento (opcional)
                OutlinedTextField(
                    value = state.fechaNacimiento?.let { DateUtils.formatDate(it) } ?: "",
                    onValueChange = { },
                    label = { Text("Fecha de Nacimiento (opcional)") },
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Seleccionar fecha"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Estado del animal
                Text(
                    text = "Estado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatusOption(
                        title = "Activo",
                        isSelected = state.estado == "activo",
                        color = Color(0xFF4CAF50),
                        onClick = {
                            if (state.estadoAnterior in listOf("vendido", "muerto") && state.ganado?.nota?.isNotBlank() == true) {
                                pendingState = "activo"
                                showStateChangeDialog = true
                            } else {
                                viewModel.onEvent(EditGanadoEvent.EstadoChanged("activo"))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    StatusOption(
                        title = "Vendido",
                        isSelected = state.estado == "vendido",
                        color = Color(0xFFFFC107),
                        onClick = {
                            if (state.ganado?.nota?.isNotBlank() == true) {
                                pendingState = "vendido"
                                showStateChangeDialog = true
                            } else {
                                viewModel.onEvent(EditGanadoEvent.EstadoChanged("vendido"))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    StatusOption(
                        title = "Muerto",
                        isSelected = state.estado == "muerto",
                        color = Color(0xFFE53935),
                        onClick = {
                            if (state.ganado?.nota?.isNotBlank() == true) {
                                pendingState = "muerto"
                                showStateChangeDialog = true
                            } else {
                                viewModel.onEvent(EditGanadoEvent.EstadoChanged("muerto"))
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dialog de confirmación
                if (showStateChangeDialog) {
                    AlertDialog(
                        onDismissRequest = { showStateChangeDialog = false },
                        title = { Text("Confirmar cambio de estado") },
                        text = {
                            Text(
                                "Este animal tiene notas guardadas. " +
                                        "${if (pendingState == "activo") "Las notas se mantendrán y podrás editarlas."
                                        else "Las notas se guardarán pero no podrás editarlas mientras el animal esté marcado como '$pendingState'."}"
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.onEvent(EditGanadoEvent.EstadoChanged(pendingState))
                                    showStateChangeDialog = false
                                }
                            ) {
                                Text("Confirmar")
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showStateChangeDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }

                // Botón para guardar
                Button(
                    onClick = {
                        viewModel.onEvent(EditGanadoEvent.SaveGanado)
                    },
                    enabled = !state.isLoading && state.canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Guardar Cambios",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Espacio adicional al final para facilitar el scroll
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.fechaNacimiento?.time ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            viewModel.onEvent(EditGanadoEvent.FechaNacimientoChanged(date))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Observar el ID del animal actualizado
    LaunchedEffect(state.updated) {
        if (state.updated) {
            onGanadoUpdated(ganadoId)
        }
    }
}