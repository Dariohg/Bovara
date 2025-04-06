package com.example.bovara.core.notification

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bovara.core.database.AppDatabase
import com.example.bovara.core.notification.helper.PendienteCompleto
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
import java.text.SimpleDateFormat
import java.util.*

class NotificationService : Service() {
    private lateinit var alarmManager: AlarmManager
    private lateinit var notificationManager: NotificationManager
    private var notificationId = 1
    private val TAG = "NotificationService"

    private lateinit var pendienteCompletoUseCase: PendienteCompletoUseCase

    companion object {
        const val CHANNEL_ID = "periodic_notifications_channel"
        const val FOREGROUND_SERVICE_ID = 1001
        const val ACTION_START_SERVICE = "com.example.bovara.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.bovara.STOP_SERVICE"
        const val ACTION_SHOW_NOTIFICATION = "com.example.bovara.SHOW_NOTIFICATION"

        fun startService(context: Context) {
            val intent = Intent(context, NotificationService::class.java).apply {
                action = ACTION_START_SERVICE
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, NotificationService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }

        fun canScheduleExactAlarms(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.canScheduleExactAlarms()
            } else {
                true // En versiones anteriores siempre se puede
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        val db = AppDatabase.getDatabase(applicationContext)
        val pendienteRepository = PendienteRepository(db.pendienteDao())
        val medicamentoRepository = MedicamentoRepository(db.medicamentoDao())
        val ganadoRepository = GanadoRepository(db.ganadoDao())

        pendienteCompletoUseCase = PendienteCompletoUseCase(
            PendienteUseCase(pendienteRepository),
            MedicamentoUseCase(medicamentoRepository),
            GanadoUseCase(ganadoRepository)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                startForeground()
                verificarPendientesYProgramarNotificaciones()
            }
            ACTION_STOP_SERVICE -> {
                stopForeground(true)
                stopSelf()
            }
            ACTION_SHOW_NOTIFICATION -> {
                verificarPendientesYProgramarNotificaciones()
            }
        }
        return START_STICKY
    }

    private fun startForeground() {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Servicio por dias")
            .setContentText("Buscando pendientes próximos...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(FOREGROUND_SERVICE_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificaciones periódicas"
            val descriptionText = "Canal para notificaciones programadas"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun verificarPendientesYProgramarNotificaciones() {
        val fechaReferencia = Date()
        CoroutineScope(Dispatchers.IO).launch {
            val pendientesFiltrados = pendienteCompletoUseCase
                .obtenerPendientesCompletosFiltradosPorFecha(fechaReferencia)

            if (pendientesFiltrados.futuros.isNotEmpty()) {
                showNotification(pendientesFiltrados.futuros)
            } else {
                Log.d(TAG, "No hay pendientes futuros para notificar.")
            }

            scheduleNextNotification()
        }
    }

    private fun showNotification(pendientes: List<PendienteCompleto>) {
        val pendiente = pendientes.firstOrNull() ?: return
        val fechaPendiente = pendiente.pendiente.fechaProgramada
        val diasFaltantes = calcularDiasFaltantes(fechaPendiente)

        val nombreMedicina = pendiente.medicina?.nombre ?: "la medicina"
        val nombreGanado = pendiente.ganado?.apodo ?: "el ganado"

        val mensaje = when {
            diasFaltantes == 0 -> "Aplicar $nombreMedicina a $nombreGanado HOY"
            diasFaltantes == 1 -> "Falta 1 día para aplicar $nombreMedicina a $nombreGanado"
            diasFaltantes > 1 -> "Faltan $diasFaltantes días para aplicar $nombreMedicina a $nombreGanado"
            diasFaltantes == -1 -> "El pendiente de aplicar $nombreMedicina a $nombreGanado venció hace 1 día"
            else -> "El pendiente de aplicar $nombreMedicina a $nombreGanado no se completó hace ${-diasFaltantes} días"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pendiente Programado")
            .setContentText(mensaje)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (diasFaltantes <= 0) {
            // Crear acción del botón
            val aplicarIntent = Intent(this, AplicarPendienteReceiver::class.java).apply {
                putExtra("idPendiente", pendiente.pendiente.id)
            }

            val aplicarPendingIntent = PendingIntent.getBroadcast(
                this,
                pendiente.pendiente.id.toInt(),
                aplicarIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(
                android.R.drawable.ic_menu_send,
                "Aplicar",
                aplicarPendingIntent
            )
        }

        notificationManager.notify(notificationId++, builder.build())
    }


    private fun calcularDiasFaltantes(fecha: Date): Int {
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val fechaPendiente = Calendar.getInstance().apply {
            time = fecha
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diferenciaMillis = fechaPendiente.timeInMillis - hoy.timeInMillis
        return (diferenciaMillis / (1000 * 60 * 60 * 24)).toInt()
    }



    private fun scheduleNextNotification() {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            action = ACTION_SHOW_NOTIFICATION
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = SystemClock.elapsedRealtime() + 14_400_000

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    Log.d(TAG, "Alarma exacta programada correctamente.")
                } else {
                    Log.w(TAG, "No se puede usar alarmas exactas. Requiere autorización del usuario.")
                    // Alternativa: programar inexacta
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                    // Opcional: lanzar intento para abrir ajustes
                    val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(settingsIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Alarma exacta programada en Android < 12.")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error al programar alarma exacta: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
