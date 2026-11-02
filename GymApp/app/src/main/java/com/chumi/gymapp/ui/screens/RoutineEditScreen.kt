package com.chumi.gymapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.ExerciseType
import com.chumi.gymapp.data.Routine
import com.chumi.gymapp.data.RoutineExercise
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.ExercisePickerDialog
import com.chumi.gymapp.ui.components.LabeledNumberField
import com.chumi.gymapp.ui.components.MuscleBadge
import com.chumi.gymapp.ui.components.SectionCard

@Composable
fun RoutineEditScreen(vm: AppViewModel, nav: NavHostController, routineId: String) {
    val data by vm.data.collectAsStateWithLifecycle()
    val original = remember { data.routines.find { it.id == routineId } }
    var name by remember { mutableStateOf(original?.name ?: "") }
    var description by remember { mutableStateOf(original?.description ?: "") }
    // cada elemento lleva una clave única para que la lista se reordene bien
    val items = remember { mutableStateListOf<Pair<Long, RoutineExercise>>().apply { original?.exercises?.forEachIndexed { i, e -> add(i.toLong() to e) } } }
    var nextKey by remember { mutableStateOf(1000L) }
    var picking by remember { mutableStateOf(false) }

    fun save() {
        val r = (original ?: Routine(name = name)).copy(
            name = name.trim().ifBlank { "Mi rutina" },
            description = description.trim(),
            exercises = items.map { it.second },
        )
        vm.saveRoutine(r)
        nav.popBackStack()
    }

    AppScaffold(
        title = if (original == null) "Nueva rutina" else "Editar rutina",
        onBack = { nav.popBackStack() },
        actions = {
            TextButton(onClick = ::save, enabled = name.isNotBlank()) {
                Icon(Icons.Default.Check, null); Spacer(Modifier.width(4.dp)); Text("Guardar")
            }
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = pad.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre de la rutina") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(description, { description = it }, label = { Text("Descripción (opcional)") }, modifier = Modifier.fillMaxWidth())
            }
            itemsIndexed(items, key = { _, it -> it.first }) { index, (k, re) ->
                val ex = data.exercises.find { it.id == re.exerciseId }
                key(k) {
                    SectionCard(
                        title = ex?.name ?: "Ejercicio eliminado",
                        action = {
                            Row {
                                IconButton(onClick = { if (index > 0) { val t = items[index]; items[index] = items[index - 1]; items[index - 1] = t } }, enabled = index > 0) {
                                    Icon(Icons.Default.KeyboardArrowUp, "Subir")
                                }
                                IconButton(onClick = { if (index < items.lastIndex) { val t = items[index]; items[index] = items[index + 1]; items[index + 1] = t } }, enabled = index < items.lastIndex) {
                                    Icon(Icons.Default.KeyboardArrowDown, "Bajar")
                                }
                                IconButton(onClick = { items.removeAt(index) }) { Icon(Icons.Default.Close, "Quitar") }
                            }
                        },
                    ) {
                        if (ex != null) MuscleBadge(ex.muscle)
                        Spacer(Modifier.height(8.dp))
                        fun upd(f: (RoutineExercise) -> RoutineExercise) {
                            val i = items.indexOfFirst { it.first == k }
                            if (i >= 0) items[i] = k to f(items[i].second)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LabeledNumberField("Series", re.sets.toDouble(), { upd { r -> r.copy(sets = it.toInt().coerceIn(1, 20)) } }, Modifier.weight(1f), decimals = false)
                            if (ex?.type != ExerciseType.CARDIO) LabeledNumberField("Reps", re.reps.toDouble(), { upd { r -> r.copy(reps = it.toInt()) } }, Modifier.weight(1f), decimals = false)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (ex?.type != ExerciseType.CARDIO) LabeledNumberField("Peso inicial", re.weight, { upd { r -> r.copy(weight = it) } }, Modifier.weight(1f), suffix = "kg")
                            LabeledNumberField("Descanso", re.restSec.toDouble(), { upd { r -> r.copy(restSec = it.toInt()) } }, Modifier.weight(1f), decimals = false, suffix = "s")
                        }
                    }
                }
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (items.isEmpty()) Text(
                        "Añade los ejercicios de esta rutina.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    FilledTonalButton(onClick = { picking = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Añadir ejercicios")
                    }
                    Text(
                        "Consejo: al empezar la rutina se cargará automáticamente el peso que usaste la última vez.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
            }
        }
    }

    if (picking) ExercisePickerDialog(
        exercises = data.exercises,
        onPick = { ids ->
            ids.forEach { id -> items.add(nextKey++ to RoutineExercise(exerciseId = id, restSec = data.settings.defaultRestSec)) }
        },
        onCreate = vm::saveExercise,
        onDismiss = { picking = false },
    )
}

