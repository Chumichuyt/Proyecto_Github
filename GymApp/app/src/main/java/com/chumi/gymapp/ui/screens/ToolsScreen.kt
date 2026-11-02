package com.chumi.gymapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.Comparisons
import com.chumi.gymapp.data.Stats
import com.chumi.gymapp.data.formatKg
import com.chumi.gymapp.data.formatWeight
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.LabeledNumberField
import com.chumi.gymapp.ui.components.SectionCard
import kotlin.math.roundToInt

@Composable
fun ToolsScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsStateWithLifecycle()
    AppScaffold(title = "Herramientas", onBack = { nav.popBackStack() }) { pad ->
        LazyColumn(
            modifier = Modifier.imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = pad.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SectionCard(title = "🐯 ¿A qué equivale este peso?") { WeightComparator() } }
            item { SectionCard(title = "Calculadora de 1RM") { OneRepMaxCalculator() } }
            item { SectionCard(title = "Calculadora de discos") { PlateCalculator(data.settings.barWeight) } }
        }
    }
}

@Composable
private fun WeightComparator() {
    var kg by remember { mutableDoubleStateOf(130.0) }
    LabeledNumberField("Kilos", kg, { kg = it }, Modifier.fillMaxWidth(), suffix = "kg")
    Spacer(Modifier.height(10.dp))
    val c = Comparisons.compare(kg)
    if (c != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(c.thing.emoji, style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.width(12.dp))
            Text("${formatKg(kg)} kg ≈ ${c.phrase}", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun OneRepMaxCalculator() {
    var weight by remember { mutableDoubleStateOf(100.0) }
    var reps by remember { mutableIntStateOf(5) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LabeledNumberField("Peso", weight, { weight = it }, Modifier.weight(1f), suffix = "kg")
        LabeledNumberField("Reps", reps.toDouble(), { reps = it.toInt() }, Modifier.weight(1f), decimals = false)
    }
    val orm = Stats.estimated1RM(weight, reps)
    Spacer(Modifier.height(12.dp))
    if (orm <= 0) return
    Text("1RM estimado: ${formatKg(orm)} kg", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(8.dp))
    Text("Peso aproximado para cada número de repeticiones:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(4.dp))
    listOf(1, 2, 3, 4, 5, 6, 8, 10, 12, 15).forEach { r ->
        val w = if (r == 1) orm else orm / (1 + r / 30.0)
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("$r reps", modifier = Modifier.weight(1f))
            Text("${(w * 100 / orm).roundToInt()}%", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${formatWeight((w * 2).roundToInt() / 2.0)} kg", fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }
}

private val plateSizes = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

/** Calcula qué discos poner en cada lado de la barra. */
fun platesPerSide(total: Double, bar: Double): Pair<List<Double>, Double> {
    var side = (total - bar) / 2
    val res = mutableListOf<Double>()
    if (side <= 0) return res to 0.0
    for (p in plateSizes) {
        while (side + 1e-9 >= p) {
            res += p; side -= p
        }
    }
    return res to side * 2
}

@Composable
fun PlateCalculator(defaultBar: Double) {
    var total by remember { mutableDoubleStateOf(100.0) }
    var bar by remember { mutableDoubleStateOf(defaultBar) }
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabeledNumberField("Peso total", total, { total = it }, Modifier.weight(1f), suffix = "kg")
            LabeledNumberField("Barra", bar, { bar = it }, Modifier.weight(1f), suffix = "kg")
        }
        Spacer(Modifier.height(12.dp))
        val (plates, rest) = platesPerSide(total, bar)
        if (total <= bar) {
            Text("Solo la barra 🙂", style = MaterialTheme.typography.bodyMedium)
            return@Column
        }
        Text("En cada lado:", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            // dibujo sencillo de la barra con los discos
            Box(Modifier.width(28.dp).height(8.dp).background(MaterialTheme.colorScheme.outline))
            plates.forEach { p ->
                val h = (40 + p * 3).dp
                Box(
                    Modifier.padding(horizontal = 1.dp).width(if (p >= 10) 14.dp else 9.dp).height(h)
                        .clip(RoundedCornerShape(3.dp)).background(plateColor(p)),
                )
            }
            Box(Modifier.width(16.dp).height(8.dp).background(MaterialTheme.colorScheme.outline))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            if (plates.isEmpty()) "Ningún disco" else plates.groupBy { it }.entries.joinToString(" + ") { (p, l) -> "${l.size}×${formatWeight(p)} kg" },
            style = MaterialTheme.typography.titleMedium,
        )
        if (rest > 0.01) Text(
            "Sobran ${formatWeight(Math.round(rest * 100) / 100.0)} kg que no se pueden cargar con discos estándar.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun plateColor(p: Double) = when (p) {
    25.0 -> androidx.compose.ui.graphics.Color(0xFFE53935)
    20.0 -> androidx.compose.ui.graphics.Color(0xFF1E88E5)
    15.0 -> androidx.compose.ui.graphics.Color(0xFFFDD835)
    10.0 -> androidx.compose.ui.graphics.Color(0xFF43A047)
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
