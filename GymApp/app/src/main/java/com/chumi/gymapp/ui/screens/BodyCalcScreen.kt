package com.chumi.gymapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.Activity
import com.chumi.gymapp.data.Goal
import com.chumi.gymapp.data.Nutrition
import com.chumi.gymapp.data.Sex
import com.chumi.gymapp.data.formatKg
import com.chumi.gymapp.data.formatWeight
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.BmiGauge
import com.chumi.gymapp.ui.components.LabeledNumberField
import com.chumi.gymapp.ui.components.MacroBar
import com.chumi.gymapp.ui.components.ResultRow
import com.chumi.gymapp.ui.components.SectionCard
import com.chumi.gymapp.ui.components.StatTile
import com.chumi.gymapp.ui.components.macroColors
import java.time.LocalDate
import kotlin.math.roundToInt

private fun d1(v: Double) = formatWeight(Math.round(v * 10) / 10.0)

@Composable
fun BodyCalcScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsStateWithLifecycle()
    val p = data.profile
    val weight = Nutrition.currentWeight(data) ?: 0.0
    val age = Nutrition.age(p)
    val bmi = Nutrition.bmi(weight, p.heightCm)
    val bf = Nutrition.bodyFat(data)
    val navy = Nutrition.bodyFatNavy(p)
    val targets = Nutrition.targets(data)
    val colors = macroColors()

    AppScaffold(title = "Mi cuerpo y objetivos", onBack = { nav.popBackStack() }) { pad ->
        LazyColumn(
            modifier = Modifier.imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = pad.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ---------- Datos ----------
            item {
                SectionCard(title = "Tus datos") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Sex.entries.forEach { s -> FilterChip(p.sex == s, { vm.updateProfile { it.copy(sex = s) } }, label = { Text(s.label) }) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabeledNumberField("Altura", p.heightCm, { v -> vm.updateProfile { it.copy(heightCm = v) } }, Modifier.weight(1f), suffix = "cm")
                        LabeledNumberField(
                            "Edad", age.toDouble(),
                            { v -> vm.updateProfile { it.copy(birthYear = if (v > 0) LocalDate.now().year - v.toInt() else 0) } },
                            Modifier.weight(1f), decimals = false, suffix = "años",
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LabeledNumberField("Peso de hoy", weight, { v -> vm.setTodayWeight(v) }, Modifier.fillMaxWidth(), suffix = "kg")
                    Text(
                        "El peso se guarda en tu historial de \"Peso y medidas\" y se usa en toda la app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ---------- IMC ----------
            if (bmi != null) item {
                val cat = Nutrition.bmiCategory(bmi)
                val range = Nutrition.healthyWeightRange(p.heightCm)
                SectionCard(title = "Índice de masa corporal (IMC)") {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(d1(bmi), style = MaterialTheme.typography.displaySmall)
                        Text("  ${cat.label}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    BmiGauge(bmi)
                    Text(cat.advice, style = MaterialTheme.typography.bodyMedium)
                    if (range != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Peso saludable para tu altura (IMC 18,5-24,9): ${d1(range.first)} – ${d1(range.second)} kg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "💡 Ojo: el IMC no distingue músculo de grasa. Si entrenas fuerza y tienes bastante músculo puede salir \"sobrepeso\" estando en forma. Por eso te calculo también el % de grasa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // ---------- Composición corporal ----------
            item {
                SectionCard(title = "Composición corporal") {
                    Text(
                        "Mide con una cinta métrica: cuello justo debajo de la nuez, cintura a la altura del ombligo" +
                            if (p.sex == Sex.FEMALE) " y cadera en la parte más ancha." else ".",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        LabeledNumberField("Cuello", p.neckCm, { v -> vm.updateProfile { it.copy(neckCm = v) } }, Modifier.weight(1f), suffix = "cm")
                        LabeledNumberField("Cintura", p.waistCm, { v -> vm.updateProfile { it.copy(waistCm = v) } }, Modifier.weight(1f), suffix = "cm")
                        if (p.sex == Sex.FEMALE) LabeledNumberField("Cadera", p.hipCm, { v -> vm.updateProfile { it.copy(hipCm = v) } }, Modifier.weight(1f), suffix = "cm")
                    }
                    Spacer(Modifier.height(6.dp))
                    LabeledNumberField(
                        "% grasa medido (opcional: báscula, plicómetro…)", p.bodyFatManual ?: 0.0,
                        { v -> vm.updateProfile { it.copy(bodyFatManual = v.takeIf { x -> x in 2.0..70.0 }) } },
                        Modifier.fillMaxWidth(), suffix = "%",
                    )
                    Spacer(Modifier.height(12.dp))
                    if (bf != null && weight > 0) {
                        val fatKg = weight * bf / 100
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatTile("% grasa", "${d1(bf)} %", Modifier.weight(1f), sub = Nutrition.bodyFatCategory(bf, p.sex))
                            StatTile("Masa magra", "${d1(weight - fatKg)} kg", Modifier.weight(1f), sub = "grasa: ${d1(fatKg)} kg")
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            when {
                                p.bodyFatManual != null -> "Usando el % que has medido."
                                navy != null && bf == navy -> "Estimado con la fórmula de la Marina de EE. UU. (error ±3-4 %)."
                                else -> "Usando tu último registro de % grasa."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(10.dp))
                        if (p.heightCm > 0) {
                            val ffmi = Nutrition.ffmi(weight, p.heightCm, bf)
                            ResultRow(
                                "FFMI (índice de masa libre de grasa)", d1(ffmi),
                                when {
                                    ffmi < 18 -> "Por debajo de la media: mucho margen para ganar músculo."
                                    ffmi < 20 -> "Media de la población."
                                    ffmi < 22 -> "Por encima de la media: se nota que entrenas."
                                    ffmi < 25 -> "Excelente: muy musculado."
                                    else -> "Excepcional: límite natural aproximado ≈ 25."
                                },
                            )
                        }
                    } else {
                        Text("Rellena cuello y cintura" + (if (p.sex == Sex.FEMALE) " y cadera" else "") + " (o tu % medido) para ver tu % de grasa y masa muscular.", style = MaterialTheme.typography.bodyMedium)
                    }
                    Nutrition.waistToHeight(p)?.let { whtr ->
                        ResultRow(
                            "Índice cintura-altura", "%.2f".format(whtr).replace('.', ','),
                            if (whtr < 0.5) "✅ Saludable (menor de 0,5): poco riesgo cardiovascular."
                            else if (whtr < 0.6) "⚠️ Algo elevado: reducir cintura mejora tu salud."
                            else "❗ Elevado: prioriza perder grasa abdominal.",
                        )
                    }
                }
            }

            // ---------- Actividad ----------
            item {
                SectionCard(title = "Actividad diaria") {
                    Activity.entries.forEach { a ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { vm.updateProfile { it.copy(activity = a) } }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = p.activity == a, onClick = { vm.updateProfile { it.copy(activity = a) } })
                            Column {
                                Text(a.label, style = MaterialTheme.typography.bodyLarge)
                                Text(a.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // ---------- Objetivo ----------
            item {
                SectionCard(title = "Tu objetivo") {
                    Goal.entries.forEach { gl ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .clickable { vm.updateProfile { it.copy(goal = gl, rateKgWeek = gl.defaultRate, calorieAdjust = 0) } }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = p.goal == gl, onClick = { vm.updateProfile { it.copy(goal = gl, rateKgWeek = gl.defaultRate, calorieAdjust = 0) } })
                            Column {
                                Text(gl.label, style = MaterialTheme.typography.bodyLarge)
                                Text(gl.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    val rates = when (p.goal) {
                        Goal.LOSE -> listOf(-0.25, -0.5, -0.75, -1.0)
                        Goal.GAIN -> listOf(0.1, 0.25, 0.4, 0.5)
                        else -> emptyList()
                    }
                    if (rates.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Ritmo (kg por semana)", style = MaterialTheme.typography.labelLarge)
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rates.forEach { r ->
                                FilterChip(p.rateKgWeek == r, { vm.updateProfile { it.copy(rateKgWeek = r) } }, label = { Text((if (r > 0) "+" else "") + formatWeight(r)) })
                            }
                        }
                        Text(
                            if (p.goal == Goal.LOSE) "Recomendado: 0,5 kg/sem (o 0,5-1 % de tu peso). Más rápido = más riesgo de perder músculo."
                            else "Recomendado: +0,25 kg/sem. Si eres principiante puedes ir a +0,4-0,5.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ---------- Resultado ----------
            item {
                SectionCard(title = "🍽️ Tu plan diario") {
                    if (targets == null) {
                        Text(
                            "Completa altura, edad y peso para calcular tus calorías y macros.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        return@SectionCard
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile("Metabolismo basal", "${formatKg(targets.bmr.toDouble())}", Modifier.weight(1f), sub = "kcal en reposo")
                        StatTile("Gasto diario", "${formatKg(targets.tdee.toDouble())}", Modifier.weight(1f), sub = "kcal mantenimiento")
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("${formatKg(targets.kcal.toDouble())} kcal al día", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    val diff = targets.kcal - targets.tdee
                    Text(
                        when {
                            diff < -30 -> "Déficit de ${-diff} kcal sobre tu mantenimiento"
                            diff > 30 -> "Superávit de $diff kcal sobre tu mantenimiento"
                            else -> "Calorías de mantenimiento"
                        } + if (p.calorieAdjust != 0) " (incluye ajuste del coach: ${if (p.calorieAdjust > 0) "+" else ""}${p.calorieAdjust})" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (p.calorieAdjust != 0) TextButton(onClick = { vm.updateProfile { it.copy(calorieAdjust = 0) } }) { Text("Quitar ajuste") }
                    Spacer(Modifier.height(12.dp))
                    val k = targets.kcal.toDouble().coerceAtLeast(1.0)
                    MacroBar("Proteína · ${(targets.protein * 4 / k * 100).roundToInt()} %", targets.protein.toDouble(), targets.protein.toDouble(), colors.protein)
                    Spacer(Modifier.height(8.dp))
                    MacroBar("Carbohidratos · ${(targets.carbs * 4 / k * 100).roundToInt()} %", targets.carbs.toDouble(), targets.carbs.toDouble(), colors.carbs)
                    Spacer(Modifier.height(8.dp))
                    MacroBar("Grasas · ${(targets.fat * 9 / k * 100).roundToInt()} %", targets.fat.toDouble(), targets.fat.toDouble(), colors.fat)
                    Spacer(Modifier.height(12.dp))
                    if (weight > 0) Text(
                        "Proteína: ${d1(targets.protein / weight)} g/kg · Fibra: ${targets.fiber} g · Agua: ${formatWeight(targets.waterMl / 1000.0)} L",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    targets.warnings.forEach {
                        Spacer(Modifier.height(8.dp))
                        Text("⚠️ $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // ---------- Ajustes avanzados ----------
            item {
                SectionCard(title = "Ajustes avanzados (opcional)") {
                    Text("Proteína (g por kg de peso)", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(p.proteinPerKg == 0.0, { vm.updateProfile { it.copy(proteinPerKg = 0.0) } }, label = { Text("Auto (${formatWeight(Nutrition.autoProteinPerKg(p.goal))})") })
                        listOf(1.6, 1.8, 2.0, 2.2, 2.5).forEach { v ->
                            FilterChip(p.proteinPerKg == v, { vm.updateProfile { it.copy(proteinPerKg = v) } }, label = { Text(formatWeight(v)) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Grasas (% de las calorías)", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(p.fatPercent == 0.0, { vm.updateProfile { it.copy(fatPercent = 0.0) } }, label = { Text("Auto (25)") })
                        listOf(20.0, 30.0, 35.0, 40.0).forEach { v ->
                            FilterChip(p.fatPercent == v, { vm.updateProfile { it.copy(fatPercent = v) } }, label = { Text(formatWeight(v)) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Más grasa y menos carbohidratos solo tiene sentido si te sienta mejor; para rendir en el gym lo normal es 20-30 % de grasa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                Text(
                    "Estos cálculos son estimaciones de partida (Mifflin-St Jeor / Katch-McArdle). Ajústalos con el coach de \"Progreso\" según cómo cambia tu peso. " +
                        "No sustituyen el consejo de un dietista-nutricionista o médico, sobre todo si tienes alguna enfermedad.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
