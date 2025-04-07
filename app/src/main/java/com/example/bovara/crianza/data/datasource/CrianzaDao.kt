package com.example.bovara.crianza.data.datasource

import androidx.room.*
import com.example.bovara.crianza.data.model.CrianzaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CrianzaDao {
    @Query("SELECT * FROM crianza WHERE madreId = :madreId ORDER BY fechaNacimiento DESC")
    fun getCrianzasByMadreId(madreId: Int): Flow<List<CrianzaEntity>>

    @Query("SELECT * FROM crianza WHERE criaId = :criaId")
    fun getCrianzaByCriaId(criaId: Int): Flow<CrianzaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrianza(crianza: CrianzaEntity): Long

    @Update
    suspend fun updateCrianza(crianza: CrianzaEntity)

    @Delete
    suspend fun deleteCrianza(crianza: CrianzaEntity)
}