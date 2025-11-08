package com.example.eduverse.ui.teacher

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
fun OCRProcessingScreen(onBack: () -> Unit) {
    var selectedImage by remember { mutableStateOf<String?>(null) }
    var extractedText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImage = "image_${System.currentTimeMillis()}.jpg"
            // Simulate OCR processing
            isProcessing = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OCR Processing") },
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Extract Text from Images",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Use ML Kit to extract text from images for creating flashcards and quizzes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Image Selection
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { imagePicker.launch("image/*") }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        selectedImage ?: "Select Image",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "JPG, PNG supported",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Process Button
            if (selectedImage != null && !isProcessing && extractedText.isEmpty()) {
                Button(
                    onClick = {
                        isProcessing = true
                        // Simulate OCR delay
                        extractedText = """
                            Chapter 5: Introduction to Machine Learning
                            
                            Machine Learning is a subset of artificial intelligence that enables systems to learn and improve from experience without being explicitly programmed.
                            
                            Key Concepts:
                            1. Supervised Learning
                            2. Unsupervised Learning
                            3. Reinforcement Learning
                            
                            Applications include image recognition, natural language processing, and predictive analytics.
                        """.trimIndent()
                        isProcessing = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Scanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extract Text with ML Kit")
                }
            }

            // Processing Indicator
            if (isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Processing with ML Kit OCR...")
                    }
                }
            }

            // Extracted Text
            if (extractedText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Extracted Text",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = extractedText,
                    onValueChange = { extractedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    label = { Text("Edit extracted text") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* Save to Firestore */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save")
                    }
                    OutlinedButton(
                        onClick = { /* Copy */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy")
                    }
                }
            }
        }
    }
}
