package com.example.bovara.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.util.Log
import com.example.bovara.MainActivity
import com.example.bovara.R
import com.example.bovara.core.notification.helper.AccionHandler
import com.example.bovara.core.notification.helper.PendienteCompleto
import com.example.bovara.core.notification.helper.PendienteCompletoConHora
import com.example.bovara.core.notification.helper.PendientesFiltradosPorFecha
import com.example.bovara.core.notification.helper.PendientesFiltradosPorHora
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID_HOUR = "bovara_hour_notifications"
        private const val CHANNEL_ID_DAY = "bovara_day_notifications"
        private const val NOTIFICATION_GROUP = "bovara_notifications"

        // Notification IDs
        private const val NOTIFICATION_ID_FUTURE = 1000
        private const val NOTIFICATION_ID_PAST = 2000
        private const val NOTIFICATION_ID_SAME_DAY = 3000
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Hour-based notification channel
            val hourChannel = NotificationChannel(
                CHANNEL_ID_HOUR,
                "Notificaciones por Hora",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para pendientes programados por hora"
                enableVibration(true)
            }

            // Day-based notification channel
            val dayChannel = NotificationChannel(
                CHANNEL_ID_DAY,
                "Notificaciones por Día",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones para pendientes programados por día"
                enableVibration(true)
            }

            // Register the channels with the system
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(hourChannel)
            notificationManager.createNotificationChannel(dayChannel)
        }
    }

    // Notification for hour-based events
    fun showHourNotifications(pendientesFiltrados: PendientesFiltradosPorHora) {
        // Notify for future events (only if horasRestantes > 0)
        pendientesFiltrados.futuros
            .filter { it.horasRestantes > 0 } // Only show future notifications if hours > 0
            .forEachIndexed { index, pendiente ->
                showFutureHourNotification(pendiente, NOTIFICATION_ID_FUTURE + index)
            }

        // Notify for past events
        pendientesFiltrados.pasados.forEachIndexed { index, pendiente ->
            showPastHourNotification(pendiente, NOTIFICATION_ID_PAST + index)
        }

        // Notify for same day events
        pendientesFiltrados.mismoDia.forEachIndexed { index, pendiente ->
            showSameDayHourNotification(pendiente, NOTIFICATION_ID_SAME_DAY + index)
        }
    }

    // Notification for day-based events
    fun showDayNotifications(pendientesFiltrados: PendientesFiltradosPorFecha) {
        // Notify for future events (only if diasFaltantes > 0)
        pendientesFiltrados.futuros
            .filter { it.diasFaltantes != null && it.diasFaltantes > 0 } // Only show future notifications if days > 0
            .forEachIndexed { index, pendiente ->
                showFutureDayNotification(pendiente, NOTIFICATION_ID_FUTURE + index)
            }

        // Notify for past events
        pendientesFiltrados.pasados.forEachIndexed { index, pendiente ->
            showPastDayNotification(pendiente, NOTIFICATION_ID_PAST + index)
        }

        // Notify for same day events
        pendientesFiltrados.mismoDia.forEachIndexed { index, pendiente ->
            showSameDayNotification(pendiente, NOTIFICATION_ID_SAME_DAY + index)
        }
    }

    // Future hour notification (no action button)
    private fun showFutureHourNotification(pendiente: PendienteCompletoConHora, notificationId: Int) {
        val title = "Pendiente próximo: ${pendiente.medicina?.nombre ?: "Medicina"}"
        val content = "Ganado: ${pendiente.ganado?.apodo ?: "Sin apodo"}\n" +
                "Faltan ${pendiente.horasRestantes} horas"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_HOUR)
            .setSmallIcon(R.drawable.ic_vaccine) // Asegúrate de tener este icono
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(NOTIFICATION_GROUP)

        showNotification(notificationId, builder.build())
    }

    // Past hour notification (with action button)
    private fun showPastHourNotification(pendiente: PendienteCompletoConHora, notificationId: Int) {
        val title = "Pendiente vencido: ${pendiente.medicina?.nombre ?: "Medicina"}"
        val content = "Ganado: ${pendiente.ganado?.apodo ?: "Sin apodo"}\n" +
                "Pasaron ${Math.abs(pendiente.horasRestantes)} horas"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        AccionHandler.accionPorHora(pendiente)

        val actionIntent = Intent(context, MainActivity::class.java).apply {
            action = "MARK_COMPLETED"
            putExtra("PENDIENTE_ID", pendiente.pendiente.id)
        }
        val actionPendingIntent = PendingIntent.getActivity(
            context, pendiente.pendiente.id.toInt(), actionIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_HOUR)
            .setSmallIcon(R.drawable.ic_vaccine)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_vaccine, "Marcar como completado", actionPendingIntent)
            .setAutoCancel(true)
            .setGroup(NOTIFICATION_GROUP)

        showNotification(notificationId, builder.build())
    }

    // Same day hour notification (with action button)
    private fun showSameDayHourNotification(pendiente: PendienteCompletoConHora, notificationId: Int) {
        val title = "Pendiente para hoy: ${pendiente.medicina?.nombre ?: "Medicina"}"
        val content = "Ganado: ${pendiente.ganado?.apodo ?: "Sin apodo"}\n" +
                "Programado para hoy (${formatTime(pendiente.pendiente.fechaProgramada)})"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        AccionHandler.accionPorHora(pendiente)

        val actionIntent = Intent(context, MainActivity::class.java).apply {
            action = "MARK_COMPLETED"
            putExtra("PENDIENTE_ID", pendiente.pendiente.id)
        }
        val actionPendingIntent = PendingIntent.getActivity(
            context, pendiente.pendiente.id.toInt(), actionIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_HOUR)
            .setSmallIcon(R.drawable.ic_vaccine)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_vaccine, "Marcar como completado", actionPendingIntent)
            .setAutoCancel(true)
            .setGroup(NOTIFICATION_GROUP)

        showNotification(notificationId, builder.build())
    }

    // Future day notification (no action button)
    private fun showFutureDayNotification(pendiente: PendienteCompleto, notificationId: Int) {
        val title = "Pendiente próximo: ${pendiente.medicina?.nombre ?: "Medicina"}"
        val content = "Ganado: ${pendiente.ganado?.apodo ?: "Sin apodo"}\n" +
                "Faltan ${pendiente.diasFaltantes} días"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DAY)
            .setSmallIcon(R.drawable.ic_vaccine)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(NOTIFICATION_GROUP)

        showNotification(notificationId, builder.build())
    }

    // Past day notification (with action button)
    private fun showPastDayNotification(pendiente: PendienteCompleto, notificationId: Int) {
        val title = "Pendiente vencido: ${pendiente.medicina?.nombre ?: "Medicina"}"
        val content = "Ganado: ${pendiente.ganado?.apodo ?: "Sin apodo"}\n" +
                "Pasaron ${Math.abs(pendiente.diasFaltantes ?: 0)} días"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        AccionHandler.accionPorFecha(pendiente)
        val actionIntent = Intent(context, MainActivity::class.java).apply {
            action = "MARK_COMPLETED"
            putExtra("PENDIENTE_ID", pendiente.pendiente.id)
        }
        val actionPendingIntent = PendingIntent.getActivity(
            context, pendiente.pendiente.id.toInt(), actionIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DAY)
            .setSmallIcon(R.drawable.ic_vaccine)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_vaccine, "Marcar como completado", actionPendingIntent)
            .setAutoCancel(true)
            .setGroup(NOTIFICATION_GROUP)

        showNotification(notificationId, builder.build())
    }

    // Same day notification (with action button)
    private fun showSameDayNotification(pendiente: PendienteCompleto, notificationId: Int) {
        val title = "Pendiente para hoy: ${pendiente.medicina?.nombre ?: "Medicina"}"
        val content = "Ganado: ${pendiente.ganado?.apodo ?: "Sin apodo"}\n" +
                "Programado para hoy (${formatDate(pendiente.pendiente.fechaProgramada)})"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        AccionHandler.accionPorFecha(pendiente)

        val actionIntent = Intent(context, MainActivity::class.java).apply {
            action = "MARK_COMPLETED"
            putExtra("PENDIENTE_ID", pendiente.pendiente.id)
        }
        val actionPendingIntent = PendingIntent.getActivity(
            context, pendiente.pendiente.id.toInt(), actionIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_DAY)
            .setSmallIcon(R.drawable.ic_vaccine)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_vaccine, "Marcar como completado", actionPendingIntent)
            .setAutoCancel(true)
            .setGroup(NOTIFICATION_GROUP)

        showNotification(notificationId, builder.build())
    }

    // Helper method to safely show notification with permission check
    private fun showNotification(notificationId: Int, notification: android.app.Notification) {
        val notificationManager = NotificationManagerCompat.from(context)

        // Check for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(notificationId, notification)
            } else {
                Log.d("NotificationHelper", "No permission to post notifications")
                // You could store this notification to show later when permission is granted
            }
        } else {
            // For Android 12 and below
            try {
                notificationManager.notify(notificationId, notification)
            } catch (e: SecurityException) {
                Log.e("NotificationHelper", "Security exception when posting notification: ${e.message}")
            }
        }
    }

    // Helper method to format time
    private fun formatTime(date: Date): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    // Helper method to format date
    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(date)
    }
}

