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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.FoodEntry
import com.chumi.gymapp.data.Meal
import com.chumi.gymapp.data.Nutrition
import com.chumi.gymapp.data.NutritionContent
import com.chumi.gymapp.data.formatKg
import com.chumi.gymapp.data.formatWeight
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.Routes
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.CalorieRing
import com.chumi.gymapp.ui.components.ConfirmDialog
import com.chumi.gymapp.ui.components.LabeledNumberField
import com.chumi.gymapp.ui.components.MacroBar
import com.chumi.gymapp.ui.components.SectionCard
import com.chumi.gymapp.ui.components.g
import com.chumi.gymapp.ui.components.macroColors
import com.chumi.gymapp.ui.components.mediumDate
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val dayTitle = DateTimeFormatter.ofPattern("EEEE d MMM", Locale("es", "ES"))

fun dayLabel(day: Long): String {
    val today = LocalDate.now().toEpochDay()
    return when (day) {
        today -> "Hoy"
        today - 1 -> "Ayer"
        today + 1 -> "Mañana"
        else -> LocalDate.ofEpochDay(day).format(dayTitle).replaceFirstChar { it.uppercase() }
    }
}

@Composable
fun NutritionScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsStateWithLifecycle()
    var day by rememberSaveable { mutableStateOf(Nutrition.today()) }
    var editing by remember { mutableStateOf<FoodEntry?>(null) }
    var copyFromYesterday by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val targets = Nutrition.targets(data)
    val totals = Nutrition.dayTotals(data, day)
    val colors = macroColors()

    AppScaffold(
        title = "Nutrición",
        isTab = true,
        snackbar = snackbar,
        actions = {
            IconButton(onClick = { nav.navigate(Routes.NUTRITION_PROGRESS) }) { Icon(Icons.Default.Insights, "Progreso") }
            IconButton(onClick = { nav.navigate(Routes.BODY_CALC) }) { Icon(Icons.Default.Person, "Mi cuerpo y objetivos") }
        },
    ) { pad ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding(), bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Selector de día
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { day -= 1 }) { Icon(Icons.Default.ChevronLeft, "Día anterior") }
                    Text(
                        dayLabel(day), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).clickable { day = Nutrition.today() },
                    )
                    IconButton(onClick = { day += 1 }) { Icon(Icons.Default.ChevronRight, "Día siguiente") }
                }
            }

            if (targets == null) item {
                SectionCard(title = "👋 Empieza por aquí") {
                    Text(
                        "Dime tu altura, peso, edad, actividad y objetivo y te calculo tus calorías y macros exactos, " +
                            "como haría tu nutricionista.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { nav.navigate(Routes.BODY_CALC) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Person, null); Spacer(Modifier.width(6.dp)); Text("Configurar mi perfil")
                    }
                }
            }

            // Resumen del día
            item {
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CalorieRing(totals.kcal, targets?.kcal?.toDouble())
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            MacroBar("Proteína", totals.protein, targets?.protein?.toDouble(), colors.protein)
                            MacroBar("Carbohidratos", totals.carbs, targets?.carbs?.toDouble(), colors.carbs)
                            MacroBar("Grasas", totals.fat, targets?.fat?.toDouble(), colors.fat)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row {
                        Text(
                            "Comido: ${formatKg(totals.kcal)} kcal" + (targets?.let { " de ${formatKg(it.kcal.toDouble())}" } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "Fibra ${totals.fiber.roundToInt()}" + (targets?.let { "/${it.fiber}" } ?: "") + " g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Agua
            item {
                val water = data.water[day] ?: 0
                val goal = targets?.waterMl ?: 2500
                SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💧", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Agua: ${formatWeight(water / 1000.0)} / ${formatWeight(goal / 1000.0)} L", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (water.toFloat() / goal).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.secondary,
                                drawStopIndicator = {},
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { vm.addWater(day, -250) }, enabled = water > 0, modifier = Modifier.weight(1f)) { Text("−250") }
                        OutlinedButton(onClick = { vm.addWater(day, 250) }, modifier = Modifier.weight(1f)) { Text("+250 ml") }
                        OutlinedButton(onClick = { vm.addWater(day, 500) }, modifier = Modifier.weight(1f)) { Text("+500 ml") }
                    }
                }
            }

            // Accesos rápidos
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { nav.navigate(Routes.ideas(day)) }, label = { Text("Ideas de comidas") }, leadingIcon = { Icon(Icons.Default.RestaurantMenu, null) })
                    AssistChip(onClick = { nav.navigate(Routes.TIPS) }, label = { Text("Consejos") }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.MenuBook, null) })
                    AssistChip(onClick = { copyFromYesterday = true }, label = { Text("Copiar el día anterior") }, leadingIcon = { Icon(Icons.Default.ContentCopy, null) })
                }
            }

            // Comidas
            Meal.entries.forEach { meal ->
                val entries = data.foodLog.filter { it.day == day && it.meal == meal }.sortedBy { it.createdAt }
                val isOptional = meal == Meal.PRE_WORKOUT || meal == Meal.POST_WORKOUT
                if (isOptional && entries.isEmpty()) return@forEach
                item(key = "meal_${meal.name}") {
                    MealCard(
                        meal = meal,
                        entries = entries,
                        onAdd = { nav.navigate(Routes.addFood(day, meal)) },
                        onEntry = { editing = it },
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Meal.PRE_WORKOUT, Meal.POST_WORKOUT)
                        .filter { m -> data.foodLog.none { it.day == day && it.meal == m } }
                        .forEach { m ->
                            AssistChip(
                                onClick = { nav.navigate(Routes.addFood(day, m)) },
                                label = { Text("${m.emoji} ${m.label}") },
                                leadingIcon = { Icon(Icons.Default.Add, null) },
                            )
                        }
                }
            }

            // Consejo del día
            item {
                val tip = NutritionContent.tipOfTheDay(day)
                SectionCard(
                    modifier = Modifier.clickable { nav.navigate(Routes.TIPS) },
                    title = "Consejo del día",
                    action = { Icon(Icons.Default.Lightbulb, null, tint = MaterialTheme.colorScheme.primary) },
                ) {
                    Text("${tip.emoji} ${tip.title}", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(tip.text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    editing?.let { e ->
        EntryEditDialog(
            entry = e,
            onSave = { grams, meal ->
                if (grams != e.grams && grams > 0) vm.updateEntryGrams(e.id, grams)
                if (meal != e.meal) vm.moveEntry(e.id, meal)
            },
            onDelete = { vm.deleteEntry(e.id) },
            onDismiss = { editing = null },
        )
    }
    if (copyFromYesterday) {
        val prev = data.foodLog.count { it.day == day - 1 }
        ConfirmDialog(
            title = "Copiar el día anterior",
            text = if (prev == 0) "El ${dayLabel(day - 1).lowercase()} no tiene comidas registradas."
            else "Se añadirán a ${dayLabel(day).lowercase()} los $prev alimentos de ${LocalDate.ofEpochDay(day - 1).format(mediumDate)}.",
            confirm = "Copiar",
            onConfirm = {
                if (prev > 0) {
                    vm.copyMeals(day - 1, day)
                    scope.launch { snackbar.showSnackbar("Copiado ✅") }
                }
            },
            onDismiss = { copyFromYesterday = false },
        )
    }
}

@Composable
private fun MealCard(meal: Meal, entries: List<FoodEntry>, onAdd: () -> Unit, onEntry: (FoodEntry) -> Unit) {
    val kcal = entries.sumOf { it.kcal }
    SectionCard(
        title = "${meal.emoji} ${meal.label}" + if (entries.isNotEmpty()) " · ${formatKg(kcal)} kcal" else "",
        action = { IconButton(onClick = onAdd) { Icon(Icons.Default.Add, "Añadir a ${meal.label}", tint = MaterialTheme.colorScheme.primary) } },
    ) {
        if (entries.isEmpty()) {
            Text(
                "Nada todavía. Toca + para añadir.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onAdd),
            )
            return@SectionCard
        }
        entries.forEachIndexed { i, e ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onEntry(e) }.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(e.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                    Text(
                        (if (e.grams > 0) "${formatWeight(e.grams)} g · " else "") +
                            "P ${e.protein.roundToInt()} · C ${e.carbs.roundToInt()} · G ${e.fat.roundToInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("${e.kcal.roundToInt()} kcal", style = MaterialTheme.typography.labelLarge)
            }
            if (i < entries.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        }
        val p = entries.sumOf { it.protein }
        val c = entries.sumOf { it.carbs }
        val f = entries.sumOf { it.fat }
        Spacer(Modifier.height(6.dp))
        Text(
            "Total: proteína ${g(p)} · carbohidratos ${g(c)} · grasas ${g(f)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EntryEditDialog(entry: FoodEntry, onSave: (Double, Meal) -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit) {
    var grams by remember { mutableDoubleStateOf(entry.grams) }
    var meal by remember { mutableStateOf(entry.meal) }
    val ratio = if (entry.grams > 0 && grams > 0) grams / entry.grams else 1.0
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(entry.name) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (entry.grams > 0) {
                    LabeledNumberField("Cantidad", grams, { grams = it }, Modifier.fillMaxWidth(), suffix = "g")
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    "${(entry.kcal * ratio).roundToInt()} kcal · P ${(entry.protein * ratio).roundToInt()} g · " +
                        "C ${(entry.carbs * ratio).roundToInt()} g · G ${(entry.fat * ratio).roundToInt()} g",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                Text("Mover a:", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Meal.entries.forEach { m -> FilterChip(meal == m, { meal = m }, label = { Text(m.label) }) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(grams, meal); onDismiss() }) { Text("Guardar") } },
        dismissButton = {
            TextButton(onClick = { onDelete(); onDismiss() }) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(4.dp))
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
    )
}
