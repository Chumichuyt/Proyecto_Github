package com.chumi.gymapp.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** Cálculos de estadísticas a partir de los datos guardados. */
object Stats {

    private val zone: ZoneId get() = ZoneId.systemDefault()

    fun toDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    fun weekStart(d: LocalDate): LocalDate = d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    fun startOfDayMillis(d: LocalDate): Long = d.atStartOfDay(zone).toInstant().toEpochMilli()

    /** Fórmula de Epley para estimar la repetición máxima (1RM). */
    fun estimated1RM(weight: Double, reps: Int): Double = when {
        reps <= 0 || weight <= 0 -> 0.0
        reps == 1 -> weight
        else -> weight * (1 + reps / 30.0)
    }

    /** Peso corporal más reciente conocido en una fecha. */
    fun bodyWeightAt(data: AppData, millis: Long): Double? =
        data.body.filter { it.date <= millis }.maxByOrNull { it.date }?.weight
            ?: data.body.minByOrNull { it.date }?.weight

    fun setVolume(set: WorkoutSet, type: ExerciseType, bodyWeight: Double?, countBodyweight: Boolean): Double {
        if (!set.done) return 0.0
        return when (type) {
            ExerciseType.WEIGHT_REPS -> set.weight * set.reps
            ExerciseType.BODYWEIGHT -> {
                val bw = if (countBodyweight) bodyWeight ?: 0.0 else 0.0
                (bw + set.weight) * set.reps
            }
            ExerciseType.CARDIO -> 0.0
        }
    }

    fun exerciseVolume(data: AppData, we: WorkoutExercise, bodyWeight: Double?): Double {
        val type = data.exercises.find { it.id == we.exerciseId }?.type ?: ExerciseType.WEIGHT_REPS
        return we.sets.sumOf { setVolume(it, type, bodyWeight, data.settings.countBodyweight) }
    }

    fun workoutVolume(data: AppData, w: Workout): Double {
        val bw = w.bodyWeight ?: bodyWeightAt(data, w.startTime)
        return w.exercises.sumOf { exerciseVolume(data, it, bw) }
    }

    fun completedSets(w: Workout): Int = w.exercises.sumOf { e -> e.sets.count { it.done } }
    fun totalReps(w: Workout): Int = w.exercises.sumOf { e -> e.sets.filter { it.done }.sumOf { it.reps } }
    fun durationMin(w: Workout): Long = ((w.endTime ?: System.currentTimeMillis()) - w.startTime) / 60000

    fun finished(data: AppData): List<Workout> = data.workouts.sortedByDescending { it.startTime }

    fun lifetimeVolume(data: AppData): Double = data.workouts.sumOf { workoutVolume(data, it) }

    // ---------- Progreso por ejercicio ----------

    data class ExercisePoint(
        val date: Long,
        val workoutId: String,
        val maxWeight: Double,
        val best1RM: Double,
        val volume: Double,
        val bestSet: WorkoutSet?,
        val totalReps: Int,
        val durationMin: Double,
        val distanceKm: Double,
    )

    fun exerciseHistory(data: AppData, exerciseId: String): List<ExercisePoint> {
        val ex = data.exercises.find { it.id == exerciseId }
        val type = ex?.type ?: ExerciseType.WEIGHT_REPS
        return data.workouts.sortedBy { it.startTime }.mapNotNull { w ->
            val sets = w.exercises.filter { it.exerciseId == exerciseId }
                .flatMap { it.sets }.filter { it.done && !it.warmup }
            if (sets.isEmpty()) return@mapNotNull null
            val bw = w.bodyWeight ?: bodyWeightAt(data, w.startTime)
            val effective = { s: WorkoutSet ->
                if (type == ExerciseType.BODYWEIGHT && data.settings.countBodyweight) s.weight + (bw ?: 0.0) else s.weight
            }
            val best = sets.maxByOrNull { estimated1RM(effective(it), it.reps) }
            ExercisePoint(
                date = w.startTime,
                workoutId = w.id,
                maxWeight = sets.maxOf { it.weight },
                best1RM = sets.maxOf { estimated1RM(effective(it), it.reps) },
                volume = sets.sumOf { setVolume(it, type, bw, data.settings.countBodyweight) },
                bestSet = best,
                totalReps = sets.sumOf { it.reps },
                durationMin = sets.sumOf { it.durationMin },
                distanceKm = sets.sumOf { it.distanceKm },
            )
        }
    }

    /** Series de la última vez que se hizo el ejercicio (excluyendo un entreno concreto). */
    fun previousSets(data: AppData, exerciseId: String, excludeWorkoutId: String? = null): List<WorkoutSet> {
        val w = data.workouts.filter { it.id != excludeWorkoutId }
            .sortedByDescending { it.startTime }
            .firstOrNull { wk -> wk.exercises.any { it.exerciseId == exerciseId && it.sets.any { s -> s.done } } }
            ?: return emptyList()
        return w.exercises.filter { it.exerciseId == exerciseId }.flatMap { it.sets }.filter { it.done }
    }

    data class PersonalRecords(
        val maxWeight: Double = 0.0,
        val maxWeightReps: Int = 0,
        val best1RM: Double = 0.0,
        val maxVolume: Double = 0.0,
        val maxReps: Int = 0,
        val timesDone: Int = 0,
    )

    fun records(data: AppData, exerciseId: String): PersonalRecords {
        val h = exerciseHistory(data, exerciseId)
        if (h.isEmpty()) return PersonalRecords()
        val allSets = data.workouts.flatMap { w -> w.exercises.filter { it.exerciseId == exerciseId }.flatMap { it.sets } }
            .filter { it.done && !it.warmup }
        val heaviest = allSets.maxByOrNull { it.weight }
        return PersonalRecords(
            maxWeight = heaviest?.weight ?: 0.0,
            maxWeightReps = heaviest?.reps ?: 0,
            best1RM = h.maxOf { it.best1RM },
            maxVolume = h.maxOf { it.volume },
            maxReps = allSets.maxOfOrNull { it.reps } ?: 0,
            timesDone = h.size,
        )
    }

    /** Récords nuevos conseguidos en un entreno (comparado con todos los anteriores). */
    data class NewRecord(val exerciseName: String, val kind: String, val value: String)

    fun newRecordsIn(data: AppData, workout: Workout): List<NewRecord> {
        val before = data.copy(workouts = data.workouts.filter { it.startTime < workout.startTime && it.id != workout.id })
        val withIt = data.copy(workouts = before.workouts + workout)
        val result = mutableListOf<NewRecord>()
        workout.exercises.map { it.exerciseId }.distinct().forEach { exId ->
            val ex = data.exercises.find { it.id == exId } ?: return@forEach
            if (ex.type == ExerciseType.CARDIO) return@forEach
            val old = records(before, exId)
            val now = records(withIt, exId)
            if (old.timesDone == 0) return@forEach // primera vez: no cuenta como récord
            if (now.maxWeight > old.maxWeight && now.maxWeight > 0)
                result += NewRecord(ex.name, "Peso máximo", "${formatWeight(now.maxWeight)} kg × ${now.maxWeightReps}")
            if (now.best1RM > old.best1RM + 0.01)
                result += NewRecord(ex.name, "1RM estimado", "${formatWeight(Math.round(now.best1RM * 10) / 10.0)} kg")
            if (now.maxVolume > old.maxVolume + 0.01)
                result += NewRecord(ex.name, "Volumen en una sesión", "${formatKg(now.maxVolume)} kg")
        }
        return result
    }

    // ---------- Resúmenes globales ----------

    data class WeekBucket(val start: LocalDate, val volume: Double, val workouts: Int, val sets: Int)

    fun weekly(data: AppData, weeks: Int = 12): List<WeekBucket> {
        val thisWeek = weekStart(LocalDate.now(zone))
        val starts = (weeks - 1 downTo 0).map { thisWeek.minusWeeks(it.toLong()) }
        val byWeek = data.workouts.groupBy { weekStart(toDate(it.startTime)) }
        return starts.map { s ->
            val ws = byWeek[s].orEmpty()
            WeekBucket(s, ws.sumOf { workoutVolume(data, it) }, ws.size, ws.sumOf { completedSets(it) })
        }
    }

    fun workoutsThisWeek(data: AppData): Int {
        val s = weekStart(LocalDate.now(zone))
        return data.workouts.count { !toDate(it.startTime).isBefore(s) }
    }

    fun volumeThisWeek(data: AppData): Double {
        val s = weekStart(LocalDate.now(zone))
        return data.workouts.filter { !toDate(it.startTime).isBefore(s) }.sumOf { workoutVolume(data, it) }
    }

    /** Semanas seguidas (incluyendo la actual si ya tiene entreno) con al menos un entreno. */
    fun weekStreak(data: AppData): Int {
        val weeks = data.workouts.map { weekStart(toDate(it.startTime)) }.toSet()
        var w = weekStart(LocalDate.now(zone))
        if (w !in weeks) w = w.minusWeeks(1)
        var streak = 0
        while (w in weeks) {
            streak++; w = w.minusWeeks(1)
        }
        return streak
    }

    /** Días seguidos sin fallar más de 2 días de descanso no se cuenta: aquí solo días entrenados por fecha. */
    fun trainingDays(data: AppData): Set<LocalDate> = data.workouts.map { toDate(it.startTime) }.toSet()

    fun muscleSets(data: AppData, days: Int = 30): List<Pair<MuscleGroup, Int>> {
        val from = System.currentTimeMillis() - days * 86_400_000L
        val counts = mutableMapOf<MuscleGroup, Int>()
        data.workouts.filter { it.startTime >= from }.forEach { w ->
            w.exercises.forEach { we ->
                val m = data.exercises.find { it.id == we.exerciseId }?.muscle ?: return@forEach
                val n = we.sets.count { it.done && !it.warmup }
                if (n > 0) counts[m] = (counts[m] ?: 0) + n
            }
        }
        return counts.entries.sortedByDescending { it.value }.map { it.key to it.value }
    }

    fun mostDoneExercises(data: AppData, limit: Int = 5): List<Pair<Exercise, Int>> {
        val counts = mutableMapOf<String, Int>()
        data.workouts.forEach { w -> w.exercises.map { it.exerciseId }.distinct().forEach { counts[it] = (counts[it] ?: 0) + 1 } }
        return counts.entries.sortedByDescending { it.value }.mapNotNull { (id, n) ->
            data.exercises.find { it.id == id }?.let { it to n }
        }.take(limit)
    }

    fun averageDurationMin(data: AppData): Long =
        if (data.workouts.isEmpty()) 0 else data.workouts.map { durationMin(it) }.average().toLong()
}
