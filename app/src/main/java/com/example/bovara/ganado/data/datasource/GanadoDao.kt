package com.example.bovara.ganado.data.datasource

import androidx.room.*
import com.example.bovara.ganado.data.model.GanadoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GanadoDao {
    @Query("SELECT * FROM ganado ORDER BY fechaRegistro DESC")
    fun getAllGanado(): Flow<List<GanadoEntity>>

    @Query("SELECT * FROM ganado WHERE id = :id")
    fun getGanadoById(id: Int): Flow<GanadoEntity?>

    @Query("SELECT * FROM ganado WHERE tipo = :tipo")
    fun getGanadoByTipo(tipo: String): Flow<List<GanadoEntity>>

    @Query("SELECT * FROM ganado WHERE estado = :estado")
    fun getGanadoByEstado(estado: String): Flow<List<GanadoEntity>>

    @Query("SELECT * FROM ganado WHERE madreId = :madreId")
    fun getCriasByMadreId(madreId: Int): Flow<List<GanadoEntity>>

    @Query("SELECT * FROM ganado WHERE apodo LIKE '%' || :query || '%' OR numeroArete LIKE '%' || :query || '%'")
    fun searchGanado(query: String): Flow<List<GanadoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGanado(ganado: GanadoEntity): Long

    @Update
    suspend fun updateGanado(ganado: GanadoEntity)

    @Query("UPDATE ganado SET cantidadCrias = cantidadCrias + 1 WHERE id = :madreId")
    suspend fun incrementarCriasDeMadre(madreId: Int)

    @Query("UPDATE ganado SET estado = :nuevoEstado WHERE id = :ganadoId")
    suspend fun actualizarEstado(ganadoId: Int, nuevoEstado: String)

    @Delete
    suspend fun deleteGanado(ganado: GanadoEntity)
}