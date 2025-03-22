package com.example.bovara.core.navigation

object Screens {
    const val HOME = "home"

    // Rutas para gestión de ganado
    const val GANADO_LIST = "ganado_list"
    const val ADD_GANADO = "add_ganado"
    const val EDIT_GANADO = "edit_ganado"
    const val GANADO_DETAIL = "ganado_detail"

    // Rutas para crianza
    const val REGISTER_CRIA = "register_cria"

    const val VACUNAS_GANADO = "vacunas_ganado" // Historial de vacunas de un animal
    const val ADD_VACUNA = "add_vacuna" // Agregar vacuna a un animal
    const val VACUNAS_PROGRAMADAS = "vacunas_programadas" // Ver todas las vacunas programadas
    const val VACUNACION_LOTE = "vacunacion_lote" // Vacunación en lote

    const val BATCH_VACCINATION = "batch_vaccination"
    const val VACCINATION_HISTORY = "vaccination_history"
    const val BATCH_DETAIL = "batch_detail"
}