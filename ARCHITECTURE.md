# E-Education Platform Architecture

## 📐 System Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│                    (Jetpack Compose UI)                      │
├─────────────────────────────────────────────────────────────┤
│  LoginScreen  │ RegisterScreen │ Admin │ Teacher │ Student  │
│               │                │ Dash  │  Dash   │  Dash    │
└───────────────┴────────────────┴───────┴─────────┴──────────┘
                        ↕
┌─────────────────────────────────────────────────────────────┐
│                      Navigation Layer                         │
│                  (Navigation Compose)                         │
├─────────────────────────────────────────────────────────────┤
│  NavGraph.kt - Manages all screen transitions & routes      │
└─────────────────────────────────────────────────────────────┘
                        ↕
┌─────────────────────────────────────────────────────────────┐
│                      ViewModel Layer                          │
│                     (Business Logic)                          │
├─────────────────────────────────────────────────────────────┤
│  AuthViewModel - Manages authentication state & operations  │
│                  Uses StateFlow for reactive UI              │
└─────────────────────────────────────────────────────────────┘
                        ↕
┌─────────────────────────────────────────────────────────────┐
│                      Repository Layer                         │
│                    (Data Abstraction)                         │
├─────────────────────────────────────────────────────────────┤
│  AuthRepository - Abstracts Firebase Auth & Firestore ops   │
│                   Provides suspend functions                 │
└─────────────────────────────────────────────────────────────┘
                        ↕
┌─────────────────────────────────────────────────────────────┐
│                      Data Sources                             │
│                  (Firebase Services)                          │
├─────────────────────────────────────────────────────────────┤
│  Firebase Auth  │  Firestore  │  Storage  │  ML Kit         │
└─────────────────────────────────────────────────────────────┘
```

## 🏛️ MVVM Architecture Pattern

### Model

**Location:** `data/model/`

- **User.kt**: Data class representing user entity
- **UserRole.kt**: Enum defining ADMIN, TEACHER, STUDENT roles

### View

**Location:** `ui/`

- Jetpack Compose UI components
- Declarative UI with Material Design 3
- Reactive to StateFlow changes
- Screen modules: auth, admin, teacher, student

### ViewModel

**Location:** `ui/auth/AuthViewModel.kt`

- Manages UI state with StateFlow
- Handles business logic
- Communicates with repositories
- Lifecycle-aware

## 🔄 Data Flow

```
User Action (Compose UI)
    ↓
ViewModel receives event
    ↓
ViewModel calls Repository
    ↓
Repository interacts with Firebase
    ↓
Firebase returns data/result
    ↓
Repository processes & returns
    ↓
ViewModel updates StateFlow
    ↓
Compose UI recomposes automatically
```

## 💉 Dependency Injection (Hilt)

```
@HiltAndroidApp
EduVerseApplication
    ↓
@InstallIn(SingletonComponent::class)
AppModule provides:
    - FirebaseAuth
    - FirebaseFirestore
    - FirebaseStorage
    ↓
@Singleton
AuthRepository
    ↓
@HiltViewModel
AuthViewModel
    ↓
@Composable Screen
hiltViewModel()
```

## 🗺️ Navigation Architecture

```
Screen Sealed Class:
├── Login
├── Register
├── AdminDashboard
├── TeacherDashboard
└── StudentDashboard

NavHost:
├── startDestination: Based on auth state
├── Login Route → LoginScreen
├── Register Route → RegisterScreen
├── Admin Route → AdminDashboardScreen
├── Teacher Route → TeacherDashboardScreen
└── Student Route → StudentDashboardScreen
```

## 🔐 Authentication Flow

```
App Launch
    ↓
Check auth.currentUser
    ├── null → Login Screen
    └── exists → getUserFromFirestore()
            ↓
        Get user.role
            ├── ADMIN → AdminDashboard
            ├── TEACHER → TeacherDashboard
            └── STUDENT → StudentDashboard

Login/Register
    ↓
Firebase Auth
    ↓
Create/Read Firestore user document
    ↓
Update AuthState with user
    ↓
Navigate to role-based dashboard
```

## 📦 Module Structure

### Data Module

```
data/
├── model/
│   └── User.kt               # Data classes
└── repository/
    └── AuthRepository.kt     # Data operations
```

### Domain Module (Implicit)

- Business logic resides in ViewModels
- Use cases can be added as needed

### Presentation Module

```
ui/
├── navigation/
│   └── NavGraph.kt           # Navigation setup
├── auth/
│   ├── AuthViewModel.kt      # State management
│   ├── LoginScreen.kt        # UI
│   └── RegisterScreen.kt     # UI
├── admin/
│   └── AdminDashboardScreen.kt
├── teacher/
│   └── TeacherDashboardScreen.kt
└── student/
    └── StudentDashboardScreen.kt
```

## 🔧 Dependency Injection Graph

```
AppModule
    ├── provideFirebaseAuth()
    │       ↓
    │   FirebaseAuth.getInstance()
    │
    ├── provideFirebaseFirestore()
    │       ↓
    │   FirebaseFirestore.getInstance()
    │
    └── provideFirebaseStorage()
            ↓
        FirebaseStorage.getInstance()

AuthRepository (Constructor Injection)
    ├── @Inject FirebaseAuth
    └── @Inject FirebaseFirestore

AuthViewModel (Constructor Injection)
    └── @Inject AuthRepository
```

## 🔄 State Management

```kotlin
// ViewModel
private val _authState = MutableStateFlow(AuthState())
val authState: StateFlow<AuthState> = _authState.asStateFlow()

// Composable
val authState by viewModel.authState.collectAsState()

// State updates trigger recomposition automatically
```

## 🎯 Key Design Patterns

### 1. Repository Pattern

- Abstracts data sources
- Single source of truth
- Testable and mockable

### 2. Observer Pattern

- StateFlow/Flow for reactive updates
- UI observes ViewModel state
- Automatic recomposition

### 3. Dependency Injection

- Loose coupling
- Testability
- Single responsibility

### 4. Navigation Pattern

- Type-safe routes
- Centralized navigation logic
- Deep linking support

## 🚀 Scalability Considerations

### Adding New Features

1. Create data models in `data/model/`
2. Add repository functions in `data/repository/`
3. Create ViewModel in feature package
4. Build Composable UI screens
5. Add navigation routes in `NavGraph.kt`

### Adding New User Roles

1. Add role to `UserRole` enum
2. Create dashboard screen
3. Add navigation route
4. Update authentication flow

### Performance Optimizations

- LazyColumn for lists
- remember for expensive calculations
- derivedStateOf for computed values
- Flow operators for data transformation

## 🧪 Testing Strategy

### Unit Tests

- ViewModel logic
- Repository operations
- Data transformations

### Integration Tests

- Repository + Firebase
- ViewModel + Repository

### UI Tests

- Screen navigation
- User interactions
- State changes

## 📊 Data Models

```kotlin
data class User(
    val uid: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val profileImageUrl: String,
    val createdAt: Long
)

enum class UserRole {
    ADMIN,
    TEACHER,
    STUDENT
}

data class AuthState(
    val isLoading: Boolean,
    val user: User?,
    val error: String?
)
```

## 🔗 External Dependencies

### Firebase

- Auth: User authentication
- Firestore: NoSQL database
- Storage: File storage
- ML Kit: Text recognition

### Jetpack

- Compose: UI framework
- Navigation: Screen navigation
- Lifecycle: Lifecycle management
- Hilt: Dependency injection

### Coroutines

- Async operations
- Flow for reactive streams
- Structured concurrency

---

**This architecture provides:**

- ✅ Separation of concerns
- ✅ Testability
- ✅ Scalability
- ✅ Maintainability
- ✅ Type safety
- ✅ Reactive UI
