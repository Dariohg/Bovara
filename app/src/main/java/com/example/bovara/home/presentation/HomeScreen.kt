package com.example.bovara.home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.bovara.R
import com.example.bovara.core.utils.DateUtils
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ui.theme.AccentGreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import com.example.bovara.core.utils.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToGanadoByCategory: () -> Unit,
    onNavigateToGanadoByDate: () -> Unit,
    onNavigateToAddGanado: () -> Unit,
    onNavigateToVacunas: () -> Unit,
    onNavigateToVacunacionHistorial: () -> Unit,
    onGanadoClick: (Int) -> Unit,
    onNavigateToSearchResults: (String) -> Unit,
    viewModel: HomeViewModel,
    onNavigateToStatistics: () -> Unit,
) {
    val ganado by viewModel.ganado.collectAsState()
    val filteredGanado by viewModel.filteredGanado.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Bovara",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddGanado,
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar Animal"
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 80.dp
                )
            ) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar por número de arete") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar"
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
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
                }
                // Mostrar resultados de búsqueda si hay una consulta activa
                if (isSearchActive) {
                    item {
                        SearchResultsHeader(
                            query = searchQuery,
                            resultCount = filteredGanado.size,
                            onViewAllResults = {
                                onNavigateToSearchResults(searchQuery)
                            },
                            onClearSearch = {
                                viewModel.clearSearch()
                            }
                        )
                    }

                    // Mostrar los resultados filtrados
                    items(filteredGanado.take(5)) { animal ->
                        GanadoListItem(
                            ganado = animal,
                            onClick = { onGanadoClick(animal.id) }
                        )
                    }

                    // Si hay más resultados, mostrar un botón para ver todos
                    if (filteredGanado.size > 5) {
                        item {
                            Button(
                                onClick = { onNavigateToSearchResults(searchQuery) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text("Ver todos los resultados (${filteredGanado.size})")
                            }
                        }
                    }
                }

                item {
                    StatsCard(ganado)
                }

                // Sección de Categorías
                item {
                    SectionHeader(
                        title = "Categorías",
                        icon = Icons.Rounded.Category,
                        actionText = "Ver todos",
                        onActionClick = onNavigateToGanadoByCategory

                    )
                }

                // Categorías de ganado
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item {
                            CategoryCard(
                                title = "Toritos",
                                count = ganado.count { it.tipo == "toro" || it.tipo == "torito" },
                                systemIcon = Icons.Default.Male,
                                onClick = {
                                    viewModel.filterByType("toro_torito")
                                    onNavigateToSearchResults("toro_torito")
                                }
                            )
                        }
                        item {
                            CategoryCard(
                                title = "Vacas",
                                count = ganado.count { it.tipo == "vaca" },
                                systemIcon = Icons.Default.Female, // Icono para hembras adultas
                                onClick = {
                                    viewModel.filterByType("vaca")
                                    onNavigateToSearchResults("vaca")
                                }
                            )
                        }
                        item {
                            CategoryCard(
                                title = "Becerras",
                                count = ganado.count { it.tipo == "becerra" },
                                systemIcon = Icons.Default.Pets, // Icono para crías femeninas
                                onClick = {
                                    viewModel.filterByType("becerra")
                                    onNavigateToSearchResults("becerra")
                                }
                            )
                        }
                        item {
                            CategoryCard(
                                title = "Becerros",
                                count = ganado.count { it.tipo == "becerro" },
                                systemIcon = Icons.Default.Pets, // Icono para crías masculinas
                                onClick = {
                                    viewModel.filterByType("becerro")
                                    onNavigateToSearchResults("becerro")
                                }
                            )
                        }
                    }
                }

                // Sección de Vacunas y Registros - CON LAS DOS TARJETAS
                item {
                    SectionHeader(
                        title = "Vacunas y Registros",
                        icon = Icons.Outlined.HealthAndSafety,
                        actionText = null,
                        onActionClick = null
                    )
                }

                // Dos tarjetas lado a lado
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Primera tarjeta: Vacunación por lotes
                        ActionCard(
                            title = "Vacunación por Lotes",
                            description = "Aplicar vacunas a múltiples animales",
                            icon = Icons.Outlined.HealthAndSafety,
                            onClick = onNavigateToVacunas,
                            modifier = Modifier.weight(1f)
                        )

                        // Segunda tarjeta: Historial de vacunaciones
                        ActionCard(
                            title = "Historial",
                            description = "Ver registro de vacunaciones",
                            icon = Icons.Default.History,
                            onClick = onNavigateToVacunacionHistorial,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Sección de Ganado Reciente
                item {
                    SectionHeader(
                        title = "Ganado Reciente",
                        icon = Icons.Rounded.History,
                        actionText = "Ver todos",
                        onActionClick = onNavigateToGanadoByDate

                    )
                }

                // Lista de ganado reciente
                if (ganado.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "No hay ganado registrado",
                            icon = Icons.Rounded.Add,
                            actionText = "Agregar animal",
                            onActionClick = onNavigateToAddGanado
                        )
                    }
                } else {
                    val recentGanado = ganado.sortedByDescending { it.fechaRegistro }.take(5)
                    items(recentGanado) { animal ->
                        GanadoListItem(
                            ganado = animal,
                            onClick = { onGanadoClick(animal.id) }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun StatsCard(ganado: List<GanadoEntity>) {
    // Eliminamos la Card y usamos directamente un Box con sombra y fondo degradado
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50),  // Verde más vivo
                        Color(0xFFFFC107)   // Ámbar más vivo
                    )
                )
            )
    ) {
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Resumen de Ganado",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    title = "Total",
                    value = ganado.size.toString(),
                    iconContentDescription = "Total de ganado"
                )
                StatItem(
                    title = "Activos",
                    value = ganado.count { it.estado == "activo" }.toString(),
                    iconContentDescription = "Ganado activo"
                )
                StatItem(
                    title = "Ventas",
                    value = ganado.count { it.estado == "vendido" }.toString(),
                    iconContentDescription = "Ganado vendido"
                )
            }
        }
    }
}
@Composable
fun StatItem(
    title: String,
    value: String,
    iconContentDescription: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.weight(1f)
        )

        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick
            ) {
                Text(
                    text = actionText,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    title: String,
    count: Int,
    systemIcon: ImageVector,
    onClick: () -> Unit
) {
    // Aumentamos un poco la altura para acomodar títulos más largos
    Card(
        modifier = Modifier
            .width(117.dp)
            .height(130.dp) // Aumentamos la altura de 120dp a 130dp
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly // Usamos SpaceEvenly en lugar de Center
        ) {
            Icon(
                imageVector = systemIcon,
                contentDescription = title,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(36.dp) // Reducimos ligeramente el tamaño del ícono
            )

            // Texto con tamaño potencialmente ajustable para textos largos
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                // Si el título es largo, ajustamos el tamaño de la fuente
                fontSize = if (title.length > 10) 14.sp else 16.sp,
                // Aseguramos que quepa en máximo 2 líneas
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Hacemos el contador más visible
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4CAF50)
                ),
                // Aseguramos que el contador se vea claramente
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun VaccinationCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Usamos un icono del sistema en lugar del drawable personalizado
            Icon(
                imageVector = Icons.Outlined.HealthAndSafety,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vacunas programadas",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Administrar vacunaciones para todo el ganado",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun GanadoListItem(
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
            // Foto del animal o avatar por defecto
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (ganado.imagenUrl != null) {
                    // Cargar imagen desde el almacenamiento interno si existe
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
                        // Mostrar ícono si no se pudo cargar la imagen
                        Icon(
                            imageVector = if (ganado.sexo == "macho") Icons.Rounded.Male else Icons.Rounded.Female,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(30.dp)
                        )
                    }
                } else {
                    // Mostrar ícono si no hay imagen
                    Icon(
                        imageVector = if (ganado.sexo == "macho") Icons.Rounded.Male else Icons.Rounded.Female,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(30.dp)
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
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
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

@Composable
fun EmptyStateCard(
    message: String,
    icon: ImageVector,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        label = "alpha"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(alpha),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                if (actionText != null && onActionClick != null) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onActionClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            text = actionText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
@Composable
fun SearchResultsHeader(
    query: String,
    resultCount: Int,
    onViewAllResults: () -> Unit,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Resultados para \"$query\"",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onClearSearch) {
                Text("Cancelar")
            }
        }

        Text(
            text = "$resultCount animales encontrados",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Divider()
    }
}