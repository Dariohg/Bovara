package com.example.bovara.ganado.domain

import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.data.repository.GanadoRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

class GanadoUseCase(private val repository: GanadoRepository) {
    fun getAllGanado(): Flow<List<GanadoEntity>> = repository.getAllGanado()

    fun getGanadoById(id: Int): Flow<GanadoEntity?> = repository.getGanadoById(id)

    fun getGanadoByTipo(tipo: String): Flow<List<GanadoEntity>> = repository.getGanadoByTipo(tipo)

    fun getGanadoByEstado(estado: String): Flow<List<GanadoEntity>> = repository.getGanadoByEstado(estado)

    fun getCriasByMadreId(madreId: Int): Flow<List<GanadoEntity>> = repository.getCriasByMadreId(madreId)

    fun searchGanado(query: String): Flow<List<GanadoEntity>> = repository.searchGanado(query)

    suspend fun saveGanado(
        id: Int = 0,
        numeroArete: String,
        apodo: String? = null,
        sexo: String,
        tipo: String,
        color: String,
        fechaNacimiento: Date? = null,
        madreId: Int? = null,
        cantidadCrias: Int = 0,
        estado: String = "activo",
        imagenUrl: String? = null,
        imagenesSecundarias: List<String>? = null
    ): Long {
        // Validación del formato de arete (siempre inicia con 07 seguido de 8 números)
        require(numeroArete.matches(Regex("^07\\d{8}$"))) {
            "El número de arete debe iniciar con 07 seguido de 8 dígitos"
        }

        // Validación del sexo
        require(sexo in listOf("macho", "hembra")) {
            "El sexo debe ser 'macho' o 'hembra'"
        }

        // Validación del tipo según el sexo
        when (sexo) {
            "macho" -> require(tipo in listOf("toro", "torito", "becerro")) {
                "Para machos, el tipo debe ser 'toro', 'torito' o 'becerro'"
            }
            "hembra" -> require(tipo in listOf("vaca", "becerra")) {
                "Para hembras, el tipo debe ser 'vaca' o 'becerra'"
            }
        }

        // Validación del estado
        require(estado in listOf("activo", "vendido", "muerto")) {
            "El estado debe ser 'activo', 'vendido' o 'muerto'"
        }

        val ganado = GanadoEntity(
            id = id,
            numeroArete = numeroArete,
            apodo = apodo,
            sexo = sexo,
            tipo = tipo,
            color = color,
            fechaNacimiento = fechaNacimiento,
            madreId = madreId,
            cantidadCrias = cantidadCrias,
            estado = estado,
            imagenUrl = imagenUrl,
            imagenesSecundarias = imagenesSecundarias,
            fechaRegistro = if (id == 0) Date() else Date() // Solo actualiza la fecha de registro si es nuevo
        )

        return repository.insertGanado(ganado)
    }

    suspend fun updateGanado(ganado: GanadoEntity) = repository.updateGanado(ganado)

    suspend fun incrementarCriasDeMadre(madreId: Int) = repository.incrementarCriasDeMadre(madreId)

    suspend fun actualizarEstado(ganadoId: Int, nuevoEstado: String) = repository.actualizarEstado(ganadoId, nuevoEstado)

    suspend fun deleteGanado(ganado: GanadoEntity) = repository.deleteGanado(ganado)
}