package com.example.bovara.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.bovara.di.AppModule
import com.example.bovara.ganado.presentation.AddGanadoScreen
import com.example.bovara.ganado.presentation.EditGanadoScreen
import com.example.bovara.ganado.presentation.GanadoDetailScreen
import com.example.bovara.ganado.presentation.GanadoListMode
import com.example.bovara.ganado.presentation.GanadoListScreen
import com.example.bovara.home.presentation.HomeScreen
import com.example.bovara.home.presentation.HomeViewModel
import com.example.bovara.medicamento.presentation.AddVacunaScreen
import com.example.bovara.medicamento.presentation.BatchDetailScreen
import com.example.bovara.medicamento.presentation.BatchVaccinationScreen
import com.example.bovara.medicamento.presentation.RegisterMedicamentoScreen
import com.example.bovara.medicamento.presentation.VaccinationHistoryScreen
import com.example.bovara.medicamento.presentation.VacunasGanadoScreen
import com.example.bovara.statistics.presentation.StatisticsScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    val context = LocalContext.current


    NavHost(navController = navController, startDestination = Screens.HOME) {

        // Home/Dashboard
        composable(route = Screens.HOME) {
            // Obtener las dependencias necesarias
            val ganadoUseCase = AppModule.provideGanadoUseCase(context)

            // Crear el ViewModel usando la Factory
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(ganadoUseCase)
            )

            HomeScreen(
                onNavigateToGanadoByCategory = {
                    navController.navigate(Screens.GANADO_LIST_BY_CATEGORY)
                },
                onNavigateToGanadoByDate = {
                    navController.navigate(Screens.GANADO_LIST_BY_DATE)
                },
                onNavigateToAddGanado = {
                    navController.navigate(Screens.ADD_GANADO)
                },
                onNavigateToVacunas = {
                    navController.navigate(Screens.BATCH_VACCINATION)
                },
                onNavigateToVacunacionHistorial = {
                    navController.navigate(Screens.VACCINATION_HISTORY)
                },
                onGanadoClick = { ganadoId ->
                    navController.navigate("${Screens.GANADO_DETAIL}/$ganadoId")
                },
                onNavigateToSearchResults = { query ->
                    // Pasar la consulta como argumento de navegación
                    navController.navigate("${Screens.GANADO_LIST_BY_CATEGORY}?query=$query")
                },
                onNavigateToStatistics = {
                    navController.navigate(Screens.STATISTICS)
                },
                viewModel = homeViewModel
            )
        }

        // Agregar Ganado (ahora con soporte para madreId)
        composable(
            route = "${Screens.ADD_GANADO}?madreId={madreId}",
            arguments = listOf(
                navArgument("madreId") {
                    type = NavType.IntType
                    defaultValue = -1 // -1 significa sin madre
                }
            )
        ) { backStackEntry ->
            val madreId = backStackEntry.arguments?.getInt("madreId") ?: -1

            AddGanadoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onGanadoAdded = { ganadoId ->
                    // Navegar al detalle del ganado recién agregado
                    navController.navigate("${Screens.GANADO_DETAIL}/$ganadoId") {
                        // Eliminar la pantalla de agregar ganado del backstack
                        popUpTo(Screens.ADD_GANADO) { inclusive = true }
                    }
                },
                madreId = if (madreId != -1) madreId else null
            )
        }

        // Detalle de Ganado (con información de madre y crías)
        composable(
            route = "${Screens.GANADO_DETAIL}/{ganadoId}",
            arguments = listOf(
                navArgument("ganadoId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val ganadoId = backStackEntry.arguments?.getInt("ganadoId") ?: 0

            GanadoDetailScreen(
                ganadoId = ganadoId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEdit = { id ->
                    navController.navigate("${Screens.EDIT_GANADO}/$id")
                },
                onNavigateHome = {
                    navController.navigate(Screens.HOME) {
                        popUpTo(Screens.HOME) { inclusive = true }
                    }
                },
                onNavigateToVacunas = { id ->
                    navController.navigate("${Screens.VACUNAS_GANADO}/$id")
                },
                onNavigateToAddVacuna = { id ->
                    navController.navigate("${Screens.ADD_VACUNA}/$id")
                },
                onGanadoClick = { id ->
                    // Para navegar a otra ficha de ganado (madre o cría)
                    navController.navigate("${Screens.GANADO_DETAIL}/$id")
                },
                onNavigateToAddCria = { madreId ->
                    // Navegar a la pantalla de agregar ganado con la madre preseleccionada
                    navController.navigate("${Screens.ADD_GANADO}?madreId=$madreId")
                }
            )
        }

        // Editar Ganado
        composable(
            route = "${Screens.EDIT_GANADO}/{ganadoId}",
            arguments = listOf(
                navArgument("ganadoId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val ganadoId = backStackEntry.arguments?.getInt("ganadoId") ?: 0

            EditGanadoScreen(
                ganadoId = ganadoId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onGanadoUpdated = { id ->
                    // Regresar a la pantalla de detalle después de actualizar
                    navController.popBackStack()
                }
            )
        }

        // Historial de vacunas de un animal específico
        composable(
            route = "${Screens.VACUNAS_GANADO}/{ganadoId}",
            arguments = listOf(
                navArgument("ganadoId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val ganadoId = backStackEntry.arguments?.getInt("ganadoId") ?: 0

            VacunasGanadoScreen(
                ganadoId = ganadoId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddVacuna = { id ->
                    navController.navigate("${Screens.ADD_VACUNA}/$id")
                }
            )
        }

        // Agregar vacuna a un animal específico
        composable(
            route = "${Screens.ADD_VACUNA}/{ganadoId}",
            arguments = listOf(
                navArgument("ganadoId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val ganadoId = backStackEntry.arguments?.getInt("ganadoId") ?: 0

            AddVacunaScreen(
                ganadoId = ganadoId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onVacunaAdded = {
                    navController.popBackStack()
                }
            )
        }

        // Historial de vacunaciones
        composable(route = Screens.VACCINATION_HISTORY) {
            VaccinationHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToBatchDetail = { lote ->
                    navController.navigate("${Screens.BATCH_DETAIL}/$lote")
                }
            )
        }

        // Detalle de un lote de vacunación
        composable(
            route = "${Screens.BATCH_DETAIL}/{lote}",
            arguments = listOf(
                navArgument("lote") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lote = backStackEntry.arguments?.getString("lote") ?: ""

            BatchDetailScreen(
                lote = lote,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screens.REGISTER_MEDICAMENTO) {
            RegisterMedicamentoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onMedicamentoRegistered = { medicamentoId ->
                    // Solo guardar el ID en lugar del objeto completo
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "selected_medicamento_id",
                        medicamentoId
                    )
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screens.BATCH_VACCINATION) {
            // Obtener el ID del medicamento desde savedStateHandle si existe
            val medicamentoId = navController.currentBackStackEntry?.savedStateHandle?.get<Int>("selected_medicamento_id")

            BatchVaccinationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onFinishVaccination = {
                    navController.navigate(Screens.VACCINATION_HISTORY) {
                        popUpTo(Screens.BATCH_VACCINATION) { inclusive = true }
                    }
                },
                onNavigateToRegisterMedicamento = {
                    navController.navigate(Screens.REGISTER_MEDICAMENTO)
                },
                selectedMedicamentoId = medicamentoId
            )

            // Limpiar para evitar duplicados
            if (medicamentoId != null) {
                navController.currentBackStackEntry?.savedStateHandle?.remove<Int>("selected_medicamento_id")
            }
        }

        // Ruta para lista de ganado por categoría con parámetro de búsqueda opcional
        composable(
            route = "${Screens.GANADO_LIST_BY_CATEGORY}?query={query}",
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val searchQuery = backStackEntry.arguments?.getString("query") ?: ""

            GanadoListScreen(
                mode = GanadoListMode.BY_CATEGORY,
                initialSearchQuery = searchQuery,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onGanadoClick = { ganadoId ->
                    navController.navigate("${Screens.GANADO_DETAIL}/$ganadoId")
                }
            )
        }

        // Ruta para lista de ganado por fecha con parámetro de búsqueda opcional
        composable(
            route = "${Screens.GANADO_LIST_BY_DATE}?query={query}",
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val searchQuery = backStackEntry.arguments?.getString("query") ?: ""

            GanadoListScreen(
                mode = GanadoListMode.BY_DATE,
                initialSearchQuery = searchQuery,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onGanadoClick = { ganadoId ->
                    navController.navigate("${Screens.GANADO_DETAIL}/$ganadoId")
                }
            )
        }

        // Ruta placeholder para la lista de ganado
        composable(route = Screens.GANADO_LIST) {
            // Placeholder - regresa a la pantalla de inicio
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
        }

        composable(route = Screens.STATISTICS) {
            val ganadoUseCase = AppModule.provideGanadoUseCase(context)

            StatisticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                ganadoUseCase = ganadoUseCase
            )
        }
    }
}