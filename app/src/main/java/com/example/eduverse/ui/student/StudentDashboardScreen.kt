package com.example.eduverse.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eduverse.ui.auth.AuthViewModel
import com.example.eduverse.ui.components.*
import com.example.eduverse.ui.teacher.UploadMaterialScreen
import com.example.eduverse.ui.teacher.OCRProcessingScreen
import com.example.eduverse.ui.teacher.FlashcardGeneratorScreen
import com.example.eduverse.ui.teacher.MCQQuizCreatorScreen
import com.example.eduverse.data.model.StudentProgress
import com.example.eduverse.data.model.Badge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    studentViewModel: StudentViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    var selectedScreen by remember { mutableStateOf<StudentScreen?>(null) }
    var currentNavRoute by remember { mutableStateOf("dashboard") }

    val studentProgress by studentViewModel.studentProgress.collectAsState()
    val newBadges by studentViewModel.newBadges.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Show badge unlock animation dialog
    if (newBadges.isNotEmpty()) {
        BadgeUnlockAnimation(
            badges = newBadges,
            onDismiss = { studentViewModel.clearNewBadges() }
        )
    }

    // Navigate to feature screens
    if (selectedScreen != null) {
        when (selectedScreen) {
            StudentScreen.Flashcards -> MyFlashcardsScreen(
                viewModel = studentViewModel,
                onBack = {
                    selectedScreen = null
                    currentNavRoute = "dashboard"
                }
            )
            StudentScreen.Quizzes -> QuizScreen(
                viewModel = studentViewModel,
                onBack = {
                    selectedScreen = null
                    currentNavRoute = "dashboard"
                }
            )
            StudentScreen.Progress -> ProgressScreen(
                viewModel = studentViewModel,
                onBack = {
                    selectedScreen = null
                    currentNavRoute = "dashboard"
                }
            )
            StudentScreen.UploadMaterial -> UploadMaterialScreen(
                onBack = {
                    selectedScreen = null
                    currentNavRoute = "dashboard"
                }
            )

            StudentScreen.OCRProcessing -> OCRProcessingScreen(
                onBack = {
                    selectedScreen = null
                    currentNavRoute = "dashboard"
                }
            )

            StudentScreen.FlashcardGenerator -> FlashcardGeneratorScreen(
                onBack = {
                    selectedScreen = null
                    currentNavRoute = "dashboard"
                }
            )

            StudentScreen.QuizCreator -> MCQQuizCreatorScreen(
                onBack = {
                    selectedScreen = null
                    currentNavRoute = "dashboard"
                }
            )
            null -> {}
        }
    } else {
        // Main Dashboard with Scaffold and NavigationBar
        Scaffold(
            topBar = {
                EduVerseLargeTopAppBar(
                    title = "Student Dashboard",
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
                        label = "Flashcards",
                        icon = Icons.Default.Style,
                        selectedIcon = Icons.Filled.Style,
                        route = "flashcards"
                    ),
                    NavigationItem(
                        label = "Quizzes",
                        icon = Icons.Default.Quiz,
                        selectedIcon = Icons.Filled.Quiz,
                        route = "quizzes"
                    ),
                    NavigationItem(
                        label = "Progress",
                        icon = Icons.Default.TrendingUp,
                        selectedIcon = Icons.Filled.TrendingUp,
                        route = "progress"
                    )
                )

                EduVerseNavigationBar(
                    items = navigationItems,
                    selectedRoute = currentNavRoute,
                    onItemClick = { route ->
                        currentNavRoute = route
                        when (route) {
                            "dashboard" -> selectedScreen = null
                            "flashcards" -> selectedScreen = StudentScreen.Flashcards
                            "quizzes" -> selectedScreen = StudentScreen.Quizzes
                            "progress" -> selectedScreen = StudentScreen.Progress
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

                // Enhanced Gamification Cards
                studentProgress?.let { progress ->
                    // XP Bar with smooth animations
                    AnimatedXPBar(
                        currentXP = progress.xp % 100, // Current XP in level
                        xpForNextLevel = 100,
                        level = progress.level,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Streak Counter with fire animation
                    AnimatedStreakCounter(
                        streakDays = progress.streak,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Learning Hub",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Continue your learning journey",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Feature Grid with animated cards
                studentFeatures.chunked(2).forEach { rowFeatures ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowFeatures.forEach { feature ->
                            StudentFeatureCard(
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

                // Quick Stats Section
                studentProgress?.let { progress ->
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Quick Stats",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    QuickStatsCard(progress)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Quick Stats Card showing overview
 */
@Composable
private fun QuickStatsCard(progress: StudentProgress) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickStatItem(
                icon = Icons.Default.EmojiEvents,
                value = progress.badges.size.toString(),
                label = "Badges",
                color = MaterialTheme.colorScheme.primary
            )
            QuickStatItem(
                icon = Icons.Default.Quiz,
                value = progress.completedQuizzes.toString(),
                label = "Quizzes",
                color = MaterialTheme.colorScheme.tertiary
            )
            QuickStatItem(
                icon = Icons.Default.Star,
                value = progress.xp.toString(),
                label = "Total XP",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun QuickStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun BadgeUnlockDialog(badges: List<Badge>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(badges.first().emoji, style = MaterialTheme.typography.displayMedium)
        },
        title = {
            Text(
                if (badges.size == 1) "New Badge Unlocked!" else "${badges.size} New Badges!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                badges.forEach { badge ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            badge.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            badge.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Awesome!")
            }
        }
    )
}

enum class StudentScreen {
    Flashcards,
    Quizzes,
    Progress,
    UploadMaterial,
    OCRProcessing,
    FlashcardGenerator,
    QuizCreator
}

data class StudentFeature(
    val screen: StudentScreen,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

val studentFeatures = listOf(
    StudentFeature(
        StudentScreen.Flashcards,
        "Flashcards",
        "Study smart cards",
        Icons.Default.Style,
        Color(0xFF2196F3)
    ),
    StudentFeature(
        StudentScreen.Quizzes,
        "Quizzes",
        "Test your knowledge",
        Icons.Default.Quiz,
        Color(0xFF4CAF50)
    ),
    StudentFeature(
        StudentScreen.Progress,
        "Progress",
        "Track achievements",
        Icons.Default.TrendingUp,
        Color(0xFFFF9800)
    ),
    StudentFeature(
        StudentScreen.UploadMaterial,
        "Upload Material",
        "Share study resources",
        Icons.Default.CloudUpload,
        Color(0xFF9C27B0)
    ),
    StudentFeature(
        StudentScreen.OCRProcessing,
        "OCR Scanner",
        "Scan & extract text",
        Icons.Default.CameraAlt,
        Color(0xFFE91E63)
    ),
    StudentFeature(
        StudentScreen.FlashcardGenerator,
        "Create Flashcards",
        "Build your own cards",
        Icons.Default.AutoAwesome,
        Color(0xFF00BCD4)
    ),
    StudentFeature(
        StudentScreen.QuizCreator,
        "Create Quiz",
        "Make custom quizzes",
        Icons.Default.Create,
        Color(0xFFFF5722)
    )
)

@Composable
fun StudentFeatureCard(
    feature: StudentFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
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

@Composable
fun GamificationCard(progress: StudentProgress) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Level ${progress.level}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${progress.xp} XP",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Streak Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF6B35)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${progress.streak} day",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // XP Progress to next level
            val xpToNextLevel = (progress.level * 100)
            val currentLevelXP = progress.xp % 100
            val progressFraction = currentLevelXP / 100f

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Progress to Level ${progress.level + 1}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        "$currentLevelXP / 100 XP",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
