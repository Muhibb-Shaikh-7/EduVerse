package com.example.eduverse.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MCQQuizCreatorScreen(onBack: () -> Unit) {
    var quizTitle by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<MCQQuestion>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MCQ Quiz Creator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (questions.isNotEmpty()) {
                        IconButton(onClick = { showAssignDialog = true }) {
                            Icon(Icons.Default.Send, "Assign Quiz")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, "Add Question")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Create MCQ Quiz",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Create multiple choice quizzes and assign to students",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quiz Title
            OutlinedTextField(
                value = quizTitle,
                onValueChange = { quizTitle = it },
                label = { Text("Quiz Title") },
                placeholder = { Text("e.g., Machine Learning Quiz") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Questions Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Questions (${questions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (questions.isNotEmpty()) {
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (questions.isEmpty()) {
                // Empty State
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Quiz,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No questions yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Tap the + button to add questions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Questions List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(questions) { index, question ->
                        QuestionItem(
                            question = question,
                            index = index + 1,
                            onDelete = {
                                questions = questions.filterIndexed { i, _ -> i != index }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Question Dialog
    if (showAddDialog) {
        AddQuestionDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { question ->
                questions = questions + question
                showAddDialog = false
            }
        )
    }

    // Assign Quiz Dialog
    if (showAssignDialog) {
        AssignQuizDialog(
            quizTitle = quizTitle.ifBlank { "Untitled Quiz" },
            questionCount = questions.size,
            onDismiss = { showAssignDialog = false },
            onAssign = { students, classes ->
                // Save quiz to Firestore and assign
                showAssignDialog = false
            }
        )
    }
}

data class MCQQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String
)

@Composable
fun QuestionItem(
    question: MCQQuestion,
    index: Int,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Question #$index",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                question.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                question.options.forEachIndexed { optIndex, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        if (optIndex == question.correctAnswer) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Correct",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (question.explanation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Explanation:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                question.explanation,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddQuestionDialog(
    onDismiss: () -> Unit,
    onAdd: (MCQQuestion) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var option1 by remember { mutableStateOf("") }
    var option2 by remember { mutableStateOf("") }
    var option3 by remember { mutableStateOf("") }
    var option4 by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableIntStateOf(0) }
    var explanation by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Question") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Question") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Options
                listOf(
                    option1 to { o: String -> option1 = o },
                    option2 to { o: String -> option2 = o },
                    option3 to { o: String -> option3 = o },
                    option4 to { o: String -> option4 = o }
                ).forEachIndexed { index, (value, setValue) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = correctAnswer == index,
                            onClick = { correctAnswer = index }
                        )
                        OutlinedTextField(
                            value = value,
                            onValueChange = setValue,
                            label = { Text("Option ${index + 1}") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Explanation (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        MCQQuestion(
                            question = question,
                            options = listOf(option1, option2, option3, option4),
                            correctAnswer = correctAnswer,
                            explanation = explanation
                        )
                    )
                },
                enabled = question.isNotBlank() && option1.isNotBlank() && option2.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AssignQuizDialog(
    quizTitle: String,
    questionCount: Int,
    onDismiss: () -> Unit,
    onAssign: (List<String>, List<String>) -> Unit
) {
    var selectedStudents by remember { mutableStateOf(setOf<String>()) }
    var selectedClasses by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Quiz") },
        text = {
            Column {
                Text(
                    "$quizTitle ($questionCount questions)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text("Assign to:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                // Mock class/student selection
                listOf("Class 10A", "Class 10B", "Advanced CS").forEach { className ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = selectedClasses.contains(className),
                            onCheckedChange = {
                                selectedClasses = if (it) {
                                    selectedClasses + className
                                } else {
                                    selectedClasses - className
                                }
                            }
                        )
                        Text(className)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAssign(selectedStudents.toList(), selectedClasses.toList())
                },
                enabled = selectedClasses.isNotEmpty() || selectedStudents.isNotEmpty()
            ) {
                Icon(Icons.Default.Send, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Assign")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
