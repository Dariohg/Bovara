package com.example.bovara.statistics.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.statistics.StatisticsModule
import com.example.bovara.statistics.data.model.Respaldo
import com.example.bovara.statistics.presentation.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    ganadoUseCase: GanadoUseCase,
    connectivityChecker: ConnectivityChecker = ConnectivityChecker(),
    viewModel: StatisticsViewModel = viewModel(
        factory = StatisticsModule.provideStatisticsViewModelFactory(ganadoUseCase)
    )
) {
    var isConnected by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isCreatingBackup by viewModel.isCreatingBackup.collectAsState()

    // Verificar la conexión al iniciar
    LaunchedEffect(Unit) {
        isConnected = connectivityChecker.isNetworkAvailable(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Estadísticas",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (!isConnected) {
                NoInternetMessage()
            } else {
                StatisticsContent(
                    uiState = uiState,
                    isCreatingBackup = isCreatingBackup,
                    onRefresh = { viewModel.fetchBackups() },
                    onCreateBackup = { viewModel.createBackup() }
                )
            }
        }
    }
}

@Composable
fun StatisticsContent(
    uiState: StatisticsUiState,
    isCreatingBackup: Boolean,
    onRefresh: () -> Unit,
    onCreateBackup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón para realizar respaldo
        Button(
            onClick = onCreateBackup,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isCreatingBackup,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            if (isCreatingBackup) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isCreatingBackup) "Realizando respaldo..." else "Realizar respaldo",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }

        Text(
            text = "Este botón guarda una copia de todos los datos actuales del ganado",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Contenido principal basado en el estado
        when (uiState) {
            is StatisticsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is StatisticsUiState.Empty -> {
                EmptyStateMessage(onRefresh)
            }
            is StatisticsUiState.Error -> {
                ErrorMessage(message = uiState.message, onRetry = onRefresh)
            }
            is StatisticsUiState.Success -> {
                BackupStatisticsContent(respaldos = uiState.respaldos)
            }
        }
    }
}

@Composable
fun BackupStatisticsContent(respaldos: List<Respaldo>) {
    // Ordenamos los respaldos por fecha, más reciente primero
    val sortedRespaldos = respaldos.sortedByDescending { it.fechaRespaldo }

    // Tomamos los dos respaldos más recientes
    val latestBackup = sortedRespaldos.getOrNull(0)
    val previousBackup = sortedRespaldos.getOrNull(1)

    if (latestBackup != null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resumen actual
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Resumen de ganado",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Fecha: ${formatDate(latestBackup.fechaRespaldo)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Resumen total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TotalStatItem(
                                title = "Total",
                                value = latestBackup.respaldo.totalMachos + latestBackup.respaldo.totalHembras,
                                previousValue = previousBackup?.let {
                                    it.respaldo.totalMachos + it.respaldo.totalHembras
                                }
                            )

                            TotalStatItem(
                                title = "Machos",
                                value = latestBackup.respaldo.totalMachos,
                                previousValue = previousBackup?.respaldo?.totalMachos,
                                color = Color(0xFF4CAF50) // Verde
                            )

                            TotalStatItem(
                                title = "Hembras",
                                value = latestBackup.respaldo.totalHembras,
                                previousValue = previousBackup?.respaldo?.totalHembras,
                                color = Color(0xFFFFC107) // Amarillo
                            )
                        }
                    }
                }
            }

            // Gráfico de distribución por sexo
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Distribución por sexo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Gráfico tipo pie chart
                        PieChartView(
                            dataPoints = listOf(
                                PieChartData("Machos", latestBackup.respaldo.totalMachos.toFloat(), Color(0xFF4CAF50)),
                                PieChartData("Hembras", latestBackup.respaldo.totalHembras.toFloat(), Color(0xFFFFC107))
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }

            // Detalles de machos
            item {
                DetailCard(
                    title = "Detalle de Machos",
                    color = Color(0xFF4CAF50), // Verde
                    infoText = "Desglose de la cantidad de machos por categoría",
                    items = listOf(
                        DetailItem("Becerros", latestBackup.respaldo.detalleMachos.becerro, previousBackup?.respaldo?.detalleMachos?.becerro),
                        DetailItem("Toritos", latestBackup.respaldo.detalleMachos.torito, previousBackup?.respaldo?.detalleMachos?.torito),
                        DetailItem("Toros", latestBackup.respaldo.detalleMachos.toro, previousBackup?.respaldo?.detalleMachos?.toro)
                    )
                )
            }

            // Detalles de hembras
            item {
                DetailCard(
                    title = "Detalle de Hembras",
                    color = Color(0xFFFFC107), // Amarillo
                    infoText = "Desglose de la cantidad de hembras por categoría",
                    items = listOf(
                        DetailItem("Becerras", latestBackup.respaldo.detalleHembras.becerra, previousBackup?.respaldo?.detalleHembras?.becerra),
                        DetailItem("Vacas", latestBackup.respaldo.detalleHembras.vaca, previousBackup?.respaldo?.detalleHembras?.vaca)
                    )
                )
            }

            // Estado de los animales
            item {
                DetailCard(
                    title = "Estado de los Animales",
                    color = MaterialTheme.colorScheme.secondary,
                    infoText = "Muestra cómo están distribuidos los animales según su estado actual: activos (vivos en el rancho), vendidos (transferidos a otro dueño) o muertos",
                    items = listOf(
                        DetailItem("Activos", latestBackup.respaldo.estadoAnimales.activos, previousBackup?.respaldo?.estadoAnimales?.activos),
                        DetailItem("Vendidos", latestBackup.respaldo.estadoAnimales.vendidos, previousBackup?.respaldo?.estadoAnimales?.vendidos),
                        DetailItem("Muertos", latestBackup.respaldo.estadoAnimales.muertos, previousBackup?.respaldo?.estadoAnimales?.muertos)
                    )
                )
            }

            // Espacio al final
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    } else {
        EmptyStateMessage(onRefresh = {})
    }
}

// Función de utilidad para formatear fechas
fun formatDate(timestamp: Long): String {
    val date = Date(timestamp * 1000)
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return format.format(date)
}