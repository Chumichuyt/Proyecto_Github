package com.chumi.gymapp.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.FoodDatabase
import com.chumi.gymapp.data.Goal
import com.chumi.gymapp.data.Meal
import com.chumi.gymapp.data.MealIdea
import com.chumi.gymapp.data.Nutrition
import com.chumi.gymapp.data.NutritionContent
import com.chumi.gymapp.data.formatKg
import com.chumi.gymapp.data.formatWeight
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.BarChart
import com.chumi.gymapp.ui.components.BarItem
import com.chumi.gymapp.ui.components.ChartPoint
import com.chumi.gymapp.ui.components.LineChart
import com.chumi.gymapp.ui.components.SectionCard
import com.chumi.gymapp.ui.components.StatTile
import com.chumi.gymapp.ui.components.mediumDate
import com.chumi.gymapp.ui.components.shortDate
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

// =============== Ideas de comidas ===============

@Composable
fun MealIdeasScreen(vm: AppViewModel, nav: NavHostController, day: Long) {
    val data by vm.data.collectAsStateWithLifecycle()
    var meal by remember { mutableStateOf<Meal?>(null) }
    var forMyGoal by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf<String?>(null) }
    var adding by remember { mutableStateOf<MealIdea?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val goal = data.profile.goal
    val list = NutritionContent.ideas
        .filter { meal == null || it.meal == meal }
        .filter { !forMyGoal || goal in it.goals }

    AppScaffold(title = "Ideas de comidas", onBack = { nav.popBackStack() }, snackbar = snackbar) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding(), bottom = pad.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(forMyGoal, { forMyGoal = !forMyGoal }, label = { Text("Para mi objetivo: ${goal.label}") })
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(meal == null, { meal = null }, label = { Text("Todas") })
                    Meal.entries.forEach { m -> FilterChip(meal == m, { meal = if (meal == m) null else m }, label = { Text("${m.emoji} ${m.label}") }) }
                }
            }
            items(list, key = { it.id }) { idea ->
                val m = NutritionContent.ideaMacros(idea)
                val open = expanded == idea.id
                SectionCard(modifier = Modifier.animateContentSize().clickable { expanded = if (open) null else idea.id }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(idea.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${idea.meal.emoji} ${idea.meal.label} · ⏱ ${idea.minutes} min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${m.kcal.roundToInt()} kcal · P ${m.protein.roundToInt()} g · C ${m.carbs.roundToInt()} g · G ${m.fat.roundToInt()} g",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (open) {
                        Spacer(Modifier.height(10.dp))
                        Text("Ingredientes", style = MaterialTheme.typography.titleSmall)
                        idea.ingredients.forEach { (id, grams) ->
                            val f = FoodDatabase.byId(id) ?: return@forEach
                            Text("• ${f.name}: ${formatWeight(grams)} g", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Preparación", style = MaterialTheme.typography.titleSmall)
                        Text(idea.steps, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("🧠 ${idea.why}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { adding = idea }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Añadir a mi diario (${dayLabel(day).lowercase()})")
                        }
                    }
                }
            }
        }
    }

    adding?.let { idea ->
        var target by remember(idea.id) { mutableStateOf(idea.meal) }
        AlertDialog(
            onDismissRequest = { adding = null },
            title = { Text("¿En qué comida?") },
            text = {
                Column {
                    Meal.entries.forEach { m ->
                        FilterChip(target == m, { target = m }, label = { Text("${m.emoji} ${m.label}") })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.addIdea(day, target, idea)
                    adding = null
                    scope.launch { snackbar.showSnackbar("✅ ${idea.name} añadido a ${target.label}") }
                }) { Text("Añadir") }
            },
            dismissButton = { TextButton(onClick = { adding = null }) { Text("Cancelar") } },
        )
    }
}

// =============== Consejos ===============

@Composable
fun TipsScreen(nav: NavHostController) {
    val categories = NutritionContent.tips.map { it.category }.distinct()
    var category by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf<String?>(null) }
    val list = NutritionContent.tips.filter { category == null || it.category == category }

    AppScaffold(title = "Consejos de tu nutricionista", onBack = { nav.popBackStack() }) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding(), bottom = pad.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(category == null, { category = null }, label = { Text("Todos") })
                    categories.forEach { c -> FilterChip(category == c, { category = if (category == c) null else c }, label = { Text(c) }) }
                }
            }
            items(list, key = { it.title }) { t ->
                val open = expanded == t.title
                SectionCard(modifier = Modifier.animateContentSize().clickable { expanded = if (open) null else t.title }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(t.emoji, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.title, style = MaterialTheme.typography.titleMedium)
                            Text(t.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    }
                    if (open) {
                        Spacer(Modifier.height(8.dp))
                        Text(t.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

// =============== Progreso de nutrición + coach ===============

@Composable
fun NutritionProgressScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsStateWithLifecycle()
    var range by remember { mutableIntStateOf(14) }
    val targets = Nutrition.targets(data)
    val coach = Nutrition.coach(data)
    val today = Nutrition.today()
    val days = (range - 1 downTo 0).map { today - it }
    val totalsByDay = days.associateWith { Nutrition.dayTotals(data, it) }
    val logged = totalsByDay.filterValues { it.kcal > 0 }

    AppScaffold(title = "Progreso de nutrición", onBack = { nav.popBackStack() }) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding(), bottom = pad.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Coach
            item {
                SectionCard(title = "🧑‍⚕️ Tu coach nutricional") {
                    Text(coach.message, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatTile(
                            "Tendencia peso",
                            coach.trendKgWeek?.let { (if (it > 0) "+" else "") + "%.2f".format(it).replace('.', ',') + " kg/sem" } ?: "—",
                            Modifier.weight(1f),
                            sub = "objetivo ${(if (data.profile.rateKgWeek > 0) "+" else "") + formatWeight(data.profile.rateKgWeek)}",
                        )
                        StatTile(
                            "Gasto real estimado",
                            coach.realTdee?.let { "${formatKg(it.toDouble())} kcal" } ?: "—",
                            Modifier.weight(1f),
                            sub = targets?.let { "calculado: ${formatKg(it.tdee.toDouble())}" },
                        )
                    }
                    if (coach.realTdee == null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Registra tu comida al menos 7 días y pésate varias veces y calcularé tu gasto REAL (mucho más preciso que cualquier fórmula).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (coach.suggestedAdjust != 0) {
                        Spacer(Modifier.height(10.dp))
                        FilledTonalButton(onClick = { vm.applyCalorieAdjust(coach.suggestedAdjust) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Aplicar ajuste (${if (coach.suggestedAdjust > 0) "+" else ""}${coach.suggestedAdjust} kcal)")
                        }
                        Text(
                            "Consejo: aplica un ajuste y espera 2 semanas antes del siguiente para ver su efecto.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(7, 14, 30).forEach { r -> FilterChip(range == r, { range = r }, label = { Text("$r días") }) }
                }
            }

            // Adherencia
            item {
                val avgKcal = if (logged.isEmpty()) 0.0 else logged.values.map { it.kcal }.average()
                val avgProt = if (logged.isEmpty()) 0.0 else logged.values.map { it.protein }.average()
                val onTarget = targets?.let { t -> logged.values.count { abs(it.kcal - t.kcal) <= t.kcal * 0.1 } } ?: 0
                val protOk = targets?.let { t -> logged.values.count { it.protein >= t.protein * 0.9 } } ?: 0
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("Días registrados", "${logged.size}/$range", Modifier.weight(1f))
                    StatTile("Media kcal", if (logged.isEmpty()) "—" else formatKg(avgKcal), Modifier.weight(1f), sub = targets?.let { "obj. ${formatKg(it.kcal.toDouble())}" })
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("Media proteína", if (logged.isEmpty()) "—" else "${avgProt.roundToInt()} g", Modifier.weight(1f), sub = targets?.let { "obj. ${it.protein} g" })
                    StatTile(
                        "Días en objetivo", if (logged.isEmpty() || targets == null) "—" else "${(onTarget * 100 / logged.size)} %",
                        Modifier.weight(1f), sub = if (targets != null && logged.isNotEmpty()) "proteína OK: ${protOk * 100 / logged.size} %" else "±10 % de las kcal",
                    )
                }
            }

            item {
                SectionCard(title = "Calorías por día") {
                    BarChart(
                        bars = days.map { d ->
                            val ld = LocalDate.ofEpochDay(d)
                            BarItem(if (range <= 7) dayLabelShort(ld) else ld.format(shortDate), totalsByDay[d]?.kcal ?: 0.0, ld.format(mediumDate))
                        },
                        valueText = { "${formatKg(it)} kcal" },
                        target = targets?.kcal?.toDouble(),
                        labelEvery = when (range) { 7 -> 1; 14 -> 2; else -> 7 },
                    )
                }
            }
            item {
                SectionCard(title = "Proteína por día") {
                    BarChart(
                        bars = days.map { d ->
                            val ld = LocalDate.ofEpochDay(d)
                            BarItem(if (range <= 7) dayLabelShort(ld) else ld.format(shortDate), totalsByDay[d]?.protein ?: 0.0, ld.format(mediumDate))
                        },
                        valueText = { "${it.roundToInt()} g" },
                        target = targets?.protein?.toDouble(),
                        labelEvery = when (range) { 7 -> 1; 14 -> 2; else -> 7 },
                        color = com.chumi.gymapp.ui.components.macroColors().protein,
                    )
                }
            }
            item {
                SectionCard(title = "Peso corporal (60 días)") {
                    val from = System.currentTimeMillis() - 60L * 86_400_000
                    LineChart(
                        data.body.filter { it.date >= from }.map { ChartPoint(it.date, it.weight) },
                        valueText = { "${formatWeight(Math.round(it * 10) / 10.0)} kg" },
                        height = 160,
                    )
                }
            }
            if (data.profile.goal != Goal.MAINTAIN) item {
                Text(
                    "Cómo funciona el coach: compara cómo cambia tu peso realmente (tendencia de 3 semanas) con el ritmo que buscas y te propone ajustar en pasos de 50 kcal, como haría un nutricionista en las revisiones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

private fun dayLabelShort(d: LocalDate): String = listOf("L", "M", "X", "J", "V", "S", "D")[d.dayOfWeek.value - 1]
