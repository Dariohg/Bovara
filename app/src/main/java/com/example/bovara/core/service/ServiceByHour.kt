package com.example.bovara.core.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.bovara.core.database.AppDatabase
import com.example.bovara.core.notification.NotificationHelper
import com.example.bovara.core.notification.helper.FiltroPendientesPorHoraUseCase
import com.example.bovara.core.notification.helper.PendientesFiltradosPorHora
import com.example.bovara.ganado.data.repository.GanadoRepository
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.medicamento.data.repository.MedicamentoRepository
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import com.example.bovara.pendiente.data.repository.PendienteRepository
import com.example.bovara.pendiente.domain.PendienteUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class ServiceByHour : Service() {

    private lateinit var pendientesPorHoraUseCase: FiltroPendientesPorHoraUseCase
    private lateinit var notificationHelper: NotificationHelper

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun run() {
            if (!handler.hasCallbacks(this)) {
                Log.d("NotificationByHourService", "Servicio activado y ejecutando tareas.")
                obtenerPendientes()

                handler.postDelayed(this, 90000)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationByHourService", "Servicio creado.")
        val db = AppDatabase.getDatabase(applicationContext)
        val pendienteRepository = PendienteRepository(db.pendienteDao())
        val medicamentoRepository = MedicamentoRepository(db.medicamentoDao())
        val ganadoRepository = GanadoRepository(db.ganadoDao())

        pendientesPorHoraUseCase = FiltroPendientesPorHoraUseCase(
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
        Log.d("NotificationByHourService", "Servicio iniciado.")
        if (!handler.hasCallbacks(runnable)) {
            handler.post(runnable)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        Log.d("NotificationByHourService", "Servicio detenido.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun obtenerPendientes() {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pendientesFiltrados: PendientesFiltradosPorHora = pendientesPorHoraUseCase.filtrarPendientesPorHora()

                // Log for debugging
                Log.d("NotificationByHourService", "Pendientes futuros: ${pendientesFiltrados.futuros}")
                Log.d("NotificationByHourService", "Pendientes pasados: ${pendientesFiltrados.pasados}")
                Log.d("NotificationByHourService", "Pendientes en el mismo día: ${pendientesFiltrados.mismoDia}")

                // Show notifications
                CoroutineScope(Dispatchers.Main).launch {
                    notificationHelper.showHourNotifications(pendientesFiltrados)
                    pendientesFiltrados.futuros.clear()
                    pendientesFiltrados.pasados.clear()
                    pendientesFiltrados.mismoDia.clear()
                }

            } catch (e: Exception) {
                Log.e("NotificationByHourService", "Error al obtener pendientes: ${e.message}")
            }
        }
    }
}

