package com.example.bovara.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AplicarPendienteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val idPendiente = intent?.getLongExtra("idPendiente", -1L)
        Log.d("AplicarPendienteReceiver", "Aplicar pendiente ID: $idPendiente")

        // TODO: implementar la lógica de aplicación del pendiente
    }
}
