package com.addo.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.addo.app.ui.MainViewModel
import com.addo.app.ui.components.ConfettiBurst
import com.addo.app.ui.detail.TaskDetailSheet
import com.addo.app.ui.focus.FocusScreen
import com.addo.app.ui.home.HomeScreen
import com.addo.app.ui.theme.AddoTheme

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_TASK_ID = "com.addo.app.extra.TASK_ID"
    }

    private val vm: MainViewModel by viewModels()

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        setContent {
            AddoTheme {
                AddoRoot(vm, initialTaskId = intent.taskIdExtra())
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private fun Intent?.taskIdExtra(): Long? =
    this?.getLongExtra(MainActivity.EXTRA_TASK_ID, -1L)?.takeIf { it > 0 }

@Composable
private fun AddoRoot(vm: MainViewModel, initialTaskId: Long?) {
    val navController = rememberNavController()
    var detailTaskId by rememberSaveable { mutableStateOf(initialTaskId) }
    var confettiTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        vm.celebrate.collect { confettiTrigger++ }
    }
    LaunchedEffect(Unit) {
        vm.pickedTaskId.collect { id ->
            navController.navigate("focus/$id")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(vm = vm, onOpenTask = { detailTaskId = it })
            }
            composable(
                route = "focus/{taskId}",
                arguments = listOf(navArgument("taskId") { type = NavType.LongType })
            ) { entry ->
                val taskId = entry.arguments?.getLong("taskId") ?: return@composable
                FocusScreen(vm = vm, taskId = taskId, onBack = { navController.popBackStack() })
            }
        }

        detailTaskId?.let { id ->
            TaskDetailSheet(
                vm = vm,
                taskId = id,
                onDismiss = { detailTaskId = null },
                onFocus = { focusId ->
                    detailTaskId = null
                    navController.navigate("focus/$focusId")
                }
            )
        }

        ConfettiBurst(trigger = confettiTrigger, modifier = Modifier.fillMaxSize())
    }
}
