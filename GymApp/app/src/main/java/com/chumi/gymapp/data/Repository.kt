package com.chumi.gymapp.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Guarda todos los datos en un único fichero JSON dentro de la app.
 * Se escribe de forma atómica (fichero temporal + renombrado) para no corromper datos.
 */
class Repository(context: Context) {

    private val file = File(context.filesDir, "gym_data.json")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _data = MutableStateFlow(load())
    val data: StateFlow<AppData> = _data.asStateFlow()

    init {
        @OptIn(FlowPreview::class)
        _data.drop(1).debounce(300).onEach { save(it) }.launchIn(scope)
    }

    fun update(transform: (AppData) -> AppData) = _data.update(transform)

    /** Fuerza el guardado inmediato (al salir de la app). */
    fun flush() = save(_data.value)

    fun exportJson(): String = json.encodeToString(AppData.serializer(), _data.value)

    fun importJson(text: String) {
        val imported = json.decodeFromString(AppData.serializer(), text)
        _data.value = withDefaults(imported)
        save(_data.value)
    }

    fun resetAll() {
        _data.value = AppData()
        save(_data.value)
    }

    private fun load(): AppData {
        return try {
            if (!file.exists()) AppData()
            else withDefaults(json.decodeFromString(AppData.serializer(), file.readText()))
        } catch (e: Exception) {
            // Si algo va mal, conserva una copia del fichero dañado y arranca limpio.
            runCatching { file.copyTo(File(file.parentFile, "gym_data_corrupto_${System.currentTimeMillis()}.json")) }
            AppData()
        }
    }

    /** Añade ejercicios predefinidos nuevos que no existan (por si se amplía la lista). */
    private fun withDefaults(d: AppData): AppData {
        val ids = d.exercises.map { it.id }.toSet()
        val missing = DefaultData.exercises.filter { it.id !in ids }
        return if (missing.isEmpty()) d else d.copy(exercises = d.exercises + missing)
    }

    @Synchronized
    private fun save(d: AppData) {
        try {
            val tmp = File(file.parentFile, "gym_data.tmp")
            tmp.writeText(json.encodeToString(AppData.serializer(), d))
            if (!tmp.renameTo(file)) {
                file.delete(); tmp.renameTo(file)
            }
        } catch (_: Exception) {
        }
    }

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = false
        }
    }
}
