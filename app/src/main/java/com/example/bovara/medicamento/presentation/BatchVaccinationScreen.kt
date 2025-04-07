// File: app/src/main/java/com/example/bovara/medicamento/presentation/BatchVaccinationScreen.kt
package com.example.bovara.medicamento.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bovara.core.utils.DateUtils
import com.example.bovara.core.utils.ImageUtils
import com.example.bovara.di.AppModule
import com.example.bovara.ui.theme.AccentGreen
import java.util.Date
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchVaccinationScreen(
    onNavigateBack: () -> Unit,
    onFinishVaccination: () -> Unit,
    onNavigateToRegisterMedicamento: () -> Unit,
    selectedMedicamentoId: Int? = null
) {
    val context = LocalContext.current
    val ganadoUseCase = AppModule.provideGanadoUseCase(context)
    val medicamentoUseCase = AppModule.provideMedicamentoUseCase(context)

    val viewModel: BatchVaccinationViewModel = viewModel(
        factory = BatchVaccinationViewModel.Factory(ganadoUseCase, medicamentoUseCase)
    )

    val state by viewModel.state.collectAsState()
    var showMedicamentoPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showConfirmSave by remember { mutableStateOf(false) }
    var showConfirmPause by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMedicamentoId) {
        selectedMedicamentoId?.let { id ->
            // Cargar el medicamento usando el ID
            medicamentoUseCase.getMedicamentoById(id).collect { medicamento ->
                medicamento?.let {
                    viewModel.onEvent(BatchVaccinationEvent.MedicamentoSelected(it))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vacunación Programada") },
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de selección de medicamento
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Información de Vacunación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Nombre del medicamento
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Medicamento",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = state.selectedMedicamento?.nombre ?: "Seleccionar medicamento",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(onClick = { showMedicamentoPicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Cambiar medicamento"
                            )
                        }
                    }

                    Divider()

                    // Fecha de aplicación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fecha de Aplicación",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = DateUtils.formatDate(state.fechaAplicacion),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Cambiar fecha"
                            )
                        }
                    }

                    // Dosis por animal
                    if (state.selectedMedicamento != null) {
                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dosis por Animal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "${state.dosisML} ml",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Botones para aumentar/disminuir dosis
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.onEvent(BatchVaccinationEvent.DosisDecreased) },
                                    enabled = state.dosisML > 0.5f
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Disminuir dosis")
                                }

                                IconButton(
                                    onClick = { viewModel.onEvent(BatchVaccinationEvent.DosisIncreased) }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Aumentar dosis")
                                }
                            }
                        }
                    }
                }
            }

            // Contador de animales
            if (state.animalesSeleccionados.isNotEmpty() || state.animalesCandidatos.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Animales (${state.animalesSeleccionados.size}/${state.animalesCandidatos.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (state.animalesSeleccionados.isNotEmpty()) {
                        Row {
                            // Botón para marcar todos
                            if (state.animalesSeleccionados.size < state.animalesCandidatos.size) {
                                TextButton(
                                    onClick = { viewModel.onEvent(BatchVaccinationEvent.SelectAllAnimals) }
                                ) {
                                    Text("Seleccionar todos")
                                }
                            } else {
                                TextButton(
                                    onClick = { viewModel.onEvent(BatchVaccinationEvent.UnselectAllAnimals) }
                                ) {
                                    Text("Deseleccionar todos")
                                }
                            }
                        }
                    }
                }
            }

            // Lista de animales
            if (state.animalesCandidatos.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(72.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (state.selectedMedicamento == null) "Seleccione un medicamento" else "No hay animales activos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Grid de animales
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Si no hay animales candidatos, mostrar mensaje
                    if (state.animalesCandidatos.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(72.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = if (state.selectedMedicamento == null) "Seleccione un medicamento" else "No hay animales activos",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        // Sección de Toros
                        if (state.animalesAgrupados.toros.isNotEmpty()) {
                            item {
                                CategoryHeader(title = "Toros", count = state.animalesAgrupados.toros.size)
                            }

                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((state.animalesAgrupados.toros.size / 2 + state.animalesAgrupados.toros.size % 2) * 180.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.animalesAgrupados.toros) { animal ->
                                        val isSelected = state.animalesSeleccionados.contains(animal.id)
                                        AnimalVaccinationCard(
                                            imageUrl = animal.imagenUrl,
                                            numeroArete = animal.numeroArete,
                                            apodo = animal.apodo,
                                            isSelected = isSelected,
                                            onToggleSelection = {
                                                viewModel.onEvent(BatchVaccinationEvent.ToggleAnimalSelection(animal.id))
                                            }
                                        )
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Sección de toritos
                        if (state.animalesAgrupados.toritos.isNotEmpty()) {
                            item {
                                CategoryHeader(title = "Toritos", count = state.animalesAgrupados.toritos.size)
                            }

                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((state.animalesAgrupados.toritos.size / 2 + state.animalesAgrupados.toritos.size % 2) * 180.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.animalesAgrupados.toritos) { animal ->
                                        val isSelected = state.animalesSeleccionados.contains(animal.id)
                                        AnimalVaccinationCard(
                                            imageUrl = animal.imagenUrl,
                                            numeroArete = animal.numeroArete,
                                            apodo = animal.apodo,
                                            isSelected = isSelected,
                                            onToggleSelection = {
                                                viewModel.onEvent(BatchVaccinationEvent.ToggleAnimalSelection(animal.id))
                                            }
                                        )
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Sección de Vacas
                        if (state.animalesAgrupados.vacas.isNotEmpty()) {
                            item {
                                CategoryHeader(title = "Vacas", count = state.animalesAgrupados.vacas.size)
                            }

                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((state.animalesAgrupados.vacas.size / 2 + state.animalesAgrupados.vacas.size % 2) * 180.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.animalesAgrupados.vacas) { animal ->
                                        val isSelected = state.animalesSeleccionados.contains(animal.id)
                                        AnimalVaccinationCard(
                                            imageUrl = animal.imagenUrl,
                                            numeroArete = animal.numeroArete,
                                            apodo = animal.apodo,
                                            isSelected = isSelected,
                                            onToggleSelection = {
                                                viewModel.onEvent(BatchVaccinationEvent.ToggleAnimalSelection(animal.id))
                                            }
                                        )
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Sección de Becerras
                        if (state.animalesAgrupados.becerras.isNotEmpty()) {
                            item {
                                CategoryHeader(title = "Becerras", count = state.animalesAgrupados.becerras.size)
                            }

                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((state.animalesAgrupados.becerras.size / 2 + state.animalesAgrupados.becerras.size % 2) * 180.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.animalesAgrupados.becerras) { animal ->
                                        val isSelected = state.animalesSeleccionados.contains(animal.id)
                                        AnimalVaccinationCard(
                                            imageUrl = animal.imagenUrl,
                                            numeroArete = animal.numeroArete,
                                            apodo = animal.apodo,
                                            isSelected = isSelected,
                                            onToggleSelection = {
                                                viewModel.onEvent(BatchVaccinationEvent.ToggleAnimalSelection(animal.id))
                                            }
                                        )
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Sección de Becerros
                        if (state.animalesAgrupados.becerros.isNotEmpty()) {
                            item {
                                CategoryHeader(title = "Becerros", count = state.animalesAgrupados.becerros.size)
                            }

                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((state.animalesAgrupados.becerros.size / 2 + state.animalesAgrupados.becerros.size % 2) * 180.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.animalesAgrupados.becerros) { animal ->
                                        val isSelected = state.animalesSeleccionados.contains(animal.id)
                                        AnimalVaccinationCard(
                                            imageUrl = animal.imagenUrl,
                                            numeroArete = animal.numeroArete,
                                            apodo = animal.apodo,
                                            isSelected = isSelected,
                                            onToggleSelection = {
                                                viewModel.onEvent(BatchVaccinationEvent.ToggleAnimalSelection(animal.id))
                                            }
                                        )
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        // Sección de Otros (por si hay tipos no contemplados)
                        if (state.animalesAgrupados.otros.isNotEmpty()) {
                            item {
                                CategoryHeader(title = "Otros", count = state.animalesAgrupados.otros.size)
                            }

                            item {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((state.animalesAgrupados.otros.size / 2 + state.animalesAgrupados.otros.size % 2) * 180.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(state.animalesAgrupados.otros) { animal ->
                                        val isSelected = state.animalesSeleccionados.contains(animal.id)
                                        AnimalVaccinationCard(
                                            imageUrl = animal.imagenUrl,
                                            numeroArete = animal.numeroArete,
                                            apodo = animal.apodo,
                                            isSelected = isSelected,
                                            onToggleSelection = {
                                                viewModel.onEvent(BatchVaccinationEvent.ToggleAnimalSelection(animal.id))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Botones de acción en la parte inferior
            if (state.animalesSeleccionados.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botón de pausar
                    OutlinedButton(
                        onClick = { showConfirmPause = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Pausar")
                    }

                    // Botón de finalizar
                    Button(
                        onClick = { showConfirmSave = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text("Finalizar")
                    }
                }
            }
        }
    }

    // Dialog para seleccionar medicamento
    if (showMedicamentoPicker) {
        MedicamentoPickerDialog(
            medicamentos = state.medicamentosDisponibles,
            onDismiss = { showMedicamentoPicker = false },
            onMedicamentoSelected = { medicamento ->
                viewModel.onEvent(BatchVaccinationEvent.MedicamentoSelected(medicamento))
                showMedicamentoPicker = false
            },
            onCreateNewRequested = {
                showMedicamentoPicker = false
                onNavigateToRegisterMedicamento()
            }
        )
    }

    // DatePicker para fecha de aplicación
    // Date picker dialog
    if (showDatePicker) {
        // Al inicializar el DatePicker, restamos DOS días
        val calendar = Calendar.getInstance()
        //calendar.add(Calendar.DAY_OF_MONTH, -1)

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = calendar.timeInMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Extraer componentes directamente sin normalización
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = millis

                            // Crear una nueva fecha limpia
                            val result = Calendar.getInstance()
                            result.clear()
                            result.set(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH),
                                12, 0, 0 // mediodía
                            )

                            viewModel.onEvent(BatchVaccinationEvent.DateChanged(result.time))
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

    // Dialog de confirmación para finalizar
    if (showConfirmSave) {
        AlertDialog(
            onDismissRequest = { showConfirmSave = false },
            title = { Text("Finalizar Vacunación") },
            text = {
                Text("¿Está seguro que desea finalizar y registrar la vacunación de ${state.animalesSeleccionados.size} animales?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(BatchVaccinationEvent.FinishVaccination)
                        showConfirmSave = false
                        // Completar el registro de vacunación
                        onFinishVaccination()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen
                    )
                ) {
                    Text("Finalizar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmSave = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de confirmación para pausar
    if (showConfirmPause) {
        AlertDialog(
            onDismissRequest = { showConfirmPause = false },
            title = { Text("Pausar Vacunación") },
            text = {
                Text("¿Desea guardar el progreso actual? Podrá retomar la vacunación más tarde.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(BatchVaccinationEvent.PauseVaccination)
                        showConfirmPause = false
                    }
                ) {
                    Text("Guardar y Pausar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmPause = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AnimalVaccinationCard(
    imageUrl: String?,
    numeroArete: String,
    apodo: String?,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clickable(onClick = onToggleSelection),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Imagen
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    val bitmap = remember(imageUrl) {
                        ImageUtils.loadImageFromInternalStorage(context, imageUrl)
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto de animal",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Checkbox de selección
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) AccentGreen else MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seleccionado",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Número de arete (con últimos 4 dígitos resaltados)
            Text(
                text = numeroArete.dropLast(4) + numeroArete.takeLast(4),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            // Apodo (si existe)
            if (!apodo.isNullOrEmpty()) {
                Text(
                    text = apodo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun MedicamentoPickerDialog(
    medicamentos: List<MedicamentoEntity>,
    onDismiss: () -> Unit,
    onMedicamentoSelected: (MedicamentoEntity) -> Unit,
    onCreateNewRequested: () -> Unit // Cambiamos por una función que navegue a la pantalla de registro
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar Medicamento") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (medicamentos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay medicamentos disponibles",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        medicamentos.forEach { medicamento ->
                            MedicamentoPickerItem(
                                medicamento = medicamento,
                                onClick = { onMedicamentoSelected(medicamento) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onCreateNewRequested) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Medicamento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun MedicamentoPickerItem(
    medicamento: MedicamentoEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono según el tipo
            Icon(
                imageVector = when (medicamento.tipo) {
                    "desparasitante" -> Icons.Default.Healing
                    "vitamina" -> Icons.Default.LocalPharmacy
                    "antibiótico" -> Icons.Default.Biotech
                    else -> Icons.Default.HealthAndSafety
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = medicamento.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = medicamento.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Divider(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}