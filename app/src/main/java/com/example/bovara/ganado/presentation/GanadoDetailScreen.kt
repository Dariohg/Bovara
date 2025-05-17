package com.example.bovara.ganado.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bovara.core.utils.DateUtils
import com.example.bovara.core.utils.ImageUtils
import com.example.bovara.di.AppModule
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.presentation.components.AnimalNoteComponent
import com.example.bovara.ganado.presentation.components.CriaItem
import com.example.bovara.ui.theme.AccentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GanadoDetailScreen(
    ganadoId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToVacunas: (Int) -> Unit,
    onNavigateToAddVacuna: (Int) -> Unit,
    onGanadoClick: (Int) -> Unit, // Función para navegar a otro ganado (madre o cría)
    onNavigateToAddCria: (Int) -> Unit // Función para navegar a la pantalla de agregar cría
) {
    val context = LocalContext.current
    val ganadoUseCase = AppModule.provideGanadoUseCase(context)
    val medicamentoUseCase = AppModule.provideMedicamentoUseCase(context)

    val viewModel: GanadoDetailViewModel = viewModel(
        factory = GanadoDetailViewModel.Factory(ganadoId, ganadoUseCase, medicamentoUseCase)
    )

    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Efecto para navegar de vuelta si el ganado no existe o ha sido eliminado
    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onNavigateHome()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.ganado != null) {
                            state.ganado!!.apodo ?: "Animal #${state.ganado!!.numeroArete.takeLast(4)}"
                        } else {
                            "Detalles de Animal"
                        }
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
                actions = {
                    // Botón de editar
                    IconButton(onClick = { onNavigateToEdit(ganadoId) }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Editar"
                        )
                    }

                    // Botón de eliminar
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
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
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else if (state.ganado == null) {
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
                        imageVector = Icons.Filled.Warning,
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

                    Text(
                        text = "El animal que busca no existe o ha sido eliminado.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

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
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Imagen o Avatar del animal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.ganado!!.imagenUrl != null) {
                        // Cargar imagen desde el almacenamiento interno si existe
                        val bitmap = remember(state.ganado!!.imagenUrl) {
                            ImageUtils.loadImageFromInternalStorage(context, state.ganado!!.imagenUrl!!)
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Foto de ${state.ganado!!.apodo ?: "animal"}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Mostrar ícono si no se pudo cargar la imagen
                            Icon(
                                imageVector = if (state.ganado!!.sexo == "macho") Icons.Rounded.Male else Icons.Rounded.Female,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    } else {
                        // Mostrar ícono si no hay imagen
                        Icon(
                            imageVector = if (state.ganado!!.sexo == "macho") Icons.Rounded.Male else Icons.Rounded.Female,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                // Badge de estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    StatusBadge(estado = state.ganado!!.estado)
                }

                // Información principal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Número de arete
                        DetailItem(
                            icon = Icons.Default.Tag,
                            label = "Número de Arete",
                            value = state.ganado!!.numeroArete
                        )

                        // Apodo (si existe)
                        if (!state.ganado!!.apodo.isNullOrEmpty()) {
                            DetailItem(
                                icon = Icons.Default.Pets,
                                label = "Apodo",
                                value = state.ganado!!.apodo!!
                            )
                        }

                        // Tipo y Sexo
                        DetailItem(
                            icon = if (state.ganado!!.sexo == "macho") Icons.Rounded.Male else Icons.Rounded.Female,
                            label = "Tipo",
                            value = "${state.ganado!!.tipo.replaceFirstChar { it.uppercase() }} (${state.ganado!!.sexo.replaceFirstChar { it.uppercase() }})"
                        )

                        // Color
                        DetailItem(
                            icon = Icons.Default.FormatColorFill,
                            label = "Color",
                            value = state.ganado!!.color
                        )

                        // Fecha de nacimiento (si existe)
                        if (state.ganado!!.fechaNacimiento != null) {
                            DetailItem(
                                icon = Icons.Outlined.CalendarMonth,
                                label = "Fecha de Nacimiento",
                                value = DateUtils.formatDate(state.ganado!!.fechaNacimiento)
                            )

                            // Edad calculada
                            DetailItem(
                                icon = Icons.Default.Cake,
                                label = "Edad",
                                value = DateUtils.calculateAge(state.ganado!!.fechaNacimiento)
                            )
                        }

                        // Cantidad de crías (solo para hembras)
                        if (state.ganado!!.sexo == "hembra" && state.ganado!!.cantidadCrias > 0) {
                            DetailItem(
                                icon = Icons.Default.ChildCare,
                                label = "Cantidad de Crías",
                                value = state.ganado!!.cantidadCrias.toString()
                            )
                        }

                        // Fecha de registro
                        DetailItem(
                            icon = Icons.Default.DateRange,
                            label = "Fecha de Registro",
                            value = DateUtils.formatDate(state.ganado!!.fechaRegistro)
                        )
                    }
                }

                // Sección para mostrar información de la madre (si tiene)
                if (state.madre != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Female,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Información de la Madre",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                // Botón para ver detalle de la madre
                                IconButton(
                                    onClick = {
                                        state.madre?.id?.let { madreId ->
                                            onGanadoClick(madreId)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Ver madre"
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            // Datos principales de la madre
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Círculo con la inicial o foto pequeña de la madre
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (state.madre?.imagenUrl != null) {
                                        val bitmap = remember(state.madre?.imagenUrl) {
                                            ImageUtils.loadImageFromInternalStorage(context, state.madre?.imagenUrl!!)
                                        }

                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Madre",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text(
                                                text = (state.madre?.apodo?.firstOrNull() ?: "M").toString(),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = (state.madre?.apodo?.firstOrNull() ?: "M").toString(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Información de la madre
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = state.madre?.apodo ?: "Sin nombre",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = "Arete: ${state.madre?.numeroArete}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "Cantidad de crías: ${state.madre?.cantidadCrias}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Sección para mostrar las crías (si es una vaca o becerra)
                if (state.ganado?.tipo == "vaca" || state.ganado?.tipo == "becerra") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ChildCare,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Añadimos el contador de crías en el título
                                    Text(
                                        text = "Crías (${state.crias.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Botón para agregar cría (solo si el animal está activo)
                                if (state.ganado?.estado == "activo") {
                                    Button(
                                        onClick = {
                                            // Navegar a pantalla de agregar cría con la madre preseleccionada
                                            onNavigateToAddCria(state.ganado!!.id)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        Text("Agregar Cría")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.crias.isEmpty()) {
                                // Mensaje si no hay crías
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No hay crías registradas",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                // Lista de crías ordenadas por fecha de nacimiento (las más recientes primero)
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.crias.forEach { cria ->
                                        CriaItem(
                                            cria = cria,
                                            onClick = { onGanadoClick(cria.id) }
                                        )

                                        Divider()
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Historial de vacunas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Historial de Vacunas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Botón para ir al historial completo
                            TextButton(
                                onClick = { onNavigateToVacunas(ganadoId) }
                            ) {
                                Text("Ver todo")
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Mostrar mensaje o lista de vacunas recientes
                        if (state.vacunasRecientes.isEmpty()) {
                            Text(
                                text = "No hay registro de vacunas",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            // Mostrar las últimas 2-3 vacunas
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.vacunasRecientes.take(3).forEach { vacuna ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when (vacuna.tipo) {
                                                "desparasitante" -> Icons.Default.Healing
                                                "vitamina" -> Icons.Default.LocalPharmacy
                                                "antibiótico" -> Icons.Default.Biotech
                                                else -> Icons.Default.HealthAndSafety
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = vacuna.nombre,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )

                                            Text(
                                                text = DateUtils.formatDate(vacuna.fechaAplicacion),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Dosis aplicada
                                        Text(
                                            text = "${vacuna.dosisML} ml",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    if (state.vacunasRecientes.indexOf(vacuna) < state.vacunasRecientes.size - 1 &&
                                        state.vacunasRecientes.indexOf(vacuna) < 2) {
                                        Divider(modifier = Modifier.padding(start = 28.dp))
                                    }
                                }
                            }
                        }

                        // Botón para agregar vacuna
                        if (state.ganado != null && state.ganado!!.estado == "activo") {
                            Button(
                                onClick = { onNavigateToAddVacuna(ganadoId) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text("Registrar Vacuna")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                state.ganado?.let { ganado ->
                    AnimalNoteComponent(
                        note = ganado.nota,
                        onNoteChange = { newNote -> viewModel.updateNote(newNote) },
                        isEditable = ganado.estado == "activo"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Animal") },
            text = { Text("¿Está seguro que desea eliminar este animal? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGanado()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatusBadge(estado: String) {
    val (color, text) = when (estado) {
        "activo" -> Pair(AccentGreen, "Activo")
        "vendido" -> Pair(Color(0xFFFFC107), "Vendido")
        "muerto" -> Pair(Color(0xFFE53935), "Muerto")
        else -> Pair(MaterialTheme.colorScheme.primary, estado.replaceFirstChar { it.uppercase() })
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}