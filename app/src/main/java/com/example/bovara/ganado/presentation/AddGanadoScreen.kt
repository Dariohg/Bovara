package com.example.bovara.ganado.presentation

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.bovara.ganado.presentation.components.StatusOption
import com.example.bovara.ganado.presentation.components.TypeOption
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGanadoScreen(
    onNavigateBack: () -> Unit,
    onGanadoAdded: (Int) -> Unit
) {
    val context = LocalContext.current
    val ganadoUseCase = AppModule.provideGanadoUseCase(context)

    val viewModel: AddGanadoViewModel = viewModel(
        factory = AddGanadoViewModel.Factory(ganadoUseCase)
    )

    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Lanzador para la galería de imágenes
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Guarda la ruta de la imagen en el estado
            val imagePath = ImageUtils.saveImageToInternalStorage(context, it)
            viewModel.onEvent(AddGanadoEvent.ImageUrlChanged(imagePath))
        }
    }

    // Lanzador para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Si el permiso es concedido, abre la galería
            imagePicker.launch("image/*")
        } else {
            // Manejo de permiso denegado (puedes mostrar un mensaje)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Animal") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        // Solicitar permiso al hacer clic en la imagen
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    // Mostrar imagen seleccionada
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Imagen del animal",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Mostrar placeholder
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Agregar foto",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Toca para agregar una foto",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Número de arete
            OutlinedTextField(
                value = state.numeroArete,
                onValueChange = { viewModel.onEvent(AddGanadoEvent.NumeroAreteChanged(it)) },
                label = { Text("Número de Arete*") },
                placeholder = { Text("Ej: 0712345678") },
                singleLine = true,
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

            // Apodo (opcional)
            OutlinedTextField(
                value = state.apodo,
                onValueChange = { viewModel.onEvent(AddGanadoEvent.ApodoChanged(it)) },
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

            // Selector de sexo
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
                    onClick = { viewModel.onEvent(AddGanadoEvent.SexoChanged("macho")) },
                    modifier = Modifier.weight(1f)
                )

                GenderOption(
                    title = "Hembra",
                    icon = Icons.Rounded.Female,
                    isSelected = state.sexo == "hembra",
                    onClick = { viewModel.onEvent(AddGanadoEvent.SexoChanged("hembra")) },
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

            // Tipo de animal (depende del sexo)
            Text(
                text = "Tipo*",
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
                        onClick = { viewModel.onEvent(AddGanadoEvent.TipoChanged("toro")) },
                        modifier = Modifier.weight(1f)
                    )

                    TypeOption(
                        title = "Torito",
                        isSelected = state.tipo == "torito",
                        onClick = { viewModel.onEvent(AddGanadoEvent.TipoChanged("torito")) },
                        modifier = Modifier.weight(1f)
                    )

                    TypeOption(
                        title = "Becerro",
                        isSelected = state.tipo == "becerro",
                        onClick = { viewModel.onEvent(AddGanadoEvent.TipoChanged("becerro")) },
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
                        onClick = { viewModel.onEvent(AddGanadoEvent.TipoChanged("vaca")) },
                        modifier = Modifier.weight(1f)
                    )

                    TypeOption(
                        title = "Becerra",
                        isSelected = state.tipo == "becerra",
                        onClick = { viewModel.onEvent(AddGanadoEvent.TipoChanged("becerra")) },
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
                onValueChange = { viewModel.onEvent(AddGanadoEvent.ColorChanged(it)) },
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

            // Estado del animal (por defecto "activo")
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
                    onClick = { viewModel.onEvent(AddGanadoEvent.EstadoChanged("activo")) },
                    modifier = Modifier.weight(1f)
                )

                StatusOption(
                    title = "Vendido",
                    isSelected = state.estado == "vendido",
                    color = Color(0xFFFFC107),
                    onClick = { viewModel.onEvent(AddGanadoEvent.EstadoChanged("vendido")) },
                    modifier = Modifier.weight(1f)
                )

                StatusOption(
                    title = "Muerto",
                    isSelected = state.estado == "muerto",
                    color = Color(0xFFE53935),
                    onClick = { viewModel.onEvent(AddGanadoEvent.EstadoChanged("muerto")) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Botón para guardar
            Button(
                onClick = {
                    viewModel.onEvent(AddGanadoEvent.SaveGanado)
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
                        text = "Guardar Animal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Espacio adicional al final para facilitar el scroll
            Spacer(modifier = Modifier.height(32.dp))
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
                            viewModel.onEvent(AddGanadoEvent.FechaNacimientoChanged(date))
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

    // Observar el ID del animal recién creado
    LaunchedEffect(state.savedGanadoId) {
        state.savedGanadoId?.let { id ->
            onGanadoAdded(id)
        }
    }
}