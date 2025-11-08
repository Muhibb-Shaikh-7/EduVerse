package com.example.eduverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.eduverse.data.model.UserRole
import com.example.eduverse.ui.auth.AuthViewModel
import com.example.eduverse.ui.navigation.NavGraph
import com.example.eduverse.ui.navigation.Screen
import com.example.eduverse.ui.theme.EduVerseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduVerseTheme {
                E_EducationPlatformApp()
            }
        }
    }
}

@Composable
fun E_EducationPlatformApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    // Determine start destination based on auth state
    val startDestination = when {
        authState.user != null -> {
            when (authState.user?.role) {
                UserRole.ADMIN -> Screen.AdminDashboard.route
                UserRole.TEACHER -> Screen.TeacherDashboard.route
                UserRole.STUDENT -> Screen.StudentDashboard.route
                null -> Screen.Login.route
            }
        }

        else -> Screen.Login.route
    }

    NavGraph(
        navController = navController,
        startDestination = startDestination
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    EduVerseTheme {
        E_EducationPlatformApp()
    }
}