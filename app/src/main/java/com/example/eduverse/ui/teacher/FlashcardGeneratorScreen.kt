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
fun FlashcardGeneratorScreen(onBack: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    var flashcards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var setName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flashcard Generator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (flashcards.isNotEmpty()) {
                        IconButton(onClick = { showSaveDialog = true }) {
                            Icon(Icons.Default.Save, "Save")
                        }
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
            Text(
                "Generate Flashcards",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Enter text or paste OCR extracted content to generate flashcards",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (flashcards.isEmpty()) {
                // Input Section
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    label = { Text("Enter text content") },
                    placeholder = { Text("Paste OCR extracted text or type your content here...") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isGenerating = true
                        // Simulate AI generation
                        flashcards = listOf(
                            Flashcard(
                                "What is Machine Learning?",
                                "A subset of AI that enables systems to learn from experience"
                            ),
                            Flashcard(
                                "Types of Machine Learning",
                                "Supervised, Unsupervised, and Reinforcement Learning"
                            ),
                            Flashcard(
                                "Application of ML",
                                "Image recognition, NLP, and predictive analytics"
                            )
                        )
                        isGenerating = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = inputText.isNotBlank() && !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generating...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Flashcards")
                    }
                }
            } else {
                // Flashcard List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${flashcards.size} Flashcards Generated",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { flashcards = emptyList() }) {
                        Text("Start Over")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(flashcards) { index, card ->
                        FlashcardItem(
                            card = card,
                            index = index + 1,
                            onEdit = { front, back ->
                                flashcards = flashcards.toMutableList().also {
                                    it[index] = Flashcard(front, back)
                                }
                            },
                            onDelete = {
                                flashcards = flashcards.filterIndexed { i, _ -> i != index }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Flashcard Set") },
            text = {
                OutlinedTextField(
                    value = setName,
                    onValueChange = { setName = it },
                    label = { Text("Set Name") },
                    placeholder = { Text("e.g., Machine Learning Basics") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Save to Firestore under teacher's ID
                        showSaveDialog = false
                        flashcards = emptyList()
                    },
                    enabled = setName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class Flashcard(
    val front: String,
    val back: String
)

@Composable
fun FlashcardItem(
    card: Flashcard,
    index: Int,
    onEdit: (String, String) -> Unit,
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
                    "Card #$index",
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

            Text(
                "Front: ${card.front}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Back:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    card.back,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
