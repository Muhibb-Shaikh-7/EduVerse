package com.example.eduverse.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduverse.ui.auth.AuthViewModel
import com.example.eduverse.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    var selectedScreen by remember { mutableStateOf<TeacherScreen?>(null) }
    var currentNavRoute by remember { mutableStateOf("dashboard") }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Navigate to feature screens
    if (selectedScreen != null) {
        when (selectedScreen) {
            TeacherScreen.UploadMaterial -> UploadMaterialScreen(
                onBack = {
                    selectedScreen = null
                    currentNavRoute = "dashboard"
                }
            )

            TeacherScreen.OCRProcessing -> OCRProcessingScreen(
                onBack = {
                    selectedScreen = null
                    currentNavRoute = "dashboard"
                }
            )

            TeacherScreen.FlashcardGenerator -> FlashcardGeneratorScreen(
                onBack = {
                    selectedScreen = null
                    currentNavRoute = "dashboard"
                }
            )

            TeacherScreen.MCQQuizCreator -> MCQQuizCreatorScreen(
                onBack = {
                    selectedScreen = null
                    currentNavRoute = "dashboard"
                }
            )
            null -> {}
        }
    } else {
        // Main dashboard with Material 3 Scaffold
        Scaffold(
            topBar = {
                EduVerseLargeTopAppBar(
                    title = "Teacher Dashboard",
                    actions = {
                        IconButton(onClick = {
                            viewModel.logout()
                            onLogout()
                        }) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Logout",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                // Navigation Bar for quick access
                val navigationItems = listOf(
                    NavigationItem(
                        label = "Dashboard",
                        icon = Icons.Default.Dashboard,
                        selectedIcon = Icons.Filled.Dashboard,
                        route = "dashboard"
                    ),
                    NavigationItem(
                        label = "Upload",
                        icon = Icons.Default.Upload,
                        selectedIcon = Icons.Filled.Upload,
                        route = "upload"
                    ),
                    NavigationItem(
                        label = "OCR",
                        icon = Icons.Default.Scanner,
                        selectedIcon = Icons.Filled.Scanner,
                        route = "ocr"
                    ),
                    NavigationItem(
                        label = "Tools",
                        icon = Icons.Default.Construction,
                        selectedIcon = Icons.Filled.Construction,
                        route = "tools"
                    )
                )

                EduVerseNavigationBar(
                    items = navigationItems,
                    selectedRoute = currentNavRoute,
                    onItemClick = { route ->
                        currentNavRoute = route
                        when (route) {
                            "dashboard" -> selectedScreen = null
                            "upload" -> selectedScreen = TeacherScreen.UploadMaterial
                            "ocr" -> selectedScreen = TeacherScreen.OCRProcessing
                            "tools" -> { /* Show tools menu */
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Welcome Section
                WelcomeCard()

                Spacer(modifier = Modifier.height(24.dp))

                // Section Header
                Text(
                    "Teaching Tools",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Select a tool to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Feature Grid with improved layout
                teacherFeatures.chunked(2).forEach { rowFeatures ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowFeatures.forEach { feature ->
                            FeatureCard(
                                feature = feature,
                                onClick = { selectedScreen = feature.screen },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Add empty space if odd number of items
                        if (rowFeatures.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Welcome Card with quick info
 */
@Composable
private fun WelcomeCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Welcome back!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Ready to create engaging content for your students",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

enum class TeacherScreen {
    UploadMaterial,
    OCRProcessing,
    FlashcardGenerator,
    MCQQuizCreator
}

data class TeacherFeature(
    val screen: TeacherScreen,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

val teacherFeatures = listOf(
    TeacherFeature(
        TeacherScreen.UploadMaterial,
        "Upload Material",
        "Upload PDFs or images",
        Icons.Default.Upload,
        Color(0xFF2196F3)
    ),
    TeacherFeature(
        TeacherScreen.OCRProcessing,
        "OCR Processing",
        "Extract text from images",
        Icons.Default.Scanner,
        Color(0xFF4CAF50)
    ),
    TeacherFeature(
        TeacherScreen.FlashcardGenerator,
        "Flashcards",
        "Generate study cards",
        Icons.Default.Style,
        Color(0xFFFF9800)
    ),
    TeacherFeature(
        TeacherScreen.MCQQuizCreator,
        "MCQ Quiz",
        "Create quizzes",
        Icons.Default.Quiz,
        Color(0xFF9C27B0)
    )
)

@Composable
fun FeatureCard(
    feature: TeacherFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = feature.color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = feature.color,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                feature.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = feature.color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
