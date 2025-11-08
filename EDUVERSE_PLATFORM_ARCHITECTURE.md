# EduVerse - Complete E-Education Platform Architecture

## 🏗️ Project Overview

A comprehensive Android E-Education Platform built with:

- **Jetpack Compose** for modern UI
- **Firebase** for backend (Auth, Firestore, Storage, ML Kit)
- **RunanywhereAI SDK** for AI-powered quiz/flashcard generation
- **MVVM Architecture** with clean separation of concerns
- **Hilt** for dependency injection
- **Three User Roles**: Admin, Teacher, Student

---

## 📁 Project Structure

```
app/src/main/java/com/example/eduverse/
├── data/
│   ├── models/              # Data classes
│   │   ├── User.kt
│   │   ├── Quiz.kt
│   │   ├── Flashcard.kt
│   │   ├── StudentProgress.kt
│   │   └── Achievement.kt
│   ├── repository/          # Data layer
│   │   ├── AuthRepository.kt
│   │   ├── UserRepository.kt
│   │   ├── QuizRepository.kt
│   │   ├── FlashcardRepository.kt
│   │   └── StorageRepository.kt
│   └── local/               # Local data sources
│       └── PreferencesManager.kt
│
├── domain/
│   ├── use_cases/           # Business logic
│   │   ├── auth/
│   │   ├── quiz/
│   │   ├── flashcard/
│   │   └── progress/
│   └── ai/                  # AI services
│       ├── QuizGeneratorService.kt
│       ├── FlashcardGeneratorService.kt
│       └── OCRService.kt
│
├── presentation/
│   ├── navigation/          # Navigation setup
│   │   ├── NavGraph.kt
│   │   ├── NavigationRoutes.kt
│   │   └── RoleBasedNav.kt
│   │
│   ├── auth/                # Authentication screens
│   │   ├── LoginScreen.kt
│   │   ├── SignUpScreen.kt
│   │   ├── LoginViewModel.kt
│   │   └── SignUpViewModel.kt
│   │
│   ├── admin/               # Admin module
│   │   ├── AdminDashboardScreen.kt
│   │   ├── UserManagementScreen.kt
│   │   ├── AnalyticsScreen.kt
│   │   ├── ContentModerationScreen.kt
│   │   └── AdminViewModel.kt
│   │
│   ├── teacher/             # Teacher module
│   │   ├── TeacherDashboardScreen.kt
│   │   ├── UploadContentScreen.kt
│   │   ├── QuizCreationScreen.kt
│   │   ├── FlashcardCreationScreen.kt
│   │   ├── StudentPerformanceScreen.kt
│   │   └── TeacherViewModel.kt
│   │
│   ├── student/             # Student module
│   │   ├── StudentDashboardScreen.kt
│   │   ├── QuizTakingScreen.kt
│   │   ├── FlashcardViewScreen.kt
│   │   ├── ProgressScreen.kt
│   │   ├── AchievementsScreen.kt
│   │   └── StudentViewModel.kt
│   │
│   ├── components/          # Reusable UI components
│   │   ├── QuizCard.kt
│   │   ├── FlashcardComponent.kt
│   │   ├── ProgressChart.kt
│   │   ├── AchievementBadge.kt
│   │   └── UploadDialog.kt
│   │
│   └── theme/               # Material 3 theming
│       ├── Theme.kt
│       ├── Color.kt
│       └── Type.kt
│
├── di/                      # Hilt modules
│   ├── AppModule.kt
│   ├── FirebaseModule.kt
│   ├── RepositoryModule.kt
│   └── AIModule.kt
│
└── util/                    # Utilities
    ├── Constants.kt
    ├── Extensions.kt
    ├── NetworkResult.kt
    └── UiState.kt
```

---

## 🔥 Firebase Setup

### 1. Firebase Console Configuration

1. Create Firebase project: https://console.firebase.google.com
2. Add Android app with package name: `com.example.eduverse`
3. Download `google-services.json` → place in `app/` directory
4. Enable services:
    - **Authentication**: Email/Password + Google Sign-In
    - **Firestore Database**: Native mode
    - **Storage**: Default bucket
    - **ML Kit**: Text Recognition API

### 2. Firestore Database Structure

```javascript
// Users Collection
users/{userId}
├── email: String
├── displayName: String
├── role: String  // "admin" | "teacher" | "student"
├── photoUrl: String?
├── createdAt: Timestamp
├── approved: Boolean  // For teachers
└── stats: {
    xp: Number,
    level: Number,
    streak: Number,
    badges: Array<String>
}

// Quizzes Collection
quizzes/{quizId}
├── title: String
├── description: String
├── createdBy: String  // teacherId
├── difficulty: String  // "easy" | "medium" | "hard"
├── questions: Array<{
│   question: String,
│   options: Array<String>,
│   correctAnswer: Int,
│   explanation: String
│}>
├── assignedTo: Array<String>  // studentIds
├── createdAt: Timestamp
└── tags: Array<String>

// Flashcards Collection
flashcards/{flashcardId}
├── topic: String
├── createdBy: String  // teacherId
├── cards: Array<{
│   front: String,
│   back: String
│}>
├── createdAt: Timestamp
└── visibility: String  // "public" | "private"

// StudentProgress Collection
student_progress/{studentId}/quizzes/{quizId}
├── score: Number
├── totalQuestions: Number
├── timeSpent: Number  // seconds
├── answers: Array<{
│   questionIndex: Int,
│   selectedAnswer: Int,
│   isCorrect: Boolean
│}>
├── completedAt: Timestamp
└── xpEarned: Number

// Achievements Collection
achievements/{achievementId}
├── name: String
├── description: String
├── icon: String
├── requirement: {
│   type: String,  // "quiz_count" | "streak" | "score"
│   value: Number
│}
└── xpReward: Number

// UserAchievements Collection
user_achievements/{userId}
└── achievements: Array<{
    achievementId: String,
    unlockedAt: Timestamp
}>
```

### 3. Storage Structure

```
storage/
├── uploads/
│   ├── pdfs/{userId}/{filename}.pdf
│   └── images/{userId}/{filename}.jpg
├── user_photos/{userId}/profile.jpg
└── content/{contentId}/file
```

---

## 🎯 Core Features Implementation

### 1. Authentication System

**Files to create:**

- `data/repository/AuthRepository.kt`
- `presentation/auth/LoginScreen.kt`
- `presentation/auth/LoginViewModel.kt`

**Key Functions:**

```kotlin
// Email/Password login
suspend fun signInWithEmailPassword(email: String, password: String): Result<User>

// Google Sign-In
suspend fun signInWithGoogle(idToken: String): Result<User>

// Sign up
suspend fun signUp(email: String, password: String, displayName: String, role: UserRole): Result<User>

// Sign out
suspend fun signOut()

// Get current user
fun getCurrentUser(): Flow<User?>
```

### 2. AI-Powered Quiz Generation

**Using RunanywhereAI SDK:**

```kotlin
class QuizGeneratorService @Inject constructor() {
    
    suspend fun generateQuizFromText(
        text: String,
        questionCount: Int,
        difficulty: String
    ): List<QuizQuestion> {
        val prompt = """
        Generate $questionCount multiple-choice questions from the following text.
        Difficulty: $difficulty
        
        Text: $text
        
        Format each question as JSON:
        {
          "question": "...",
          "options": ["A", "B", "C", "D"],
          "correctAnswer": 0,  // index
          "explanation": "..."
        }
        """.trimIndent()
        
        val response = RunAnywhere.generate(prompt)
        return parseQuestionsFromResponse(response)
    }
}
```

### 3. OCR Integration (ML Kit)

```kotlin
class OCRService @Inject constructor() {
    
    suspend fun extractTextFromImage(uri: Uri): Result<String> {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        
        return suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(Result.success(visionText.text))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.failure(e))
                }
        }
    }
    
    suspend fun extractTextFromPDF(uri: Uri): Result<String> {
        // Use PDFBox-Android to extract text
        val pdfDocument = PDDocument.load(inputStream)
        val stripper = PDFTextStripper()
        val text = stripper.getText(pdfDocument)
        pdfDocument.close()
        return Result.success(text)
    }
}
```

### 4. Gamification System

```kotlin
data class GamificationManager(
    private val userRepository: UserRepository
) {
    // XP levels
    fun calculateLevel(xp: Int): Int = floor(sqrt(xp / 100.0)).toInt()
    
    // Award XP
    suspend fun awardXP(userId: String, amount: Int, reason: String) {
        userRepository.updateUserXP(userId, amount)
        checkForLevelUp(userId)
        checkForAchievements(userId)
    }
    
    // Streak management
    suspend fun updateStreak(userId: String) {
        val lastActivity = userRepository.getLastActivityDate(userId)
        val today = LocalDate.now()
        
        when {
            lastActivity == today.minusDays(1) -> {
                // Continue streak
                userRepository.incrementStreak(userId)
            }
            lastActivity == today -> {
                // Already logged in today
            }
            else -> {
                // Streak broken
                userRepository.resetStreak(userId)
            }
        }
    }
    
    // Badge system
    suspend fun checkForAchievements(userId: String) {
        val progress = userRepository.getUserProgress(userId)
        val achievements = achievementRepository.getAllAchievements()
        
        achievements.forEach { achievement ->
            if (meetsRequirement(progress, achievement) && !hasAchievement(userId, achievement.id)) {
                unlockAchievement(userId, achievement.id)
            }
        }
    }
}
```

---

## 🎨 UI Components

### Admin Dashboard

```kotlin
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin Dashboard") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            // Overview Cards
            item {
                StatsCard(
                    title = "Total Users",
                    value = stats.totalUsers.toString(),
                    icon = Icons.Default.People
                )
            }
            
            item {
                StatsCard(
                    title = "Active Teachers",
                    value = stats.activeTeachers.toString(),
                    icon = Icons.Default.School
                )
            }
            
            item {
                StatsCard(
                    title = "Active Students",
                    value = stats.activeStudents.toString(),
                    icon = Icons.Default.Person
                )
            }
            
            // Pending Approvals
            item {
                Text(
                    "Pending Teacher Approvals",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            items(stats.pendingTeachers) { teacher ->
                TeacherApprovalCard(
                    teacher = teacher,
                    onApprove = { viewModel.approveTeacher(teacher.id) },
                    onReject = { viewModel.rejectTeacher(teacher.id) }
                )
            }
            
            // Analytics Charts
            item {
                EngagementChart(data = stats.engagementData)
            }
            
            item {
                QuizPerformanceChart(data = stats.quizPerformance)
            }
        }
    }
}
```

### Teacher Upload Screen

```kotlin
@Composable
fun UploadContentScreen(
    viewModel: TeacherViewModel = hiltViewModel()
) {
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    var extractedText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedFile = uri
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Upload Button
        Button(
            onClick = { launcher.launch("*/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Upload, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Upload PDF or Image")
        }
        
        // Selected File
        selectedFile?.let { uri ->
            Card(modifier = Modifier.padding(vertical = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Selected: ${uri.lastPathSegment}")
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            isProcessing = true
                            viewModel.extractText(uri)
                        },
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Extract Text")
                        }
                    }
                }
            }
        }
        
        // Extracted Text
        if (extractedText.isNotEmpty()) {
            OutlinedTextField(
                value = extractedText,
                onValueChange = { extractedText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("Extracted Text") }
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.generateFlashcards(extractedText) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Generate Flashcards")
                }
                
                Button(
                    onClick = { viewModel.generateQuiz(extractedText) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Generate Quiz")
                }
            }
        }
    }
}
```

### Student Quiz Taking Screen

```kotlin
@Composable
fun QuizTakingScreen(
    quizId: String,
    viewModel: StudentViewModel = hiltViewModel()
) {
    val quiz by viewModel.currentQuiz.collectAsState()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val showResult by viewModel.showResult.collectAsState()
    
    quiz?.let { quizData ->
        val currentQuestion = quizData.questions[currentQuestionIndex]
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Progress
            LinearProgressIndicator(
                progress = { (currentQuestionIndex + 1f) / quizData.questions.size },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Question ${currentQuestionIndex + 1}/${quizData.questions.size}",
                style = MaterialTheme.typography.labelLarge
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Question
            Text(
                currentQuestion.question,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Options
            currentQuestion.options.forEachIndexed { index, option ->
                OptionCard(
                    text = option,
                    isSelected = selectedAnswer == index,
                    isCorrect = if (showResult) index == currentQuestion.correctAnswer else null,
                    onClick = { viewModel.selectAnswer(index) }
                )
                
                Spacer(Modifier.height(12.dp))
            }
            
            // Explanation (shown after answer)
            if (showResult && selectedAnswer != null) {
                Spacer(Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedAnswer == currentQuestion.correctAnswer)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            if (selectedAnswer == currentQuestion.correctAnswer) 
                                "✓ Correct!" 
                            else 
                                "✗ Incorrect",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(currentQuestion.explanation)
                    }
                }
            }
            
            Spacer(Modifier.weight(1f))
            
            // Next Button
            Button(
                onClick = {
                    if (showResult) {
                        viewModel.nextQuestion()
                    } else {
                        viewModel.submitAnswer()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAnswer != null
            ) {
                Text(
                    if (showResult) {
                        if (currentQuestionIndex < quizData.questions.size - 1)
                            "Next Question"
                        else
                            "Finish Quiz"
                    } else {
                        "Submit Answer"
                    }
                )
            }
        }
    }
}
```

---

## 🔐 Security Rules

### Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper functions
    function isSignedIn() {
      return request.auth != null;
    }
    
    function isAdmin() {
      return isSignedIn() && 
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    
    function isTeacher() {
      return isSignedIn() && 
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'teacher';
    }
    
    function isStudent() {
      return isSignedIn() && 
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'student';
    }
    
    function isOwner(userId) {
      return isSignedIn() && request.auth.uid == userId;
    }
    
    // Users collection
    match /users/{userId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn() && isOwner(userId);
      allow update: if isOwner(userId) || isAdmin();
      allow delete: if isAdmin();
    }
    
    // Quizzes collection
    match /quizzes/{quizId} {
      allow read: if isSignedIn();
      allow create: if isTeacher() || isAdmin();
      allow update, delete: if isAdmin() || 
        resource.data.createdBy == request.auth.uid;
    }
    
    // Flashcards collection
    match /flashcards/{flashcardId} {
      allow read: if isSignedIn();
      allow create: if isTeacher() || isAdmin();
      allow update, delete: if isAdmin() || 
        resource.data.createdBy == request.auth.uid;
    }
    
    // Student progress
    match /student_progress/{studentId}/{document=**} {
      allow read: if isOwner(studentId) || isTeacher() || isAdmin();
      allow write: if isOwner(studentId);
    }
    
    // Achievements
    match /achievements/{achievementId} {
      allow read: if isSignedIn();
      allow write: if isAdmin();
    }
    
    // User achievements
    match /user_achievements/{userId} {
      allow read: if isOwner(userId) || isAdmin();
      allow write: if false; // Only server-side
    }
  }
}
```

---

## 📊 Implementation Phases

### Phase 1: Foundation (Week 1-2)

- ✅ Set up Firebase project
- ✅ Configure dependencies
- ✅ Implement authentication system
- ✅ Create user role management
- ✅ Build basic navigation structure

### Phase 2: Core Features (Week 3-4)

- ✅ Implement OCR service
- ✅ Build upload functionality
- ✅ Create quiz generation service
- ✅ Develop flashcard generation
- ✅ Build basic dashboards

### Phase 3: User Modules (Week 5-6)

- ✅ Complete Admin dashboard
- ✅ Complete Teacher module
- ✅ Complete Student module
- ✅ Implement progress tracking
- ✅ Add performance analytics

### Phase 4: Gamification (Week 7)

- ✅ Implement XP system
- ✅ Create badge/achievement system
- ✅ Build streak tracking
- ✅ Add leaderboards

### Phase 5: Polish & Testing (Week 8)

- ✅ UI/UX refinements
- ✅ Performance optimization
- ✅ Comprehensive testing
- ✅ Bug fixes

---

## 🚀 Next Steps

1. **Firebase Setup**
    - Create Firebase project
    - Download `google-services.json`
    - Enable required services

2. **Initial Implementation**
    - Start with authentication
    - Build role-based navigation
    - Create basic dashboards

3. **Feature Implementation**
    - Follow phase-by-phase approach
    - Test each module independently
    - Integrate AI features progressively

4. **Testing & Deployment**
    - Comprehensive testing
    - Beta testing with real users
    - Production deployment

---

This architecture provides a solid foundation for building the complete E-Education Platform. Each
module is designed to be independent yet integrated, allowing for parallel development and easy
maintenance.
