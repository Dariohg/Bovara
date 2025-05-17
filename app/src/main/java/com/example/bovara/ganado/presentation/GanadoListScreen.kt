package com.example.bovara.ganado.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bovara.core.utils.ImageUtils
import com.example.bovara.di.AppModule
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ui.theme.AccentGreen

enum class GanadoListMode {
    BY_CATEGORY, // Para mostrar agrupados por tipo/categoría
    BY_DATE      // Para mostrar por fecha de registro
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GanadoListScreen(
    mode: GanadoListMode,
    initialSearchQuery: String = "",
    onNavigateBack: () -> Unit,
    onGanadoClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val ganadoUseCase = AppModule.provideGanadoUseCase(context)

    // Crear y obtener el viewModel
    val viewModel: GanadoListViewModel = viewModel(
        factory = GanadoListViewModel.Factory(ganadoUseCase, initialSearchQuery)
    )

    val state by viewModel.state.collectAsState()

    LaunchedEffect(mode, initialSearchQuery) {
        viewModel.setListMode(mode)
        if (initialSearchQuery.isNotBlank()) {
            viewModel.updateSearchQuery(initialSearchQuery)
        }
    }

    // Determinar el título según el parámetro de búsqueda
    val screenTitle = when (initialSearchQuery) {
        "toro_torito" -> "Toros y Toritos"
        "vaca" -> "Vacas"
        "becerra" -> "Becerras"
        "becerro" -> "Becerros"
        "" -> if (mode == GanadoListMode.BY_CATEGORY) "Ganado por Categoría" else "Ganado Reciente"
        else -> "Resultados de búsqueda"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(screenTitle)
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
        ) {
            // Buscador
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por número de arete") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Contenido diferente según el modo
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AccentGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else if (state.filteredGanado.isEmpty()) {
                // Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(72.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (state.searchQuery.isEmpty())
                                "No hay ganado registrado"
                            else
                                "No se encontraron resultados para \"${state.searchQuery}\"",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                when (mode) {
                    GanadoListMode.BY_CATEGORY -> {
                        // Para filtros específicos, mostramos solo esa categoría
                        when (initialSearchQuery) {
                            "toro_torito" -> GanadoByTypeList(
                                title = "Toros y Toritos",
                                ganado = state.filteredGanado.filter { it.tipo == "toro" || it.tipo == "torito" },
                                onGanadoClick = onGanadoClick
                            )
                            "vaca" -> GanadoByTypeList(
                                title = "Vacas",
                                ganado = state.filteredGanado.filter { it.tipo == "vaca" },
                                onGanadoClick = onGanadoClick
                            )
                            "becerra" -> GanadoByTypeList(
                                title = "Becerras",
                                ganado = state.filteredGanado.filter { it.tipo == "becerra" },
                                onGanadoClick = onGanadoClick
                            )
                            "becerro" -> GanadoByTypeList(
                                title = "Becerros",
                                ganado = state.filteredGanado.filter { it.tipo == "becerro" },
                                onGanadoClick = onGanadoClick
                            )
                            else -> GanadoByCategoryList(
                                ganado = state.filteredGanado,
                                onGanadoClick = onGanadoClick
                            )
                        }
                    }
                    GanadoListMode.BY_DATE -> GanadoByDateList(
                        ganado = state.filteredGanado,
                        onGanadoClick = onGanadoClick
                    )
                }
            }
        }
    }
}

@Composable
fun GanadoByCategoryList(
    ganado: List<GanadoEntity>,
    onGanadoClick: (Int) -> Unit
) {
    // Agrupar ganado por categoría
    val toros = ganado.filter { it.tipo == "toro" }
    val vacas = ganado.filter { it.tipo == "vaca" }
    val toritos = ganado.filter { it.tipo == "torito" }
    val becerros = ganado.filter { it.tipo == "becerro" }
    val becerras = ganado.filter { it.tipo == "becerra" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sección de toros
        if (toros.isNotEmpty()) {
            item {
                CategoryHeader(title = "Toros", count = toros.size)
            }

            items(toros.sortedByDescending { it.numeroArete }) { animal ->
                GanadoItemCard(ganado = animal, onClick = { onGanadoClick(animal.id) })
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Sección de toritos
        if (toritos.isNotEmpty()) {
            item {
                CategoryHeader(title = "Toritos", count = toritos.size)
            }

            items(toritos.sortedByDescending { it.numeroArete }) { animal ->
                GanadoItemCard(ganado = animal, onClick = { onGanadoClick(animal.id) })
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Sección de vacas
        if (vacas.isNotEmpty()) {
            item {
                CategoryHeader(title = "Vacas", count = vacas.size)
            }

            items(vacas.sortedByDescending { it.numeroArete }) { animal ->
                GanadoItemCard(ganado = animal, onClick = { onGanadoClick(animal.id) })
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Sección de becerras
        if (becerras.isNotEmpty()) {
            item {
                CategoryHeader(title = "Becerras", count = becerras.size)
            }

            items(becerras.sortedByDescending { it.numeroArete }) { animal ->
                GanadoItemCard(ganado = animal, onClick = { onGanadoClick(animal.id) })
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Sección de becerros
        if (becerros.isNotEmpty()) {
            item {
                CategoryHeader(title = "Becerros", count = becerros.size)
            }

            items(becerros.sortedByDescending { it.numeroArete }) { animal ->
                GanadoItemCard(ganado = animal, onClick = { onGanadoClick(animal.id) })
            }
        }

        // Espacio final para el FAB
        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
fun GanadoByTypeList(
    title: String,
    ganado: List<GanadoEntity>,
    onGanadoClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CategoryHeader(title = title, count = ganado.size)
        }

        items(ganado.sortedByDescending { it.numeroArete }) { animal ->
            GanadoItemCard(ganado = animal, onClick = { onGanadoClick(animal.id) })
        }

        // Espacio final para el FAB
        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
fun GanadoByDateList(
    ganado: List<GanadoEntity>,
    onGanadoClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ganado.sortedByDescending { it.fechaRegistro }) { animal ->
            GanadoItemCard(ganado = animal, onClick = { onGanadoClick(animal.id) })
        }

        // Espacio final para el FAB
        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
fun CategoryHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
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
        thickness = 1.dp
    )
}

@Composable
fun GanadoItemCard(
    ganado: GanadoEntity,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto o ícono de género
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (ganado.imagenUrl != null) {
                    val bitmap = remember(ganado.imagenUrl) {
                        ImageUtils.loadImageFromInternalStorage(context, ganado.imagenUrl)
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto de ${ganado.apodo ?: "animal"}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = if (ganado.sexo == "macho") Icons.Rounded.Male else Icons.Rounded.Female,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = if (ganado.sexo == "macho") Icons.Rounded.Male else Icons.Rounded.Female,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#${ganado.numeroArete.takeLast(4)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (ganado.apodo != null) {
                        Text(
                            text = " - ${ganado.apodo}",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // ← AGREGAR ESTE BLOQUE AQUÍ
                    // Agregar icono si tiene notas
                    if (ganado.nota.isNotBlank()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = "Tiene notas",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // ← FIN DEL BLOQUE AGREGADO
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = ganado.tipo.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Arete completo: ${ganado.numeroArete}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            when (ganado.estado) {
                "activo" -> {
                    Badge(
                        containerColor = AccentGreen,
                        contentColor = Color.White,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = "Activo",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                "vendido" -> {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = Color.White,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = "Vendido",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                "muerto" -> {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = "Muerto",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}