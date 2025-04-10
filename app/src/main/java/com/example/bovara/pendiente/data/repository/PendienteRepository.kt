package com.example.bovara.pendiente.data.repository

import com.example.bovara.pendiente.data.datasource.PendienteDao
import com.example.bovara.pendiente.data.model.PendienteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PendienteRepository(private val pendienteDao: PendienteDao) {

    fun obtenerTodos(): Flow<List<PendienteEntity>> =
        pendienteDao.obtenerTodos()

    suspend fun obtenerPorId(id: Int): PendienteEntity? =
        pendienteDao.obtenerPorId(id)

    fun obtenerPorMedicina(idMedicina: Int): Flow<List<PendienteEntity>> =
        pendienteDao.obtenerPorMedicina(idMedicina)

    fun obtenerPorEstatus(estatus: String): Flow<List<PendienteEntity>> =
        pendienteDao.obtenerPorEstatus(estatus)

    fun obtenerPorRangoDeFechas(inicio: Date, fin: Date): Flow<List<PendienteEntity>> =
        pendienteDao.obtenerPorRangoDeFechas(inicio, fin)

    suspend fun insertar(pendiente: PendienteEntity): Long =
        pendienteDao.insertar(pendiente)

    suspend fun insertarVarios(pendientes: List<PendienteEntity>): List<Long> =
        pendienteDao.insertarVarios(pendientes)

    suspend fun actualizar(pendiente: PendienteEntity) =
        pendienteDao.actualizar(pendiente)

    suspend fun eliminar(pendiente: PendienteEntity) =
        pendienteDao.eliminar(pendiente)

    suspend fun obtenerPendientesDelDiaConHorasRelativas(): Flow<List<PendienteEntity>> {
        val ahora = Date()

        val calendar = Calendar.getInstance().apply {
            time = ahora
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val inicioDelDia = calendar.time
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val finDelDia = calendar.time

        val pendientesHoy = pendienteDao.obtenerDelDiaActual(inicioDelDia, finDelDia)

        val rangos = listOf(5, 3, 1, 0, -1, -3, -5)
        val toleranciaMinutos = 1

        val pendientesFiltrados = mutableListOf<PendienteEntity>()

        for (pendiente in pendientesHoy) {
            val formato = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
            val horaDate = formato.parse(pendiente.hora)!!

            val diferenciaMinutos = ((horaDate.time - ahora.time) / 60000).toInt()

            for (rango in rangos) {
                val minutosEsperados = rango * 60
                if (diferenciaMinutos in (minutosEsperados - toleranciaMinutos)..(minutosEsperados + toleranciaMinutos)) {
                    pendientesFiltrados.add(pendiente)
                    break
                }
            }
        }

        return flowOf(pendientesFiltrados)
    }


}
