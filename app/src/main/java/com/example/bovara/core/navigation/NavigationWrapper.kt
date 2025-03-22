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
import com.example.bovara.home.presentation.HomeScreen
import com.example.bovara.home.presentation.HomeViewModel
import com.example.bovara.medicamento.presentation.AddVacunaScreen
import com.example.bovara.medicamento.presentation.BatchDetailScreen
import com.example.bovara.medicamento.presentation.BatchVaccinationScreen
import com.example.bovara.medicamento.presentation.VaccinationHistoryScreen
import com.example.bovara.medicamento.presentation.VacunasGanadoScreen

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
                onNavigateToGanado = {
                    navController.navigate(Screens.GANADO_LIST)
                },
                onNavigateToAddGanado = {
                    navController.navigate(Screens.ADD_GANADO)
                },
                onNavigateToVacunas = {
                    navController.navigate(Screens.BATCH_VACCINATION)
                },
                onGanadoClick = { ganadoId ->
                    navController.navigate("${Screens.GANADO_DETAIL}/$ganadoId")
                },
                viewModel = homeViewModel
            )
        }

        // Agregar Ganado
        composable(route = Screens.ADD_GANADO) {
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
                }
            )
        }

        // Detalle de Ganado
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

        // Aquí podrías agregar más rutas para otras pantallas de la aplicación

        // Placeholder para la lista de ganado (aún no implementada)
        composable(route = Screens.GANADO_LIST) {
            // Placeholder - regresa a la pantalla de inicio
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
        }

        // Placeholder para la pantalla de vacunación (aún no implementada)
        composable(route = Screens.BATCH_VACCINATION) {
            // Placeholder - regresa a la pantalla de inicio
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
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

        // Vacunación por lotes
        composable(route = Screens.BATCH_VACCINATION) {
            BatchVaccinationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onFinishVaccination = {
                    // Navegar al historial después de completar
                    navController.navigate(Screens.VACCINATION_HISTORY) {
                        popUpTo(Screens.BATCH_VACCINATION) { inclusive = true }
                    }
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
    }
}