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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.Food
import com.chumi.gymapp.data.FoodCategory
import com.chumi.gymapp.data.Meal
import com.chumi.gymapp.data.Nutrition
import com.chumi.gymapp.data.formatWeight
import com.chumi.gymapp.data.macrosFor
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.ConfirmDialog
import com.chumi.gymapp.ui.components.EmptyState
import com.chumi.gymapp.ui.components.LabeledNumberField
import com.chumi.gymapp.ui.components.normalize
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class FoodTab(val label: String) { RECENT("Recientes"), ALL("Todos"), MINE("Mis alimentos") }

@Composable
fun AddFoodScreen(vm: AppViewModel, nav: NavHostController, day: Long, meal: Meal) {
    val data by vm.data.collectAsStateWithLifecycle()
    val recents = remember(data.foodLog.size) { Nutrition.recentFoods(data) }
    var tab by remember { mutableStateOf(if (recents.isEmpty()) FoodTab.ALL else FoodTab.RECENT) }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<FoodCategory?>(null) }
    var picked by remember { mutableStateOf<Food?>(null) }
    var quick by remember { mutableStateOf(false) }
    var creating by remember { mutableStateOf(false) }
    var editingFood by remember { mutableStateOf<Food?>(null) }
    var added by remember { mutableStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val source = when {
        query.isNotBlank() -> Nutrition.allFoods(data) // al buscar, se busca en todo
        tab == FoodTab.RECENT -> recents
        tab == FoodTab.MINE -> data.customFoods
        else -> Nutrition.allFoods(data)
    }
    val list = source
        .filter { query.isNotBlank() || tab != FoodTab.ALL || category == null || it.category == category }
        .filter { query.isBlank() || normalize(it.name).contains(normalize(query)) }
        .let { l -> if (tab == FoodTab.RECENT && query.isBlank()) l else l.sortedBy { it.name } }

    fun added(name: String) {
        added++
        scope.launch { snackbar.showSnackbar("✅ $name añadido a ${meal.label}") }
    }

    AppScaffold(
        title = "Añadir a ${meal.label}" + if (added > 0) " ($added)" else "",
        onBack = { nav.popBackStack() },
        snackbar = snackbar,
        actions = {
            if (added > 0) TextButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.Check, null); Spacer(Modifier.width(4.dp)); Text("Listo") }
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding(), bottom = pad.calculateBottomPadding() + 24.dp),
        ) {
            item {
                OutlinedTextField(
                    query, { query = it }, leadingIcon = { Icon(Icons.Default.Search, null) },
                    placeholder = { Text("Buscar alimento (pollo, arroz, avena…)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(onClick = { quick = true }, label = { Text("Añadir rápido") }, icon = { Icon(Icons.Default.Bolt, null) })
                    SuggestionChip(onClick = { creating = true }, label = { Text("Crear alimento") }, icon = { Icon(Icons.Default.Add, null) })
                }
                if (query.isBlank()) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FoodTab.entries.forEach { t -> FilterChip(tab == t, { tab = t }, label = { Text(t.label) }) }
                    }
                    if (tab == FoodTab.ALL) Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(category == null, { category = null }, label = { Text("Todas") })
                        FoodCategory.entries.forEach { c ->
                            FilterChip(category == c, { category = if (category == c) null else c }, label = { Text(c.label) })
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            if (list.isEmpty()) item {
                EmptyState(
                    "🔍",
                    when {
                        query.isNotBlank() -> "No encuentro \"$query\""
                        tab == FoodTab.RECENT -> "Aún no hay recientes"
                        tab == FoodTab.MINE -> "No has creado alimentos"
                        else -> "Sin alimentos"
                    },
                    "Puedes crearlo con \"Crear alimento\" copiando los valores de la etiqueta nutricional (por 100 g).",
                )
            }
            items(list, key = { it.id }) { f ->
                Row(
                    Modifier.fillMaxWidth().clickable { picked = f }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(f.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${f.kcal.roundToInt()} kcal · P ${formatWeight(f.protein)} · C ${formatWeight(f.carbs)} · G ${formatWeight(f.fat)}  (100 g)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (f.custom) IconButton(onClick = { editingFood = f }) { Icon(Icons.Default.Edit, "Editar") }
                    else Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
            item {
                Text(
                    "Valores aproximados por 100 g. Pesa los alimentos en crudo salvo que indique \"cocido\".",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }

    picked?.let { f ->
        FoodAmountDialog(
            food = f,
            initialGrams = Nutrition.lastGrams(data, f.id) ?: f.servingG,
            onAdd = { grams -> vm.addFood(day, meal, f, grams); added(f.name) },
            onDismiss = { picked = null },
        )
    }
    if (quick) QuickAddDialog(
        onAdd = { name, kcal, p, c, fat -> vm.addQuick(day, meal, name, kcal, p, c, fat); added(name.ifBlank { "Añadido rápido" }) },
        onDismiss = { quick = false },
    )
    if (creating) CustomFoodDialog(null, onSave = vm::saveCustomFood, onDelete = {}, onDismiss = { creating = false })
    editingFood?.let { f ->
        CustomFoodDialog(f, onSave = vm::saveCustomFood, onDelete = { vm.deleteCustomFood(f.id) }, onDismiss = { editingFood = null })
    }
}

@Composable
private fun FoodAmountDialog(food: Food, initialGrams: Double, onAdd: (Double) -> Unit, onDismiss: () -> Unit) {
    var grams by remember { mutableDoubleStateOf(initialGrams) }
    var fieldKey by remember { mutableStateOf(0) }
    val m = food.macrosFor(grams)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(food.name) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                androidx.compose.runtime.key(fieldKey) {
                    LabeledNumberField("Cantidad", grams, { grams = it }, Modifier.fillMaxWidth(), suffix = "g")
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1.0, 2.0, 0.5).forEach { n ->
                        val label = (if (n == 0.5) "½" else formatWeight(n)) + " ${food.servingName} (${formatWeight(food.servingG * n)} g)"
                        FilterChip(false, { grams = food.servingG * n; fieldKey++ }, label = { Text(label) })
                    }
                    FilterChip(false, { grams = 100.0; fieldKey++ }, label = { Text("100 g") })
                }
                Spacer(Modifier.height(12.dp))
                Text("${m.kcal.roundToInt()} kcal", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Proteína ${formatWeight(Math.round(m.protein * 10) / 10.0)} g · Carbohidratos ${formatWeight(Math.round(m.carbs * 10) / 10.0)} g · Grasas ${formatWeight(Math.round(m.fat * 10) / 10.0)} g",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = { TextButton(enabled = grams > 0, onClick = { onAdd(grams); onDismiss() }) { Text("Añadir") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun QuickAddDialog(onAdd: (String, Double, Double, Double, Double) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableDoubleStateOf(0.0) }
    var p by remember { mutableDoubleStateOf(0.0) }
    var c by remember { mutableDoubleStateOf(0.0) }
    var f by remember { mutableDoubleStateOf(0.0) }
    // si no se ponen kcal, se calculan con los macros
    val computed = if (kcal > 0) kcal else p * 4 + c * 4 + f * 9
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir rápido") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Para comidas fuera de casa o platos que no están en la lista.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(name, { name = it }, label = { Text("Nombre (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                LabeledNumberField("Calorías", kcal, { kcal = it }, Modifier.fillMaxWidth(), suffix = "kcal")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LabeledNumberField("Prot.", p, { p = it }, Modifier.weight(1f), suffix = "g")
                    LabeledNumberField("Carb.", c, { c = it }, Modifier.weight(1f), suffix = "g")
                    LabeledNumberField("Grasa", f, { f = it }, Modifier.weight(1f), suffix = "g")
                }
                if (kcal <= 0 && computed > 0) Text("≈ ${computed.roundToInt()} kcal según los macros", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(enabled = computed > 0, onClick = { onAdd(name.trim(), computed, p, c, f); onDismiss() }) { Text("Añadir") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomFoodDialog(initial: Food?, onSave: (Food) -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: FoodCategory.OTHER) }
    var kcal by remember { mutableDoubleStateOf(initial?.kcal ?: 0.0) }
    var p by remember { mutableDoubleStateOf(initial?.protein ?: 0.0) }
    var c by remember { mutableDoubleStateOf(initial?.carbs ?: 0.0) }
    var f by remember { mutableDoubleStateOf(initial?.fat ?: 0.0) }
    var fiber by remember { mutableDoubleStateOf(initial?.fiber ?: 0.0) }
    var serving by remember { mutableDoubleStateOf(initial?.servingG ?: 100.0) }
    var servingName by remember { mutableStateOf(initial?.servingName ?: "ración") }
    var catOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val computed = if (kcal > 0) kcal else p * 4 + c * 4 + f * 9

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo alimento" else "Editar alimento") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = catOpen, onExpandedChange = { catOpen = it }) {
                    OutlinedTextField(
                        category.label, {}, readOnly = true, label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catOpen) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    )
                    ExposedDropdownMenu(expanded = catOpen, onDismissRequest = { catOpen = false }) {
                        FoodCategory.entries.forEach { cat -> DropdownMenuItem(text = { Text(cat.label) }, onClick = { category = cat; catOpen = false }) }
                    }
                }
                Text("Valores por 100 g (mira la etiqueta)", style = MaterialTheme.typography.labelMedium)
                LabeledNumberField("Calorías", kcal, { kcal = it }, Modifier.fillMaxWidth(), suffix = "kcal")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    LabeledNumberField("Prot.", p, { p = it }, Modifier.weight(1f), suffix = "g")
                    LabeledNumberField("Carb.", c, { c = it }, Modifier.weight(1f), suffix = "g")
                    LabeledNumberField("Grasa", f, { f = it }, Modifier.weight(1f), suffix = "g")
                }
                LabeledNumberField("Fibra (opcional)", fiber, { fiber = it }, Modifier.fillMaxWidth(), suffix = "g")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(servingName, { servingName = it }, label = { Text("Ración") }, singleLine = true, modifier = Modifier.weight(1f))
                    LabeledNumberField("Peso ración", serving, { serving = it }, Modifier.weight(1f), suffix = "g")
                }
                if (initial != null) TextButton(onClick = { confirmDelete = true }) {
                    Text("Eliminar alimento", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank() && computed > 0, onClick = {
                val base = initial ?: Food(name = name.trim(), kcal = computed, protein = p, carbs = c, fat = f, custom = true)
                onSave(base.copy(
                    name = name.trim(), category = category, kcal = computed, protein = p, carbs = c, fat = f, fiber = fiber,
                    servingG = if (serving > 0) serving else 100.0, servingName = servingName.ifBlank { "ración" }, custom = true,
                ))
                onDismiss()
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
    if (confirmDelete) ConfirmDialog(
        "Eliminar alimento", "Se quitará de tu lista. Lo que ya registraste en el diario se conserva.", "Eliminar",
        onConfirm = { onDelete(); onDismiss() }, onDismiss = { confirmDelete = false },
    )
}
