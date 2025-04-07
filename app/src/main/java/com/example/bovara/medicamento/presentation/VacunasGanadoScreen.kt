// File: app/src/main/java/com/example/bovara/medicamento/presentation/VacunasGanadoScreen.kt
package com.example.bovara.medicamento.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bovara.core.utils.DateUtils
import com.example.bovara.di.AppModule
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.ui.theme.AccentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacunasGanadoScreen(
    ganadoId: Int,
    onNavigateBack: () -> Unit,
    onAddVacuna: (Int) -> Unit
) {
    val context = LocalContext.current
    val medicamentoUseCase = AppModule.provideMedicamentoUseCase(context)
    val ganadoUseCase = AppModule.provideGanadoUseCase(context)

    val viewModel: VacunasGanadoViewModel = viewModel(
        factory = VacunasGanadoViewModel.Factory(ganadoId, medicamentoUseCase, ganadoUseCase)
    )

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.ganado?.let {
                            "Vacunas: ${state.ganado!!.apodo ?: "Animal #${state.ganado!!.numeroArete.takeLast(4)}"}"
                        } ?: "Historial de Vacunas"
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddVacuna(ganadoId) },
                containerColor = AccentGreen,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Registrar Vacuna"
                )
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.vacunas.isEmpty()) {
            EmptyVacunasState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Sección de vacunas programadas pendientes
                val vacunasPendientes = state.vacunas.filter {
                    it.esProgramado && !it.aplicado
                }

                if (vacunasPendientes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Vacunas Programadas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(vacunasPendientes) { vacuna ->
                        VacunaItem(
                            vacuna = vacuna,
                            onMarcarAplicada = { viewModel.marcarVacunaComoAplicada(it) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // Sección de vacunas aplicadas
                val vacunasAplicadas = state.vacunas.filter { it.aplicado }

                if (vacunasAplicadas.isNotEmpty()) {
                    item {
                        Text(
                            text = "Vacunas Aplicadas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(vacunasAplicadas) { vacuna ->
                        VacunaItem(
                            vacuna = vacuna,
                            onMarcarAplicada = null // Ya está aplicada
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VacunaItem(
    vacuna: MedicamentoEntity,
    onMarcarAplicada: ((Int) -> Unit)?
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Título e icono
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (vacuna.tipo) {
                            "desparasitante" -> Icons.Default.Healing
                            "vitamina" -> Icons.Default.LocalPharmacy
                            "antibiótico" -> Icons.Default.Biotech
                            else -> Icons.Default.HealthAndSafety // vacuna por defecto
                        },
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = vacuna.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Badge o menú
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (onMarcarAplicada != null) {
                            DropdownMenuItem(
                                text = { Text("Marcar como aplicada") },
                                onClick = {
                                    onMarcarAplicada(vacuna.id)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null
                                    )
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Ver detalles") },
                            onClick = { showMenu = false },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = vacuna.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Datos adicionales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Fecha
                Column {
                    Text(
                        text = if (vacuna.aplicado) "Aplicada el:" else "Programada para:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = DateUtils.formatDate(
                            if (vacuna.aplicado) vacuna.fechaAplicacion
                            else vacuna.fechaProgramada ?: vacuna.fechaAplicacion
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Dosis
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Dosis:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${vacuna.dosisML} ml",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Badge de estado
            if (!vacuna.aplicado && vacuna.esProgramado) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFC107))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Pendiente",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyVacunasState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.HealthAndSafety,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No hay registros de vacunas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Pulse el botón + para registrar la primera vacuna",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}