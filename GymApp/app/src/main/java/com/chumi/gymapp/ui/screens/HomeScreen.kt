package com.chumi.gymapp.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.Comparisons
import com.chumi.gymapp.data.Stats
import com.chumi.gymapp.data.formatKg
import com.chumi.gymapp.data.formatWeight
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.Routes
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.ComparisonCard
import com.chumi.gymapp.ui.components.SectionCard
import com.chumi.gymapp.ui.components.StatTile
import com.chumi.gymapp.ui.components.formatDuration
import com.chumi.gymapp.ui.components.mediumDate
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun HomeScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsStateWithLifecycle()
    val hour = LocalTime.now().hour
    val hello = when (hour) {
        in 6..13 -> "¡Buenos días"
        in 14..20 -> "¡Buenas tardes"
        else -> "¡Buenas noches"
    }
    val name = data.settings.userName.takeIf { it.isNotBlank() }?.let { ", $it" } ?: ""

    AppScaffold(
        title = "$hello$name!",
        isTab = true,
        actions = {
            IconButton(onClick = { nav.navigate(Routes.TOOLS) }) { Icon(Icons.Default.Build, "Herramientas") }
            IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) { Icon(Icons.Default.Settings, "Ajustes") }
        },
    ) { pad ->
        val today = LocalDate.now()
        val todayVolume = data.workouts.filter { Stats.toDate(it.startTime) == today }.sumOf { Stats.workoutVolume(data, it) }
        val weekVolume = Stats.volumeThisWeek(data)
        val lifetime = Stats.lifetimeVolume(data)
        val weekCount = Stats.workoutsThisWeek(data)
        val goal = data.settings.weeklyGoal.coerceAtLeast(1)
        val last = data.workouts.maxByOrNull { it.startTime }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                if (data.activeWorkout == null) {
                    Button(
                        onClick = { nav.navigate(Routes.TRAIN) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Empezar entreno", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    Button(
                        onClick = { nav.navigate(Routes.WORKOUT) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Continuar: ${data.activeWorkout?.name}", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    }
                }
            }

            item {
                when {
                    todayVolume > 0 -> ComparisonCard(todayVolume, "Hoy has levantado")
                    weekVolume > 0 -> ComparisonCard(weekVolume, "Esta semana has levantado")
                    else -> SectionCard {
                        Text("🐯 ¿Cuánto pesará lo que levantes hoy?", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Cuando termines un entreno te diré a qué equivale todo el peso que has movido: un bebé tigre, un caballo, un elefante…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                SectionCard(title = "Objetivo semanal") {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$weekCount", style = MaterialTheme.typography.displaySmall)
                        Text(" / $goal entrenos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp))
                        Spacer(Modifier.weight(1f))
                        val streak = Stats.weekStreak(data)
                        Text(
                            if (streak > 0) "🔥 $streak ${if (streak == 1) "semana" else "semanas"} seguidas" else "¡Empieza tu racha!",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (weekCount.toFloat() / goal).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        drawStopIndicator = {},
                    )
                    if (weekCount >= goal) {
                        Spacer(Modifier.height(8.dp))
                        Text("🏆 ¡Objetivo de la semana cumplido!", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile("Entrenos", "${data.workouts.size}", Modifier.weight(1f), sub = "en total")
                    StatTile("Esta semana", "${formatKg(weekVolume)} kg", Modifier.weight(1f), sub = "de volumen")
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile("Duración media", formatDuration(Stats.averageDurationMin(data)), Modifier.weight(1f))
                    val bw = data.body.maxByOrNull { it.date }
                    StatTile(
                        "Peso corporal",
                        bw?.let { "${formatWeight(it.weight)} kg" } ?: "—",
                        Modifier.weight(1f).clickable { nav.navigate(Routes.BODY) },
                        sub = if (bw == null) "Toca para añadir" else Stats.toDate(bw.date).format(mediumDate),
                    )
                }
            }

            if (lifetime > 0) item {
                SectionCard(title = "Desde que usas la app") {
                    val c = Comparisons.compare(lifetime)
                    Text("${formatKg(lifetime)} kg", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        Comparisons.sentence(lifetime, "Has levantado"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (c != null) {
                        // Próximo hito: el siguiente objeto más pesado
                        val next = Comparisons.things.firstOrNull { it.kg > lifetime }
                        if (next != null) {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Próximo hito: ${next.article} ${next.singular} ${next.emoji} (${formatKg(next.kg)} kg)",
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (lifetime / next.kg).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.tertiary,
                                drawStopIndicator = {},
                            )
                        }
                    }
                }
            }

            if (last != null) item {
                SectionCard(
                    modifier = Modifier.clickable { nav.navigate(Routes.workoutDetail(last.id)) },
                    title = "Último entreno",
                    action = { Icon(Icons.Default.ChevronRight, null) },
                ) {
                    Text(last.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${Stats.toDate(last.startTime).format(mediumDate)} · ${formatDuration(Stats.durationMin(last))} · " +
                            "${Stats.completedSets(last)} series · ${formatKg(Stats.workoutVolume(data, last))} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                SectionCard(
                    modifier = Modifier.clickable { nav.navigate(Routes.BODY) },
                    title = "Peso y medidas",
                    action = { Icon(Icons.Default.MonitorWeight, null) },
                ) {
                    Text(
                        "Registra tu peso corporal y medidas para ver la evolución en gráficos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
