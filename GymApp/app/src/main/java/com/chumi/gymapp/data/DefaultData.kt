package com.chumi.gymapp.data

import com.chumi.gymapp.data.ExerciseType.BODYWEIGHT
import com.chumi.gymapp.data.ExerciseType.CARDIO
import com.chumi.gymapp.data.ExerciseType.WEIGHT_REPS
import com.chumi.gymapp.data.MuscleGroup.BACK
import com.chumi.gymapp.data.MuscleGroup.BICEPS
import com.chumi.gymapp.data.MuscleGroup.CHEST
import com.chumi.gymapp.data.MuscleGroup.CORE
import com.chumi.gymapp.data.MuscleGroup.FULL_BODY
import com.chumi.gymapp.data.MuscleGroup.GLUTES
import com.chumi.gymapp.data.MuscleGroup.LEGS
import com.chumi.gymapp.data.MuscleGroup.SHOULDERS
import com.chumi.gymapp.data.MuscleGroup.TRICEPS

object DefaultData {

    private fun ex(id: String, name: String, muscle: MuscleGroup, equipment: String, type: ExerciseType = WEIGHT_REPS) =
        Exercise(id = "def_$id", name = name, muscle = muscle, type = type, equipment = equipment)

    val exercises: List<Exercise> = listOf(
        // Pecho
        ex("bench_press", "Press de banca", CHEST, "Barra"),
        ex("incline_bench", "Press inclinado con barra", CHEST, "Barra"),
        ex("db_bench", "Press de banca con mancuernas", CHEST, "Mancuernas"),
        ex("incline_db", "Press inclinado con mancuernas", CHEST, "Mancuernas"),
        ex("decline_bench", "Press declinado", CHEST, "Barra"),
        ex("db_fly", "Aperturas con mancuernas", CHEST, "Mancuernas"),
        ex("cable_cross", "Cruce de poleas", CHEST, "Polea"),
        ex("pec_deck", "Contractor de pecho (Pec deck)", CHEST, "Máquina"),
        ex("chest_press_machine", "Press de pecho en máquina", CHEST, "Máquina"),
        ex("push_up", "Flexiones", CHEST, "Peso corporal", BODYWEIGHT),
        ex("dips_chest", "Fondos en paralelas", CHEST, "Peso corporal", BODYWEIGHT),
        // Espalda
        ex("deadlift", "Peso muerto", BACK, "Barra"),
        ex("pull_up", "Dominadas", BACK, "Peso corporal", BODYWEIGHT),
        ex("chin_up", "Dominadas supinas", BACK, "Peso corporal", BODYWEIGHT),
        ex("lat_pulldown", "Jalón al pecho", BACK, "Polea"),
        ex("barbell_row", "Remo con barra", BACK, "Barra"),
        ex("db_row", "Remo con mancuerna", BACK, "Mancuernas"),
        ex("seated_row", "Remo sentado en polea", BACK, "Polea"),
        ex("t_bar_row", "Remo en T", BACK, "Barra"),
        ex("pullover", "Pullover en polea", BACK, "Polea"),
        ex("hyperext", "Hiperextensiones lumbares", BACK, "Banco", BODYWEIGHT),
        ex("shrug", "Encogimientos (trapecio)", BACK, "Mancuernas"),
        // Hombros
        ex("ohp", "Press militar", SHOULDERS, "Barra"),
        ex("db_shoulder_press", "Press de hombro con mancuernas", SHOULDERS, "Mancuernas"),
        ex("arnold", "Press Arnold", SHOULDERS, "Mancuernas"),
        ex("lateral_raise", "Elevaciones laterales", SHOULDERS, "Mancuernas"),
        ex("cable_lateral", "Elevaciones laterales en polea", SHOULDERS, "Polea"),
        ex("front_raise", "Elevaciones frontales", SHOULDERS, "Mancuernas"),
        ex("rear_delt", "Pájaros (deltoides posterior)", SHOULDERS, "Mancuernas"),
        ex("face_pull", "Face pull", SHOULDERS, "Polea"),
        ex("upright_row", "Remo al mentón", SHOULDERS, "Barra"),
        // Bíceps
        ex("barbell_curl", "Curl con barra", BICEPS, "Barra"),
        ex("db_curl", "Curl con mancuernas", BICEPS, "Mancuernas"),
        ex("hammer_curl", "Curl martillo", BICEPS, "Mancuernas"),
        ex("preacher_curl", "Curl predicador (Scott)", BICEPS, "Barra Z"),
        ex("cable_curl", "Curl en polea", BICEPS, "Polea"),
        ex("concentration_curl", "Curl concentrado", BICEPS, "Mancuernas"),
        // Tríceps
        ex("pushdown", "Extensión de tríceps en polea", TRICEPS, "Polea"),
        ex("skull_crusher", "Press francés", TRICEPS, "Barra Z"),
        ex("overhead_ext", "Extensión de tríceps sobre la cabeza", TRICEPS, "Mancuernas"),
        ex("close_grip_bench", "Press de banca agarre cerrado", TRICEPS, "Barra"),
        ex("dips_bench", "Fondos en banco", TRICEPS, "Peso corporal", BODYWEIGHT),
        ex("kickback", "Patada de tríceps", TRICEPS, "Mancuernas"),
        // Piernas
        ex("squat", "Sentadilla", LEGS, "Barra"),
        ex("front_squat", "Sentadilla frontal", LEGS, "Barra"),
        ex("leg_press", "Prensa de piernas", LEGS, "Máquina"),
        ex("hack_squat", "Sentadilla hack", LEGS, "Máquina"),
        ex("lunge", "Zancadas", LEGS, "Mancuernas"),
        ex("bulgarian", "Sentadilla búlgara", LEGS, "Mancuernas"),
        ex("leg_ext", "Extensión de cuádriceps", LEGS, "Máquina"),
        ex("leg_curl", "Curl femoral", LEGS, "Máquina"),
        ex("rdl", "Peso muerto rumano", LEGS, "Barra"),
        ex("calf_raise", "Elevación de gemelos de pie", LEGS, "Máquina"),
        ex("seated_calf", "Gemelos sentado", LEGS, "Máquina"),
        ex("goblet_squat", "Sentadilla goblet", LEGS, "Mancuernas"),
        // Glúteos
        ex("hip_thrust", "Hip thrust", GLUTES, "Barra"),
        ex("glute_bridge", "Puente de glúteos", GLUTES, "Peso corporal", BODYWEIGHT),
        ex("abductor", "Abductores en máquina", GLUTES, "Máquina"),
        ex("cable_kickback", "Patada de glúteo en polea", GLUTES, "Polea"),
        // Abdomen
        ex("crunch", "Crunch abdominal", CORE, "Peso corporal", BODYWEIGHT),
        ex("leg_raise", "Elevación de piernas colgado", CORE, "Peso corporal", BODYWEIGHT),
        ex("cable_crunch", "Crunch en polea", CORE, "Polea"),
        ex("russian_twist", "Giros rusos", CORE, "Disco"),
        ex("ab_wheel", "Rueda abdominal", CORE, "Rueda", BODYWEIGHT),
        ex("plank", "Plancha", CORE, "Peso corporal", CARDIO),
        // Cardio
        ex("treadmill", "Cinta de correr", MuscleGroup.CARDIO, "Máquina", CARDIO),
        ex("bike", "Bicicleta estática", MuscleGroup.CARDIO, "Máquina", CARDIO),
        ex("elliptical", "Elíptica", MuscleGroup.CARDIO, "Máquina", CARDIO),
        ex("rowing", "Remo ergómetro", MuscleGroup.CARDIO, "Máquina", CARDIO),
        ex("stairs", "Escaladora", MuscleGroup.CARDIO, "Máquina", CARDIO),
        ex("jump_rope", "Comba", MuscleGroup.CARDIO, "Comba", CARDIO),
        // Cuerpo completo
        ex("clean", "Cargada de potencia", FULL_BODY, "Barra"),
        ex("kb_swing", "Swing con kettlebell", FULL_BODY, "Kettlebell"),
        ex("burpee", "Burpees", FULL_BODY, "Peso corporal", BODYWEIGHT),
        ex("farmer_walk", "Paseo del granjero", FULL_BODY, "Mancuernas"),
        ex("thruster", "Thruster", FULL_BODY, "Barra"),
    )

    private fun r(id: String, sets: Int, reps: Int, rest: Int = 90) =
        RoutineExercise(exerciseId = "def_$id", sets = sets, reps = reps, restSec = rest)

    val routines: List<Routine> = listOf(
        Routine(
            id = "def_push", name = "Empuje (Push)",
            description = "Pecho, hombros y tríceps",
            exercises = listOf(
                r("bench_press", 4, 8, 150), r("incline_db", 3, 10, 120), r("ohp", 3, 8, 120),
                r("lateral_raise", 3, 15, 60), r("pushdown", 3, 12, 60), r("overhead_ext", 3, 12, 60),
            ),
        ),
        Routine(
            id = "def_pull", name = "Tirón (Pull)",
            description = "Espalda y bíceps",
            exercises = listOf(
                r("deadlift", 3, 5, 180), r("pull_up", 3, 8, 120), r("barbell_row", 3, 10, 120),
                r("face_pull", 3, 15, 60), r("barbell_curl", 3, 10, 60), r("hammer_curl", 3, 12, 60),
            ),
        ),
        Routine(
            id = "def_legs", name = "Pierna (Legs)",
            description = "Cuádriceps, femoral, glúteo y gemelo",
            exercises = listOf(
                r("squat", 4, 8, 180), r("rdl", 3, 10, 120), r("leg_press", 3, 12, 120),
                r("leg_curl", 3, 12, 60), r("hip_thrust", 3, 10, 90), r("calf_raise", 4, 15, 60),
            ),
        ),
        Routine(
            id = "def_full", name = "Cuerpo completo",
            description = "Ideal para 2-3 días por semana",
            exercises = listOf(
                r("squat", 3, 8, 150), r("bench_press", 3, 8, 150), r("barbell_row", 3, 10, 120),
                r("ohp", 3, 10, 90), r("rdl", 3, 10, 120), r("plank", 3, 1, 60),
            ),
        ),
    )
}
