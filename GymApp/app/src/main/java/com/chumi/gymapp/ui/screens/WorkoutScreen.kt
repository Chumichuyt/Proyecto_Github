package com.chumi.gymapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.AppData
import com.chumi.gymapp.data.Comparisons
import com.chumi.gymapp.data.Exercise
import com.chumi.gymapp.data.ExerciseType
import com.chumi.gymapp.data.Stats
import com.chumi.gymapp.data.WorkoutExercise
import com.chumi.gymapp.data.WorkoutSet
import com.chumi.gymapp.data.formatKg
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.RestTimer
import com.chumi.gymapp.ui.Routes
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.ConfirmDialog
import com.chumi.gymapp.ui.components.ExercisePickerDialog
import com.chumi.gymapp.ui.components.MuscleBadge
import com.chumi.gymapp.ui.components.NumberCell
import com.chumi.gymapp.ui.components.SectionCard
import com.chumi.gymapp.ui.components.TextInputDialog
import com.chumi.gymapp.ui.components.formatClock
import com.chumi.gymapp.ui.components.formatSec
import com.chumi.gymapp.ui.components.setText
import kotlinx.coroutines.delay

@Composable
fun WorkoutScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsStateWithLifecycle()
    val rest by vm.rest.collectAsStateWithLifecycle()
    val workout = data.activeWorkout
    var finishing by remember { mutableStateOf(false) }

    LaunchedEffect(workout == null) {
        if (workout == null && !finishing) nav.popBackStack()
    }
    if (workout == null) return

    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(1000) } }

    var menu by remember { mutableStateOf(false) }
    var renaming by remember { mutableStateOf(false) }
    var notesOpen by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(false) }
    var replacing by remember { mutableStateOf<String?>(null) }
    var confirmFinish by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var plates by remember { mutableStateOf(false) }
    var restEditFor by remember { mutableStateOf<WorkoutExercise?>(null) }

    BackHandler { nav.popBackStack() } // volver no cancela el entreno; queda en curso

    val volume = Stats.workoutVolume(data, workout)
    val doneSets = Stats.completedSets(workout)
    val totalSets = workout.exercises.sumOf { it.sets.size }
    val elapsedSec = ((now - workout.startTime) / 1000).toInt()

    fun finish() {
        finishing = true
        val id = vm.finishWorkout()
        if (id != null) nav.navigate(Routes.workoutDetail(id, justFinished = true)) {
            popUpTo(Routes.WORKOUT) { inclusive = true }
        } else nav.popBackStack()
    }

    AppScaffold(
        title = workout.name,
        onBack = { nav.popBackStack() },
        actions = {
            Button(onClick = { confirmFinish = true }, modifier = Modifier.padding(end = 4.dp)) { Text("Terminar") }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Más") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Cambiar nombre") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { menu = false; renaming = true })
                    DropdownMenuItem(text = { Text("Notas del entreno") }, leadingIcon = { Icon(Icons.Default.Notes, null) }, onClick = { menu = false; notesOpen = true })
                    DropdownMenuItem(text = { Text("Calculadora de discos") }, leadingIcon = { Icon(Icons.Default.Calculate, null) }, onClick = { menu = false; plates = true })
                    DropdownMenuItem(text = { Text("Descanso manual (2:00)") }, leadingIcon = { Icon(Icons.Default.Timer, null) }, onClick = { menu = false; vm.startRest(120) })
                    DropdownMenuItem(text = { Text("Descartar entreno") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { menu = false; confirmDiscard = true })
                }
            }
        },
        bottomBar = { RestBar(rest, vm) },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.imePadding(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = pad.calculateTopPadding(), bottom = pad.calculateBottomPadding() + 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    LiveStat("Tiempo", formatClock(elapsedSec))
                    LiveStat("Volumen", "${formatKg(volume)} kg")
                    LiveStat("Series", "$doneSets / $totalSets")
                }
                if (volume > 0) {
                    Spacer(Modifier.height(8.dp))
                    val c = Comparisons.compare(volume)
                    Text(
                        "Ya llevas ${formatKg(volume)} kg${c?.let { " ≈ ${it.phrase} ${it.thing.emoji}" } ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            items(workout.exercises, key = { it.id }) { we ->
                val ex = data.exercises.find { it.id == we.exerciseId }
                ExerciseCard(
                    data = data,
                    we = we,
                    ex = ex,
                    workoutId = workout.id,
                    vm = vm,
                    onReplace = { replacing = we.id },
                    onRest = { restEditFor = we },
                    onOpenDetail = { if (ex != null) nav.navigate(Routes.exercise(ex.id)) },
                )
            }

            item {
                FilledTonalButton(onClick = { picking = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Añadir ejercicio")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { confirmFinish = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Icon(Icons.Default.Check, null); Spacer(Modifier.width(6.dp)); Text("Terminar entreno")
                }
            }
        }
    }

    if (picking) ExercisePickerDialog(
        exercises = data.exercises,
        onPick = vm::addExercisesToActive,
        onCreate = vm::saveExercise,
        onDismiss = { picking = false },
    )
    replacing?.let { weId ->
        ExercisePickerDialog(
            exercises = data.exercises, multiple = false,
            onPick = { ids -> ids.firstOrNull()?.let { vm.replaceExercise(weId, it) } },
            onCreate = vm::saveExercise,
            onDismiss = { replacing = null },
        )
    }
    if (renaming) TextInputDialog("Nombre del entreno", workout.name, "Nombre", vm::renameActive) { renaming = false }
    if (notesOpen) NotesDialog(workout.notes, vm::setActiveNotes) { notesOpen = false }
    if (plates) AlertDialog(
        onDismissRequest = { plates = false },
        title = { Text("Calculadora de discos") },
        text = { PlateCalculator(data.settings.barWeight) },
        confirmButton = { TextButton(onClick = { plates = false }) { Text("Cerrar") } },
    )
    restEditFor?.let { we ->
        RestPickerDialog(we.restSec, { vm.setExerciseRest(we.id, it) }) { restEditFor = null }
    }
    if (confirmFinish) {
        val pending = totalSets - doneSets
        ConfirmDialog(
            title = "¿Terminar entreno?",
            text = when {
                doneSets == 0 -> "No has marcado ninguna serie como hecha, así que el entreno no se guardará."
                pending > 0 -> "Tienes $pending series sin marcar (✓). Solo se guardarán las series hechas."
                else -> "¡Buen trabajo! Se guardará el entreno en tu historial."
            },
            confirm = "Terminar",
            onConfirm = ::finish,
            onDismiss = { confirmFinish = false },
        )
    }
    if (confirmDiscard) ConfirmDialog(
        title = "Descartar entreno",
        text = "Se perderá todo lo registrado en este entreno. ¿Seguro?",
        confirm = "Descartar",
        onConfirm = { vm.discardWorkout() },
        onDismiss = { confirmDiscard = false },
    )
}

@Composable
private fun LiveStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun ExerciseCard(
    data: AppData,
    we: WorkoutExercise,
    ex: Exercise?,
    workoutId: String,
    vm: AppViewModel,
    onReplace: () -> Unit,
    onRest: () -> Unit,
    onOpenDetail: () -> Unit,
) {
    val type = ex?.type ?: ExerciseType.WEIGHT_REPS
    val prev = remember(we.exerciseId, data.workouts.size) { Stats.previousSets(data, we.exerciseId, workoutId) }
    val prevWorking = prev.filter { !it.warmup }
    var menu by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(false) }

    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).clickable(onClick = onOpenDetail)) {
                Text(ex?.name ?: "Ejercicio eliminado", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (ex != null) MuscleBadge(ex.muscle)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Descanso ${formatSec(we.restSec)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable(onClick = onRest).padding(4.dp),
                    )
                }
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Opciones") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Subir") }, leadingIcon = { Icon(Icons.Default.KeyboardArrowUp, null) }, onClick = { menu = false; vm.moveExercise(we.id, -1) })
                    DropdownMenuItem(text = { Text("Bajar") }, leadingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) }, onClick = { menu = false; vm.moveExercise(we.id, 1) })
                    DropdownMenuItem(text = { Text("Cambiar ejercicio") }, leadingIcon = { Icon(Icons.Default.SwapHoriz, null) }, onClick = { menu = false; onReplace() })
                    DropdownMenuItem(text = { Text("Tiempo de descanso") }, leadingIcon = { Icon(Icons.Default.Timer, null) }, onClick = { menu = false; onRest() })
                    DropdownMenuItem(text = { Text("Notas") }, leadingIcon = { Icon(Icons.Default.Notes, null) }, onClick = { menu = false; notes = true })
                    DropdownMenuItem(text = { Text("Quitar ejercicio") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { menu = false; vm.removeExercise(we.id) })
                }
            }
        }
        if (we.notes.isNotBlank()) {
            Text("📝 ${we.notes}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 4.dp))
        }
        if (ex?.notes?.isNotBlank() == true) {
            Text("💡 ${ex.notes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))

        // Cabecera de la tabla
        val (col1, col2) = when (type) {
            ExerciseType.WEIGHT_REPS -> "KG" to "REPS"
            ExerciseType.BODYWEIGHT -> "+KG" to "REPS"
            ExerciseType.CARDIO -> "MIN" to "KM"
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            HeaderCell("SERIE", Modifier.width(44.dp))
            HeaderCell("ANTERIOR", Modifier.weight(1f))
            HeaderCell(col1, Modifier.width(76.dp))
            Spacer(Modifier.width(6.dp))
            HeaderCell(col2, Modifier.width(64.dp))
            Spacer(Modifier.width(6.dp))
            HeaderCell("✓", Modifier.width(44.dp))
        }

        var workingIndex = 0
        we.sets.forEachIndexed { i, s ->
            val label = if (s.warmup) "C" else (++workingIndex).toString()
            val p = if (s.warmup) null else prevWorking.getOrNull(workingIndex - 1)
            key(s.id) {
                SetRow(
                    label = label,
                    set = s,
                    type = type,
                    previous = p?.let { setText(it.weight, it.reps, type, it.durationMin, it.distanceKm) } ?: "—",
                    onPrevious = p?.let { { vm.updateSet(we.id, s.id) { cur -> cur.copy(weight = it.weight, reps = it.reps, durationMin = it.durationMin, distanceKm = it.distanceKm) } } },
                    onChange = { f -> vm.updateSet(we.id, s.id, f) },
                    onToggle = { vm.toggleSetDone(we.id, s.id) },
                    onDelete = { vm.removeSet(we.id, s.id) },
                )
            }
            if (i < we.sets.lastIndex) Spacer(Modifier.height(6.dp))
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { vm.addSet(we.id) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("Añadir serie")
        }
    }

    if (notes) NotesDialog(we.notes, { vm.setExerciseNotes(we.id, it) }) { notes = false }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier) {
    Text(
        text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center, modifier = modifier,
    )
}

@Composable
private fun SetRow(
    label: String,
    set: WorkoutSet,
    type: ExerciseType,
    previous: String,
    onPrevious: (() -> Unit)?,
    onChange: ((WorkoutSet) -> WorkoutSet) -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    val doneBg = if (set.done) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceContainer
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(doneBg).padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(44.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(32.dp).clip(CircleShape)
                    .background(if (set.warmup) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable { menu = true },
                contentAlignment = Alignment.Center,
            ) {
                Text(label, fontWeight = FontWeight.Bold, color = if (set.warmup) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface)
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(
                    text = { Text(if (set.warmup) "Marcar como serie normal" else "Marcar como calentamiento") },
                    onClick = { menu = false; onChange { it.copy(warmup = !it.warmup) } },
                )
                DropdownMenuItem(text = { Text("Eliminar serie") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { menu = false; onDelete() })
            }
        }
        Text(
            previous,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.weight(1f).let { if (onPrevious != null) it.clickable(onClick = onPrevious) else it },
        )
        when (type) {
            ExerciseType.CARDIO -> {
                NumberCell(set.durationMin, { v -> onChange { it.copy(durationMin = v) } }, Modifier.width(76.dp))
                Spacer(Modifier.width(6.dp))
                NumberCell(set.distanceKm, { v -> onChange { it.copy(distanceKm = v) } }, Modifier.width(64.dp))
            }
            else -> {
                NumberCell(set.weight, { v -> onChange { it.copy(weight = v) } }, Modifier.width(76.dp))
                Spacer(Modifier.width(6.dp))
                NumberCell(set.reps.toDouble(), { v -> onChange { it.copy(reps = v.toInt()) } }, Modifier.width(64.dp), decimals = false)
            }
        }
        Spacer(Modifier.width(6.dp))
        Box(Modifier.width(44.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (set.done) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Check, "Hecha",
                    tint = if (set.done) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RestBar(rest: RestTimer?, vm: AppViewModel) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(rest) {
        while (rest != null) { now = System.currentTimeMillis(); delay(200) }
    }
    AnimatedVisibility(visible = rest != null) {
        val r = rest ?: return@AnimatedVisibility
        val remaining = ((r.endAt - now) / 1000).toInt().coerceAtLeast(0) + if ((r.endAt - now) % 1000 > 0) 1 else 0
        Surface(color = MaterialTheme.colorScheme.secondaryContainer, tonalElevation = 3.dp) {
            Column(Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Descanso ${formatSec(remaining.coerceAtMost(r.totalSec))}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { vm.adjustRest(-15) }) { Text("−15") }
                    TextButton(onClick = { vm.adjustRest(15) }) { Text("+15") }
                    TextButton(onClick = { vm.stopRest() }) { Text("Saltar") }
                }
                LinearProgressIndicator(
                    progress = { (remaining.toFloat() / r.totalSec).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    drawStopIndicator = {},
                )
            }
        }
    }
}

@Composable
private fun NotesDialog(initial: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notas") },
        text = { OutlinedTextField(text, { text = it }, minLines = 3, modifier = Modifier.fillMaxWidth()) },
        confirmButton = { TextButton(onClick = { onSave(text.trim()); onDismiss() }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun RestPickerDialog(current: Int, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    val options = listOf(0, 30, 45, 60, 75, 90, 120, 150, 180, 240, 300)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tiempo de descanso") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                options.forEach { s ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onPick(s); onDismiss() }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(if (s == 0) "Sin descanso automático" else formatSec(s), modifier = Modifier.weight(1f))
                        if (s == current) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
    )
}
