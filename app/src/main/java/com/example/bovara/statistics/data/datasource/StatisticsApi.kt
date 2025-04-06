package com.example.bovara.statistics.data.datasource

import com.example.bovara.statistics.data.model.RespaldoResponse
import com.example.bovara.statistics.data.model.Respaldo
import com.example.bovara.statistics.data.model.DatosRespaldo
import com.example.bovara.statistics.data.model.DetalleMachos
import com.example.bovara.statistics.data.model.DetalleHembras
import com.example.bovara.statistics.data.model.EstadoAnimales
import kotlinx.coroutines.delay

interface StatisticsApi {
    suspend fun getRespaldos(): RespaldoResponse
    suspend fun createRespaldo(): Boolean
}

class StatisticsApiMock : StatisticsApi {
    override suspend fun getRespaldos(): RespaldoResponse {
        delay(1000)
        return RespaldoResponse(
            respaldos = obtenerRespaldosMock()
        )
    }

    override suspend fun createRespaldo(): Boolean {
        delay(2000)
        return true
    }

    private fun obtenerRespaldosMock(): List<Respaldo> {
        return listOf(
            Respaldo(
                idRespaldo = "60b8dfe8bce8f020a5b1e292",
                fechaRespaldo = 1712131200,
                respaldo = DatosRespaldo(
                    totalMachos = 10,
                    totalHembras = 12,
                    detalleMachos = DetalleMachos(becerro = 5, torito = 3, toro = 2),
                    detalleHembras = DetalleHembras(becerra = 6, vaca = 6),
                    estadoAnimales = EstadoAnimales(activos = 18, muertos = 2, vendidos = 2)
                )
            ),
            Respaldo(
                idRespaldo = "60b8dfe8bce8f020a5b1e293",
                fechaRespaldo = 1712217600,
                respaldo = DatosRespaldo(
                    totalMachos = 11,
                    totalHembras = 13,
                    detalleMachos = DetalleMachos(becerro = 6, torito = 3, toro = 2),
                    detalleHembras = DetalleHembras(becerra = 7, vaca = 6),
                    estadoAnimales = EstadoAnimales(activos = 19, muertos = 3, vendidos = 2)
                )
            )
        )
    }
}