package com.example.bovara.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class NotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            NotificationService.ACTION_SHOW_NOTIFICATION -> {
                val serviceIntent = Intent(context, NotificationService::class.java).apply {
                    action = NotificationService.ACTION_SHOW_NOTIFICATION
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                NotificationService.startService(context)
            }
        }
    }
}

