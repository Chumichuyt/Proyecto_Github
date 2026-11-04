package com.chumi.gymapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.ExerciseType
import com.chumi.gymapp.data.MuscleGroup
import com.chumi.gymapp.data.Stats
import com.chumi.gymapp.data.formatKg
import com.chumi.gymapp.data.formatWeight
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.Routes
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.ChartPoint
import com.chumi.gymapp.ui.components.ConfirmDialog
import com.chumi.gymapp.ui.components.EmptyState
import com.chumi.gymapp.ui.components.ExerciseEditDialog
import com.chumi.gymapp.ui.components.LineChart
import com.chumi.gymapp.ui.components.MuscleBadge
import com.chumi.gymapp.ui.components.MuscleFilterRow
import com.chumi.gymapp.ui.components.SectionCard
import com.chumi.gymapp.ui.components.StatTile
import com.chumi.gymapp.ui.components.mediumDate
import com.chumi.gymapp.ui.components.normalize
import com.chumi.gymapp.ui.components.setText

@Composable
fun ExercisesScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var muscle by remember { mutableStateOf<MuscleGroup?>(null) }
    var creating by remember { mutableStateOf(false) }
    val list = data.exercises
        .filter { muscle == null || it.muscle == muscle }
        .filter { query.isBlank() || normalize(it.name).contains(normalize(query)) }
        .sortedBy { it.name }
    val grouped = list.groupBy { it.muscle }.toSortedMap(compareBy { it.ordinal })

    AppScaffold(
        title = "Ejercicios (${data.exercises.size})",
        onBack = { nav.popBackStack() },
        fab = {
            ExtendedFloatingActionButton(onClick = { creating = true }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Nuevo") })
        },
    ) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = pad.calculateBottomPadding() + 96.dp),
        ) {
            item {
                OutlinedTextField(
                    query, { query = it }, leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Buscar ejercicio…") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                MuscleFilterRow(muscle, { muscle = it })
                Spacer(Modifier.height(8.dp))
            }
            grouped.forEach { (m, exs) ->
                item(key = "h_${m.name}") {
                    Text(
                        "${m.label} (${exs.size})",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                }
                items(exs, key = { it.id }) { e ->
                    val times = remember(data.workouts) { data.workouts.count { w -> w.exercises.any { it.exerciseId == e.id } } }
                    Row(
                        Modifier.fillMaxWidth().clickable { nav.navigate(Routes.exercise(e.id)) }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(e.name, style = MaterialTheme.typography.bodyLarge, fontWeight = if (times > 0) FontWeight.SemiBold else FontWeight.Normal)
                            Text(
                                listOfNotNull(e.equipment.ifBlank { null }, if (times > 0) "$times sesiones" else null, if (e.custom) "Personalizado" else null).joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
            }
            if (list.isEmpty()) item { EmptyState("🔍", "Sin resultados", "Prueba con otra búsqueda o crea el ejercicio.") }
        }
    }
    if (creating) ExerciseEditDialog(null, vm::saveExercise) { creating = false }
}

private enum class ExMetric(val label: String) {
    E1RM("1RM estimado"), MAX("Peso máximo"), VOLUME("Volumen"), REPS("Reps totales"), MINUTES("Minutos"), KM("Km")
}

@Composable
fun ExerciseDetailScreen(vm: AppViewModel, nav: NavHostController, exerciseId: String) {
    val data by vm.data.collectAsStateWithLifecycle()
    val ex = data.exercises.find { it.id == exerciseId }
    var editing by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    if (ex == null) {
        AppScaffold("Ejercicio", onBack = { nav.popBackStack() }) { pad ->
            Box(Modifier.padding(pad)) { EmptyState("🤷", "Ejercicio no encontrado", "") }
        }
        return
    }
    val metrics = when (ex.type) {
        ExerciseType.CARDIO -> listOf(ExMetric.MINUTES, ExMetric.KM)
        ExerciseType.BODYWEIGHT -> listOf(ExMetric.REPS, ExMetric.E1RM, ExMetric.MAX, ExMetric.VOLUME)
        ExerciseType.WEIGHT_REPS -> listOf(ExMetric.E1RM, ExMetric.MAX, ExMetric.VOLUME, ExMetric.REPS)
    }
    var metric by remember { mutableStateOf(metrics.first()) }
    val hist = remember(data.workouts, data.body) { Stats.exerciseHistory(data, ex.id) }
    val pr = remember(data.workouts) { Stats.records(data, ex.id) }

    AppScaffold(
        title = ex.name,
        onBack = { nav.popBackStack() },
        actions = {
            IconButton(onClick = { editing = true }) { Icon(Icons.Default.Edit, "Editar") }
            if (ex.custom) IconButton(onClick = { deleting = true }) { Icon(Icons.Default.Delete, "Borrar") }
        },
    ) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = pad.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MuscleBadge(ex.muscle)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        listOf(ex.equipment, ex.type.label).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (ex.notes.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text("💡 ${ex.notes}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (ex.type != ExerciseType.CARDIO) item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("Peso máx.", if (pr.maxWeight > 0) "${formatWeight(pr.maxWeight)} kg" else "—", Modifier.weight(1f), sub = if (pr.maxWeight > 0) "× ${pr.maxWeightReps} reps" else null)
                    StatTile("1RM est.", if (pr.best1RM > 0) "${formatKg(pr.best1RM)} kg" else "—", Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("Mejor sesión", if (pr.maxVolume > 0) "${formatKg(pr.maxVolume)} kg" else "—", Modifier.weight(1f), sub = "de volumen")
                    StatTile("Sesiones", "${pr.timesDone}", Modifier.weight(1f), sub = "máx. ${pr.maxReps} reps")
                }
            }
            item {
                SectionCard(title = "Evolución") {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        metrics.forEach { m -> FilterChip(metric == m, { metric = m }, label = { Text(m.label) }) }
                    }
                    Spacer(Modifier.height(8.dp))
                    val pts = hist.map {
                        ChartPoint(
                            it.date,
                            when (metric) {
                                ExMetric.E1RM -> Math.round(it.best1RM * 10) / 10.0
                                ExMetric.MAX -> it.maxWeight
                                ExMetric.VOLUME -> it.volume
                                ExMetric.REPS -> it.totalReps.toDouble()
                                ExMetric.MINUTES -> it.durationMin
                                ExMetric.KM -> it.distanceKm
                            },
                        )
                    }
                    val unit = when (metric) {
                        ExMetric.REPS -> " reps"; ExMetric.MINUTES -> " min"; ExMetric.KM -> " km"; else -> " kg"
                    }
                    LineChart(pts, valueText = { formatWeight(Math.round(it * 10) / 10.0) + unit })
                    if (metric == ExMetric.E1RM) Text(
                        "El 1RM estimado (fórmula de Epley) es el peso que podrías levantar una sola vez.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            item { Text("Historial", style = MaterialTheme.typography.titleMedium) }
            if (hist.isEmpty()) item { EmptyState("🏋️", "Sin registros", "Aún no has hecho este ejercicio.") }
            items(hist.reversed(), key = { it.workoutId }) { p ->
                val w = data.workouts.find { it.id == p.workoutId } ?: return@items
                SectionCard(modifier = Modifier.clickable { nav.navigate(Routes.workoutDetail(w.id)) }) {
                    Row {
                        Text(Stats.toDate(w.startTime).format(mediumDate), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        if (p.volume > 0) Text("${formatKg(p.volume)} kg", style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        w.exercises.filter { it.exerciseId == ex.id }.flatMap { it.sets }
                            .joinToString("  ·  ") { (if (it.warmup) "C " else "") + setText(it.weight, it.reps, ex.type, it.durationMin, it.distanceKm) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
    if (editing) ExerciseEditDialog(ex, vm::saveExercise) { editing = false }
    if (deleting) ConfirmDialog(
        "Borrar ejercicio", "Se quitará de tus rutinas. Los entrenos pasados se conservan.", "Borrar",
        onConfirm = { nav.popBackStack(); vm.deleteExercise(ex.id) },
        onDismiss = { deleting = false },
    )
}
