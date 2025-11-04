package com.runanywhere.startup_hackathon20.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.runanywhere.startup_hackathon20.ChatScreen
import com.runanywhere.startup_hackathon20.ChatViewModel
import com.runanywhere.startup_hackathon20.ui.auth.LoginScreen
import com.runanywhere.startup_hackathon20.ui.auth.RegisterScreen
import com.runanywhere.startup_hackathon20.ui.projects.ProjectDetailScreen
import com.runanywhere.startup_hackathon20.ui.projects.ProjectListScreen
import com.runanywhere.startup_hackathon20.viewmodel.AuthViewModel
import com.runanywhere.startup_hackathon20.viewmodel.ProjectViewModel
import com.runanywhere.startup_hackathon20.viewmodel.TaskViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ProjectList : Screen("project_list")
    object ProjectDetail : Screen("project_detail")
    object Chat : Screen("chat")
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    projectViewModel: ProjectViewModel,
    taskViewModel: TaskViewModel,
    chatViewModel: ChatViewModel,
    navController: NavHostController = rememberNavController()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Dynamically determine start destination based on login state
    val startDestination = if (isLoggedIn) {
        Screen.ProjectList.route
    } else {
        Screen.Login.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.ProjectList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.ProjectList.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        // Project List Screen
        composable(Screen.ProjectList.route) {
            ProjectListScreen(
                projectViewModel = projectViewModel,
                authViewModel = authViewModel,
                onProjectClick = { project ->
                    projectViewModel.setSelectedProject(project)
                    navController.navigate(Screen.ProjectDetail.route)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Project Detail Screen
        composable(Screen.ProjectDetail.route) {
            val selectedProject by projectViewModel.selectedProject.collectAsState()
            selectedProject?.let { project ->
                ProjectDetailScreen(
                    project = project,
                    taskViewModel = taskViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Chat Screen
        composable(Screen.Chat.route) {
            ChatScreen(viewModel = chatViewModel)
        }
    }
}

