package com.example.eduverse.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.eduverse.data.model.Quiz
import com.example.eduverse.data.model.QuizAnswer
import com.example.eduverse.data.model.StudentProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: StudentViewModel,
    onBack: () -> Unit
) {
    val quizzes by viewModel.quizzes.collectAsState()
    var selectedQuiz by remember { mutableStateOf<Quiz?>(null) }

    if (selectedQuiz != null) {
        QuizTakingScreen(
            quiz = selectedQuiz!!,
            onComplete = { score, answers ->
                // Save quiz completion to progress
                viewModel.completeQuiz(
                    quiz = selectedQuiz!!,
                    score = score,
                    totalQuestions = selectedQuiz!!.questions.size,
                    answers = answers
                )
                selectedQuiz = null
            },
            onBack = { selectedQuiz = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Available Quizzes") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Test Your Knowledge",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(quizzes) { quiz ->
                    QuizCard(quiz = quiz, onClick = { selectedQuiz = quiz })
                }

                if (quizzes.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Quiz,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No quizzes available",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Check back later for new quizzes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizCard(quiz: Quiz, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        quiz.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        quiz.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "${quiz.questions.size} questions",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "•",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            "${quiz.xpReward} XP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
                Button(onClick = onClick) {
                    Text("Start")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizTakingScreen(
    quiz: Quiz,
    onComplete: (score: Int, answers: List<QuizAnswer>) -> Unit,
    onBack: () -> Unit
) {
    var currentQ by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var showExplanation by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    val answers = remember { mutableStateOf<List<QuizAnswer>>(emptyList()) }
    var showResult by remember { mutableStateOf(false) }

    val questions = quiz.questions

    if (showResult) {
        QuizResultScreen(
            score = score,
            total = questions.size,
            xpEarned = quiz.xpReward,
            onBack = {
                onComplete(score, answers.value)
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(quiz.title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (currentQ + 1f) / questions.size },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Question ${currentQ + 1}/${questions.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(questions[currentQ].question, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(24.dp))

                questions[currentQ].options.forEachIndexed { index, option ->
                    val isCorrect = index == questions[currentQ].correctAnswer
                    val isSelected = index == selectedAnswer

                    Card(
                        onClick = {
                            if (!showExplanation) {
                                selectedAnswer = index
                                showExplanation = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                showExplanation && isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                showExplanation && isSelected && !isCorrect -> Color(0xFFF44336).copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    if (!showExplanation) {
                                        selectedAnswer = index
                                        showExplanation = true
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option, modifier = Modifier.weight(1f))
                            if (showExplanation && isCorrect) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                            } else if (showExplanation && isSelected && !isCorrect) {
                                Icon(Icons.Default.Cancel, null, tint = Color(0xFFF44336))
                            }
                        }
                    }
                }

                if (showExplanation) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (selectedAnswer == questions[currentQ].correctAnswer)
                                        Icons.Default.CheckCircle
                                    else
                                        Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (selectedAnswer == questions[currentQ].correctAnswer)
                                        Color(0xFF4CAF50)
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (selectedAnswer == questions[currentQ].correctAnswer)
                                        "Correct!"
                                    else
                                        "Explanation:",
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedAnswer == questions[currentQ].correctAnswer)
                                        Color(0xFF4CAF50)
                                    else
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(questions[currentQ].explanation)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val isCorrect = selectedAnswer == questions[currentQ].correctAnswer
                        if (isCorrect) score++

                        // Record answer
                        answers.value = answers.value + QuizAnswer(
                            questionId = questions[currentQ].id,
                            selectedAnswer = selectedAnswer,
                            correctAnswer = questions[currentQ].correctAnswer,
                            isCorrect = isCorrect
                        )
                        
                        if (currentQ < questions.size - 1) {
                            currentQ++
                            selectedAnswer = -1
                            showExplanation = false
                        } else {
                            showResult = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedAnswer != -1
                ) {
                    Text(if (currentQ < questions.size - 1) "Next Question" else "Finish Quiz")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(score: Int, total: Int, xpEarned: Int, onBack: () -> Unit) {
    val percentage = (score.toFloat() / total * 100).toInt()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Complete!") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                null,
                modifier = Modifier.size(100.dp),
                tint = when {
                    percentage >= 90 -> Color(0xFFFFD700) // Gold
                    percentage >= 70 -> Color(0xFFC0C0C0) // Silver
                    else -> Color(0xFFCD7F32) // Bronze
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                when {
                    percentage >= 90 -> "Excellent!"
                    percentage >= 70 -> "Great Job!"
                    percentage >= 50 -> "Good Effort!"
                    else -> "Keep Practicing!"
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Score: $score/$total", style = MaterialTheme.typography.headlineMedium)
            Text(
                "$percentage% Correct",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("XP Earned", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "+$xpEarned XP",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back to Quizzes")
            }
        }
    }
}
