package com.example.bovara.medicamento.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bovara.core.utils.DateUtils
import com.example.bovara.di.AppModule
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TimePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVacunaScreen(
    ganadoId: Int,
    onNavigateBack: () -> Unit,
    onVacunaAdded: () -> Unit
) {
    val context = LocalContext.current
    val medicamentoUseCase = AppModule.provideMedicamentoUseCase(context)
    val ganadoUseCase = AppModule.provideGanadoUseCase(context)
    val pendienteUseCase = AppModule.providePendienteUseCase(context)

    val viewModel: AddVacunaViewModel = viewModel(
        factory = AddVacunaViewModel.Factory(ganadoId, medicamentoUseCase, ganadoUseCase, pendienteUseCase)
    )

    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var showDatePicker by remember { mutableStateOf(false) }

    // Para el campo de número de aplicaciones
    var numeroAplicacionesText by remember {
        mutableStateOf(if (state.numeroAplicaciones > 0) state.numeroAplicaciones.toString() else "")
    }

    // Para el campo de intervalo en días
    var intervaloEnDiasText by remember {
        mutableStateOf(if (state.intervaloEnDias > 0) state.intervaloEnDias.toString() else "")
    }

    // Al iniciar la pantalla, asegurarse de que esté marcado como aplicado y no programado
    LaunchedEffect(key1 = Unit) {
        viewModel.onEvent(AddVacunaEvent.AplicadoChanged(true))
        viewModel.onEvent(AddVacunaEvent.EsProgramadoChanged(false))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.ganadoInfo?.let {
                            "Vacunar: ${it.first ?: "Animal #${it.second.takeLast(4)}"}"
                        } ?: "Registrar Vacuna"
                    )
                },
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
            Spacer(modifier = Modifier.height(8.dp))

            // Nombre del medicamento
            OutlinedTextField(
                value = state.nombre,
                onValueChange = { viewModel.onEvent(AddVacunaEvent.NombreChanged(it)) },
                label = { Text("Nombre del Medicamento*") },
                placeholder = { Text("Ej: Ivermectina, Vacuna Viral, etc.") },
                singleLine = true,
                isError = state.nombreError != null,
                supportingText = state.nombreError?.let {
                    { Text(text = it, color = MaterialTheme.colorScheme.error) }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
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

            // Descripción o propósito
            OutlinedTextField(
                value = state.descripcion,
                onValueChange = { viewModel.onEvent(AddVacunaEvent.DescripcionChanged(it)) },
                label = { Text("Descripción o Propósito*") },
                placeholder = { Text("Ej: Prevención de fiebre aftosa") },
                isError = state.descripcionError != null,
                supportingText = state.descripcionError?.let {
                    { Text(text = it, color = MaterialTheme.colorScheme.error) }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Tipo de medicamento
            Text(
                text = "Tipo de Medicamento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MedicamentoTypeOption(
                    selected = state.tipo == "vacuna",
                    title = "Vacuna",
                    icon = Icons.Default.HealthAndSafety,
                    onClick = { viewModel.onEvent(AddVacunaEvent.TipoChanged("vacuna")) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                MedicamentoTypeOption(
                    selected = state.tipo == "desparasitante",
                    title = "Desparasitante",
                    icon = Icons.Default.Healing,
                    onClick = { viewModel.onEvent(AddVacunaEvent.TipoChanged("desparasitante")) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MedicamentoTypeOption(
                    selected = state.tipo == "vitamina",
                    title = "Vitamina",
                    icon = Icons.Default.LocalPharmacy,
                    onClick = { viewModel.onEvent(AddVacunaEvent.TipoChanged("vitamina")) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                MedicamentoTypeOption(
                    selected = state.tipo == "antibiótico",
                    title = "Antibiótico",
                    icon = Icons.Default.Biotech,
                    onClick = { viewModel.onEvent(AddVacunaEvent.TipoChanged("antibiótico")) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Dosis en mililitros
            OutlinedTextField(
                value = state.dosisML,
                onValueChange = { viewModel.onEvent(AddVacunaEvent.DosisMLChanged(it)) },
                label = { Text("Dosis (ml)*") },
                placeholder = { Text("Ej: 5.5") },
                singleLine = true,
                isError = state.dosisMLError != null,
                supportingText = state.dosisMLError?.let {
                    { Text(text = it, color = MaterialTheme.colorScheme.error) }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Opción única: Ya aplicada (sin opción de programación)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Fecha de aplicación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            // Fecha de aplicación obligatoria
            OutlinedTextField(
                value = state.fechaAplicacion?.let {
                    DateUtils.formatDate(it)
                } ?: "",
                onValueChange = { /* No editable directamente */ },
                label = { Text("Fecha de Aplicación") },
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

            // Después del campo de fecha de aplicación, añadimos esta sección
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = state.esMultipleAplicacion,
                    onCheckedChange = { viewModel.onEvent(AddVacunaEvent.EsMultipleAplicacionChanged(it)) }
                )

                Text(
                    text = "Aplicaciones múltiples",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (state.esMultipleAplicacion) {
                // Campo de número de aplicaciones
                OutlinedTextField(
                    value = numeroAplicacionesText,
                    onValueChange = { newValue ->
                        numeroAplicacionesText = newValue
                        if (newValue.isEmpty()) {
                            // No hacer nada si está vacío, mantener campo vacío
                        } else {
                            // Intenta convertir a número sólo si hay algún valor
                            newValue.toIntOrNull()?.let { num ->
                                if (num > 0) {
                                    viewModel.onEvent(AddVacunaEvent.NumeroAplicacionesChanged(num))
                                }
                            }
                        }
                    },
                    label = { Text("Número de aplicaciones") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Repeat,
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

                // Campo de intervalo en días
                OutlinedTextField(
                    value = intervaloEnDiasText,
                    onValueChange = { newValue ->
                        intervaloEnDiasText = newValue
                        if (newValue.isEmpty()) {
                            // No hacer nada si está vacío, mantener campo vacío
                        } else {
                            // Intenta convertir a número sólo si hay algún valor
                            newValue.toIntOrNull()?.let { num ->
                                if (num >= 0) {
                                    viewModel.onEvent(AddVacunaEvent.IntervaloEnDiasChanged(num))
                                }
                            }
                        }
                    },
                    label = { Text("Intervalo en días") },
                    placeholder = { Text("Ej: 2 (para aplicar cada 2 días)") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
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

                // Hora de aplicación
                var showTimePicker by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = state.horaAplicacion?.let {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                    } ?: "",
                    onValueChange = { /* No editable directamente */ },
                    label = { Text("Hora de aplicación (opcional)") },
                    placeholder = { Text("Ej: 16:00") },
                    singleLine = true,
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Seleccionar hora"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Time picker dialog
                if (showTimePicker) {
                    val timePickerState = rememberTimePickerState(
                        initialHour = state.horaAplicacion?.let {
                            Calendar.getInstance().apply { time = it }.get(Calendar.HOUR_OF_DAY)
                        } ?: 12,
                        initialMinute = state.horaAplicacion?.let {
                            Calendar.getInstance().apply { time = it }.get(Calendar.MINUTE)
                        } ?: 0
                    )

                    TimePickerDialog(
                        onDismissRequest = { showTimePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    // Crear fecha con la hora seleccionada
                                    val hora = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                        set(Calendar.MINUTE, timePickerState.minute)
                                        set(Calendar.SECOND, 0)
                                    }.time

                                    viewModel.onEvent(AddVacunaEvent.HoraAplicacionChanged(hora))
                                    showTimePicker = false
                                }
                            ) {
                                Text("Aceptar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text("Cancelar")
                            }
                        }
                    ) {
                        TimePicker(state = timePickerState)
                    }
                }

                // Texto informativo
                if (state.numeroAplicaciones > 1 && state.intervaloEnDias > 0) {
                    Text(
                        text = "Se aplicará ${state.numeroAplicaciones} veces, con un intervalo de ${state.intervaloEnDias} día(s) entre cada aplicación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Mostrar las fechas de aplicación
                    if (state.fechaAplicacion != null) {
                        Text(
                            text = "Fechas de aplicación:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                            // Primera aplicación (actual)
                            Text(
                                text = "1. ${DateUtils.formatDate(state.fechaAplicacion!!)} (hoy)",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            // Aplicaciones siguientes
                            for (i in 2..state.numeroAplicaciones) {
                                val fechaProxima = Calendar.getInstance().apply {
                                    time = state.fechaAplicacion!!
                                    add(Calendar.DAY_OF_YEAR, state.intervaloEnDias * (i - 1))
                                }.time

                                Text(
                                    text = "$i. ${DateUtils.formatDate(fechaProxima)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Notas adicionales (opcional)
            OutlinedTextField(
                value = state.notas,
                onValueChange = { viewModel.onEvent(AddVacunaEvent.NotasChanged(it)) },
                label = { Text("Notas adicionales (opcional)") },
                placeholder = { Text("Agregue cualquier información adicional") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            // Botón para guardar
            Button(
                onClick = { viewModel.onEvent(AddVacunaEvent.SaveVacuna) },
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
                        text = "Guardar Medicamento",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val displayCalendar = Calendar.getInstance()

        // Si ya hay una fecha guardada, usarla y restarle 1 día para la visualización
        if (state.fechaAplicacion != null) {
            displayCalendar.time = state.fechaAplicacion!!
            //displayCalendar.add(Calendar.DAY_OF_MONTH, -1) // Restar un día
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = displayCalendar.timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Creamos un objeto Date con la fecha seleccionada
                            val selectedDate = Date(millis)

                            // Ajustamos la fecha sumando el día que restamos antes
                            val adjustedCalendar = Calendar.getInstance().apply {
                                time = selectedDate
                                add(Calendar.DAY_OF_MONTH, 1) // Sumar el día nuevamente
                            }

                            viewModel.onEvent(AddVacunaEvent.FechaAplicacionChanged(adjustedCalendar.time))
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

    // Observar si se guardó el medicamento
    LaunchedEffect(state.savedSuccessfully) {
        if (state.savedSuccessfully) {
            onVacunaAdded()
        }
    }
}

@Composable
fun MedicamentoTypeOption(
    selected: Boolean,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .height(80.dp)
            .padding(4.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = { content() }
    )
}