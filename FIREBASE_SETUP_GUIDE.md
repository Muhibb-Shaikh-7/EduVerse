# Firebase Setup & Testing Guide for EduVerse

## Overview

This guide walks you through setting up Firebase services for EduVerse and testing them for each
user role (Admin, Teacher, Student).

## Table of Contents

1. [Firebase Console Setup](#firebase-console-setup)
2. [Firebase Authentication](#firebase-authentication)
3. [Firestore Database](#firestore-database)
4. [Firebase Storage](#firebase-storage)
5. [ML Kit OCR](#ml-kit-ocr)
6. [Testing Instructions](#testing-instructions)
7. [Troubleshooting](#troubleshooting)

---

## Firebase Console Setup

### Step 1: Create/Access Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Your project `eduverse-7a899` is already created
3. The `google-services.json` file is already in your project at `app/google-services.json`

### Step 2: Verify Project Configuration

```json
Project ID: eduverse-7a899
Project Number: 971918386111
Storage Bucket: eduverse-7a899.firebasestorage.app
Package Name: com.example.eduverse
```

---

## Firebase Authentication

### Console Setup

1. Go to Firebase Console → **Authentication**
2. Click **Get Started**
3. Enable **Email/Password** sign-in method:
    - Click on **Email/Password**
    - Toggle **Enable**
    - Toggle **Email link (passwordless sign-in)** if desired
    - Click **Save**

### Security Rules

Authentication is handled automatically. Users are stored in Firestore with their roles.

### Implementation

The app uses `FirebaseAuthService` for all authentication operations:

```kotlin
// Registration
firebaseAuthService.register(
    email = "student@example.com",
    password = "password123",
    displayName = "John Doe",
    role = UserRole.STUDENT
)

// Login
firebaseAuthService.login(
    email = "student@example.com",
    password = "password123"
)

// Get Current User
val currentUser = firebaseAuthService.getCurrentUser()

// Logout
firebaseAuthService.logout()
```

---

## Firestore Database

### Console Setup

1. Go to Firebase Console → **Firestore Database**
2. Click **Create Database**
3. Select **Start in test mode** (for development)
4. Choose your preferred location (e.g., `us-central`)
5. Click **Enable**

### Database Structure

```
firestore/
├── users/
│   └── {userId}/
│       ├── uid: string
│       ├── email: string
│       ├── displayName: string
│       ├── role: string ("ADMIN" | "TEACHER" | "STUDENT")
│       ├── createdAt: timestamp
│       └── updatedAt: timestamp
│
├── materials/
│   └── {materialId}/
│       ├── title: string
│       ├── description: string
│       ├── fileUrl: string
│       ├── fileType: string
│       ├── uploadedBy: string (userId)
│       ├── uploadedAt: timestamp
│       └── subject: string
│
├── quizzes/
│   └── {quizId}/
│       ├── title: string
│       ├── description: string
│       ├── questions: array
│       ├── createdBy: string (userId)
│       ├── createdAt: timestamp
│       └── subject: string
│
├── flashcards/
│   └── {flashcardId}/
│       ├── front: string
│       ├── back: string
│       ├── subject: string
│       ├── createdBy: string (userId)
│       └── createdAt: timestamp
│
├── student_progress/
│   └── {studentId}/
│       ├── xp: number
│       ├── level: number
│       ├── streak: number
│       ├── badges: array
│       ├── completedQuizzes: number
│       └── quizResults: array
│
└── quiz_results/
    └── {resultId}/
        ├── quizId: string
        ├── quizTitle: string
        ├── score: number
        ├── totalQuestions: number
        ├── xpEarned: number
        └── completedAt: timestamp
```

### Security Rules (Production)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is authenticated
    function isSignedIn() {
      return request.auth != null;
    }
    
    // Helper function to get user role
    function getUserRole() {
      return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role;
    }
    
    // Users collection
    match /users/{userId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn();
      allow update: if isSignedIn() && request.auth.uid == userId;
      allow delete: if getUserRole() == 'ADMIN';
    }
    
    // Materials collection
    match /materials/{materialId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn() && (getUserRole() == 'TEACHER' || getUserRole() == 'ADMIN');
      allow update: if isSignedIn() && (getUserRole() == 'TEACHER' || getUserRole() == 'ADMIN');
      allow delete: if isSignedIn() && (getUserRole() == 'TEACHER' || getUserRole() == 'ADMIN');
    }
    
    // Quizzes collection
    match /quizzes/{quizId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn() && (getUserRole() == 'TEACHER' || getUserRole() == 'ADMIN');
      allow update: if isSignedIn() && (getUserRole() == 'TEACHER' || getUserRole() == 'ADMIN');
      allow delete: if isSignedIn() && (getUserRole() == 'TEACHER' || getUserRole() == 'ADMIN');
    }
    
    // Flashcards collection
    match /flashcards/{flashcardId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn() && (getUserRole() == 'TEACHER' || getUserRole() == 'ADMIN');
      allow update: if isSignedIn() && (getUserRole() == 'TEACHER' || getUserRole() == 'ADMIN');
      allow delete: if isSignedIn() && (getUserRole() == 'TEACHER' || getUserRole() == 'ADMIN');
    }
    
    // Student progress collection
    match /student_progress/{studentId} {
      allow read: if isSignedIn() && (request.auth.uid == studentId || getUserRole() == 'ADMIN' || getUserRole() == 'TEACHER');
      allow write: if isSignedIn();
    }
    
    // Quiz results collection
    match /quiz_results/{resultId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn();
      allow update: if false;
      allow delete: if getUserRole() == 'ADMIN';
    }
  }
}
```

**To apply these rules:**

1. Go to Firestore Database → **Rules**
2. Paste the rules above
3. Click **Publish**

### Implementation

```kotlin
// Save material
firestoreService.saveMaterial(
    Material(
        title = "Introduction to Calculus",
        description = "Basic calculus concepts",
        fileUrl = "https://...",
        fileType = "PDF",
        uploadedBy = currentUser.uid,
        uploadedAt = System.currentTimeMillis(),
        subject = "Mathematics"
    )
)

// Get all materials
val materials = firestoreService.getAllMaterials()

// Save quiz
firestoreService.saveQuiz(quiz)

// Update student progress
firestoreService.updateStudentProgress(studentId, progress)
```

---

## Firebase Storage

### Console Setup

1. Go to Firebase Console → **Storage**
2. Click **Get Started**
3. Start in **Test mode** (for development)
4. Click **Done**

### Storage Structure

```
storage/
├── materials/
│   └── {userId}/
│       ├── {materialType}/
│       │   └── material_*.pdf
│       └── material_*.jpg
│
├── profiles/
│   └── {userId}/
│       └── profile.jpg
│
├── flashcards/
│   └── {userId}/
│       └── flashcard_*.jpg
│
└── quizzes/
    └── {userId}/
        └── quiz_*.jpg
```

### Security Rules (Production)

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    // Helper function to check authentication
    function isSignedIn() {
      return request.auth != null;
    }
    
    // Helper function to check file size (10MB limit)
    function isValidSize() {
      return request.resource.size < 10 * 1024 * 1024;
    }
    
    // Helper function to check file type
    function isImage() {
      return request.resource.contentType.matches('image/.*');
    }
    
    function isPDF() {
      return request.resource.contentType == 'application/pdf';
    }
    
    // Materials
    match /materials/{userId}/{allPaths=**} {
      allow read: if isSignedIn();
      allow write: if isSignedIn() && request.auth.uid == userId && isValidSize();
    }
    
    // Profiles
    match /profiles/{userId}/{allPaths=**} {
      allow read: if isSignedIn();
      allow write: if isSignedIn() && request.auth.uid == userId && isImage() && isValidSize();
    }
    
    // Flashcards
    match /flashcards/{userId}/{allPaths=**} {
      allow read: if isSignedIn();
      allow write: if isSignedIn() && request.auth.uid == userId && isValidSize();
    }
    
    // Quizzes
    match /quizzes/{userId}/{allPaths=**} {
      allow read: if isSignedIn();
      allow write: if isSignedIn() && request.auth.uid == userId && isValidSize();
    }
  }
}
```

**To apply these rules:**

1. Go to Storage → **Rules**
2. Paste the rules above
3. Click **Publish**

### Implementation

```kotlin
// Upload PDF
val result = firebaseStorageService.uploadPDF(
    uri = fileUri,
    userId = currentUser.uid,
    fileName = "calculus_notes.pdf"
)

// Upload image
val result = firebaseStorageService.uploadImage(
    uri = imageUri,
    userId = currentUser.uid,
    path = "materials"
)

// Get download URL
val url = firebaseStorageService.getDownloadUrl(storagePath)

// Delete file
firebaseStorageService.deleteFile(storagePath)
```

---

## ML Kit OCR

### Setup

ML Kit is already configured in the app. No console setup required!

### Implementation

```kotlin
// Extract text from image
val result = ocrService.extractTextFromBitmap(bitmap)

result.onSuccess { ocrResult ->
    println("Text: ${ocrResult.fullText}")
    println("Confidence: ${ocrResult.confidence}")
    ocrResult.blocks.forEach { block ->
        println("Block: ${block.text}")
    }
}

// Extract text from URI
val text = ocrService.extractTextFromUri(imageUri)
```

---

## Testing Instructions

### Prerequisites

1. Sync Gradle to download Firebase dependencies
2. Run the app on an emulator or physical device
3. Ensure internet connection is available

### Test 1: Admin User Registration & Login

#### Register Admin

1. Launch the app
2. Click **"Create Account"**
3. Fill in:
    - Email: `admin@eduverse.com`
    - Password: `Admin@123`
    - Display Name: `Admin User`
    - Role: Select **ADMIN**
4. Click **"Register"**
5. ✅ **Expected**: Navigate to Admin Dashboard

#### Verify in Firebase Console

1. Go to Firebase Console → **Authentication**
2. ✅ **Expected**: See `admin@eduverse.com` in user list
3. Go to **Firestore Database** → `users` collection
4. ✅ **Expected**: See document with role: "ADMIN"

#### Test Admin Features

1. On Admin Dashboard, verify you can see:
    - User management section
    - System analytics
    - All materials from teachers
2. ✅ **Expected**: All admin features accessible

### Test 2: Teacher User & Material Upload

#### Register Teacher

1. Logout (click logout icon)
2. Click **"Create Account"**
3. Fill in:
    - Email: `teacher@eduverse.com`
    - Password: `Teacher@123`
    - Display Name: `Teacher User`
    - Role: Select **TEACHER**
4. Click **"Register"**
5. ✅ **Expected**: Navigate to Teacher Dashboard

#### Upload Material

1. Click **"Upload Material"**
2. Select a PDF or image file
3. Fill in:
    - Title: `Calculus Introduction`
    - Description: `Basic calculus concepts`
    - Subject: `Mathematics`
4. Click **"Upload"**
5. ✅ **Expected**: Success message shown

#### Verify in Firebase Console

1. Go to **Firestore Database** → `materials` collection
2. ✅ **Expected**: See new document with material data
3. Go to **Storage** → `materials/{teacherId}/`
4. ✅ **Expected**: See uploaded file

### Test 3: OCR Text Recognition

#### Test OCR Feature

1. Stay logged in as Teacher
2. Click **"OCR Processing"**
3. Select an image with text
4. Click **"Extract Text"**
5. ✅ **Expected**: Extracted text displayed on screen

#### Verify Text Quality

1. Use an image with clear, printed text
2. ✅ **Expected**: High accuracy (>80% confidence)
3. Use an image with handwritten text
4. ✅ **Expected**: Lower accuracy but still functional

### Test 4: Student User & Quiz Taking

#### Register Student

1. Logout
2. Click **"Create Account"**
3. Fill in:
    - Email: `student@eduverse.com`
    - Password: `Student@123`
    - Display Name: `Student User`
    - Role: Select **STUDENT**
4. Click **"Register"**
5. ✅ **Expected**: Navigate to Student Dashboard

#### View Materials

1. Click **"Flashcards"** or **"Quizzes"**
2. ✅ **Expected**: See materials uploaded by teacher

#### Take Quiz

1. Click **"Quizzes"**
2. Select a quiz
3. Answer all questions
4. Submit quiz
5. ✅ **Expected**: Score displayed, XP added

#### Verify Progress in Firestore

1. Go to **Firestore Database** → `student_progress` collection
2. ✅ **Expected**: See document with:
    - Updated XP
    - Updated level
    - Quiz results array
3. Go to `quiz_results` collection
4. ✅ **Expected**: See quiz result document

### Test 5: Real-time Data Sync

#### Test Real-time Updates

1. Open app on two devices (or emulator + browser)
2. Login as Teacher on Device 1
3. Login as Student on Device 2
4. Upload material on Device 1
5. ✅ **Expected**: Material appears on Device 2 without refresh

#### Test Progress Updates

1. Student completes quiz
2. Check Admin dashboard
3. ✅ **Expected**: Student progress updated in real-time

### Test 6: Storage Operations

#### Test File Download

1. Login as Student
2. View a material
3. Click download button
4. ✅ **Expected**: File downloads successfully

#### Test File Deletion (Admin/Teacher)

1. Login as Teacher
2. Go to uploaded materials
3. Delete a material
4. ✅ **Expected**:
    - Material removed from Firestore
    - File removed from Storage

---

## Troubleshooting

### Issue: "Default FirebaseApp is not initialized"

**Solution:**

1. Ensure `google-services.json` is in `app/` directory
2. Sync Gradle
3. Clean and rebuild project

### Issue: "Permission Denied" errors

**Solution:**

1. Check Firestore/Storage security rules
2. Ensure user is authenticated
3. Verify user role matches required permissions

### Issue: OCR not working

**Solution:**

1. Check image quality (clear, well-lit)
2. Ensure image is not too large (< 10MB)
3. Check internet connection (ML Kit downloads models)

### Issue: File upload fails

**Solution:**

1. Check file size (< 10MB limit)
2. Verify Storage rules allow writes
3. Check internet connection
4. Verify file URI is valid

### Issue: Build errors after adding Firebase

**Solution:**

```bash
# Clean project
./gradlew clean

# Sync Gradle
./gradlew --refresh-dependencies

# Rebuild
./gradlew assembleDebug
```

---

## Performance Optimization

### Firestore

- **Offline Persistence**: Enabled by default in `FirebaseModule`
- **Query Indexing**: Add composite indexes for complex queries
- **Data Pagination**: Implement pagination for large lists

### Storage

- **Image Compression**: Compress images before upload
- **Caching**: Use Coil library for efficient image caching
- **Background Uploads**: Use WorkManager for large files

### ML Kit

- **Model Caching**: ML Kit automatically caches models
- **Image Preprocessing**: Resize/compress images before OCR
- **Batch Processing**: Process multiple images in parallel

---

## Next Steps

1. ✅ Enable Firebase services in console
2. ✅ Apply production security rules
3. ✅ Test all user roles
4. ✅ Verify real-time sync
5. ✅ Test file uploads/downloads
6. ✅ Test OCR functionality
7. 🔄 Monitor Firebase usage and costs
8. 🔄 Set up Firebase Analytics
9. 🔄 Configure Firebase Crashlytics
10. 🔄 Enable Firebase Performance Monitoring

---

## Support & Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firestore Best Practices](https://firebase.google.com/docs/firestore/best-practices)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition/android)
- [Firebase Storage Best Practices](https://firebase.google.com/docs/storage/best-practices)

---

## Conclusion

Your EduVerse app is now fully integrated with Firebase! All services are configured and ready for
testing across Admin, Teacher, and Student roles.

**Status:**

- ✅ Firebase Authentication
- ✅ Firestore Database
- ✅ Firebase Storage
- ✅ ML Kit OCR

Happy coding! 🚀
