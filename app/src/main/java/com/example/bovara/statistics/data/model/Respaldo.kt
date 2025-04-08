package com.example.bovara.statistics.data.model

import com.example.bovara.core.network.DateToLongTypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.JsonAdapter

data class RespaldoResponse(
    @SerializedName("respaldos")
    val respaldos: List<Respaldo>
)

data class Respaldo(
    @SerializedName("idRespaldo")
    val id: String,

    @SerializedName("fechaRespaldo")
    @JsonAdapter(DateToLongTypeAdapter::class)
    val fechaRespaldo: Long,

    @SerializedName("respaldo")
    val respaldo: RespaldoData
)

data class RespaldoData(
    @SerializedName("totalMachos")
    val totalMachos: Int,

    @SerializedName("totalHembras")
    val totalHembras: Int,

    @SerializedName("detalleMachos")
    val detalleMachos: DetallesMachos,

    @SerializedName("detalleHembras")
    val detalleHembras: DetallesHembras,

    @SerializedName("estadoAnimales")
    val estadoAnimales: EstadoAnimales
)

data class DetallesMachos(
    @SerializedName("becerro")
    val becerro: Int = 0,

    @SerializedName("torito")
    val torito: Int = 0,

    @SerializedName("toro")
    val toro: Int = 0
)

data class DetallesHembras(
    @SerializedName("becerra")
    val becerra: Int = 0,

    @SerializedName("vaca")
    val vaca: Int = 0
)

data class EstadoAnimales(
    @SerializedName("activos")
    val activos: Int = 0,

    @SerializedName("vendidos")
    val vendidos: Int = 0,

    @SerializedName("muertos")
    val muertos: Int = 0
)

// Class for creating a new backup (POST request)
data class RespaldoRequest(
    @SerializedName("idDispositivo")
    val idDispositivo : String,
    @SerializedName("totalMachos")
    val totalMachos: Int,

    @SerializedName("totalHembras")
    val totalHembras: Int,

    @SerializedName("detalleMachos")
    val detalleMachos: DetallesMachos,

    @SerializedName("detalleHembras")
    val detalleHembras: DetallesHembras,

    @SerializedName("estadoAnimales")
    val estadoAnimales: EstadoAnimales
)

