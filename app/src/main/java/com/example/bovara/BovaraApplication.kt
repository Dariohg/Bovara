package com.example.bovara

import android.app.Application
import com.example.bovara.di.AppDependencies

class BovaraApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar las dependencias de la aplicación
        AppDependencies.initialize(applicationContext)
    }
}