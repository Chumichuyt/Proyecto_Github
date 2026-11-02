package com.chumi.gymapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.chumi.gymapp.data.Comparisons
import com.chumi.gymapp.data.Exercise
import com.chumi.gymapp.data.ExerciseType
import com.chumi.gymapp.data.MuscleGroup
import com.chumi.gymapp.data.formatKg
import com.chumi.gymapp.data.formatWeight

// ---------- Formato ----------

fun parseNumber(s: String): Double? = s.trim().replace(',', '.').toDoubleOrNull()

fun formatDuration(min: Long): String = if (min < 60) "$min min" else "${min / 60} h ${min % 60} min"

fun formatSec(sec: Int): String = "%d:%02d".format(sec / 60, sec % 60)

fun formatClock(sec: Int): String =
    if (sec >= 3600) "%d:%02d:%02d".format(sec / 3600, (sec % 3600) / 60, sec % 60) else formatSec(sec)

fun setText(weight: Double, reps: Int, type: ExerciseType, durationMin: Double = 0.0, distanceKm: Double = 0.0): String = when (type) {
    ExerciseType.WEIGHT_REPS -> "${formatWeight(weight)} kg × $reps"
    ExerciseType.BODYWEIGHT -> if (weight > 0) "+${formatWeight(weight)} kg × $reps" else "$reps reps"
    ExerciseType.CARDIO -> buildString {
        append("${formatWeight(durationMin)} min")
        if (distanceKm > 0) append(" · ${formatWeight(distanceKm)} km")
    }
}

// ---------- Tarjetas ----------

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            if (title != null || action != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    if (title != null) Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    action?.invoke()
                }
                Spacer(Modifier.height(12.dp))
            }
            content()
        }
    }
}

@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier, sub: String? = null) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            if (sub != null) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

/** Tarjeta grande y divertida: "Hoy has levantado 130 kg, lo mismo que pesa un bebé tigre 🐯". */
@Composable
fun ComparisonCard(kg: Double, prefix: String, modifier: Modifier = Modifier) {
    val c = Comparisons.compare(kg)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp),
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(c?.thing?.emoji ?: "🏋️", style = MaterialTheme.typography.displaySmall)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${formatKg(kg)} kg",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    Comparisons.sentence(kg, prefix),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
fun MuscleBadge(muscle: MuscleGroup, modifier: Modifier = Modifier) {
    Text(
        muscle.label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun EmptyState(emoji: String, title: String, text: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

// ---------- Campos numéricos ----------

/**
 * Campo numérico compacto. Mantiene su propio texto para que se pueda escribir "62," sin que se borre.
 */
@Composable
fun NumberCell(
    value: Double,
    onValue: (Double) -> Unit,
    modifier: Modifier = Modifier,
    decimals: Boolean = true,
    placeholder: String = "0",
    enabled: Boolean = true,
) {
    var text by remember { mutableStateOf(if (value == 0.0) "" else formatWeight(value)) }
    LaunchedEffect(value) {
        if ((parseNumber(text) ?: 0.0) != value) text = if (value == 0.0) "" else formatWeight(value)
    }
    val style = MaterialTheme.typography.titleMedium.copy(
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
    BasicTextField(
        value = text,
        onValueChange = { s ->
            val filtered = s.filter { it.isDigit() || (decimals && (it == ',' || it == '.')) }.take(7)
            text = filtered
            onValue(parseNumber(filtered) ?: 0.0)
        },
        enabled = enabled,
        singleLine = true,
        textStyle = style,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(keyboardType = if (decimals) KeyboardType.Decimal else KeyboardType.Number),
        modifier = modifier.height(44.dp),
        decorationBox = { inner ->
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (text.isEmpty()) Text(placeholder, style = style.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)))
                inner()
            }
        },
    )
}

@Composable
fun LabeledNumberField(label: String, value: Double, onValue: (Double) -> Unit, modifier: Modifier = Modifier, decimals: Boolean = true, suffix: String? = null) {
    var text by remember { mutableStateOf(if (value == 0.0) "" else formatWeight(value)) }
    OutlinedTextField(
        value = text,
        onValueChange = { s ->
            val f = s.filter { it.isDigit() || (decimals && (it == ',' || it == '.')) }.take(8)
            text = f
            onValue(parseNumber(f) ?: 0.0)
        },
        label = { Text(label) },
        suffix = suffix?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimals) KeyboardType.Decimal else KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
fun ConfirmDialog(title: String, text: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = { onConfirm(); onDismiss() }) { Text(confirm) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
fun TextInputDialog(title: String, initial: String, label: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(text, { text = it }, label = { Text(label) }, singleLine = true) },
        confirmButton = {
            TextButton(enabled = text.isNotBlank(), onClick = { onConfirm(text.trim()); onDismiss() }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

// ---------- Selector de ejercicios ----------

@Composable
fun MuscleFilterRow(selected: MuscleGroup?, onSelect: (MuscleGroup?) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("Todos") })
        MuscleGroup.entries.forEach { m ->
            FilterChip(selected = selected == m, onClick = { onSelect(if (selected == m) null else m) }, label = { Text(m.label) })
        }
    }
}

fun normalize(s: String): String = java.text.Normalizer.normalize(s.lowercase(), java.text.Normalizer.Form.NFD)
    .replace(Regex("\\p{M}"), "")

@Composable
fun ExercisePickerDialog(
    exercises: List<Exercise>,
    multiple: Boolean = true,
    onPick: (List<String>) -> Unit,
    onCreate: (Exercise) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var muscle by remember { mutableStateOf<MuscleGroup?>(null) }
    val picked = remember { mutableStateListOf<String>() }
    var creating by remember { mutableStateOf(false) }
    val filtered = exercises
        .filter { muscle == null || it.muscle == muscle }
        .filter { query.isBlank() || normalize(it.name).contains(normalize(query)) }
        .sortedBy { it.name }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            Modifier.fillMaxWidth(0.94f).fillMaxSize(0.88f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(if (multiple) "Añadir ejercicios" else "Elegir ejercicio", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Buscar…") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                MuscleFilterRow(muscle, { muscle = it })
                Spacer(Modifier.height(4.dp))
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 4.dp)) {
                    item {
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { creating = true }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text("Crear ejercicio nuevo", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    items(filtered, key = { it.id }) { e ->
                        val isPicked = e.id in picked
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (!multiple) { onPick(listOf(e.id)); onDismiss() }
                                    else if (isPicked) picked.remove(e.id) else picked.add(e.id)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(e.name, style = MaterialTheme.typography.bodyLarge)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    MuscleBadge(e.muscle)
                                    if (e.equipment.isNotBlank()) Text(
                                        "  ${e.equipment}", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            if (multiple) Checkbox(checked = isPicked, onCheckedChange = { if (it) picked.add(e.id) else picked.remove(e.id) })
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    if (multiple) TextButton(enabled = picked.isNotEmpty(), onClick = { onPick(picked.toList()); onDismiss() }) {
                        Icon(Icons.Default.Check, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Añadir (${picked.size})")
                    }
                }
            }
        }
    }
    if (creating) ExerciseEditDialog(
        initial = null,
        onSave = { ex -> onCreate(ex); if (multiple) picked.add(ex.id) else { onPick(listOf(ex.id)); onDismiss() } },
        onDismiss = { creating = false },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseEditDialog(initial: Exercise?, onSave: (Exercise) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var muscle by remember { mutableStateOf(initial?.muscle ?: MuscleGroup.CHEST) }
    var type by remember { mutableStateOf(initial?.type ?: ExerciseType.WEIGHT_REPS) }
    var equipment by remember { mutableStateOf(initial?.equipment ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var muscleOpen by remember { mutableStateOf(false) }
    var typeOpen by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo ejercicio" else "Editar ejercicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = muscleOpen, onExpandedChange = { muscleOpen = it }) {
                    OutlinedTextField(
                        value = muscle.label, onValueChange = {}, readOnly = true, label = { Text("Grupo muscular") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(muscleOpen) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = muscleOpen, onDismissRequest = { muscleOpen = false }) {
                        MuscleGroup.entries.forEach { m ->
                            DropdownMenuItem(text = { Text(m.label) }, onClick = { muscle = m; muscleOpen = false })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = typeOpen, onExpandedChange = { typeOpen = it }) {
                    OutlinedTextField(
                        value = type.label, onValueChange = {}, readOnly = true, label = { Text("Tipo de registro") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeOpen) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = typeOpen, onDismissRequest = { typeOpen = false }) {
                        ExerciseType.entries.forEach { t ->
                            DropdownMenuItem(text = { Text(t.label) }, onClick = { type = t; typeOpen = false })
                        }
                    }
                }
                OutlinedTextField(equipment, { equipment = it }, label = { Text("Material (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(notes, { notes = it }, label = { Text("Notas / técnica (opcional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = {
                val base = initial ?: Exercise(name = name.trim(), muscle = muscle, custom = true)
                onSave(base.copy(name = name.trim(), muscle = muscle, type = type, equipment = equipment.trim(), notes = notes.trim()))
                onDismiss()
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
