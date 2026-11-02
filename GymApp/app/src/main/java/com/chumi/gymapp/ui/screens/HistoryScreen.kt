package com.chumi.gymapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.AppData
import com.chumi.gymapp.data.Comparisons
import com.chumi.gymapp.data.ExerciseType
import com.chumi.gymapp.data.Stats
import com.chumi.gymapp.data.Workout
import com.chumi.gymapp.data.formatKg
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.Routes
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.ComparisonCard
import com.chumi.gymapp.ui.components.ConfirmDialog
import com.chumi.gymapp.ui.components.EmptyState
import com.chumi.gymapp.ui.components.SectionCard
import com.chumi.gymapp.ui.components.StatTile
import com.chumi.gymapp.ui.components.TextInputDialog
import com.chumi.gymapp.ui.components.formatDuration
import com.chumi.gymapp.ui.components.longDate
import com.chumi.gymapp.ui.components.setText
import java.time.format.DateTimeFormatter
import java.util.Locale

private val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
private val dayFmt = DateTimeFormatter.ofPattern("EEE d", Locale("es", "ES"))

@Composable
fun HistoryScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsStateWithLifecycle()
    val workouts = data.workouts.sortedByDescending { it.startTime }
    val byMonth = workouts.groupBy { Stats.toDate(it.startTime).withDayOfMonth(1) }

    AppScaffold(title = "Historial", isTab = true) { pad ->
        if (workouts.isEmpty()) {
            Box(Modifier.padding(top = pad.calculateTopPadding())) {
                EmptyState("📅", "Aún no hay entrenos", "Cuando termines tu primer entreno aparecerá aquí.")
            }
            return@AppScaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            byMonth.forEach { (month, list) ->
                item(key = "m$month") {
                    val vol = list.sumOf { Stats.workoutVolume(data, it) }
                    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Bottom) {
                        Text(month.format(monthFmt).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(
                            "${list.size} entrenos · ${formatKg(vol)} kg",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(list, key = { it.id }) { w -> WorkoutRow(data, w) { nav.navigate(Routes.workoutDetail(w.id)) } }
            }
        }
    }
}

@Composable
private fun WorkoutRow(data: AppData, w: Workout, onClick: () -> Unit) {
    val vol = Stats.workoutVolume(data, w)
    val c = Comparisons.compare(vol)
    SectionCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(w.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${Stats.toDate(w.startTime).format(dayFmt).replaceFirstChar { it.uppercase() }} · ${formatDuration(Stats.durationMin(w))} · ${Stats.completedSets(w)} series",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${formatKg(vol)} kg", style = MaterialTheme.typography.titleMedium)
                if (c != null) Text("≈ ${c.phrase} ${c.thing.emoji}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            w.exercises.mapNotNull { we -> data.exercises.find { it.id == we.exerciseId }?.name?.let { "$it ×${we.sets.size}" } }.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
        )
    }
}

@Composable
fun WorkoutDetailScreen(vm: AppViewModel, nav: NavHostController, workoutId: String, justFinished: Boolean) {
    val data by vm.data.collectAsStateWithLifecycle()
    val w = data.workouts.find { it.id == workoutId }
    var menu by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var saveAsRoutine by remember { mutableStateOf(false) }
    var editNotes by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    if (w == null) {
        AppScaffold(title = "Entreno", onBack = { nav.popBackStack() }) { pad ->
            Box(Modifier.padding(pad)) { EmptyState("🤷", "Entreno no encontrado", "Puede que se haya borrado.") }
        }
        return
    }
    val vol = Stats.workoutVolume(data, w)
    val records = remember(w.id, data.workouts.size) { Stats.newRecordsIn(data, w) }

    fun shareText(): String = buildString {
        appendLine("💪 ${w.name} — ${Stats.toDate(w.startTime).format(longDate)}")
        appendLine("⏱ ${formatDuration(Stats.durationMin(w))} · ${Stats.completedSets(w)} series · ${formatKg(vol)} kg")
        appendLine(Comparisons.sentence(vol, "He levantado"))
        if (records.isNotEmpty()) appendLine("🏆 ${records.size} récord(s) nuevo(s)")
        appendLine()
        w.exercises.forEach { we ->
            val ex = data.exercises.find { it.id == we.exerciseId } ?: return@forEach
            appendLine("• ${ex.name}: " + we.sets.joinToString(", ") { setText(it.weight, it.reps, ex.type, it.durationMin, it.distanceKm) })
        }
    }

    AppScaffold(
        title = if (justFinished) "¡Entreno completado!" else w.name,
        onBack = { nav.popBackStack() },
        actions = {
            IconButton(onClick = {
                val i = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, shareText())
                ctx.startActivity(Intent.createChooser(i, "Compartir entreno"))
            }) { Icon(Icons.Default.Share, "Compartir") }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Más") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Guardar como rutina") }, leadingIcon = { Icon(Icons.Default.BookmarkAdd, null) }, onClick = { menu = false; saveAsRoutine = true })
                    DropdownMenuItem(text = { Text("Editar notas") }, leadingIcon = { Icon(Icons.Default.Notes, null) }, onClick = { menu = false; editNotes = true })
                    DropdownMenuItem(text = { Text("Borrar entreno") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { menu = false; confirmDelete = true })
                }
            }
        },
    ) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = pad.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    (if (justFinished) "${w.name} · " else "") + Stats.toDate(w.startTime).format(longDate).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                val today = Stats.toDate(w.startTime) == java.time.LocalDate.now()
                ComparisonCard(vol, if (today) "Hoy has levantado" else "Ese día levantaste")
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("Duración", formatDuration(Stats.durationMin(w)), Modifier.weight(1f))
                    StatTile("Series", "${Stats.completedSets(w)}", Modifier.weight(1f))
                    StatTile("Reps", "${Stats.totalReps(w)}", Modifier.weight(1f))
                }
            }
            if (justFinished || w.rating > 0) item {
                SectionCard(title = "¿Qué tal te has sentido?") {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                        listOf("😫", "😕", "🙂", "😃", "🤩").forEachIndexed { i, e ->
                            val selected = w.rating == i + 1
                            Text(
                                e,
                                style = if (selected) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                    .clickable { vm.updateWorkout(w.id) { it.copy(rating = i + 1) } }
                                    .padding(6.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (w.rating == 0 || selected) 1f else 0.4f),
                            )
                        }
                    }
                }
            }
            if (records.isNotEmpty()) item {
                SectionCard(title = "🏆 Récords personales nuevos") {
                    records.forEach { r ->
                        Row(Modifier.padding(vertical = 4.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text(r.exerciseName, style = MaterialTheme.typography.bodyLarge)
                                Text(r.kind, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(r.value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            if (w.notes.isNotBlank()) item {
                SectionCard(title = "Notas") { Text(w.notes) }
            }
            items(w.exercises, key = { it.id }) { we ->
                val ex = data.exercises.find { it.id == we.exerciseId }
                val type = ex?.type ?: ExerciseType.WEIGHT_REPS
                val bw = w.bodyWeight ?: Stats.bodyWeightAt(data, w.startTime)
                val exVol = Stats.exerciseVolume(data, we, bw)
                SectionCard(
                    modifier = Modifier.clickable { if (ex != null) nav.navigate(Routes.exercise(ex.id)) },
                    title = ex?.name ?: "Ejercicio eliminado",
                    action = { if (exVol > 0) Text("${formatKg(exVol)} kg", style = MaterialTheme.typography.labelLarge) },
                ) {
                    val best = we.sets.filter { !it.warmup }.maxByOrNull { Stats.estimated1RM(it.weight, it.reps) }
                    var n = 0
                    we.sets.forEach { s ->
                        val label = if (s.warmup) "C" else (++n).toString()
                        Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(setText(s.weight, s.reps, type, s.durationMin, s.distanceKm), modifier = Modifier.weight(1f))
                            if (s == best && type != ExerciseType.CARDIO && s.weight > 0)
                                Text("1RM ≈ ${formatKg(Stats.estimated1RM(s.weight, s.reps))} kg", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                    if (we.notes.isNotBlank()) Text("📝 ${we.notes}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }

    if (confirmDelete) ConfirmDialog(
        "Borrar entreno", "Se borrará este entreno del historial y de las estadísticas.", "Borrar",
        onConfirm = { nav.popBackStack(); vm.deleteWorkout(w.id) },
        onDismiss = { confirmDelete = false },
    )
    if (saveAsRoutine) TextInputDialog("Guardar como rutina", w.name, "Nombre de la rutina", { vm.routineFromWorkout(w.id, it) }) { saveAsRoutine = false }
    if (editNotes) TextInputDialog("Notas", w.notes, "Notas", { vm.updateWorkout(w.id) { wk -> wk.copy(notes = it) } }) { editNotes = false }
}
