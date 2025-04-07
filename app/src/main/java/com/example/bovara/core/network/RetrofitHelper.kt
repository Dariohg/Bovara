package com.example.bovara.core.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder

object RetrofitHelper {
    private const val BASE_URL = "http://44.218.219.212:3000/"

    // Configuramos Gson con el adaptador para convertir la fecha
    val retrofit: Retrofit by lazy {
        val gson = GsonBuilder()
            .registerTypeAdapter(Long::class.java, DateToLongTypeAdapter())  // Registrar el adaptador para Long
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    inline fun <reified T> createApi(): T = retrofit.create(T::class.java)
}
