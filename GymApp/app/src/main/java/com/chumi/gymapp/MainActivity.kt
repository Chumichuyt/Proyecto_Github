package com.chumi.gymapp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chumi.gymapp.ui.AppViewModel
import com.chumi.gymapp.ui.GymAppRoot
import com.chumi.gymapp.ui.theme.GymTheme

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val data by vm.data.collectAsStateWithLifecycle()
            val keepOn = data.activeWorkout != null && data.settings.keepScreenOn
            DisposableEffect(keepOn) {
                if (keepOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                onDispose { }
            }
            GymTheme(mode = data.settings.themeMode, dynamic = data.settings.dynamicColor) {
                GymAppRoot(vm)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        vm.flush()
    }
}
