# EduVerse - Firestore Integration & Admin Console Guide

## Overview

EduVerse has been upgraded with a comprehensive Firebase Firestore backend and a powerful admin
console. This document outlines the architecture, features, setup instructions, and usage
guidelines.

## Table of Contents

1. [Architecture](#architecture)
2. [Features](#features)
3. [Firebase Setup](#firebase-setup)
4. [Firestore Data Model](#firestore-data-model)
5. [Security Rules](#security-rules)
6. [Admin Features](#admin-features)
7. [Analytics Integration](#analytics-integration)
8. [Testing](#testing)
9. [CI/CD](#cicd)

## Architecture

### Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: Clean Architecture (Data → Domain → Presentation)
- **Dependency Injection**: Hilt
- **Async**: Coroutines + Flow
- **Navigation**: AndroidX Navigation Compose
- **Backend**: Firebase Firestore
- **Storage**: Firebase Storage (primary) + Supabase (optional)
- **Analytics**: Firebase Analytics
- **Remote Config**: Firebase Remote Config
- **Crashlytics**: Firebase Crashlytics

### Layer Structure

```
app/
├── data/
│   ├── model/          # Data models (Course, Lesson, User, etc.)
│   ├── repository/     # Repository implementations
│   ├── firebase/       # Firebase services (Firestore, Storage, Analytics)
│   └── supabase/       # Optional Supabase services
├── domain/             # Use cases (planned for future implementation)
├── ui/
│   ├── admin/          # Admin dashboard and management screens
│   ├── auth/           # Authentication screens
│   ├── student/        # Student-facing screens
│   ├── teacher/        # Instructor screens
│   └── components/     # Reusable UI components
└── di/                 # Dependency injection modules
```

## Features

### 1. Admin Dashboard

The admin dashboard provides a comprehensive view of platform metrics:

- **User Statistics**: Total users, breakdown by role (admin/instructor/student)
- **Course Metrics**: Total courses, published vs. drafts, enrollments
- **Content Moderation**: Open reports, resolved reports
- **Real-time Updates**: Live data from Firestore

### 2. Course Management

Admins and instructors can:

- **Create Courses**: Rich course creation with metadata
    - Title, description, thumbnail
    - Tags, category, difficulty level
    - Pricing (free or paid)
    - Language selection
- **Edit Courses**: Update all course properties
- **Publish/Unpublish**: Control course visibility
- **Delete Courses**: Remove courses and all associated lessons
- **View Analytics**: Track enrollments, ratings, and engagement

### 3. Lesson Management

Comprehensive lesson CRUD with multiple content types:

- **Content Types**:
    - Video lessons (with Firebase Storage upload)
    - Article lessons (markdown support)
    - Quiz lessons (interactive assessments)
    - Document lessons (PDF uploads)
    - Interactive lessons
- **Reordering**: Drag-and-drop lesson reordering with batch updates
- **Progress Tracking**: Monitor student completion
- **Locking**: Lock lessons behind prerequisites

### 4. User Management

Role-based access control with admin oversight:

- **View All Users**: Searchable, filterable user list
- **Role Management**: Promote/demote users (student ↔ instructor ↔ admin)
- **User Search**: Find users by name or email
- **Activity Tracking**: Last login, enrollment status
- **Custom Claims**: Firebase Auth custom claims for role enforcement

### 5. Announcements

System-wide and course-specific communication:

- **Global Announcements**: Reach all users
- **Course Announcements**: Target specific course enrollees
- **Priority Levels**: Low, Normal, High, Urgent
- **Scheduling**: Schedule announcements for future publication
- **Rich Content**: Text, images, formatting
- **Target Roles**: Announce to specific user roles

### 6. Content Moderation

Report management system:

- **Report Types**: Spam, inappropriate content, harassment, copyright, etc.
- **Entity Types**: Courses, lessons, comments, users
- **Status Tracking**: Open → In Review → Resolved/Dismissed
- **Resolution Workflow**: Admins can document actions taken
- **Analytics**: Track moderation metrics

### 7. Analytics & Insights

Firebase Analytics integration for:

- **User Events**: Login, sign-up, logout
- **Course Events**: View, enroll, create, update, delete
- **Lesson Events**: Start, complete
- **Admin Actions**: All administrative operations
- **Search Tracking**: User search behavior
- **Role Changes**: User promotion/demotion events

## Firebase Setup

### Prerequisites

1. Create a Firebase project at https://console.firebase.google.com/
2. Enable the following services:
    - Authentication (Email/Password, Google Sign-In)
    - Firestore Database
    - Firebase Storage
    - Firebase Analytics
    - Firebase Crashlytics
    - Firebase Remote Config

### Setup Steps

#### 1. Download Configuration

1. In Firebase Console, go to Project Settings
2. Under "Your apps", select Android app (or create one)
3. Download `google-services.json`
4. Place it in `app/google-services.json`

#### 2. Configure SHA Certificates

For Google Sign-In to work:

```bash
# Debug SHA-1
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# Release SHA-1 (if applicable)
keytool -list -v -keystore /path/to/your/keystore -alias your-alias -storepass your-password
```

Add SHA-1 and SHA-256 to Firebase Console → Project Settings → Your apps → SHA certificate
fingerprints

#### 3. Enable Firestore

1. Go to Firestore Database in Firebase Console
2. Click "Create database"
3. Start in **production mode** (we'll add security rules)
4. Choose a location closest to your users

#### 4. Create Storage Buckets

In Firebase Storage, the following structure is recommended:

```
/courses/{courseId}/thumbnail/
/lessons/{courseId}/{lessonId}/video/
/lessons/{courseId}/{lessonId}/documents/
/profiles/{userId}/
/announcements/{announcementId}/
/materials/{userId}/{materialType}/
```

#### 5. Deploy Security Rules

See [Security Rules](#security-rules) section below.

## Firestore Data Model

### Collections

#### 1. `users` Collection

```typescript
{
  uid: string,                    // User ID (matches Firebase Auth)
  email: string,
  displayName: string,
  role: "ADMIN" | "TEACHER" | "STUDENT",
  avatarUrl: string,
  bio: string,
  createdAt: timestamp,
  updatedAt: timestamp,
  lastLoginAt: timestamp,
  isActive: boolean,
  enrolledCourses: string[],      // Course IDs
  createdCourses: string[]        // Course IDs (for instructors)
}
```

**Indexes Required**:

- `role ASC, createdAt DESC`
- `displayName ASC`

#### 2. `courses` Collection

```typescript
{
  courseId: string,
  title: string,
  description: string,
  thumbnailUrl: string,
  tags: string[],
  categoryId: string,
  categoryName: string,
  published: boolean,
  createdBy: string,              // User ID
  createdByName: string,
  createdAt: timestamp,
  updatedAt: timestamp,
  price: number,
  difficulty: "BEGINNER" | "INTERMEDIATE" | "ADVANCED" | "EXPERT",
  duration: number,               // Total duration in minutes
  enrollmentCount: number,
  ratingCount: number,
  avgRating: number,
  lessonCount: number,
  language: string,
  featured: boolean
}
```

**Indexes Required**:

- `published ASC, createdAt DESC`
- `categoryId ASC, published ASC, createdAt DESC`
- `published ASC, featured ASC, avgRating DESC`
- `createdBy ASC, createdAt DESC`

#### 3. `courses/{courseId}/lessons` Subcollection

```typescript
{
  lessonId: string,
  courseId: string,
  title: string,
  description: string,
  order: number,
  contentType: "VIDEO" | "ARTICLE" | "QUIZ" | "DOCUMENT" | "INTERACTIVE",
  durationSec: number,
  storagePath: string,            // Firebase Storage path
  videoUrl: string,
  body: string,                   // Markdown content
  quizSpec: {
    questions: QuizQuestion[],
    passingScore: number,
    allowRetry: boolean,
    showAnswers: boolean
  },
  thumbnailUrl: string,
  completed: boolean,
  locked: boolean,
  createdAt: timestamp,
  updatedAt: timestamp
}
```

**Index Required**:

- `order ASC`

#### 4. `enrollments` Collection

```typescript
{
  enrollmentId: string,
  userId: string,
  userName: string,
  courseId: string,
  courseTitle: string,
  progressPct: number,
  completedLessons: string[],     // Lesson IDs
  lastActivityAt: timestamp,
  createdAt: timestamp,
  completedAt: timestamp,
  status: "ACTIVE" | "COMPLETED" | "DROPPED" | "SUSPENDED",
  certificateUrl: string
}
```

**Indexes Required**:

- `userId ASC, createdAt DESC`
- `courseId ASC, createdAt DESC`

#### 5. `announcements` Collection

```typescript
{
  id: string,
  scope: "GLOBAL" | "COURSE",
  courseId: string,               // Empty for global
  courseTitle: string,
  title: string,
  body: string,
  priority: "LOW" | "NORMAL" | "HIGH" | "URGENT",
  scheduledAt: timestamp,
  publishedAt: timestamp,
  createdBy: string,
  createdByName: string,
  createdAt: timestamp,
  updatedAt: timestamp,
  published: boolean,
  imageUrl: string,
  targetRoles: string[]           // ["STUDENT", "TEACHER", "ADMIN"]
}
```

**Indexes Required**:

- `published ASC, publishedAt DESC`
- `createdAt DESC`

#### 6. `reports` Collection

```typescript
{
  id: string,
  type: "SPAM" | "INAPPROPRIATE_CONTENT" | "HARASSMENT" | "COPYRIGHT" | "MISINFORMATION" | "TECHNICAL_ISSUE" | "OTHER",
  entityType: "COURSE" | "LESSON" | "COMMENT" | "USER" | "ANNOUNCEMENT",
  entityId: string,
  entityTitle: string,
  reason: string,
  description: string,
  reportedBy: string,
  reportedByName: string,
  createdAt: timestamp,
  status: "OPEN" | "IN_REVIEW" | "RESOLVED" | "DISMISSED",
  resolvedBy: string,
  resolvedByName: string,
  resolvedAt: timestamp,
  resolution: string,
  actionTaken: string
}
```

**Indexes Required**:

- `status ASC, createdAt DESC`
- `createdAt DESC`

#### 7. `categories` Collection

```typescript
{
  id: string,
  name: string,
  description: string,
  iconUrl: string,
  emoji: string,
  order: number,
  courseCount: number,
  color: string,                  // Hex color
  isActive: boolean,
  createdAt: timestamp,
  updatedAt: timestamp
}
```

**Index Required**:

- `order ASC`

#### 8. `ratings` Collection or Subcollection

```typescript
{
  id: string,
  userId: string,
  userName: string,
  userAvatarUrl: string,
  courseId: string,
  courseTitle: string,
  value: number,                  // 1-5 stars
  comment: string,
  createdAt: timestamp,
  updatedAt: timestamp,
  verified: boolean,              // Verified enrollment
  helpful: number,
  reported: boolean
}
```

**Indexes Required**:

- `courseId ASC, createdAt DESC`
- `userId ASC, createdAt DESC`

### Creating Composite Indexes

Either:

1. **Via Firestore Console**: Navigate to Firestore → Indexes → Add Index
2. **Via Firebase CLI**:

Create `firestore.indexes.json`:

```json
{
  "indexes": [
    {
      "collectionGroup": "courses",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "published", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "courses",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "categoryId", "order": "ASCENDING" },
        { "fieldPath": "published", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "lessons",
      "queryScope": "COLLECTION_GROUP",
      "fields": [
        { "fieldPath": "courseId", "order": "ASCENDING" },
        { "fieldPath": "order", "order": "ASCENDING" }
      ]
    },
    {
      "collectionGroup": "enrollments",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "userId", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    }
  ]
}
```

Deploy:

```bash
firebase deploy --only firestore:indexes
```

## Security Rules

Create `firestore.rules`:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isAdmin() {
      return isAuthenticated() && 
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'ADMIN';
    }
    
    function isTeacher() {
      return isAuthenticated() && 
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'TEACHER';
    }
    
    function isStudent() {
      return isAuthenticated() && 
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'STUDENT';
    }
    
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // Users collection
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && request.auth.uid == userId;
      allow update: if isOwner(userId) || isAdmin();
      allow delete: if isAdmin();
    }
    
    // Courses collection
    match /courses/{courseId} {
      allow read: if resource.data.published == true || 
                     isAdmin() || 
                     (isAuthenticated() && resource.data.createdBy == request.auth.uid);
      allow create: if (isAdmin() || isTeacher()) && 
                       request.resource.data.createdBy == request.auth.uid;
      allow update: if isAdmin() || 
                       (isAuthenticated() && resource.data.createdBy == request.auth.uid);
      allow delete: if isAdmin() || 
                       (isAuthenticated() && resource.data.createdBy == request.auth.uid);
      
      // Lessons subcollection
      match /lessons/{lessonId} {
        allow read: if get(/databases/$(database)/documents/courses/$(courseId)).data.published == true || 
                       isAdmin() || 
                       (isAuthenticated() && get(/databases/$(database)/documents/courses/$(courseId)).data.createdBy == request.auth.uid);
        allow write: if isAdmin() || 
                        (isAuthenticated() && get(/databases/$(database)/documents/courses/$(courseId)).data.createdBy == request.auth.uid);
      }
    }
    
    // Enrollments collection
    match /enrollments/{enrollmentId} {
      allow read: if isAuthenticated() && 
                     (resource.data.userId == request.auth.uid || isAdmin());
      allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
      allow update: if isAuthenticated() && resource.data.userId == request.auth.uid;
      allow delete: if isAdmin();
    }
    
    // Announcements collection
    match /announcements/{announcementId} {
      allow read: if resource.data.published == true || isAdmin();
      allow write: if isAdmin();
    }
    
    // Reports collection
    match /reports/{reportId} {
      allow read: if isAdmin() || (isAuthenticated() && resource.data.reportedBy == request.auth.uid);
      allow create: if isAuthenticated();
      allow update: if isAdmin();
      allow delete: if isAdmin();
    }
    
    // Categories collection
    match /categories/{categoryId} {
      allow read: if true;  // Public read
      allow write: if isAdmin();
    }
    
    // Ratings collection
    match /ratings/{ratingId} {
      allow read: if true;  // Public read
      allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
      allow update: if isAuthenticated() && resource.data.userId == request.auth.uid;
      allow delete: if isAdmin() || (isAuthenticated() && resource.data.userId == request.auth.uid);
    }
  }
}
```

Deploy rules:

```bash
firebase deploy --only firestore:rules
```

## Admin Features

### Accessing Admin Dashboard

1. Sign in with an admin account
2. The app automatically routes admin users to the Admin Dashboard
3. Dashboard shows:
    - Platform statistics
    - Quick actions
    - Recent activity

### Course Management Flow

#### Creating a Course:

1. Navigate to Admin Dashboard → "Courses" tab
2. Click "Create New Course"
3. Fill in course details:
    - Title (required, max 100 chars)
    - Description (required)
    - Thumbnail image (upload)
    - Tags (comma-separated)
    - Category (select from dropdown)
    - Difficulty level
    - Price (0 for free)
    - Language
4. Click "Create Course" (saves as draft)
5. Add lessons to the course
6. Publish when ready

#### Managing Lessons:

1. Open course details
2. Navigate to "Lessons" tab
3. Click "Add Lesson"
4. Select content type:
    - **Video**: Upload video file, add description
    - **Article**: Write markdown content
    - **Quiz**: Add questions with multiple choice options
    - **Document**: Upload PDF or other documents
5. Set lesson order (drag to reorder)
6. Save lesson

#### Publishing a Course:

1. Ensure course has at least one lesson
2. Click "Publish Course" toggle
3. Confirm publication
4. Course becomes visible to students

### User Management Flow

1. Navigate to Admin Dashboard → "Users" tab
2. View all users with filters:
    - Role filter (All / Admin / Teacher / Student)
    - Search by name or email
3. Select a user
4. Click "Change Role"
5. Select new role from dropdown
6. Confirm change
7. Analytics event logged
8. User immediately has new permissions

### Announcement Flow

1. Navigate to Admin Dashboard → "Announcements" tab
2. Click "Create Announcement"
3. Fill in details:
    - Title
    - Body (supports rich text)
    - Scope (Global / Course-specific)
    - Priority
    - Target roles
    - Schedule (optional)
4. Preview announcement
5. Publish or schedule

### Moderation Flow

1. Navigate to Admin Dashboard → "Reports" tab
2. View open reports
3. Click on a report to see details
4. Review reported content
5. Take action:
    - Mark as "In Review"
    - Resolve with notes
    - Dismiss if invalid
6. Document action taken
7. Update report status

## Analytics Integration

### Tracked Events

All Firebase Analytics events are automatically tracked:

- **User Events**: `login`, `logout`, `sign_up`
- **Course Events**: `course_view`, `course_enroll`, `course_create`
- **Lesson Events**: `lesson_start`, `lesson_complete`
- **Admin Events**: `admin_action`, `user_role_change`
- **Search Events**: `search`

### Viewing Analytics

1. Go to Firebase Console → Analytics
2. View dashboards for:
    - User engagement
    - Event analytics
    - User properties
    - Custom events

### Custom Reporting

Use BigQuery integration for advanced analytics:

1. Enable BigQuery in Firebase Console
2. Analytics data automatically exported daily
3. Query with SQL for custom insights

## Testing

### Unit Tests

Run unit tests:

```bash
./gradlew test
```

Tests cover:

- Repository operations
- ViewModel state management
- Data model transformations

### Instrumented Tests

Run instrumented tests:

```bash
./gradlew connectedAndroidTest
```

Tests cover:

- Admin UI flows
- Course CRUD operations
- User role changes
- Firestore integration

### Firebase Emulator Suite

For local testing without affecting production:

1. Install Firebase CLI:

```bash
npm install -g firebase-tools
```

2. Initialize emulators:

```bash
firebase init emulators
```

3. Start emulators:

```bash
firebase emulators:start
```

4. Point app to emulators in `FirebaseModule.kt`:

```kotlin
if (BuildConfig.DEBUG) {
    firestore.useEmulator("10.0.2.2", 8080)
    storage.useEmulator("10.0.2.2", 9199)
    auth.useEmulator("10.0.2.2", 9099)
}
```

## CI/CD

### GitHub Actions Workflow

Create `.github/workflows/android.yml`:

```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew build
    
    - name: Run unit tests
      run: ./gradlew test
    
    - name: Run lint
      run: ./gradlew lint
    
    - name: Upload build artifacts
      uses: actions/upload-artifact@v3
      with:
        name: build-artifacts
        path: app/build/outputs/
    
    - name: Upload test reports
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-reports
        path: app/build/reports/
```

### Deployment

For production deployment:

1. Update version in `app/build.gradle.kts`
2. Build release APK:

```bash
./gradlew assembleRelease
```

3. Sign APK with your keystore
4. Upload to Google Play Console

## Remote Config

### Feature Flags

Firebase Remote Config enables/disables features without app updates:

```kotlin
// In FirebaseModule.kt
remoteConfig.setDefaultsAsync(
    mapOf(
        "admin_mode_enabled" to true,
        "new_firestore_backend" to true,
        "enable_offline_mode" to true,
        "max_upload_size_mb" to 100,
        "feature_announcements" to true,
        "feature_reports" to true
    )
)

// Fetch and activate
remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
    if (task.isSuccessful) {
        // Use config values
        val adminEnabled = remoteConfig.getBoolean("admin_mode_enabled")
    }
}
```

## Troubleshooting

### Common Issues

#### 1. Firestore Permission Denied

**Error**: `PERMISSION_DENIED: Missing or insufficient permissions`

**Solution**:

- Check security rules are deployed
- Verify user has correct role in `users` collection
- Check Firebase Auth token is valid

#### 2. Gradle Sync Issues

**Error**: `Unresolved reference: FirebaseStorage`

**Solution**:

```bash
./gradlew clean
./gradlew --refresh-dependencies
```

#### 3. Analytics Not Appearing

**Solution**:

- Wait 24 hours for initial data
- Check Analytics is enabled in Firebase Console
- Verify app is in debug mode for real-time reporting

## Support

For issues or questions:

1. Check Firebase documentation: https://firebase.google.com/docs
2. Review Firestore best practices: https://firebase.google.com/docs/firestore/best-practices
3. Submit issues to project repository

## License

Copyright © 2024 EduVerse. All rights reserved.
