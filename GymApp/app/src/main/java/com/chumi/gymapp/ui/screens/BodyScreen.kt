package com.chumi.gymapp.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.BodyEntry
import com.chumi.gymapp.data.Stats
import com.chumi.gymapp.data.formatWeight
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.ChartPoint
import com.chumi.gymapp.ui.components.ConfirmDialog
import com.chumi.gymapp.ui.components.EmptyState
import com.chumi.gymapp.ui.components.LabeledNumberField
import com.chumi.gymapp.ui.components.LineChart
import com.chumi.gymapp.ui.components.SectionCard
import com.chumi.gymapp.ui.components.StatTile
import com.chumi.gymapp.ui.components.mediumDate
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

private enum class BodyMetric(val label: String, val unit: String, val get: (BodyEntry) -> Double?) {
    WEIGHT("Peso", "kg", { it.weight }),
    FAT("% Grasa", "%", { it.bodyFat }),
    WAIST("Cintura", "cm", { it.waist }),
    CHEST("Pecho", "cm", { it.chest }),
    ARM("Brazo", "cm", { it.arm }),
    THIGH("Muslo", "cm", { it.thigh }),
}

@Composable
fun BodyScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<BodyEntry?>(null) }
    var metric by remember { mutableStateOf(BodyMetric.WEIGHT) }
    val entries = data.body.sortedBy { it.date }

    AppScaffold(
        title = "Peso y medidas",
        onBack = { nav.popBackStack() },
        fab = { ExtendedFloatingActionButton(onClick = { adding = true }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Registrar") }) },
    ) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = pad.calculateBottomPadding() + 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (entries.isEmpty()) {
                item { EmptyState("⚖️", "Sin registros", "Añade tu peso para ver la evolución. También se usa para calcular el volumen de dominadas, fondos, etc.") }
                return@LazyColumn
            }
            item {
                val first = entries.first().weight
                val last = entries.last().weight
                val diff = last - first
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatTile("Actual", "${formatWeight(last)} kg", Modifier.weight(1f))
                    StatTile("Cambio", (if (diff > 0) "+" else "") + "${formatWeight(Math.round(diff * 10) / 10.0)} kg", Modifier.weight(1f), sub = "desde el inicio")
                    StatTile("Mín / Máx", "${formatWeight(entries.minOf { it.weight })}–${formatWeight(entries.maxOf { it.weight })}", Modifier.weight(1.2f))
                }
            }
            item {
                SectionCard(title = "Evolución") {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BodyMetric.entries.filter { m -> entries.any { m.get(it) != null } }.forEach { m ->
                            FilterChip(metric == m, { metric = m }, label = { Text(m.label) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LineChart(
                        entries.mapNotNull { e -> metric.get(e)?.let { ChartPoint(e.date, it) } },
                        valueText = { "${formatWeight(Math.round(it * 10) / 10.0)} ${metric.unit}" },
                    )
                }
            }
            item { Text("Registros", style = MaterialTheme.typography.titleMedium) }
            items(entries.reversed(), key = { it.id }) { e ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("${formatWeight(e.weight)} kg", style = MaterialTheme.typography.titleMedium)
                        val extra = listOfNotNull(
                            e.bodyFat?.let { "${formatWeight(it)}% grasa" },
                            e.waist?.let { "cintura ${formatWeight(it)}" },
                            e.chest?.let { "pecho ${formatWeight(it)}" },
                            e.arm?.let { "brazo ${formatWeight(it)}" },
                            e.thigh?.let { "muslo ${formatWeight(it)}" },
                        ).joinToString(" · ")
                        Text(
                            Stats.toDate(e.date).format(mediumDate) + if (extra.isNotEmpty()) " · $extra" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { toDelete = e }) { Icon(Icons.Default.Delete, "Borrar") }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        }
    }

    if (adding) BodyEntryDialog(entries.lastOrNull(), onSave = vm::addBodyEntry) { adding = false }
    toDelete?.let { e ->
        ConfirmDialog("Borrar registro", "¿Borrar el registro del ${Stats.toDate(e.date).format(mediumDate)}?", "Borrar", { vm.deleteBodyEntry(e.id) }) { toDelete = null }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BodyEntryDialog(last: BodyEntry?, onSave: (BodyEntry) -> Unit, onDismiss: () -> Unit) {
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var weight by remember { mutableDoubleStateOf(last?.weight ?: 0.0) }
    var fat by remember { mutableDoubleStateOf(0.0) }
    var waist by remember { mutableDoubleStateOf(0.0) }
    var chest by remember { mutableDoubleStateOf(0.0) }
    var arm by remember { mutableDoubleStateOf(0.0) }
    var thigh by remember { mutableDoubleStateOf(0.0) }
    var pickDate by remember { mutableStateOf(false) }
    fun Double.orNull() = takeIf { it > 0 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo registro") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { pickDate = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Fecha: " + Stats.toDate(date).format(mediumDate))
                }
                LabeledNumberField("Peso", weight, { weight = it }, Modifier.fillMaxWidth(), suffix = "kg")
                Text("Opcional", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledNumberField("% Grasa", fat, { fat = it }, Modifier.weight(1f), suffix = "%")
                    LabeledNumberField("Cintura", waist, { waist = it }, Modifier.weight(1f), suffix = "cm")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledNumberField("Pecho", chest, { chest = it }, Modifier.weight(1f), suffix = "cm")
                    LabeledNumberField("Brazo", arm, { arm = it }, Modifier.weight(1f), suffix = "cm")
                }
                LabeledNumberField("Muslo", thigh, { thigh = it }, Modifier.fillMaxWidth(0.5f), suffix = "cm")
            }
        },
        confirmButton = {
            TextButton(enabled = weight > 0, onClick = {
                onSave(BodyEntry(date = date, weight = weight, bodyFat = fat.orNull(), waist = waist.orNull(), chest = chest.orNull(), arm = arm.orNull(), thigh = thigh.orNull()))
                onDismiss()
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )

    if (pickDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = Stats.toDate(date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { utc ->
                        val d = Instant.ofEpochMilli(utc).atZone(ZoneOffset.UTC).toLocalDate()
                        date = d.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    }
                    pickDate = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text("Cancelar") } },
        ) { DatePicker(state) }
    }
}
