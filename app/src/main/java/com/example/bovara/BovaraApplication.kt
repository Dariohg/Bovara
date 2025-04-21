package com.example.bovara

import android.app.Application
import android.content.Intent
import com.example.bovara.di.AppDependencies

class BovaraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppDependencies.initialize(applicationContext)
        
    }
}



