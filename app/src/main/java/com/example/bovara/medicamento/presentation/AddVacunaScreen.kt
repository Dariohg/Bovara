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
import java.util.*

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

    val viewModel: AddVacunaViewModel = viewModel(
        factory = AddVacunaViewModel.Factory(ganadoId, medicamentoUseCase, ganadoUseCase)
    )

    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var showDatePicker by remember { mutableStateOf(false) }

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
                value = state.fechaAplicacion?.let { DateUtils.formatDate(it) } ?: "",
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
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.fechaAplicacion?.time ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            viewModel.onEvent(AddVacunaEvent.FechaAplicacionChanged(date))
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