package com.example.bovara.statistics.data.model

data class RespaldoResponse(
    val respaldos: List<Respaldo>
)

data class Respaldo(
    val idRespaldo: String,
    val fechaRespaldo: Long,
    val respaldo: DatosRespaldo
)

data class DatosRespaldo(
    val totalMachos: Int,
    val totalHembras: Int,
    val detalleMachos: DetalleMachos,
    val detalleHembras: DetalleHembras,
    val estadoAnimales: EstadoAnimales
)

data class DetalleMachos(
    val becerro: Int,
    val torito: Int,
    val toro: Int
)

data class DetalleHembras(
    val becerra: Int,
    val vaca: Int
)

data class EstadoAnimales(
    val activos: Int,
    val muertos: Int,
    val vendidos: Int
)