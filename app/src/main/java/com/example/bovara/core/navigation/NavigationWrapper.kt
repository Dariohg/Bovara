package com.example.bovara.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bovara.home.presentation.HomeScreen

@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screens.HOME) {
        // Home/Dashboard
        composable(route = Screens.HOME) {
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
                }
            )
        }

        // Añadir más pantallas a medida que se implementen
    }
}