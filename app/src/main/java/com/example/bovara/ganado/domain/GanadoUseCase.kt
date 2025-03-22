package com.example.bovara.ganado.domain

import com.example.bovara.crianza.domain.CrianzaUseCase
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.data.repository.GanadoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date

class GanadoUseCase(
    private val repository: GanadoRepository,
    private val crianzaUseCase: CrianzaUseCase? = null
) {
    fun getAllGanado(): Flow<List<GanadoEntity>> = repository.getAllGanado()

    fun getGanadoById(id: Int): Flow<GanadoEntity?> = repository.getGanadoById(id)

    fun getGanadoByTipo(tipo: String): Flow<List<GanadoEntity>> = repository.getGanadoByTipo(tipo)

    fun getGanadoByEstado(estado: String): Flow<List<GanadoEntity>> = repository.getGanadoByEstado(estado)

    fun getCriasByMadreId(madreId: Int): Flow<List<GanadoEntity>> = repository.getCriasByMadreId(madreId)

    fun searchGanado(query: String): Flow<List<GanadoEntity>> = repository.searchGanado(query)

    suspend fun areteExists(numeroArete: String): Boolean {
        return repository.countByNumeroArete(numeroArete) > 0
    }

    suspend fun registrarCrianza(
        madreId: Int,
        criaId: Int,
        fechaNacimiento: Date,
        notas: String? = null
    ): Long {
        if (crianzaUseCase == null) {
            throw IllegalStateException("CrianzaUseCase no inicializado")
        }

        // Obtener la madre para verificar si es una becerra
        val madre = repository.getGanadoById(madreId).first()

        // Si la madre es una becerra, actualizarla a vaca
        if (madre != null && madre.tipo == "becerra") {
            val madreActualizada = madre.copy(tipo = "vaca")
            repository.updateGanado(madreActualizada)
        }

        // Incrementar el contador de crías de la madre
        incrementarCriasDeMadre(madreId)

        // Registrar la crianza utilizando el caso de uso de Crianza
        return crianzaUseCase.registrarCria(
            madreId = madreId,
            criaId = criaId,
            fechaNacimiento = fechaNacimiento,
            notas = notas
        )
    }

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

        // Comprobar que el arete no exista ya (si es un nuevo registro)
        if (id == 0 && areteExists(numeroArete)) {
            throw IllegalArgumentException("El número de arete ya existe")
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

        // Si es una cría y tiene madreId, validar que la madre exista
        if (madreId != null) {
            val madre = repository.getGanadoById(madreId).first()
            if (madre == null) {
                throw IllegalArgumentException("La madre seleccionada no existe")
            }
            // Ahora permitimos tanto "vaca" como "becerra" como posibles madres
            if (madre.tipo != "vaca" && madre.tipo != "becerra") {
                throw IllegalArgumentException("La madre debe ser de tipo 'vaca' o 'becerra'")
            }
            if (madre.estado != "activo") {
                throw IllegalArgumentException("La madre debe estar activa")
            }
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