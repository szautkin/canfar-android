package net.canfar.verbinal.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.canfar.verbinal.ui.MainViewModel
import net.canfar.verbinal.ui.dashboard.DashboardScreen
import net.canfar.verbinal.ui.login.LoginScreen

@Composable
fun VerbinalNavHost(
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val mainState by mainViewModel.uiState.collectAsState()
    val navController = rememberNavController()

    val startDestination = when {
        mainState.isLoading -> "loading"
        mainState.isAuthenticated -> "dashboard"
        else -> "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("loading") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = { username, userInfo ->
                    mainViewModel.updateAuthState(username, userInfo)
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }

        composable("dashboard") {
            DashboardScreen(
                username = mainState.username,
                onLogout = {
                    mainViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
            )
        }
    }
}
