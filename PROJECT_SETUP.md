# E-Education Platform - Project Setup Guide

## 🎓 Overview

**E-Education Platform** is a modern Android application built with Jetpack Compose, featuring MVVM
architecture, Hilt dependency injection, and Firebase backend integration. The platform supports
three user roles: Admin, Teacher, and Student.

## 🏗️ Architecture

### MVVM Pattern

- **Model**: Data classes and repositories (`data/` package)
- **View**: Composable UI screens (`ui/` package)
- **ViewModel**: Business logic and state management

### Project Structure

```
app/src/main/java/com/example/eduverse/
├── data/
│   ├── model/
│   │   └── User.kt                    # User data class with UserRole enum
│   └── repository/
│       └── AuthRepository.kt          # Authentication repository
├── di/
│   └── AppModule.kt                   # Hilt dependency injection module
├── ui/
│   ├── navigation/
│   │   └── NavGraph.kt                # Navigation setup with routes
│   ├── auth/
│   │   ├── AuthViewModel.kt           # Authentication ViewModel
│   │   ├── LoginScreen.kt             # Login UI
│   │   └── RegisterScreen.kt          # Registration UI
│   ├── admin/
│   │   └── AdminDashboardScreen.kt    # Admin dashboard
│   ├── teacher/
│   │   └── TeacherDashboardScreen.kt  # Teacher dashboard
│   └── student/
│       └── StudentDashboardScreen.kt  # Student dashboard
├── EduVerseApplication.kt             # Application class with @HiltAndroidApp
└── MainActivity.kt                    # Entry point with Navigation setup
```

## 📦 Dependencies

### Core Libraries

- **Jetpack Compose BOM**: Modern declarative UI
- **Material3**: Material Design 3 components
- **Navigation Compose**: Type-safe navigation
- **Hilt**: Dependency injection
- **Coroutines**: Asynchronous programming

### Firebase Services

- **Firebase Auth**: User authentication
- **Firestore**: Cloud NoSQL database
- **Storage**: File storage
- **ML Kit**: Text recognition and OCR
- **Analytics**: Usage tracking

### Additional Libraries

- **Coil**: Image loading
- **DataStore**: Preferences storage
- **Kotlinx Serialization**: JSON handling

## 🚀 Setup Instructions

### 1. Firebase Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing one
3. Add an Android app with package name: `com.example.eduverse`
4. Download `google-services.json`
5. Replace `app/google-services.json` with your downloaded file

### 2. Enable Firebase Services

In Firebase Console:

- **Authentication** → Enable Email/Password provider
- **Firestore Database** → Create database in production mode
- **Storage** → Set up Cloud Storage bucket
- **ML Kit** → Text Recognition is automatically available

### 3. Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      allow read: if request.auth != null;
    }
    
    match /courses/{courseId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        (get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'ADMIN' ||
         get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'TEACHER');
    }
  }
}
```

### 4. Build & Run

```bash
# Sync Gradle
./gradlew build

# Run on device/emulator
./gradlew installDebug
```

## 🎯 Features by Role

### 👨‍💼 Admin Dashboard

- User Management (Teachers & Students)
- Course Management
- Platform Analytics & Reports
- System Settings

### 👩‍🏫 Teacher Dashboard

- Course Creation & Management
- AI-Powered Quiz Generator (ML Kit)
- Flashcard Creator
- Student Assignment Review
- Class Analytics

### 👨‍🎓 Student Dashboard

- Course Enrollment & Learning
- Quiz Taking
- Flashcard Study
- Assignment Submission
- Progress Tracking & Achievements

## 🔐 Authentication Flow

1. **App Launch** → Check if user is logged in
2. **Not Logged In** → Show Login Screen
3. **Login/Register** → Authenticate with Firebase
4. **Success** → Navigate to role-based dashboard
    - Admin → Admin Dashboard
    - Teacher → Teacher Dashboard
    - Student → Student Dashboard

## 🧪 Testing

### Create Test Users

Use the registration screen or Firebase Console to create test accounts:

```kotlin
// Admin Test User
Email: admin@test.com
Password: admin123
Role: ADMIN

// Teacher Test User
Email: teacher@test.com
Password: teacher123
Role: TEACHER

// Student Test User
Email: student@test.com
Password: student123
Role: STUDENT
```

## 📱 Navigation Routes

- `/login` - Login screen
- `/register` - Registration screen
- `/admin_dashboard` - Admin dashboard (requires ADMIN role)
- `/teacher_dashboard` - Teacher dashboard (requires TEACHER role)
- `/student_dashboard` - Student dashboard (requires STUDENT role)

## 🔧 Configuration

### Build Configuration (app/build.gradle.kts)

```kotlin
android {
    namespace = "com.example.eduverse"
    compileSdk = 36
    
    defaultConfig {
        applicationId = "com.example.eduverse"
        minSdk = 24
        targetSdk = 36
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}
```

### Required Plugins

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")      // Firebase
    id("com.google.dagger.hilt.android")      // Hilt
    id("com.google.devtools.ksp")             // Kotlin Symbol Processing
}
```

## 🎨 Theming

The app uses Material Design 3 theming defined in `ui/theme/`:

- Dynamic color support
- Light/Dark mode
- Custom color schemes

## 🔮 Future Enhancements

- [ ] Email verification
- [ ] Password reset flow
- [ ] Profile editing
- [ ] Course content upload (PDF, images)
- [ ] Real-time chat/messaging
- [ ] Video lessons integration
- [ ] Gamification system
- [ ] Push notifications
- [ ] Offline mode with local caching

## 📚 Resources

- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Hilt Documentation](https://dagger.dev/hilt/)
- [Material Design 3](https://m3.material.io/)

## 🐛 Troubleshooting

### Firebase Issues

- Ensure `google-services.json` is properly configured
- Check Firebase Authentication is enabled
- Verify package name matches in Firebase Console

### Build Issues

- Clean and rebuild: `./gradlew clean build`
- Invalidate caches in Android Studio
- Check internet connection for dependencies

### Navigation Issues

- Ensure all navigation routes are defined in NavGraph
- Check screen composables are properly annotated

## 📄 License

This project is for educational purposes.

---

**Built with ❤️ using Jetpack Compose & Firebase**
