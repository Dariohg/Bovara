package com.example.bovara

import android.app.Application
import android.content.Intent
import com.example.bovara.core.service.ServiceByDay
import com.example.bovara.core.service.ServiceByHour
import com.example.bovara.di.AppDependencies

class BovaraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppDependencies.initialize(applicationContext)

        val serviceByHourIntent = Intent(applicationContext, ServiceByHour::class.java)
        startService(serviceByHourIntent)

        val serviceByDayIntent = Intent(applicationContext, ServiceByDay::class.java)
        startService(serviceByDayIntent)
    }
}



