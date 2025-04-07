package com.example.bovara.ganado.data.model

data class GanadoEstadistica(
    val totalMachos: Int,
    val totalHembras: Int,
    val detalleMachos: Map<String, Int>,
    val detalleHembras: Map<String, Int>,
    val estadoAnimales: Map<String, Int>
)
