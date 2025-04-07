package com.example.bovara.core.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.bovara.core.database.AppDatabase
import com.example.bovara.core.notification.NotificationHelper
import com.example.bovara.core.notification.helper.PendienteCompletoUseCase
import com.example.bovara.ganado.data.repository.GanadoRepository
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.medicamento.data.repository.MedicamentoRepository
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import com.example.bovara.pendiente.data.repository.PendienteRepository
import com.example.bovara.pendiente.domain.PendienteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ServiceByDay : Service() {

    private lateinit var pendientesPorFechaUseCase: PendienteCompletoUseCase
    private lateinit var notificationHelper: NotificationHelper

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun run() {
            if (!handler.hasCallbacks(this)) {
                Log.d("NotificationByDayService", "Servicio activado y ejecutando tareas.")
                obtenerPendientes()

                handler.postDelayed(this, 90000)  // El servicio se ejecuta cada 90 segundos
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationByDayService", "Servicio creado.")
        val db = AppDatabase.getDatabase(applicationContext)
        val pendienteRepository = PendienteRepository(db.pendienteDao())
        val medicamentoRepository = MedicamentoRepository(db.medicamentoDao())
        val ganadoRepository = GanadoRepository(db.ganadoDao())

        // Usamos el caso de uso modificado para filtrar por fecha
        pendientesPorFechaUseCase = PendienteCompletoUseCase(
            PendienteUseCase(pendienteRepository),
            MedicamentoUseCase(medicamentoRepository),
            GanadoUseCase(ganadoRepository)
        )

        // Initialize notification helper
        notificationHelper = NotificationHelper(applicationContext)

        handler.post(runnable)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("NotificationByDayService", "Servicio iniciado.")
        if (!handler.hasCallbacks(runnable)) {
            handler.post(runnable)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        Log.d("NotificationByDayService", "Servicio detenido.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun obtenerPendientes() {
        val fechaReferencia = Date()  // Fecha actual
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Llamamos al caso de uso que filtra los pendientes por fecha
                val pendientesFiltrados = pendientesPorFechaUseCase.obtenerPendientesCompletosFiltradosPorFecha(fechaReferencia)

                // Log for debugging
                Log.d("NotificationByDayService", "Pendientes futuros: ${pendientesFiltrados.futuros}")
                Log.d("NotificationByDayService", "Pendientes pasados: ${pendientesFiltrados.pasados}")
                Log.d("NotificationByDayService", "Pendientes del mismo día: ${pendientesFiltrados.mismoDia}")

                // Show notifications
                CoroutineScope(Dispatchers.Main).launch {
                    notificationHelper.showDayNotifications(pendientesFiltrados)
                }

            } catch (e: Exception) {
                Log.e("NotificationByDayService", "Error al obtener pendientes: ${e.message}")
            }
        }
    }
}

