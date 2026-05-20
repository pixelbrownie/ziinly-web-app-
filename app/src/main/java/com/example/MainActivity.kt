package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FlipbookScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ZineViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        val viewModel: ZineViewModel = viewModel()

        NavHost(
          navController = navController,
          startDestination = "dashboard",
          modifier = Modifier.fillMaxSize()
        ) {
          // 1. Dashboard Bookshelf Screen
          composable("dashboard") {
            DashboardScreen(
              viewModel = viewModel,
              onNavigateToEditor = { zineId ->
                if (zineId == null) {
                  navController.navigate("editor")
                } else {
                  navController.navigate("editor?zineId=$zineId")
                }
              },
              onNavigateToFlipbook = { zineId ->
                navController.navigate("flipbook/$zineId")
              },
              onNavigateToExplore = {
                navController.navigate("explore")
              }
            )
          }

          // 2. Blueprint Editor Screen (optional zineId)
          composable(
            route = "editor?zineId={zineId}",
            arguments = listOf(
              navArgument("zineId") {
                type = NavType.IntType
                defaultValue = -1
              }
            )
          ) { backStackEntry ->
            val zineId = backStackEntry.arguments?.getInt("zineId") ?: -1
            LaunchedEffect(zineId) {
              if (zineId != -1 && viewModel.editedZineId.value != zineId) {
                viewModel.loadZineForEditing(zineId)
              }
            }
            EditorScreen(
              viewModel = viewModel,
              onNavigateBack = {
                navController.popBackStack()
              },
              onNavigateToFlipbook = { activeId ->
                navController.navigate("flipbook/$activeId") {
                  popUpTo("dashboard")
                }
              }
            )
          }

          // 3. 3D Flipbook Viewer Screen
          composable(
            route = "flipbook/{zineId}",
            arguments = listOf(
              navArgument("zineId") { type = NavType.IntType }
            )
          ) { backStackEntry ->
            val zineId = backStackEntry.arguments?.getInt("zineId") ?: -1
            FlipbookScreen(
              viewModel = viewModel,
              zineId = zineId,
              onNavigateBack = {
                navController.popBackStack()
              }
            )
          }

          // 4. Public Explore Screen
          composable("explore") {
            ExploreScreen(
              viewModel = viewModel,
              onNavigateToBookshelf = {
                navController.navigate("dashboard") {
                  popUpTo("dashboard") { inclusive = true }
                }
              },
              onNavigateToFlipbook = { zineId ->
                navController.navigate("flipbook/$zineId")
              }
            )
          }
        }
      }
    }
  }
}

