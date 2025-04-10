package com.example.bovara.core.notification.helper

import android.util.Log
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import com.example.bovara.pendiente.domain.PendienteUseCase
import java.util.Date
import kotlinx.coroutines.flow.first

object AccionHandler {

    lateinit var pendienteUseCase: PendienteUseCase
    lateinit var medicamentoUseCase: MedicamentoUseCase

    suspend fun accionPorFecha(pendiente: PendienteCompleto) {
        Log.d("AccionHandler", "Acción por fecha: ${pendiente.pendiente.id}")
        pendiente.medicina?.let { procesarPendiente(pendiente.pendiente, it) }
    }

    suspend fun accionPorHora(pendiente: PendienteCompletoConHora) {
        Log.d("AccionHandler", "Acción por hora: ${pendiente.pendiente.id}")
        pendiente.medicina?.let { procesarPendiente(pendiente.pendiente, it) }
    }

    // Nuevo método para procesar directamente desde la notificación
    suspend fun accionDirecta(pendiente: PendienteCompleto) {
        Log.d("AccionHandler", "Acción directa: ${pendiente.pendiente.id}")
        pendiente.medicina?.let { procesarPendiente(pendiente.pendiente, it) }
    }

    private suspend fun procesarPendiente(
        pendiente: com.example.bovara.pendiente.data.model.PendienteEntity,
        medicina: com.example.bovara.medicamento.data.model.MedicamentoEntity
    ) {
        try {
            val fechaActual = Date()

            val medicamentoId = medicamentoUseCase.saveMedicamento(
                id = 0,
                nombre = medicina.nombre,
                descripcion = medicina.descripcion,
                fechaAplicacion = fechaActual,
                dosisML = medicina.dosisML,
                ganadoId = medicina.ganadoId ?: 0,
                tipo = medicina.tipo,
                esProgramado = false,
                lote = medicina.lote,
                aplicado = true,
                fechaProgramada = fechaActual,
                recordatorio = false,
                notas = medicina.notas
            )

            Log.d("AccionHandler", "Medicamento creado con ID: $medicamentoId")

            // Si se creó correctamente, eliminar el pendiente
            eliminarPendiente(pendiente)

        } catch (e: Exception) {
            Log.e("AccionHandler", "Error al crear medicamento o eliminar pendiente: ${e.message}", e)
        }
    }

    private suspend fun eliminarPendiente(pendiente: com.example.bovara.pendiente.data.model.PendienteEntity) {
        try {
            pendienteUseCase.eliminar(pendiente)
            Log.d("AccionHandler", "Pendiente eliminado: ${pendiente.id}")
        } catch (e: Exception) {
            Log.e("AccionHandler", "Error al eliminar pendiente: ${e.message}", e)
        }
    }
}