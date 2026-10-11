package com.jannlawm.alssdosanjuan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jannlawm.alssdosanjuan.data.local.TokenManager
import com.jannlawm.alssdosanjuan.data.model.ModuleItem
import com.jannlawm.alssdosanjuan.data.model.User
import com.jannlawm.alssdosanjuan.data.repository.AuthRepository
import com.jannlawm.alssdosanjuan.data.worker.SyncWorker
import com.jannlawm.alssdosanjuan.ui.chat.ChatScreen
import com.jannlawm.alssdosanjuan.ui.dashboard.DashboardScreen
import com.jannlawm.alssdosanjuan.ui.login.LoginScreen
import com.jannlawm.alssdosanjuan.ui.module.ModuleDetailScreen
import com.jannlawm.alssdosanjuan.ui.splash.SplashScreen
import com.jannlawm.alssdosanjuan.ui.theme.ALSSDOSanJuanTheme
import java.util.concurrent.TimeUnit

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Dashboard : Screen()
    data class ModuleDetail(val module: ModuleItem) : Screen()
    object Chat : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enqueue WorkManager background sync worker
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "AlsTelemetrySyncWork",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        setContent {
            val tokenManager = remember { TokenManager(applicationContext) }
            val authRepository = remember { AuthRepository(applicationContext) }
            var isDarkMode by remember { mutableStateOf(tokenManager.isDarkMode()) }

            ALSSDOSanJuanTheme(darkTheme = isDarkMode) {
                val hasToken = remember { !tokenManager.getToken().isNullOrEmpty() }

                var currentScreen by remember {
                    mutableStateOf<Screen>(Screen.Splash)
                }
                var currentUser by remember {
                    mutableStateOf<User?>(
                        if (hasToken) tokenManager.getUser() else null
                    )
                }

                when (val screen = currentScreen) {
                    is Screen.Splash -> {
                        SplashScreen(
                            onSplashFinished = {
                                currentScreen = if (hasToken && currentUser != null) Screen.Dashboard else Screen.Login
                            }
                        )
                    }
                    is Screen.Login -> {
                        LoginScreen(
                            onLoginSuccess = { user ->
                                currentUser = user
                                currentScreen = Screen.Dashboard
                            }
                        )
                    }
                    is Screen.Dashboard -> {
                        currentUser?.let { user ->
                            DashboardScreen(
                                user = user,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = { enabled ->
                                    isDarkMode = enabled
                                    tokenManager.saveDarkMode(enabled)
                                },
                                onModuleClick = { module ->
                                    currentScreen = Screen.ModuleDetail(module)
                                },
                                onOpenChat = {
                                    currentScreen = Screen.Chat
                                },
                                onLogout = {
                                    authRepository.logout()
                                    currentUser = null
                                    currentScreen = Screen.Login
                                }
                            )
                        } ?: run {
                            currentScreen = Screen.Login
                        }
                    }
                    is Screen.ModuleDetail -> {
                        ModuleDetailScreen(
                            module = screen.module,
                            onBack = {
                                currentScreen = Screen.Dashboard
                            }
                        )
                    }
                    is Screen.Chat -> {
                        ChatScreen(
                            onBack = {
                                currentScreen = Screen.Dashboard
                            }
                        )
                    }
                }
            }
        }
    }
}
