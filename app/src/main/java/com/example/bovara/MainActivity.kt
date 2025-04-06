package com.example.bovara

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.bovara.core.notification.NotificationService
import com.example.bovara.ui.theme.BovaraTheme
import com.example.bovara.core.navigation.NavigationWrapper

class MainActivity : ComponentActivity() {

    companion object {
        private const val REQUEST_SCHEDULE_EXACT_ALARM = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkExactAlarmPermission()

        setContent {
            BovaraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationWrapper()
                }
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    "Please grant permission to schedule exact alarms for minute-by-minute notifications",
                    Toast.LENGTH_LONG
                ).show()

                // Direct the user to the settings
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (NotificationService.canScheduleExactAlarms(this)) {
            NotificationService.startService(this)
        }
    }
}

