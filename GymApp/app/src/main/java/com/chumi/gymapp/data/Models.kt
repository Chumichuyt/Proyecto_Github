package com.chumi.gymapp.data

import kotlinx.serialization.Serializable
import java.util.UUID

fun newId(): String = UUID.randomUUID().toString()

@Serializable
enum class MuscleGroup(val label: String, val emoji: String) {
    CHEST("Pecho", "🫁"),
    BACK("Espalda", "🔙"),
    SHOULDERS("Hombros", "🏋️"),
    BICEPS("Bíceps", "💪"),
    TRICEPS("Tríceps", "🦾"),
    LEGS("Piernas", "🦵"),
    GLUTES("Glúteos", "🍑"),
    CORE("Abdomen", "🧱"),
    CARDIO("Cardio", "❤️"),
    FULL_BODY("Cuerpo completo", "🔥"),
}

@Serializable
enum class ExerciseType(val label: String) {
    WEIGHT_REPS("Peso y repeticiones"),
    BODYWEIGHT("Peso corporal (+ lastre)"),
    CARDIO("Tiempo y distancia"),
}

@Serializable
data class Exercise(
    val id: String = newId(),
    val name: String,
    val muscle: MuscleGroup,
    val type: ExerciseType = ExerciseType.WEIGHT_REPS,
    val equipment: String = "",
    val notes: String = "",
    val custom: Boolean = false,
)

@Serializable
data class RoutineExercise(
    val exerciseId: String,
    val sets: Int = 3,
    val reps: Int = 10,
    val weight: Double = 0.0,
    val restSec: Int = 90,
)

@Serializable
data class Routine(
    val id: String = newId(),
    val name: String,
    val description: String = "",
    val exercises: List<RoutineExercise> = emptyList(),
    val lastUsed: Long = 0L,
)

@Serializable
data class WorkoutSet(
    val id: String = newId(),
    val weight: Double = 0.0,
    val reps: Int = 0,
    val durationMin: Double = 0.0,
    val distanceKm: Double = 0.0,
    val warmup: Boolean = false,
    val done: Boolean = false,
)

@Serializable
data class WorkoutExercise(
    val id: String = newId(),
    val exerciseId: String,
    val sets: List<WorkoutSet> = listOf(WorkoutSet()),
    val restSec: Int = 90,
    val notes: String = "",
)

@Serializable
data class Workout(
    val id: String = newId(),
    val name: String,
    val routineId: String? = null,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val exercises: List<WorkoutExercise> = emptyList(),
    val notes: String = "",
    val bodyWeight: Double? = null,
    val rating: Int = 0,
)

@Serializable
data class BodyEntry(
    val id: String = newId(),
    val date: Long = System.currentTimeMillis(),
    val weight: Double,
    val bodyFat: Double? = null,
    val waist: Double? = null,
    val chest: Double? = null,
    val arm: Double? = null,
    val thigh: Double? = null,
)

@Serializable
enum class ThemeMode(val label: String) { SYSTEM("Sistema"), DARK("Oscuro"), LIGHT("Claro") }

@Serializable
data class Settings(
    val defaultRestSec: Int = 90,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val dynamicColor: Boolean = false,
    val countBodyweight: Boolean = true,
    val weeklyGoal: Int = 4,
    val keepScreenOn: Boolean = true,
    val vibrate: Boolean = true,
    val sound: Boolean = true,
    val barWeight: Double = 20.0,
    val userName: String = "",
)

@Serializable
data class AppData(
    val version: Int = 1,
    val exercises: List<Exercise> = DefaultData.exercises,
    val routines: List<Routine> = DefaultData.routines,
    val workouts: List<Workout> = emptyList(),
    val body: List<BodyEntry> = emptyList(),
    val settings: Settings = Settings(),
    val activeWorkout: Workout? = null,
)
