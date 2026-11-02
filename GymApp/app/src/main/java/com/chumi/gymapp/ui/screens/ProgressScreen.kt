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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.ExerciseType
import com.chumi.gymapp.data.Stats
import com.chumi.gymapp.data.formatKg
import com.chumi.gymapp.data.formatWeight
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.Routes
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.BarChart
import com.chumi.gymapp.ui.components.BarItem
import com.chumi.gymapp.ui.components.ChartPoint
import com.chumi.gymapp.ui.components.HorizontalBars
import com.chumi.gymapp.ui.components.LineChart
import com.chumi.gymapp.ui.components.SectionCard
import com.chumi.gymapp.ui.components.StatTile
import com.chumi.gymapp.ui.components.TrainingHeatmap
import com.chumi.gymapp.ui.components.mediumDate
import com.chumi.gymapp.ui.components.shortDate

private enum class WeekMetric(val label: String) { VOLUME("Volumen"), WORKOUTS("Entrenos"), SETS("Series") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsStateWithLifecycle()
    var weeks by remember { mutableIntStateOf(12) }
    var metric by remember { mutableStateOf(WeekMetric.VOLUME) }
    val doneExercises = remember(data.workouts) {
        val ids = data.workouts.flatMap { w -> w.exercises.map { it.exerciseId } }.toSet()
        data.exercises.filter { it.id in ids && it.type != ExerciseType.CARDIO }.sortedBy { it.name }
    }
    var selectedEx by remember { mutableStateOf<String?>(null) }
    val exId = selectedEx?.takeIf { id -> doneExercises.any { it.id == id } } ?: Stats.mostDoneExercises(data, 10)
        .firstOrNull { it.first.type != ExerciseType.CARDIO }?.first?.id
    var exMenu by remember { mutableStateOf(false) }

    AppScaffold(title = "Progreso", isTab = true) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("Entrenos", "${data.workouts.size}", Modifier.weight(1f))
                    StatTile("Volumen total", "${formatKg(Stats.lifetimeVolume(data))} kg", Modifier.weight(1.4f))
                    StatTile("Racha", "${Stats.weekStreak(data)} sem", Modifier.weight(1f))
                }
            }

            item {
                SectionCard(title = "Por semana") {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WeekMetric.entries.forEach { m -> FilterChip(metric == m, { metric = m }, label = { Text(m.label) }) }
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(4 to "4 sem", 12 to "12 sem", 26 to "6 meses", 52 to "1 año").forEach { (w, l) ->
                            FilterChip(weeks == w, { weeks = w }, label = { Text(l) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    val buckets = Stats.weekly(data, weeks)
                    BarChart(
                        bars = buckets.map { b ->
                            val v = when (metric) {
                                WeekMetric.VOLUME -> b.volume
                                WeekMetric.WORKOUTS -> b.workouts.toDouble()
                                WeekMetric.SETS -> b.sets.toDouble()
                            }
                            BarItem(b.start.format(shortDate), v, "Semana del ${b.start.format(mediumDate)}")
                        },
                        valueText = { v ->
                            when (metric) {
                                WeekMetric.VOLUME -> "${formatKg(v)} kg"
                                WeekMetric.WORKOUTS -> "${v.toInt()} entrenos"
                                WeekMetric.SETS -> "${v.toInt()} series"
                            }
                        },
                        labelEvery = when (weeks) { 4 -> 1; 12 -> 3; 26 -> 6; else -> 13 },
                    )
                }
            }

            item {
                SectionCard(title = "Días entrenados") {
                    val byDay = data.workouts.groupBy { Stats.toDate(it.startTime) }
                        .mapValues { (_, ws) -> ws.sumOf { Stats.workoutVolume(data, it) }.coerceAtLeast(1.0) }
                    TrainingHeatmap(byDay)
                }
            }

            item {
                SectionCard(
                    title = "Progreso por ejercicio",
                    action = {
                        if (exId != null) TextButton(onClick = { nav.navigate(Routes.exercise(exId)) }) { Text("Detalle") }
                    },
                ) {
                    if (doneExercises.isEmpty() || exId == null) {
                        Text("Completa algún entreno para ver tu evolución.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        ExposedDropdownMenuBox(expanded = exMenu, onExpandedChange = { exMenu = it }) {
                            OutlinedTextField(
                                value = data.exercises.find { it.id == exId }?.name ?: "",
                                onValueChange = {}, readOnly = true, label = { Text("Ejercicio") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(exMenu) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            )
                            ExposedDropdownMenu(expanded = exMenu, onDismissRequest = { exMenu = false }) {
                                doneExercises.forEach { e ->
                                    DropdownMenuItem(text = { Text(e.name) }, onClick = { selectedEx = e.id; exMenu = false })
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("1RM estimado (kg)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val hist = Stats.exerciseHistory(data, exId)
                        LineChart(hist.map { ChartPoint(it.date, Math.round(it.best1RM * 10) / 10.0) }, valueText = { "${formatWeight(it)} kg" })
                    }
                }
            }

            item {
                SectionCard(title = "Series por músculo (30 días)") {
                    val ms = Stats.muscleSets(data, 30)
                    if (ms.isEmpty()) Text("Sin datos todavía.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else HorizontalBars(ms.map { it.first.label to it.second.toDouble() }, valueText = { "${it.toInt()}" })
                }
            }

            item {
                SectionCard(
                    modifier = Modifier.clickable { nav.navigate(Routes.BODY) },
                    title = "Peso corporal",
                    action = { Icon(Icons.Default.ChevronRight, null) },
                ) {
                    if (data.body.isEmpty()) Text("Toca para registrar tu peso.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else LineChart(data.body.map { ChartPoint(it.date, it.weight) }, valueText = { "${formatWeight(it)} kg" }, height = 150)
                }
            }

            val top = Stats.mostDoneExercises(data, 8)
            if (top.isNotEmpty()) item {
                SectionCard(title = "Tus ejercicios estrella") {
                    top.forEach { (e, n) ->
                        val pr = Stats.records(data, e.id)
                        Row(
                            Modifier.fillMaxWidth().clickable { nav.navigate(Routes.exercise(e.id)) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(e.name, style = MaterialTheme.typography.bodyLarge)
                                Text("$n sesiones", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (pr.maxWeight > 0) Text("🏆 ${formatWeight(pr.maxWeight)} kg", style = MaterialTheme.typography.titleSmall)
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    }
                }
            }
        }
    }
}
