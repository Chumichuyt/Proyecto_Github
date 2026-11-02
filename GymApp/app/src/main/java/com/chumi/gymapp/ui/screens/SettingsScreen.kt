package com.chumi.gymapp.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.chumi.gymapp.data.ThemeMode
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.components.AppScaffold
import com.chumi.gymapp.ui.components.ConfirmDialog
import com.chumi.gymapp.ui.components.LabeledNumberField
import com.chumi.gymapp.ui.components.SectionCard
import com.chumi.gymapp.ui.components.formatSec
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavHostController) {
    val data by vm.data.collectAsStateWithLifecycle()
    val s = data.settings
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var confirmReset by remember { mutableStateOf(false) }
    var confirmImport by remember { mutableStateOf<android.net.Uri?>(null) }

    fun toast(msg: String) = scope.launch { snackbar.showSnackbar(msg) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.exportTo(uri) { ok -> toast(if (ok) "Copia guardada ✅" else "No se pudo guardar la copia") }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) confirmImport = uri
    }

    AppScaffold(title = "Ajustes", onBack = { nav.popBackStack() }, snackbar = snackbar) { pad ->
        LazyColumn(
            modifier = Modifier.imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = pad.calculateTopPadding() + 4.dp, bottom = pad.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SectionCard(title = "Perfil") {
                    var name by remember { mutableStateOf(s.userName) }
                    OutlinedTextField(
                        name, { name = it; vm.updateSettings { st -> st.copy(userName = it.trim()) } },
                        label = { Text("Tu nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Objetivo de entrenos por semana", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..7).forEach { n ->
                            FilterChip(s.weeklyGoal == n, { vm.updateSettings { it.copy(weeklyGoal = n) } }, label = { Text("$n") })
                        }
                    }
                }
            }
            item {
                SectionCard(title = "Entreno") {
                    Text("Descanso por defecto: ${formatSec(s.defaultRestSec)}", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(60, 90, 120, 180).forEach { sec ->
                            FilterChip(s.defaultRestSec == sec, { vm.updateSettings { it.copy(defaultRestSec = sec) } }, label = { Text(formatSec(sec)) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LabeledNumberField("Peso de la barra", s.barWeight, { v -> if (v > 0) vm.updateSettings { it.copy(barWeight = v) } }, Modifier.fillMaxWidth(), suffix = "kg")
                    SwitchRow("Vibrar al acabar el descanso", s.vibrate) { v -> vm.updateSettings { it.copy(vibrate = v) } }
                    SwitchRow("Sonido al acabar el descanso", s.sound) { v -> vm.updateSettings { it.copy(sound = v) } }
                    SwitchRow("Mantener la pantalla encendida durante el entreno", s.keepScreenOn) { v -> vm.updateSettings { it.copy(keepScreenOn = v) } }
                    SwitchRow(
                        "Contar mi peso corporal en dominadas, fondos, flexiones…",
                        s.countBodyweight,
                        sub = "Usa tu último peso registrado para el volumen total",
                    ) { v -> vm.updateSettings { it.copy(countBodyweight = v) } }
                }
            }
            item {
                SectionCard(title = "Apariencia") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ThemeMode.entries.forEach { m ->
                            FilterChip(s.themeMode == m, { vm.updateSettings { it.copy(themeMode = m) } }, label = { Text(m.label) })
                        }
                    }
                    if (Build.VERSION.SDK_INT >= 31) SwitchRow("Colores de Material You (fondo de pantalla)", s.dynamicColor) { v ->
                        vm.updateSettings { it.copy(dynamicColor = v) }
                    }
                }
            }
            item {
                SectionCard(title = "Copia de seguridad") {
                    Text(
                        "Guarda todos tus datos en un archivo (por ejemplo en Google Drive) para no perderlos si cambias de móvil.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    FilledTonalButton(onClick = { exportLauncher.launch("gym_copia_${LocalDate.now()}.json") }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Download, null); Spacer(Modifier.width(6.dp)); Text("Exportar copia")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Upload, null); Spacer(Modifier.width(6.dp)); Text("Importar copia")
                    }
                }
            }
            item {
                SectionCard(title = "Zona peligrosa") {
                    OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error); Spacer(Modifier.width(6.dp))
                        Text("Borrar todos los datos", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            item {
                Text(
                    "GymBeast 1.0 · ${data.workouts.size} entrenos · ${data.exercises.size} ejercicios · ${data.routines.size} rutinas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                )
            }
        }
    }

    if (confirmReset) ConfirmDialog(
        "Borrar todo", "Se borrarán entrenos, rutinas, ejercicios personalizados y medidas. No se puede deshacer.", "Borrar todo",
        onConfirm = { vm.resetAll(); toast("Datos borrados") },
        onDismiss = { confirmReset = false },
    )
    confirmImport?.let { uri ->
        ConfirmDialog(
            "Importar copia", "Los datos actuales se reemplazarán por los de la copia. ¿Continuar?", "Importar",
            onConfirm = { vm.importFrom(uri) { ok -> toast(if (ok) "Copia importada ✅" else "El archivo no es una copia válida") } },
            onDismiss = { confirmImport = null },
        )
    }
}

@Composable
private fun SwitchRow(title: String, checked: Boolean, sub: String? = null, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            if (sub != null) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked, onChange)
    }
}
