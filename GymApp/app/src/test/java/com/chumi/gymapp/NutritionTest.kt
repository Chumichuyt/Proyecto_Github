package com.chumi.gymapp

import com.chumi.gymapp.data.Activity
import com.chumi.gymapp.data.AppData
import com.chumi.gymapp.data.BodyEntry
import com.chumi.gymapp.data.FoodDatabase
import com.chumi.gymapp.data.FoodEntry
import com.chumi.gymapp.data.Goal
import com.chumi.gymapp.data.Meal
import com.chumi.gymapp.data.Nutrition
import com.chumi.gymapp.data.NutritionContent
import com.chumi.gymapp.data.Profile
import com.chumi.gymapp.data.Repository
import com.chumi.gymapp.data.Sex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NutritionTest {

    private val year = LocalDate.now().year
    private fun user(goal: Goal = Goal.MAINTAIN, rate: Double = 0.0) = AppData(
        profile = Profile(sex = Sex.MALE, birthYear = year - 30, heightCm = 180.0, activity = Activity.MODERATE, goal = goal, rateKgWeek = rate),
        body = listOf(BodyEntry(date = System.currentTimeMillis(), weight = 80.0)),
    )

    @Test
    fun imc() {
        assertEquals(24.69, Nutrition.bmi(80.0, 180.0)!!, 0.01)
        assertEquals("Peso normal", Nutrition.bmiCategory(24.69).label)
        assertEquals("Sobrepeso", Nutrition.bmiCategory(27.0).label)
        val (lo, hi) = Nutrition.healthyWeightRange(180.0)!!
        assertEquals(59.9, lo, 0.1); assertEquals(80.7, hi, 0.1)
    }

    @Test
    fun metabolismo_mifflin() {
        // 10*80 + 6,25*180 - 5*30 + 5 = 1780
        assertEquals(1780.0, Nutrition.bmr(80.0, user().profile, null)!!, 0.01)
        val t = Nutrition.targets(user())!!
        assertEquals(1780 * 1.55, t.tdee.toDouble(), 1.0)
        println("Mantener: $t")
        // proteína 1,8 g/kg en mantenimiento
        assertEquals(144, t.protein)
        // las calorías cuadran con los macros (±15 kcal por redondeos)
        assertEquals(t.kcal.toDouble(), t.protein * 4.0 + t.carbs * 4.0 + t.fat * 9.0, 15.0)
    }

    @Test
    fun deficit_y_superavit() {
        val cut = Nutrition.targets(user(Goal.LOSE, -0.5))!!
        val bulk = Nutrition.targets(user(Goal.GAIN, 0.25))!!
        println("Definición: $cut\nVolumen: $bulk")
        assertEquals(cut.tdee - 550.0, cut.kcal.toDouble(), 10.0)
        assertEquals(bulk.tdee + 275.0, bulk.kcal.toDouble(), 10.0)
        assertEquals(176, cut.protein) // 2,2 g/kg
        // el ritmo se limita al 1 % del peso por semana
        val crazy = Nutrition.targets(user(Goal.LOSE, -2.0))!!
        assertTrue(crazy.warnings.isNotEmpty())
        assertTrue(crazy.kcal >= 1780) // nunca por debajo del basal
    }

    @Test
    fun grasa_navy() {
        val p = Profile(sex = Sex.MALE, heightCm = 180.0, neckCm = 38.0, waistCm = 85.0)
        val bf = Nutrition.bodyFatNavy(p)!!
        println("Navy: $bf")
        assertTrue(bf in 14.0..18.0)
        assertNull(Nutrition.bodyFatNavy(p.copy(neckCm = 0.0)))
        val ffmi = Nutrition.ffmi(80.0, 180.0, 15.0)
        assertEquals(20.99, ffmi, 0.05)
    }

    @Test
    fun ideas_y_alimentos_existen() {
        NutritionContent.ideas.forEach { idea ->
            idea.ingredients.forEach { (id, _) -> assertNotNull("${idea.id}: falta $id", FoodDatabase.byId(id)) }
            val m = NutritionContent.ideaMacros(idea)
            println("${idea.name}: ${m.kcal.toInt()} kcal P${m.protein.toInt()} C${m.carbs.toInt()} G${m.fat.toInt()}")
        }
        // los macros de cada alimento cuadran aprox. con sus kcal (salvo alcohol y fibra)
        FoodDatabase.foods.filter { it.name !in listOf("Cerveza", "Vino tinto") }.forEach { f ->
            val est = f.protein * 4 + f.carbs * 4 + f.fat * 9
            assertTrue("${f.name}: $est vs ${f.kcal}", kotlin.math.abs(est - f.kcal) <= maxOf(25.0, f.kcal * 0.15))
        }
        assertEquals(FoodDatabase.foods.size, FoodDatabase.foods.map { it.id }.toSet().size)
    }

    @Test
    fun coach_propone_ajuste() {
        val day = 86_400_000L
        val now = System.currentTimeMillis()
        // objetivo -0,5 kg/sem pero el peso no baja -> debería proponer bajar calorías
        val body = (0..20 step 2).map { BodyEntry(date = now - (20 - it) * day, weight = 80.0) }
        val log = (0..13).map { FoodEntry(day = Nutrition.today() - it, meal = Meal.LUNCH, name = "x", grams = 0.0, kcal = 2500.0, protein = 150.0, carbs = 250.0, fat = 80.0) }
        val d = user(Goal.LOSE, -0.5).copy(body = body, foodLog = log)
        val c = Nutrition.coach(d)
        println(c)
        assertEquals(-300, c.suggestedAdjust)
        assertEquals(2500, c.realTdee)
    }

    @Test
    fun json_antiguo_sin_nutricion_se_lee() {
        val old = """{"version":1,"workouts":[],"body":[]}"""
        val d = Repository.json.decodeFromString(AppData.serializer(), old)
        assertEquals(Profile(), d.profile)
        val withWater = d.copy(water = mapOf(Nutrition.today() to 750))
        val back = Repository.json.decodeFromString(AppData.serializer(), Repository.json.encodeToString(AppData.serializer(), withWater))
        assertEquals(750, back.water[Nutrition.today()])
    }
}
