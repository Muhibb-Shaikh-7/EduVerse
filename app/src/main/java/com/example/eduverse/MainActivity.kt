package com.example.eduverse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eduverse.ui.theme.EduVerseTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// TODO: After Gradle sync completes, replace these temporary stub imports with actual SDK imports:
// import com.runanywhere.sdk.public.RunAnywhere
// import com.runanywhere.sdk.public.extensions.listAvailableModels
import com.example.eduverse.stubs.RunAnywhereStub as RunAnywhere
import com.example.eduverse.stubs.listAvailableModelsStub as listAvailableModels
import com.example.eduverse.stubs.ModelInfoStub as SDKModelInfo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduVerseTheme {
                EduVerseApp()
            }
        }
    }
}

// Data Models
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ModelInfo(
    val id: String,
    val name: String,
    val isDownloaded: Boolean = false,
    val isLoaded: Boolean = false
)

// ViewModel
class EduVerseViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _availableModels = MutableStateFlow<List<ModelInfo>>(emptyList())
    val availableModels: StateFlow<List<ModelInfo>> = _availableModels

    private val _currentModel = MutableStateFlow<String?>(null)
    val currentModel: StateFlow<String?> = _currentModel

    private val _downloadProgress = MutableStateFlow<Pair<String, Float>?>(null)
    val downloadProgress: StateFlow<Pair<String, Float>?> = _downloadProgress

    init {
        loadAvailableModels()
    }

    fun loadAvailableModels() {
        viewModelScope.launch {
            try {
                val models = listAvailableModels()
                _availableModels.value = models.map { model ->
                    ModelInfo(
                        id = model.id,
                        name = model.name,
                        isDownloaded = model.downloadProgress == 1.0f,
                        isLoaded = false
                    )
                }
            } catch (e: Exception) {
                addSystemMessage("Failed to load models: ${e.message}")
            }
        }
    }

    fun downloadModel(modelId: String) {
        viewModelScope.launch {
            try {
                RunAnywhere.downloadModel(modelId).collect { progress ->
                    _downloadProgress.value = modelId to progress
                }
                _downloadProgress.value = null
                loadAvailableModels()
                addSystemMessage("Model downloaded successfully!")
            } catch (e: Exception) {
                _downloadProgress.value = null
                addSystemMessage("Download failed: ${e.message}")
            }
        }
    }

    fun loadModel(modelId: String) {
        viewModelScope.launch {
            try {
                val success = RunAnywhere.loadModel(modelId)
                if (success) {
                    _currentModel.value = modelId
                    addSystemMessage("Model '$modelId' loaded successfully!")
                    loadAvailableModels()
                } else {
                    addSystemMessage("Failed to load model")
                }
            } catch (e: Exception) {
                addSystemMessage("Load failed: ${e.message}")
            }
        }
    }

    fun sendMessage(text: String) {
        if (_currentModel.value == null) {
            addSystemMessage("Please load a model first!")
            return
        }

        // Add user message
        _messages.value += ChatMessage(text, isUser = true)

        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Generate response with streaming
                var assistantResponse = ""
                RunAnywhere.generateStream(text).collect { token ->
                    assistantResponse += token

                    // Update assistant message in real-time
                    val currentMessages = _messages.value.toMutableList()
                    if (currentMessages.lastOrNull()?.isUser == false) {
                        currentMessages[currentMessages.lastIndex] =
                            ChatMessage(assistantResponse, isUser = false)
                    } else {
                        currentMessages.add(ChatMessage(assistantResponse, isUser = false))
                    }
                    _messages.value = currentMessages
                }
            } catch (e: Exception) {
                addSystemMessage("Error: ${e.message}")
            }

            _isLoading.value = false
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
    }

    private fun addSystemMessage(text: String) {
        _messages.value += ChatMessage("🤖 $text", isUser = false)
    }
}

// Main App Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduVerseApp(viewModel: EduVerseViewModel = viewModel()) {
    var showModelSelector by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        if (showModelSelector) {
            ModelSelectorScreen(
                viewModel = viewModel,
                onModelLoaded = { showModelSelector = false },
                modifier = Modifier.padding(padding)
            )
        } else {
            ChatScreen(
                viewModel = viewModel,
                onBackToModels = { showModelSelector = true },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

// Model Selector Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorScreen(
    viewModel: EduVerseViewModel,
    onModelLoaded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val availableModels by viewModel.availableModels.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "EduVerse AI",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select and load a model to start chatting",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (availableModels.isEmpty()) {
            CircularProgressIndicator()
            Text(
                text = "Loading available models...",
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(availableModels) { model ->
                    ModelCard(
                        model = model,
                        downloadProgress = if (downloadProgress?.first == model.id) downloadProgress?.second else null,
                        onDownload = { viewModel.downloadModel(model.id) },
                        onLoad = {
                            viewModel.loadModel(model.id)
                            onModelLoaded()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModelCard(
    model: ModelInfo,
    downloadProgress: Float?,
    onDownload: () -> Unit,
    onLoad: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = model.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (downloadProgress != null) {
                LinearProgressIndicator(
                    progress = { downloadProgress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${(downloadProgress * 100).toInt()}% downloaded",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!model.isDownloaded) {
                        Button(onClick = onDownload) {
                            Text("Download")
                        }
                    } else {
                        Button(onClick = onLoad) {
                            Text("Load Model")
                        }
                    }
                }
            }
        }
    }
}

// Chat Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: EduVerseViewModel,
    onBackToModels: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentModel by viewModel.currentModel.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("EduVerse AI Chat")
                        currentModel?.let {
                            Text(
                                text = "Model: $it",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    TextButton(onClick = onBackToModels) {
                        Text("Models")
                    }
                    TextButton(onClick = { viewModel.clearChat() }) {
                        Text("Clear")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Text(
                            text = "Start a conversation by typing a message below!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                items(messages) { message ->
                    MessageBubble(message)
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Input Field
            Surface(
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        enabled = !isLoading
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = !isLoading && inputText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}