package com.example.bovara.statistics.data.datasource

import com.example.bovara.statistics.data.model.Respaldo
import com.example.bovara.statistics.data.model.RespaldoRequest
import com.example.bovara.statistics.data.model.RespaldoResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface StatisticsApi {
    @GET("api/respaldo/")
    suspend fun getBackups(): RespaldoResponse

    @POST("api/respaldo/")
    suspend fun createBackup(@Body request: RespaldoRequest): Respaldo
}

