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
    // ---- Nutrición ----
    val profile: Profile = Profile(),
    val customFoods: List<Food> = emptyList(),
    val foodLog: List<FoodEntry> = emptyList(),
    /** Agua bebida por día (clave = día en formato epochDay). */
    val water: Map<Long, Int> = emptyMap(),
)

// ===================== Nutrición =====================

@Serializable
enum class Sex(val label: String) { MALE("Hombre"), FEMALE("Mujer") }

@Serializable
enum class Activity(val label: String, val description: String, val factor: Double) {
    SEDENTARY("Sedentario", "Trabajo sentado y poco movimiento fuera del gym", 1.2),
    LIGHT("Ligero", "Entrenas 1-3 días/semana, trabajo sentado", 1.375),
    MODERATE("Moderado", "Entrenas 3-5 días/semana o trabajo algo activo", 1.55),
    HIGH("Alto", "Entrenas 6-7 días/semana o trabajo de pie/físico", 1.725),
    VERY_HIGH("Muy alto", "Doble sesión o trabajo muy físico + entreno", 1.9),
}

@Serializable
enum class Goal(val label: String, val description: String, val defaultRate: Double) {
    LOSE("Perder grasa", "Déficit calórico manteniendo el músculo", -0.5),
    RECOMP("Recomposición", "Perder grasa y ganar músculo a la vez (ligero déficit)", -0.1),
    MAINTAIN("Mantener", "Mantener el peso y mejorar el rendimiento", 0.0),
    GAIN("Ganar músculo", "Superávit controlado (volumen limpio)", 0.25),
}

@Serializable
data class Profile(
    val sex: Sex = Sex.MALE,
    val birthYear: Int = 0,
    val heightCm: Double = 0.0,
    val neckCm: Double = 0.0,
    val waistCm: Double = 0.0,
    val hipCm: Double = 0.0,
    /** % de grasa medido (báscula, plicómetro...). Si es null se estima. */
    val bodyFatManual: Double? = null,
    val activity: Activity = Activity.MODERATE,
    val goal: Goal = Goal.MAINTAIN,
    /** kg por semana (negativo = perder). */
    val rateKgWeek: Double = 0.0,
    /** g de proteína por kg; 0 = automático según objetivo. */
    val proteinPerKg: Double = 0.0,
    /** % de calorías de grasa; 0 = automático (25 %). */
    val fatPercent: Double = 0.0,
    /** Ajuste manual o sugerido por el coach (kcal). */
    val calorieAdjust: Int = 0,
    val waterGoalMl: Int = 0,
)

@Serializable
enum class FoodCategory(val label: String) {
    PROTEIN("Carnes, pescados y huevos"),
    DAIRY("Lácteos"),
    CARBS("Cereales, pan y tubérculos"),
    LEGUMES("Legumbres"),
    FRUIT("Frutas"),
    VEG("Verduras"),
    FATS("Grasas y frutos secos"),
    SUPPLEMENTS("Suplementos"),
    DRINKS("Bebidas"),
    OTHER("Otros y platos"),
}

/** Valores nutricionales por cada 100 g. */
@Serializable
data class Food(
    val id: String = newId(),
    val name: String,
    val category: FoodCategory = FoodCategory.OTHER,
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val servingG: Double = 100.0,
    val servingName: String = "ración",
    val custom: Boolean = false,
)

@Serializable
enum class Meal(val label: String, val emoji: String) {
    BREAKFAST("Desayuno", "🍳"),
    LUNCH("Comida", "🍽️"),
    SNACK("Merienda", "🍎"),
    DINNER("Cena", "🌙"),
    PRE_WORKOUT("Pre-entreno", "⚡"),
    POST_WORKOUT("Post-entreno", "💪"),
}

@Serializable
data class FoodEntry(
    val id: String = newId(),
    /** Día en formato epochDay (LocalDate.toEpochDay()). */
    val day: Long,
    val meal: Meal,
    val foodId: String? = null,
    val name: String,
    val grams: Double,
    val kcal: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
)
