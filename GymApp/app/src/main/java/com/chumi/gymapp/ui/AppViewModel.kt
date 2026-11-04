package com.chumi.gymapp.ui

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chumi.gymapp.data.AppData
import com.chumi.gymapp.data.BodyEntry
import com.chumi.gymapp.data.Exercise
import com.chumi.gymapp.data.Repository
import com.chumi.gymapp.data.Routine
import com.chumi.gymapp.data.RoutineExercise
import com.chumi.gymapp.data.Settings
import com.chumi.gymapp.data.Stats
import com.chumi.gymapp.data.Workout
import com.chumi.gymapp.data.WorkoutExercise
import com.chumi.gymapp.data.WorkoutSet
import com.chumi.gymapp.data.newId
import com.chumi.gymapp.data.Food
import com.chumi.gymapp.data.FoodDatabase
import com.chumi.gymapp.data.FoodEntry
import com.chumi.gymapp.data.Meal
import com.chumi.gymapp.data.MealIdea
import com.chumi.gymapp.data.Nutrition
import com.chumi.gymapp.data.Profile
import com.chumi.gymapp.data.macrosFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RestTimer(val endAt: Long, val totalSec: Int)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repository(app)
    val data: StateFlow<AppData> = repo.data

    private val _rest = MutableStateFlow<RestTimer?>(null)
    val rest: StateFlow<RestTimer?> = _rest.asStateFlow()
    private var restJob: Job? = null

    private val active: Workout? get() = data.value.activeWorkout

    fun flush() = repo.flush()

    // ---------------- Ajustes ----------------

    fun updateSettings(transform: (Settings) -> Settings) =
        repo.update { it.copy(settings = transform(it.settings)) }

    // ---------------- Ejercicios ----------------

    fun saveExercise(e: Exercise) = repo.update { d ->
        val exists = d.exercises.any { it.id == e.id }
        d.copy(exercises = if (exists) d.exercises.map { if (it.id == e.id) e else it } else d.exercises + e)
    }

    fun deleteExercise(id: String) = repo.update { d ->
        d.copy(
            exercises = d.exercises.filterNot { it.id == id },
            routines = d.routines.map { r -> r.copy(exercises = r.exercises.filterNot { it.exerciseId == id }) },
        )
    }

    // ---------------- Rutinas ----------------

    fun saveRoutine(r: Routine) = repo.update { d ->
        val exists = d.routines.any { it.id == r.id }
        d.copy(routines = if (exists) d.routines.map { if (it.id == r.id) r else it } else d.routines + r)
    }

    fun deleteRoutine(id: String) = repo.update { d -> d.copy(routines = d.routines.filterNot { it.id == id }) }

    fun duplicateRoutine(id: String) = repo.update { d ->
        val r = d.routines.find { it.id == id } ?: return@update d
        d.copy(routines = d.routines + r.copy(id = newId(), name = r.name + " (copia)", lastUsed = 0))
    }

    fun routineFromWorkout(workoutId: String, name: String) = repo.update { d ->
        val w = d.workouts.find { it.id == workoutId } ?: return@update d
        val r = Routine(
            name = name,
            description = "Creada desde un entreno",
            exercises = w.exercises.map { we ->
                val working = we.sets.filter { it.done && !it.warmup }
                RoutineExercise(
                    exerciseId = we.exerciseId,
                    sets = working.size.coerceAtLeast(1),
                    reps = working.maxOfOrNull { it.reps } ?: 10,
                    weight = working.maxOfOrNull { it.weight } ?: 0.0,
                    restSec = we.restSec,
                )
            },
        )
        d.copy(routines = d.routines + r)
    }

    // ---------------- Entreno activo ----------------

    fun startEmptyWorkout() = repo.update { d ->
        d.copy(activeWorkout = Workout(name = "Entreno libre", bodyWeight = d.body.maxByOrNull { it.date }?.weight))
    }

    fun startRoutine(routineId: String) = repo.update { d ->
        val r = d.routines.find { it.id == routineId } ?: return@update d
        val workout = Workout(
            name = r.name,
            routineId = r.id,
            bodyWeight = d.body.maxByOrNull { it.date }?.weight,
            exercises = r.exercises.map { re -> buildExercise(d, re.exerciseId, re) },
        )
        d.copy(
            activeWorkout = workout,
            routines = d.routines.map { if (it.id == r.id) it.copy(lastUsed = System.currentTimeMillis()) else it },
        )
    }

    /** Crea un ejercicio de entreno precargando lo que hiciste la última vez. */
    private fun buildExercise(d: AppData, exerciseId: String, re: RoutineExercise?): WorkoutExercise {
        val prev = Stats.previousSets(d, exerciseId).filter { !it.warmup }
        val count = re?.sets ?: prev.size.coerceIn(1, 5).takeIf { prev.isNotEmpty() } ?: 3
        val sets = (0 until count).map { i ->
            val p = prev.getOrNull(i) ?: prev.lastOrNull()
            WorkoutSet(
                weight = p?.weight ?: re?.weight ?: 0.0,
                reps = re?.reps ?: p?.reps ?: 10,
                durationMin = p?.durationMin ?: 0.0,
                distanceKm = p?.distanceKm ?: 0.0,
            )
        }
        return WorkoutExercise(
            exerciseId = exerciseId,
            sets = sets,
            restSec = re?.restSec ?: d.settings.defaultRestSec,
        )
    }

    private fun updateActive(transform: (Workout) -> Workout) = repo.update { d ->
        val w = d.activeWorkout ?: return@update d
        d.copy(activeWorkout = transform(w))
    }

    private fun updateExercise(weId: String, transform: (WorkoutExercise) -> WorkoutExercise) = updateActive { w ->
        w.copy(exercises = w.exercises.map { if (it.id == weId) transform(it) else it })
    }

    fun renameActive(name: String) = updateActive { it.copy(name = name) }
    fun setActiveNotes(notes: String) = updateActive { it.copy(notes = notes) }

    fun addExercisesToActive(ids: List<String>) = repo.update { d ->
        val w = d.activeWorkout ?: return@update d
        d.copy(activeWorkout = w.copy(exercises = w.exercises + ids.map { buildExercise(d, it, null) }))
    }

    fun removeExercise(weId: String) = updateActive { w -> w.copy(exercises = w.exercises.filterNot { it.id == weId }) }

    fun moveExercise(weId: String, delta: Int) = updateActive { w ->
        val list = w.exercises.toMutableList()
        val i = list.indexOfFirst { it.id == weId }
        val j = i + delta
        if (i < 0 || j !in list.indices) return@updateActive w
        val tmp = list[i]; list[i] = list[j]; list[j] = tmp
        w.copy(exercises = list)
    }

    fun replaceExercise(weId: String, newExerciseId: String) = repo.update { d ->
        val w = d.activeWorkout ?: return@update d
        val fresh = buildExercise(d, newExerciseId, null)
        d.copy(activeWorkout = w.copy(exercises = w.exercises.map {
            if (it.id == weId) fresh.copy(id = it.id, sets = it.sets.map { s -> s.copy(done = false) }) else it
        }))
    }

    fun addSet(weId: String) = updateExercise(weId) { we ->
        val last = we.sets.lastOrNull()
        we.copy(sets = we.sets + (last?.copy(id = newId(), done = false, warmup = false) ?: WorkoutSet()))
    }

    fun removeSet(weId: String, setId: String) = updateExercise(weId) { we -> we.copy(sets = we.sets.filterNot { it.id == setId }) }

    fun updateSet(weId: String, setId: String, transform: (WorkoutSet) -> WorkoutSet) = updateExercise(weId) { we ->
        we.copy(sets = we.sets.map { if (it.id == setId) transform(it) else it })
    }

    fun setExerciseRest(weId: String, sec: Int) = updateExercise(weId) { it.copy(restSec = sec) }
    fun setExerciseNotes(weId: String, notes: String) = updateExercise(weId) { it.copy(notes = notes) }

    fun toggleSetDone(weId: String, setId: String) {
        val we = active?.exercises?.find { it.id == weId } ?: return
        val set = we.sets.find { it.id == setId } ?: return
        val nowDone = !set.done
        updateSet(weId, setId) { it.copy(done = nowDone) }
        if (nowDone && we.restSec > 0) startRest(we.restSec)
    }

    /** Termina el entreno. Solo se guardan las series marcadas como hechas. Devuelve el id o null si estaba vacío. */
    fun finishWorkout(): String? {
        val w = active ?: return null
        val cleaned = w.copy(
            endTime = System.currentTimeMillis(),
            exercises = w.exercises.map { e -> e.copy(sets = e.sets.filter { it.done }) }.filter { it.sets.isNotEmpty() },
        )
        stopRest()
        if (cleaned.exercises.isEmpty()) {
            repo.update { it.copy(activeWorkout = null) }
            return null
        }
        repo.update { it.copy(activeWorkout = null, workouts = it.workouts + cleaned) }
        repo.flush()
        return cleaned.id
    }

    fun discardWorkout() {
        stopRest()
        repo.update { it.copy(activeWorkout = null) }
    }

    // ---------------- Historial ----------------

    fun deleteWorkout(id: String) = repo.update { d -> d.copy(workouts = d.workouts.filterNot { it.id == id }) }

    fun updateWorkout(id: String, transform: (Workout) -> Workout) = repo.update { d ->
        d.copy(workouts = d.workouts.map { if (it.id == id) transform(it) else it })
    }

    // ---------------- Medidas corporales ----------------

    fun addBodyEntry(e: BodyEntry) = repo.update { d -> d.copy(body = (d.body.filterNot { it.id == e.id } + e).sortedBy { it.date }) }
    fun deleteBodyEntry(id: String) = repo.update { d -> d.copy(body = d.body.filterNot { it.id == id }) }

    // ---------------- Nutrición ----------------

    fun updateProfile(transform: (Profile) -> Profile) = repo.update { it.copy(profile = transform(it.profile)) }

    /** Guarda el peso de hoy (si ya hay registro hoy, lo sustituye). */
    fun setTodayWeight(kg: Double) = repo.update { d ->
        if (kg <= 0) return@update d
        val today = java.time.LocalDate.now()
        val existing = d.body.find { Stats.toDate(it.date) == today }
        val entry = existing?.copy(weight = kg) ?: BodyEntry(weight = kg)
        d.copy(body = (d.body.filterNot { it.id == entry.id } + entry).sortedBy { it.date })
    }

    fun addFood(day: Long, meal: Meal, food: Food, grams: Double) = repo.update { d ->
        val m = food.macrosFor(grams)
        d.copy(foodLog = d.foodLog + FoodEntry(
            day = day, meal = meal, foodId = food.id, name = food.name, grams = grams,
            kcal = m.kcal, protein = m.protein, carbs = m.carbs, fat = m.fat, fiber = m.fiber,
        ))
    }

    fun addQuick(day: Long, meal: Meal, name: String, kcal: Double, protein: Double, carbs: Double, fat: Double) = repo.update { d ->
        d.copy(foodLog = d.foodLog + FoodEntry(
            day = day, meal = meal, name = name.ifBlank { "Añadido rápido" }, grams = 0.0,
            kcal = kcal, protein = protein, carbs = carbs, fat = fat,
        ))
    }

    fun addIdea(day: Long, meal: Meal, idea: MealIdea) = repo.update { d ->
        val entries = idea.ingredients.mapNotNull { (id, g) ->
            val f = FoodDatabase.byId(id) ?: return@mapNotNull null
            val m = f.macrosFor(g)
            FoodEntry(day = day, meal = meal, foodId = f.id, name = f.name, grams = g,
                kcal = m.kcal, protein = m.protein, carbs = m.carbs, fat = m.fat, fiber = m.fiber)
        }
        d.copy(foodLog = d.foodLog + entries)
    }

    fun updateEntryGrams(id: String, grams: Double) = repo.update { d ->
        d.copy(foodLog = d.foodLog.map { e ->
            if (e.id != id) return@map e
            val f = Nutrition.findFood(d, e.foodId)
            if (f != null) {
                val m = f.macrosFor(grams)
                e.copy(grams = grams, kcal = m.kcal, protein = m.protein, carbs = m.carbs, fat = m.fat, fiber = m.fiber)
            } else if (e.grams > 0) {
                val k = grams / e.grams
                e.copy(grams = grams, kcal = e.kcal * k, protein = e.protein * k, carbs = e.carbs * k, fat = e.fat * k, fiber = e.fiber * k)
            } else e
        })
    }

    fun moveEntry(id: String, meal: Meal) = repo.update { d -> d.copy(foodLog = d.foodLog.map { if (it.id == id) it.copy(meal = meal) else it }) }

    fun deleteEntry(id: String) = repo.update { d -> d.copy(foodLog = d.foodLog.filterNot { it.id == id }) }

    /** Copia las comidas de un día (o solo una comida) a otro día. */
    fun copyMeals(fromDay: Long, toDay: Long, meal: Meal? = null) = repo.update { d ->
        val src = d.foodLog.filter { it.day == fromDay && (meal == null || it.meal == meal) }
        d.copy(foodLog = d.foodLog + src.map { it.copy(id = newId(), day = toDay, createdAt = System.currentTimeMillis()) })
    }

    fun saveCustomFood(f: Food) = repo.update { d ->
        val exists = d.customFoods.any { it.id == f.id }
        d.copy(customFoods = if (exists) d.customFoods.map { if (it.id == f.id) f else it } else d.customFoods + f)
    }

    fun deleteCustomFood(id: String) = repo.update { d -> d.copy(customFoods = d.customFoods.filterNot { it.id == id }) }

    fun addWater(day: Long, ml: Int) = repo.update { d ->
        val now = ((d.water[day] ?: 0) + ml).coerceAtLeast(0)
        d.copy(water = d.water + (day to now))
    }

    fun applyCalorieAdjust(delta: Int) = updateProfile { it.copy(calorieAdjust = it.calorieAdjust + delta) }

    // ---------------- Temporizador de descanso ----------------

    fun startRest(sec: Int) {
        _rest.value = RestTimer(System.currentTimeMillis() + sec * 1000L, sec)
        scheduleRestEnd()
    }

    fun adjustRest(deltaSec: Int) {
        val r = _rest.value ?: return
        val newEnd = r.endAt + deltaSec * 1000L
        if (newEnd <= System.currentTimeMillis()) {
            stopRest(); return
        }
        _rest.value = r.copy(endAt = newEnd, totalSec = (r.totalSec + deltaSec).coerceAtLeast(1))
        scheduleRestEnd()
    }

    fun stopRest() {
        restJob?.cancel()
        _rest.value = null
    }

    private fun scheduleRestEnd() {
        restJob?.cancel()
        restJob = viewModelScope.launch {
            val r = _rest.value ?: return@launch
            delay((r.endAt - System.currentTimeMillis()).coerceAtLeast(0))
            _rest.value = null
            alert()
        }
    }

    private fun alert() {
        val s = data.value.settings
        val ctx = getApplication<Application>()
        if (s.vibrate) runCatching {
            val vib: Vibrator = if (Build.VERSION.SDK_INT >= 31) {
                (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION") ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300, 150, 500), -1))
        }
        if (s.sound) runCatching {
            val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
            tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
            viewModelScope.launch { delay(1000); tg.release() }
        }
    }

    // ---------------- Copia de seguridad ----------------

    fun exportTo(uri: Uri, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = withContext(Dispatchers.IO) {
            runCatching {
                getApplication<Application>().contentResolver.openOutputStream(uri, "wt")!!.use {
                    it.write(repo.exportJson().toByteArray())
                }
            }.isSuccess
        }
        onDone(ok)
    }

    fun importFrom(uri: Uri, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = withContext(Dispatchers.IO) {
            runCatching {
                val text = getApplication<Application>().contentResolver.openInputStream(uri)!!.use { it.readBytes().decodeToString() }
                repo.importJson(text)
            }.isSuccess
        }
        onDone(ok)
    }

    fun resetAll() {
        stopRest()
        repo.resetAll()
    }
}
