package com.example.bovara.ganado.domain

import com.example.bovara.crianza.domain.CrianzaUseCase
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.data.model.GanadoEstadistica
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
        println("GanadoUseCase.saveGanado: Iniciando guardado")

        // Validación condicional del formato de arete basada en tipo
        val isBecerro = tipo == "becerro" || tipo == "becerra"

        println("GanadoUseCase.saveGanado: isBecerro=$isBecerro, numeroArete='$numeroArete'")

        // Si no es becerro/becerra, el arete es obligatorio
        if (!isBecerro && numeroArete.isBlank()) {
            println("GanadoUseCase.saveGanado: Error - arete obligatorio para no becerros")
            throw IllegalArgumentException("El número de arete es obligatorio para $tipo")
        }

        // Si se proporciona un arete (incluso para becerros), validar formato
        if (numeroArete.isNotBlank() && !numeroArete.matches(Regex("^07\\d{8}$"))) {
            println("GanadoUseCase.saveGanado: Error - formato de arete inválido")
            throw IllegalArgumentException("El número de arete debe iniciar con 07 seguido de 8 dígitos")
        }

        // Comprobar que el arete no exista ya (si es un nuevo registro y se proporciona arete)
        if (id == 0 && numeroArete.isNotBlank()) {
            val exists = areteExists(numeroArete)
            println("GanadoUseCase.saveGanado: Verificando arete existente: $exists")
            if (exists) {
                println("GanadoUseCase.saveGanado: Error - arete ya existe")
                throw IllegalArgumentException("El número de arete ya existe")
            }
        }

        // Validación del sexo
        if (sexo !in listOf("macho", "hembra")) {
            println("GanadoUseCase.saveGanado: Error - sexo inválido")
            throw IllegalArgumentException("El sexo debe ser 'macho' o 'hembra'")
        }

        // Validación del tipo según el sexo
        when (sexo) {
            "macho" -> if (tipo !in listOf("toro", "torito", "becerro")) {
                println("GanadoUseCase.saveGanado: Error - tipo inválido para macho")
                throw IllegalArgumentException("Para machos, el tipo debe ser 'toro', 'torito' o 'becerro'")
            }
            "hembra" -> if (tipo !in listOf("vaca", "becerra")) {
                println("GanadoUseCase.saveGanado: Error - tipo inválido para hembra")
                throw IllegalArgumentException("Para hembras, el tipo debe ser 'vaca' o 'becerra'")
            }
        }

        // Validación del estado
        if (estado !in listOf("activo", "vendido", "muerto")) {
            println("GanadoUseCase.saveGanado: Error - estado inválido")
            throw IllegalArgumentException("El estado debe ser 'activo', 'vendido' o 'muerto'")
        }

        println("GanadoUseCase.saveGanado: Todas las validaciones pasaron correctamente")

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
            fechaRegistro = Date()
        )

        println("GanadoUseCase.saveGanado: Entidad creada, guardando en repositorio")
        return repository.insertGanado(ganado)
    }

    suspend fun updateGanadoNote(ganadoId: Int, note: String) {
        val ganado = getGanadoById(ganadoId).first() ?: throw IllegalArgumentException("Animal no encontrado")
        val updated = ganado.copy(nota = note)
        repository.updateGanado(updated)
    }

    suspend fun updateGanado(ganado: GanadoEntity) = repository.updateGanado(ganado)

    suspend fun incrementarCriasDeMadre(madreId: Int) = repository.incrementarCriasDeMadre(madreId)

    suspend fun actualizarEstado(ganadoId: Int, nuevoEstado: String) = repository.actualizarEstado(ganadoId, nuevoEstado)

    suspend fun deleteGanado(ganado: GanadoEntity) = repository.deleteGanado(ganado)

    suspend fun obtenerEstadisticasGanado(): GanadoEstadistica {
        return repository.obtenerEstadisticas()
    }

}