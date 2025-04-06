package com.example.bovara

import android.app.Application
import com.example.bovara.core.notification.NotificationByHourService
import com.example.bovara.di.AppDependencies
import com.example.bovara.core.notification.NotificationService

class BovaraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppDependencies.initialize(applicationContext)

        if (NotificationService.canScheduleExactAlarms(applicationContext)) {
            NotificationService.startService(applicationContext)
            NotificationByHourService.startService(applicationContext)
        }
    }
}



