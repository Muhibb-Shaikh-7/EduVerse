# Firebase Firestore Integration Guide for EduVerse

## Overview

This guide explains how to integrate Firebase Firestore into the EduVerse Student Dashboard to
persist student progress, quizzes, and flashcards.

## Current State

The app currently uses **in-memory mock data** with repositories that simulate database operations.
All data is lost when the app restarts.

## Firestore Setup Steps

### 1. Add Firebase to Your Project

#### a. Add Firebase SDK dependencies to `app/build.gradle.kts`:

```kotlin
plugins {
    // ... existing plugins
    id("com.google.gms.google-services")  // Uncomment this line
}

dependencies {
    // ... existing dependencies
    
    // Firebase BOM (manages versions)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    
    // Coroutines support for Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
```

#### b. Add google-services.json

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing one
3. Add Android app with package name: `com.example.eduverse`
4. Download `google-services.json`
5. Place it in `app/` directory

#### c. Add classpath to root `build.gradle.kts`:

```kotlin
plugins {
    // ... existing plugins
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

### 2. Firestore Database Structure

```
firestore/
├── users/
│   └── {userId}/
│       ├── uid: String
│       ├── email: String
│       ├── displayName: String
│       └── role: String
│
├── student_progress/
│   └── {userId}/
│       ├── userId: String
│       ├── xp: Number
│       ├── level: Number
│       ├── streak: Number
│       ├── lastActivityDate: Timestamp
│       ├── completedQuizzes: Number
│       ├── totalQuizScore: Number
│       ├── badges: Array<Badge>
│       ├── quizResults: Array<QuizResult>
│       └── studiedFlashcardSets: Array<String>
│
├── flashcard_sets/
│   └── {flashcardSetId}/
│       ├── id: String
│       ├── title: String
│       ├── description: String
│       ├── teacherId: String
│       ├── teacherName: String
│       ├── subject: String
│       ├── cards: Array<Flashcard>
│       ├── createdAt: Timestamp
│       └── isPublic: Boolean
│
└── quizzes/
    └── {quizId}/
        ├── id: String
        ├── title: String
        ├── description: String
        ├── teacherId: String
        ├── teacherName: String
        ├── subject: String
        ├── questions: Array<QuizQuestion>
        ├── xpReward: Number
        ├── timeLimit: Number
        ├── createdAt: Timestamp
        └── isPublic: Boolean
```

### 3. Update Repositories

#### StudentProgressRepository.kt

Replace the mock implementation with Firestore:

```kotlin
@Singleton
class StudentProgressRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val progressCollection = firestore.collection("student_progress")
    
    suspend fun getStudentProgress(userId: String): Result<StudentProgress> {
        return try {
            val document = progressCollection
                .document(userId)
                .get()
                .await()
            
            val progress = document.toObject<StudentProgress>()
                ?: StudentProgress(userId = userId)
            
            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeStudentProgress(userId: String): Flow<StudentProgress?> {
        return callbackFlow {
            val listener = progressCollection
                .document(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val progress = snapshot?.toObject<StudentProgress>()
                        ?: StudentProgress(userId = userId)
                    trySend(progress)
                }
            
            awaitClose { listener.remove() }
        }
    }
    
    suspend fun completeQuiz(
        userId: String,
        quiz: Quiz,
        score: Int,
        totalQuestions: Int,
        answers: List<QuizAnswer>
    ): Result<StudentProgress> {
        return try {
            val currentProgress = getStudentProgress(userId).getOrThrow()
            
            // Calculate XP and update progress (same logic as before)
            val correctAnswers = answers.count { it.isCorrect }
            val xpEarned = XP_PER_QUIZ + (correctAnswers * XP_PER_CORRECT_ANSWER)
            
            // ... rest of the logic
            
            // Save to Firestore
            progressCollection
                .document(userId)
                .set(updatedProgress)
                .await()
            
            Result.success(updatedProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Similar updates for studyFlashcardSet and other methods
}
```

#### FlashcardRepository.kt

```kotlin
@Singleton
class FlashcardRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val flashcardsCollection = firestore.collection("flashcard_sets")
    
    suspend fun getAllFlashcardSets(): Result<List<FlashcardSet>> {
        return try {
            val snapshot = flashcardsCollection
                .whereEqualTo("isPublic", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val sets = snapshot.documents.mapNotNull { 
                it.toObject<FlashcardSet>() 
            }
            
            Result.success(sets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeFlashcardSets(): Flow<List<FlashcardSet>> {
        return callbackFlow {
            val listener = flashcardsCollection
                .whereEqualTo("isPublic", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val sets = snapshot?.documents?.mapNotNull { 
                        it.toObject<FlashcardSet>() 
                    } ?: emptyList()
                    
                    trySend(sets)
                }
            
            awaitClose { listener.remove() }
        }
    }
    
    suspend fun createFlashcardSet(flashcardSet: FlashcardSet): Result<String> {
        return try {
            val docRef = flashcardsCollection.document()
            val setWithId = flashcardSet.copy(
                id = docRef.id,
                createdAt = System.currentTimeMillis()
            )
            
            docRef.set(setWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Similar implementations for update, delete, etc.
}
```

#### QuizRepository.kt

```kotlin
@Singleton
class QuizRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val quizzesCollection = firestore.collection("quizzes")
    
    suspend fun getAllQuizzes(): Result<List<Quiz>> {
        return try {
            val snapshot = quizzesCollection
                .whereEqualTo("isPublic", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val quizList = snapshot.documents.mapNotNull { 
                it.toObject<Quiz>() 
            }
            
            Result.success(quizList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun observeQuizzes(): Flow<List<Quiz>> {
        return callbackFlow {
            val listener = quizzesCollection
                .whereEqualTo("isPublic", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val quizzes = snapshot?.documents?.mapNotNull { 
                        it.toObject<Quiz>() 
                    } ?: emptyList()
                    
                    trySend(quizzes)
                }
            
            awaitClose { listener.remove() }
        }
    }
    
    // Similar implementations for CRUD operations
}
```

### 4. Firestore Security Rules

Add these rules in Firebase Console > Firestore Database > Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users collection
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Student progress
    match /student_progress/{userId} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Flashcard sets
    match /flashcard_sets/{setId} {
      allow read: if resource.data.isPublic == true || request.auth != null;
      allow write: if request.auth != null && 
                     (request.auth.token.role == 'TEACHER' || 
                      request.auth.token.role == 'ADMIN');
    }
    
    // Quizzes
    match /quizzes/{quizId} {
      allow read: if resource.data.isPublic == true || request.auth != null;
      allow write: if request.auth != null && 
                     (request.auth.token.role == 'TEACHER' || 
                      request.auth.token.role == 'ADMIN');
    }
  }
}
```

### 5. Testing Strategy

1. **Mock Data First**: Keep mock data for testing without internet
2. **Feature Flag**: Add a flag to switch between mock and Firestore
3. **Offline Support**: Use Firestore's offline persistence

```kotlin
// Enable offline persistence
val settings = FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .build()
FirebaseFirestore.getInstance().firestoreSettings = settings
```

### 6. Migration Checklist

- [ ] Add Firebase dependencies
- [ ] Add google-services.json
- [ ] Update repository implementations
- [ ] Add Firestore security rules
- [ ] Test CRUD operations
- [ ] Enable offline persistence
- [ ] Test error handling
- [ ] Add loading states
- [ ] Test with real data

## Gamification Features Implemented

### ✅ XP System

- Base XP per quiz: 20 points
- XP per correct answer: 10 points
- Streak bonus: 5 XP per day

### ✅ Daily Streaks

- Tracks consecutive days of activity
- Resets if more than 2 days pass
- Displayed prominently on dashboard

### ✅ Badges

Automatically awarded for:

- **First Steps** 🏅: Complete first quiz
- **Quiz Novice** 🎯: Complete 10 quizzes
- **Quiz Expert** 🏆: Complete 50 quizzes
- **Rising Star** ⭐: Earn 100 XP
- **Super Star** 🌟: Earn 500 XP
- **Week Warrior** 🔥: 7-day streak
- **Dedication Master** 💪: 30-day streak

### ✅ Level System

- 100 XP per level
- Visual progress bar
- Level displayed on dashboard

### ✅ Quiz Results Tracking

- Stores all quiz attempts
- Shows score, XP earned, timestamp
- Visual percentage indicators

## Benefits of Firestore

1. **Real-time updates**: Changes sync instantly across devices
2. **Offline support**: Works without internet, syncs when online
3. **Scalability**: Automatically scales with user base
4. **Security**: Granular security rules at database level
5. **Easy queries**: Powerful query API with indexing

## Current Mock Implementation Benefits

- **Works offline**: No internet required for testing
- **Fast development**: No external dependencies
- **Easy debugging**: All data in memory
- **Cost-free**: No Firebase charges during development

## Next Steps

1. Set up Firebase project
2. Add google-services.json
3. Uncomment Firebase dependencies
4. Replace repository implementations one by one
5. Test thoroughly before production
