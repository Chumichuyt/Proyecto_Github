package com.chumi.gymapp

import com.chumi.gymapp.data.AppData
import com.chumi.gymapp.data.BodyEntry
import com.chumi.gymapp.data.Comparisons
import com.chumi.gymapp.data.Repository
import com.chumi.gymapp.data.Stats
import com.chumi.gymapp.data.Workout
import com.chumi.gymapp.data.WorkoutExercise
import com.chumi.gymapp.data.WorkoutSet
import com.chumi.gymapp.ui.screens.platesPerSide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {

    @Test
    fun `130 kg es un bebe tigre`() {
        val s = Comparisons.sentence(130.0, "Hoy has levantado")
        assertEquals("Hoy has levantado 130 kg en total, lo mismo que pesa un bebé tigre 🐯", s)
    }

    @Test
    fun `comparaciones razonables en todo el rango`() {
        listOf(0.5, 3.0, 50.0, 520.0, 1100.0, 4300.0, 9000.0, 25_000.0, 160_000.0, 9_000_000.0).forEach { kg ->
            val c = Comparisons.compare(kg)!!
            println("$kg kg -> ${Comparisons.sentence(kg)}")
            assertTrue("count razonable para $kg: ${c.count}", c.count in 0.9..2000.0)
        }
        assertEquals("2 caballos", Comparisons.compare(1100.0)!!.phrase)
    }

    @Test
    fun `volumen y 1RM`() {
        val bench = "def_bench_press"
        val pull = "def_pull_up"
        val w = Workout(
            name = "Test", startTime = 1_000L, endTime = 3_600_000L,
            exercises = listOf(
                WorkoutExercise(exerciseId = bench, sets = listOf(WorkoutSet(weight = 60.0, reps = 10, done = true), WorkoutSet(weight = 100.0, reps = 1, done = false))),
                WorkoutExercise(exerciseId = pull, sets = listOf(WorkoutSet(weight = 0.0, reps = 10, done = true))),
            ),
        )
        val data = AppData(workouts = listOf(w), body = listOf(BodyEntry(date = 0L, weight = 80.0)))
        // 60*10 (la serie no hecha no cuenta) + 80*10 de dominadas con peso corporal
        assertEquals(1400.0, Stats.workoutVolume(data, w), 0.001)
        assertEquals(80.0, Stats.estimated1RM(60.0, 10), 0.001)
        assertEquals(100.0, Stats.estimated1RM(100.0, 1), 0.001)
    }

    @Test
    fun `records nuevos`() {
        val bench = "def_bench_press"
        fun wk(t: Long, kg: Double) = Workout(
            name = "B", startTime = t, endTime = t + 1,
            exercises = listOf(WorkoutExercise(exerciseId = bench, sets = listOf(WorkoutSet(weight = kg, reps = 5, done = true)))),
        )
        val a = wk(1, 80.0); val b = wk(2, 85.0)
        val data = AppData(workouts = listOf(a, b))
        val recs = Stats.newRecordsIn(data, b)
        assertTrue(recs.any { it.kind == "Peso máximo" })
        assertTrue(Stats.newRecordsIn(data, a).isEmpty()) // primera vez no es récord
    }

    @Test
    fun `discos por lado`() {
        val (plates, rest) = platesPerSide(100.0, 20.0)
        assertEquals(listOf(25.0, 15.0), plates)
        assertEquals(0.0, rest, 0.001)
        assertEquals(listOf(20.0, 1.25), platesPerSide(62.5, 20.0).first)
    }

    @Test
    fun `serializacion ida y vuelta`() {
        val d = AppData(workouts = listOf(Workout(name = "X")))
        val text = Repository.json.encodeToString(AppData.serializer(), d)
        val back = Repository.json.decodeFromString(AppData.serializer(), text)
        assertEquals(d, back)
    }
}
