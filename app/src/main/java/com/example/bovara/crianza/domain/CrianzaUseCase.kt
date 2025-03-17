package com.example.bovara.crianza.domain

import com.example.bovara.crianza.data.model.CrianzaEntity
import com.example.bovara.crianza.data.repository.CrianzaRepository
import com.example.bovara.ganado.domain.GanadoUseCase
import kotlinx.coroutines.flow.Flow
import java.util.Date

class CrianzaUseCase(
    private val repository: CrianzaRepository,
    private val ganadoUseCase: GanadoUseCase
) {
    fun getCrianzasByMadreId(madreId: Int): Flow<List<CrianzaEntity>> =
        repository.getCrianzasByMadreId(madreId)

    fun getCrianzaByCriaId(criaId: Int): Flow<CrianzaEntity?> =
        repository.getCrianzaByCriaId(criaId)

    suspend fun registrarCria(
        madreId: Int,
        criaId: Int,
        fechaNacimiento: Date,
        notas: String? = null
    ): Long {
        // Incrementar el contador de crías de la madre
        ganadoUseCase.incrementarCriasDeMadre(madreId)

        val crianza = CrianzaEntity(
            madreId = madreId,
            criaId = criaId,
            fechaNacimiento = fechaNacimiento,
            notas = notas,
            fechaRegistro = Date()
        )

        return repository.insertCrianza(crianza)
    }

    suspend fun updateCrianza(crianza: CrianzaEntity) =
        repository.updateCrianza(crianza)

    suspend fun deleteCrianza(crianza: CrianzaEntity) =
        repository.deleteCrianza(crianza)
}