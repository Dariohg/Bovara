package com.example.bovara.ganado.domain

import com.example.bovara.crianza.domain.CrianzaUseCase
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.data.repository.GanadoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date

class GanadoUseCase(
    private val repository: GanadoRepository,
    private val crianzaUseCase: CrianzaUseCase? = null // Inyección opcional del CrianzaUseCase
) {
    fun getAllGanado(): Flow<List<GanadoEntity>> = repository.getAllGanado()

    fun getGanadoById(id: Int): Flow<GanadoEntity?> = repository.getGanadoById(id)

    fun getGanadoByTipo(tipo: String): Flow<List<GanadoEntity>> = repository.getGanadoByTipo(tipo)

    fun getGanadoByEstado(estado: String): Flow<List<GanadoEntity>> = repository.getGanadoByEstado(estado)

    fun getCriasByMadreId(madreId: Int): Flow<List<GanadoEntity>> = repository.getCriasByMadreId(madreId)

    fun searchGanado(query: String): Flow<List<GanadoEntity>> = repository.searchGanado(query)

    /**
     * Verifica si un número de arete ya existe en la base de datos
     * @param numeroArete el número de arete a verificar
     * @return true si el arete ya existe, false en caso contrario
     */
    suspend fun areteExists(numeroArete: String): Boolean {
        return repository.countByNumeroArete(numeroArete) > 0
    }

    /**
     * Registra una relación de crianza entre una madre y su cría
     * @param madreId ID de la madre
     * @param criaId ID de la cría
     * @param fechaNacimiento Fecha de nacimiento de la cría
     * @param notas Notas adicionales sobre la crianza
     * @return ID de la crianza registrada
     */
    suspend fun registrarCrianza(
        madreId: Int,
        criaId: Int,
        fechaNacimiento: Date,
        notas: String? = null
    ): Long {
        if (crianzaUseCase == null) {
            throw IllegalStateException("CrianzaUseCase no inicializado")
        }

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

        // Si es una cría y tiene madreId, validar que la madre exista y sea una vaca
        if (madreId != null) {
            val madre = repository.getGanadoById(madreId).first()
            if (madre == null) {
                throw IllegalArgumentException("La madre seleccionada no existe")
            }
            if (madre.tipo != "vaca") {
                throw IllegalArgumentException("La madre debe ser de tipo 'vaca'")
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