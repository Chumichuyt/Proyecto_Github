package com.chumi.gymapp.data

import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.roundToInt

data class Macros(
    val kcal: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
) {
    operator fun plus(o: Macros) = Macros(kcal + o.kcal, protein + o.protein, carbs + o.carbs, fat + o.fat, fiber + o.fiber)
}

fun Food.macrosFor(grams: Double): Macros {
    val k = grams / 100.0
    return Macros(kcal * k, protein * k, carbs * k, fat * k, fiber * k)
}

fun FoodEntry.macros() = Macros(kcal, protein, carbs, fat, fiber)

data class BmiCategory(val label: String, val advice: String, val level: Int) // level 0..5

data class Targets(
    val kcal: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val fiber: Int,
    val waterMl: Int,
    val bmr: Int,
    val tdee: Int,
    val warnings: List<String>,
)

/** Cálculos de nutrición y composición corporal. */
object Nutrition {

    const val KCAL_PER_KG = 7700.0

    fun today(): Long = LocalDate.now().toEpochDay()

    fun age(p: Profile): Int = if (p.birthYear <= 0) 0 else LocalDate.now().year - p.birthYear

    fun currentWeight(d: AppData): Double? = d.body.maxByOrNull { it.date }?.weight

    fun allFoods(d: AppData): List<Food> = d.customFoods + FoodDatabase.foods

    fun findFood(d: AppData, id: String?): Food? = id?.let { i -> d.customFoods.find { it.id == i } ?: FoodDatabase.byId(i) }

    // ---------- Composición corporal ----------

    fun bmi(weightKg: Double, heightCm: Double): Double? =
        if (weightKg <= 0 || heightCm <= 0) null else weightKg / ((heightCm / 100) * (heightCm / 100))

    fun bmiCategory(bmi: Double): BmiCategory = when {
        bmi < 16 -> BmiCategory("Delgadez severa", "Peso muy bajo: consulta con un profesional sanitario.", 0)
        bmi < 18.5 -> BmiCategory("Bajo peso", "Te vendría bien un superávit calórico y entrenar fuerza.", 1)
        bmi < 25 -> BmiCategory("Peso normal", "Rango saludable. ¡Buen punto de partida!", 2)
        bmi < 30 -> BmiCategory("Sobrepeso", "Si tienes mucho músculo puede ser normal. Mira tu % de grasa y cintura.", 3)
        bmi < 35 -> BmiCategory("Obesidad grado I", "Un déficit moderado y entrenar fuerza dan muy buenos resultados.", 4)
        bmi < 40 -> BmiCategory("Obesidad grado II", "Te recomiendo apoyarte en un dietista-nutricionista y tu médico.", 5)
        else -> BmiCategory("Obesidad grado III", "Te recomiendo apoyarte en un dietista-nutricionista y tu médico.", 5)
    }

    /** Rango de peso con IMC entre 18,5 y 24,9. */
    fun healthyWeightRange(heightCm: Double): Pair<Double, Double>? {
        if (heightCm <= 0) return null
        val m2 = (heightCm / 100) * (heightCm / 100)
        return 18.5 * m2 to 24.9 * m2
    }

    /** Fórmula de la Marina de EE. UU. (medidas en cm). */
    fun bodyFatNavy(p: Profile): Double? {
        val h = p.heightCm
        if (h <= 0 || p.neckCm <= 0 || p.waistCm <= 0) return null
        val bf = if (p.sex == Sex.MALE) {
            if (p.waistCm <= p.neckCm) return null
            495 / (1.0324 - 0.19077 * log10(p.waistCm - p.neckCm) + 0.15456 * log10(h)) - 450
        } else {
            if (p.hipCm <= 0 || p.waistCm + p.hipCm <= p.neckCm) return null
            495 / (1.29579 - 0.35004 * log10(p.waistCm + p.hipCm - p.neckCm) + 0.22100 * log10(h)) - 450
        }
        return bf.takeIf { it in 2.0..60.0 }
    }

    /** % de grasa a usar: el medido a mano, si no el último registro, si no la estimación Navy. */
    fun bodyFat(d: AppData): Double? =
        d.profile.bodyFatManual
            ?: d.body.filter { it.bodyFat != null }.maxByOrNull { it.date }?.bodyFat
            ?: bodyFatNavy(d.profile)

    fun bodyFatCategory(bf: Double, sex: Sex): String {
        val ranges = if (sex == Sex.MALE) listOf(6.0, 14.0, 18.0, 25.0) else listOf(14.0, 21.0, 25.0, 32.0)
        return when {
            bf < ranges[0] -> "Grasa esencial / competición"
            bf < ranges[1] -> "Atlético"
            bf < ranges[2] -> "En forma"
            bf < ranges[3] -> "Promedio"
            else -> "Elevado"
        }
    }

    /** Índice de masa libre de grasa normalizado (FFMI). >25 es difícil sin ayudas. */
    fun ffmi(weightKg: Double, heightCm: Double, bodyFat: Double): Double {
        val h = heightCm / 100
        val lbm = weightKg * (1 - bodyFat / 100)
        return lbm / (h * h) + 6.1 * (1.8 - h)
    }

    /** Índice cintura-altura: por debajo de 0,5 es saludable. */
    fun waistToHeight(p: Profile): Double? = if (p.waistCm > 0 && p.heightCm > 0) p.waistCm / p.heightCm else null

    // ---------- Metabolismo ----------

    /** Metabolismo basal: Katch-McArdle si se conoce la grasa; si no, Mifflin-St Jeor. */
    fun bmr(weightKg: Double, p: Profile, bodyFat: Double?): Double? {
        if (bodyFat != null && weightKg > 0) return 370 + 21.6 * weightKg * (1 - bodyFat / 100)
        val a = age(p)
        if (weightKg <= 0 || p.heightCm <= 0 || a <= 0) return null
        val base = 10 * weightKg + 6.25 * p.heightCm - 5 * a
        return if (p.sex == Sex.MALE) base + 5 else base - 161
    }

    fun autoProteinPerKg(goal: Goal): Double = when (goal) {
        Goal.LOSE, Goal.RECOMP -> 2.2
        Goal.MAINTAIN -> 1.8
        Goal.GAIN -> 2.0
    }

    /** Objetivos diarios. Devuelve null si faltan datos (peso, altura o edad). */
    fun targets(d: AppData): Targets? {
        val p = d.profile
        val w = currentWeight(d) ?: return null
        if (p.heightCm <= 0 || age(p) <= 0) return null
        val bf = bodyFat(d)
        val bmr = bmr(w, p, bf) ?: return null
        val tdee = bmr * p.activity.factor
        val warnings = mutableListOf<String>()

        // Ritmo: como máximo un 1% del peso por semana
        val maxRate = w * 0.01
        var rate = p.rateKgWeek
        if (abs(rate) > maxRate) {
            rate = if (rate < 0) -maxRate else maxRate
            warnings += "He limitado el ritmo a ${formatWeight(Math.round(maxRate * 100) / 100.0)} kg/semana (1% de tu peso) para no perder músculo."
        }
        var kcal = tdee + rate * KCAL_PER_KG / 7 + p.calorieAdjust
        val floor = max(if (p.sex == Sex.MALE) 1500.0 else 1200.0, bmr)
        if (kcal < floor) {
            kcal = floor
            warnings += "No bajes de ${floor.roundToInt()} kcal: comer menos que tu metabolismo basal hace perder músculo y rendimiento."
        }

        // Proteína: si el IMC es muy alto se calcula sobre el peso con IMC 25 (evita cifras exageradas)
        val bmiNow = bmi(w, p.heightCm) ?: 22.0
        val refWeight = if (bmiNow > 30 && bf == null) 25 * (p.heightCm / 100) * (p.heightCm / 100) else w
        val ppk = if (p.proteinPerKg > 0) p.proteinPerKg else autoProteinPerKg(p.goal)
        val protein = ppk * refWeight

        val fatPct = if (p.fatPercent > 0) p.fatPercent / 100 else 0.25
        val fat = max(kcal * fatPct / 9, 0.6 * w)
        var carbs = (kcal - protein * 4 - fat * 9) / 4
        if (carbs < 50) {
            warnings += "Te quedan muy pocos carbohidratos: baja algo la grasa o sube un poco las calorías para rendir en el gym."
            carbs = max(carbs, 0.0)
        }
        val water = if (p.waterGoalMl > 0) p.waterGoalMl else (35 * w + 500).roundToInt()

        return Targets(
            kcal = (kcal / 10).roundToInt() * 10,
            protein = protein.roundToInt(),
            carbs = carbs.roundToInt(),
            fat = fat.roundToInt(),
            fiber = (14 * kcal / 1000).roundToInt(),
            waterMl = (water / 50.0).roundToInt() * 50,
            bmr = bmr.roundToInt(),
            tdee = tdee.roundToInt(),
            warnings = warnings,
        )
    }

    // ---------- Diario ----------

    fun dayEntries(d: AppData, day: Long) = d.foodLog.filter { it.day == day }

    fun dayTotals(d: AppData, day: Long): Macros = dayEntries(d, day).fold(Macros()) { acc, e -> acc + e.macros() }

    fun recentFoods(d: AppData, limit: Int = 25): List<Food> {
        val ids = d.foodLog.sortedByDescending { it.createdAt }.mapNotNull { it.foodId }.distinct()
        return ids.mapNotNull { findFood(d, it) }.take(limit)
    }

    /** Última cantidad usada de un alimento, para proponerla por defecto. */
    fun lastGrams(d: AppData, foodId: String): Double? =
        d.foodLog.filter { it.foodId == foodId }.maxByOrNull { it.createdAt }?.grams

    // ---------- Progresión y coach ----------

    data class DayPoint(val day: Long, val totals: Macros)

    fun loggedDays(d: AppData, lastDays: Int): List<DayPoint> {
        val from = today() - lastDays + 1
        return d.foodLog.filter { it.day >= from }.groupBy { it.day }
            .map { (day, es) -> DayPoint(day, es.fold(Macros()) { a, e -> a + e.macros() }) }
            .sortedBy { it.day }
    }

    /** Tendencia de peso en kg/semana (regresión lineal de los últimos [days] días). */
    fun weightTrendPerWeek(d: AppData, days: Int = 21): Double? {
        val from = System.currentTimeMillis() - days * 86_400_000L
        val pts = d.body.filter { it.date >= from }
        if (pts.size < 3) return null
        val span = pts.maxOf { it.date } - pts.minOf { it.date }
        if (span < 9 * 86_400_000L) return null
        val xs = pts.map { it.date / 86_400_000.0 }
        val ys = pts.map { it.weight }
        val mx = xs.average()
        val my = ys.average()
        val num = xs.indices.sumOf { (xs[it] - mx) * (ys[it] - my) }
        val den = xs.indices.sumOf { (xs[it] - mx) * (xs[it] - mx) }
        if (den == 0.0) return null
        return num / den * 7
    }

    data class Coach(
        val avgIntake: Int?,
        val trendKgWeek: Double?,
        val realTdee: Int?,
        val suggestedAdjust: Int,
        val message: String,
    )

    fun coach(d: AppData): Coach {
        val days = loggedDays(d, 21).filter { it.totals.kcal > 300 } // ignora días medio vacíos
        val avg = if (days.size >= 4) days.map { it.totals.kcal }.average() else null
        val trend = weightTrendPerWeek(d)
        val realTdee = if (avg != null && trend != null && days.size >= 7) (avg - trend * KCAL_PER_KG / 7).roundToInt() else null
        val target = d.profile.rateKgWeek
        var adjust = 0
        val msg: String
        when {
            trend == null -> msg = "Pésate al menos 3 veces en 10 días o más (mejor cada mañana, en ayunas) y te diré si vas al ritmo correcto."
            d.profile.goal == Goal.MAINTAIN && abs(trend) < 0.15 -> msg = "Tu peso está estable. ¡Mantenimiento clavado!"
            else -> {
                val diff = target - trend // positivo = deberías subir más / bajar menos
                if (abs(diff) < 0.15) {
                    msg = "Vas al ritmo que buscas (${signed(trend)} kg/sem). Sigue así, no cambies nada."
                } else {
                    adjust = ((diff * KCAL_PER_KG / 7) / 50).roundToInt() * 50
                    adjust = adjust.coerceIn(-300, 300)
                    msg = if (adjust > 0) {
                        "Estás cambiando ${signed(trend)} kg/sem y tu objetivo es ${signed(target)} kg/sem. Te propongo subir $adjust kcal al día."
                    } else {
                        "Estás cambiando ${signed(trend)} kg/sem y tu objetivo es ${signed(target)} kg/sem. Te propongo bajar ${-adjust} kcal al día."
                    }
                }
            }
        }
        return Coach(avg?.roundToInt(), trend, realTdee, adjust, msg)
    }

    private fun signed(v: Double): String = (if (v > 0) "+" else "") + "%.2f".format(v).replace('.', ',')
}
