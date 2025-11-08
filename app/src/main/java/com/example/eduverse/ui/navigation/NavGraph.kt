package com.example.eduverse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.eduverse.ui.auth.LoginScreen
import com.example.eduverse.ui.auth.RegisterScreen
import com.example.eduverse.ui.admin.AdminDashboardScreen
import com.example.eduverse.ui.teacher.TeacherDashboardScreen
import com.example.eduverse.ui.student.StudentDashboardScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object AdminDashboard : Screen("admin_dashboard")
    object TeacherDashboard : Screen("teacher_dashboard")
    object StudentDashboard : Screen("student_dashboard")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToAdminDashboard = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToTeacherDashboard = {
                    navController.navigate(Screen.TeacherDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToStudentDashboard = {
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToAdminDashboard = {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToTeacherDashboard = {
                    navController.navigate(Screen.TeacherDashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToStudentDashboard = {
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TeacherDashboard.route) {
            TeacherDashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.StudentDashboard.route) {
            StudentDashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
