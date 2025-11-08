package com.example.eduverse.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduverse.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Dashboard") },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add new content */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Content")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                text = "Teaching Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Teacher Features
            TeacherFeatureCard(
                icon = Icons.Default.Book,
                title = "My Courses",
                description = "Manage your courses and lessons"
            )

            Spacer(modifier = Modifier.height(16.dp))

            TeacherFeatureCard(
                icon = Icons.Default.Quiz,
                title = "AI Quiz Generator",
                description = "Create quizzes with ML Kit"
            )

            Spacer(modifier = Modifier.height(16.dp))

            TeacherFeatureCard(
                icon = Icons.Default.Style,
                title = "Flashcard Creator",
                description = "Generate flashcards for students"
            )

            Spacer(modifier = Modifier.height(16.dp))

            TeacherFeatureCard(
                icon = Icons.Default.Assignment,
                title = "Student Assignments",
                description = "Review and grade submissions"
            )

            Spacer(modifier = Modifier.height(16.dp))

            TeacherFeatureCard(
                icon = Icons.Default.BarChart,
                title = "Class Analytics",
                description = "Track student progress"
            )
        }
    }
}

@Composable
fun TeacherFeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
