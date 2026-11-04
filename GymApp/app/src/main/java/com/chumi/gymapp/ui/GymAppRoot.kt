package com.chumi.gymapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chumi.gymapp.ui.components.formatDuration
import com.chumi.gymapp.ui.screens.AddFoodScreen
import com.chumi.gymapp.ui.screens.BodyCalcScreen
import com.chumi.gymapp.ui.screens.BodyScreen
import com.chumi.gymapp.ui.screens.MealIdeasScreen
import com.chumi.gymapp.ui.screens.NutritionProgressScreen
import com.chumi.gymapp.ui.screens.NutritionScreen
import com.chumi.gymapp.ui.screens.TipsScreen
import com.chumi.gymapp.ui.screens.ExerciseDetailScreen
import com.chumi.gymapp.ui.screens.ExercisesScreen
import com.chumi.gymapp.ui.screens.HistoryScreen
import com.chumi.gymapp.ui.screens.HomeScreen
import com.chumi.gymapp.ui.screens.ProgressScreen
import com.chumi.gymapp.ui.screens.RoutineEditScreen
import com.chumi.gymapp.ui.screens.SettingsScreen
import com.chumi.gymapp.ui.screens.ToolsScreen
import com.chumi.gymapp.ui.screens.TrainScreen
import com.chumi.gymapp.ui.screens.WorkoutDetailScreen
import com.chumi.gymapp.ui.screens.WorkoutScreen
import kotlinx.coroutines.delay

object Routes {
    const val HOME = "home"
    const val TRAIN = "train"
    const val HISTORY = "history"
    const val PROGRESS = "progress"
    const val EXERCISES = "exercises"
    const val WORKOUT = "workout"
    const val SETTINGS = "settings"
    const val BODY = "body"
    const val TOOLS = "tools"
    const val NUTRITION = "nutrition"
    const val BODY_CALC = "bodycalc"
    const val TIPS = "tips"
    const val NUTRITION_PROGRESS = "nutritionProgress"
    fun addFood(day: Long, meal: com.chumi.gymapp.data.Meal) = "addFood/$day/${meal.name}"
    fun ideas(day: Long) = "ideas/$day"
    fun routine(id: String) = "routine/$id"
    fun workoutDetail(id: String, justFinished: Boolean = false) = "workoutDetail/$id?finished=$justFinished"
    fun exercise(id: String) = "exercise/$id"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "Inicio", Icons.Default.Home),
    Tab(Routes.TRAIN, "Entrenar", Icons.Default.FitnessCenter),
    Tab(Routes.NUTRITION, "Nutrición", Icons.Default.Restaurant),
    Tab(Routes.PROGRESS, "Progreso", Icons.Default.ShowChart),
    Tab(Routes.HISTORY, "Historial", Icons.Default.History),
)

fun NavHostController.navigateTab(route: String) = navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun GymAppRoot(vm: AppViewModel) {
    val nav = rememberNavController()
    val data by vm.data.collectAsStateWithLifecycle()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val isTab = tabs.any { it.route == route }
    val active = data.activeWorkout

    Scaffold(
        bottomBar = {
            if (isTab) Column {
                if (active != null) ActiveWorkoutBanner(active.name, active.startTime) { nav.navigate(Routes.WORKOUT) }
                NavigationBar {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = { nav.navigateTab(t.route) },
                            icon = { Icon(t.icon, null) },
                            label = { Text(t.label, maxLines = 1) },
                        )
                    }
                }
            }
        },
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(bottom = if (isTab) inner.calculateBottomPadding() else 0.dp),
        ) {
            composable(Routes.HOME) { HomeScreen(vm, nav) }
            composable(Routes.TRAIN) { TrainScreen(vm, nav) }
            composable(Routes.HISTORY) { HistoryScreen(vm, nav) }
            composable(Routes.PROGRESS) { ProgressScreen(vm, nav) }
            composable(Routes.EXERCISES) { ExercisesScreen(vm, nav) }
            composable(Routes.WORKOUT) { WorkoutScreen(vm, nav) }
            composable(Routes.SETTINGS) { SettingsScreen(vm, nav) }
            composable(Routes.BODY) { BodyScreen(vm, nav) }
            composable(Routes.TOOLS) { ToolsScreen(vm, nav) }
            composable(Routes.NUTRITION) { NutritionScreen(vm, nav) }
            composable(Routes.BODY_CALC) { BodyCalcScreen(vm, nav) }
            composable(Routes.TIPS) { TipsScreen(nav) }
            composable(Routes.NUTRITION_PROGRESS) { NutritionProgressScreen(vm, nav) }
            composable("addFood/{day}/{meal}") {
                val day = it.arguments?.getString("day")?.toLongOrNull() ?: com.chumi.gymapp.data.Nutrition.today()
                val meal = runCatching { com.chumi.gymapp.data.Meal.valueOf(it.arguments?.getString("meal") ?: "") }.getOrDefault(com.chumi.gymapp.data.Meal.LUNCH)
                AddFoodScreen(vm, nav, day, meal)
            }
            composable("ideas/{day}") {
                MealIdeasScreen(vm, nav, it.arguments?.getString("day")?.toLongOrNull() ?: com.chumi.gymapp.data.Nutrition.today())
            }
            composable("routine/{id}") { RoutineEditScreen(vm, nav, it.arguments?.getString("id") ?: "new") }
            composable("exercise/{id}") { ExerciseDetailScreen(vm, nav, it.arguments?.getString("id") ?: "") }
            composable("workoutDetail/{id}?finished={finished}") {
                WorkoutDetailScreen(
                    vm, nav,
                    it.arguments?.getString("id") ?: "",
                    justFinished = it.arguments?.getString("finished") == "true",
                )
            }
        }
    }
}

@Composable
private fun ActiveWorkoutBanner(name: String, start: Long, onClick: () -> Unit) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(15_000); now = System.currentTimeMillis() } }
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.onPrimary)
        Spacer(Modifier.width(8.dp))
        Text(
            "$name · ${formatDuration((now - start) / 60000)}",
            color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f), maxLines = 1,
        )
        Text("Continuar", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
    }
}
