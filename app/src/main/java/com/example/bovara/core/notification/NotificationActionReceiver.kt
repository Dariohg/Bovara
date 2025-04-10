// Nuevo archivo: app/src/main/java/com/example/bovara/core/notification/NotificationActionReceiver.kt
package com.example.bovara.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.bovara.core.database.AppDatabase
import com.example.bovara.core.notification.helper.AccionHandler
import com.example.bovara.ganado.data.repository.GanadoRepository
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.medicamento.data.repository.MedicamentoRepository
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import com.example.bovara.pendiente.data.repository.PendienteRepository
import com.example.bovara.pendiente.domain.PendienteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COMPLETE_TASK = "com.example.bovara.ACTION_COMPLETE_TASK"
        const val EXTRA_PENDIENTE_ID = "pendiente_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_COMPLETE_TASK) {
            val pendienteId = intent.getLongExtra(EXTRA_PENDIENTE_ID, -1L)
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

            if (pendienteId != -1L) {
                Log.d("NotificationAction", "Procesando acción para pendiente ID: $pendienteId")

                // Inicializar dependencias
                val db = AppDatabase.getDatabase(context)
                val pendienteRepository = PendienteRepository(db.pendienteDao())
                val medicamentoRepository = MedicamentoRepository(db.medicamentoDao())
                val ganadoRepository = GanadoRepository(db.ganadoDao())

                val pendienteUseCase = PendienteUseCase(pendienteRepository)
                val medicamentoUseCase = MedicamentoUseCase(medicamentoRepository)
                val ganadoUseCase = GanadoUseCase(ganadoRepository)

                // Configurar el AccionHandler
                AccionHandler.pendienteUseCase = pendienteUseCase
                AccionHandler.medicamentoUseCase = medicamentoUseCase

                // Procesar en un coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Obtener el pendiente
                        val pendiente = pendienteUseCase.obtenerPorId(pendienteId.toInt())

                        pendiente?.let {
                            // Conseguir el medicamento asociado
                            val medicamento = medicamentoUseCase.getMedicamentoById(it.idMedicina.toInt()).first()

                            // Conseguir el ganado asociado
                            val ganado = medicamento?.ganadoId?.let { ganadoId ->
                                ganadoUseCase.getGanadoById(ganadoId).first()
                            }

                            // Crear un objeto PendienteCompleto para el handler
                            val pendienteCompleto = com.example.bovara.core.notification.helper.PendienteCompleto(
                                pendiente = it,
                                medicina = medicamento,
                                ganado = ganado,
                                diasFaltantes = 0
                            )

                            // Ejecutar la acción
                            AccionHandler.accionDirecta(pendienteCompleto)

                            // Cancelar la notificación
                            CoroutineScope(Dispatchers.Main).launch {
                                if (notificationId != -1) {
                                    NotificationManagerCompat.from(context).cancel(notificationId)
                                }
                            }

                            Log.d("NotificationAction", "Acción completada exitosamente para pendiente ID: $pendienteId")
                        } ?: Log.e("NotificationAction", "Pendiente no encontrado: $pendienteId")

                    } catch (e: Exception) {
                        Log.e("NotificationAction", "Error al procesar acción: ${e.message}", e)
                    }
                }
            }
        }
    }
}