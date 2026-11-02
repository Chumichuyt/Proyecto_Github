package com.chumi.gymapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.Routine
import com.chumi.gymapp.data.Stats
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.Routes
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.ConfirmDialog
import com.chumi.gymapp.ui.components.EmptyState
import com.chumi.gymapp.ui.components.SectionCard
import com.chumi.gymapp.ui.components.mediumDate

@Composable
fun TrainScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsStateWithLifecycle()
    var pendingStart by remember { mutableStateOf<(() -> Unit)?>(null) }
    var toDelete by remember { mutableStateOf<Routine?>(null) }

    fun start(action: () -> Unit) {
        if (data.activeWorkout != null) pendingStart = action
        else { action(); nav.navigate(Routes.WORKOUT) }
    }

    AppScaffold(
        title = "Entrenar",
        isTab = true,
        fab = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Routes.routine("new")) },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nueva rutina") },
            )
        },
    ) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard(title = "Entreno rápido") {
                    Text(
                        "Empieza sin rutina y añade los ejercicios sobre la marcha.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    FilledTonalButton(onClick = { start { vm.startEmptyWorkout() } }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Empezar entreno vacío")
                    }
                }
            }
            item {
                Text("Mis rutinas (${data.routines.size})", style = MaterialTheme.typography.titleMedium)
            }
            if (data.routines.isEmpty()) item {
                EmptyState("📋", "Aún no tienes rutinas", "Crea una con el botón \"Nueva rutina\".")
            }
            items(data.routines.sortedByDescending { it.lastUsed }, key = { it.id }) { r ->
                RoutineCard(
                    r = r,
                    exerciseNames = r.exercises.mapNotNull { re -> data.exercises.find { it.id == re.exerciseId }?.name },
                    onStart = { start { vm.startRoutine(r.id) } },
                    onEdit = { nav.navigate(Routes.routine(r.id)) },
                    onDuplicate = { vm.duplicateRoutine(r.id) },
                    onDelete = { toDelete = r },
                )
            }
        }
    }

    pendingStart?.let { action ->
        ConfirmDialog(
            title = "Ya tienes un entreno en curso",
            text = "Si empiezas otro, se descartará el entreno actual sin guardar. ¿Continuar?",
            confirm = "Descartar y empezar",
            onConfirm = { vm.discardWorkout(); action(); nav.navigate(Routes.WORKOUT) },
            onDismiss = { pendingStart = null },
        )
    }
    toDelete?.let { r ->
        ConfirmDialog(
            title = "Borrar rutina",
            text = "¿Seguro que quieres borrar \"${r.name}\"? Tu historial no se borra.",
            confirm = "Borrar",
            onConfirm = { vm.deleteRoutine(r.id) },
            onDismiss = { toDelete = null },
        )
    }
}

@Composable
private fun RoutineCard(
    r: Routine,
    exerciseNames: List<String>,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    SectionCard(
        modifier = Modifier.clickable(onClick = onEdit),
        title = r.name,
        action = {
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, "Opciones") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Editar") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { menu = false; onEdit() })
                    DropdownMenuItem(text = { Text("Duplicar") }, leadingIcon = { Icon(Icons.Default.ContentCopy, null) }, onClick = { menu = false; onDuplicate() })
                    DropdownMenuItem(text = { Text("Borrar") }, leadingIcon = { Icon(Icons.Default.Delete, null) }, onClick = { menu = false; onDelete() })
                }
            }
        },
    ) {
        if (r.description.isNotBlank()) {
            Text(r.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
        }
        Text(
            exerciseNames.joinToString(" · ").ifBlank { "Sin ejercicios" },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (r.lastUsed > 0) "Última vez: ${Stats.toDate(r.lastUsed).format(mediumDate)}" else "${r.exercises.size} ejercicios",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(onClick = onStart, shape = RoundedCornerShape(12.dp), enabled = r.exercises.isNotEmpty()) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(4.dp))
                Text("Empezar")
            }
        }
    }
}
