# EduVerse Material 3 Theming Guide

## Overview

EduVerse features a complete Material 3 design system implementation with:

- ✨ Professional education-focused color palette
- 🌓 Full light/dark mode support
- 📱 Edge-to-edge UI with proper system bar handling
- 🎨 Custom typography optimized for educational content
- 🎭 Smooth animations and transitions
- 🏗️ Consistent component architecture

## Theme System

### Color Palette

#### Light Theme

```kotlin
Primary: #1976D2 (Deep Blue) - Trust, Knowledge
Secondary: #00897B (Teal) - Growth, Learning  
Tertiary: #D32F2F (Red) - Energy, Passion
Background: #FDFCFF (Nearly White)
Surface: #FDFCFF (Nearly White)
```

#### Dark Theme

```kotlin
Primary: #90CAF9 (Light Blue)
Secondary: #4DB6AC (Light Teal)
Tertiary: #EF5350 (Light Red)
Background: #1A1C1E (Dark Gray)
Surface: #1A1C1E (Dark Gray)
```

#### Semantic Colors

```kotlin
Success: Green (#4CAF50 light / #81C784 dark)
Warning: Orange (#FF9800 light / #FFB74D dark)
Info: Blue (#2196F3 light / #64B5F6 dark)
```

#### Gamification Colors

```kotlin
FlashcardColor: #2196F3
QuizColor: #4CAF50
ProgressColor: #FF9800
StreakColor: #FF6B35
BadgeGoldColor: #FFD700
XPColor: #9C27B0
```

### Typography

Material 3 typography scale optimized for educational content:

- **Display (Large/Medium/Small)**: Hero text, major headings
- **Headline (Large/Medium/Small)**: Page titles, section headers
- **Title (Large/Medium/Small)**: Card headers, list item titles
- **Body (Large/Medium/Small)**: Main content text
- **Label (Large/Medium/Small)**: Buttons, chips, tabs

### Shapes

```kotlin
Extra Small: 4dp - Small chips, badges
Small: 8dp - Buttons, small cards
Medium: 12dp - Standard cards, dialogs
Large: 16dp - Large cards, bottom sheets
Extra Large: 24dp - Hero cards, featured content
```

## Components

### Navigation Components

#### EduVerseNavigationBar

Bottom navigation bar with animated selection and badge support.

```kotlin
@Composable
fun MyScreen() {
    val navigationItems = listOf(
        NavigationItem(
            label = "Dashboard",
            icon = Icons.Default.Dashboard,
            selectedIcon = Icons.Filled.Dashboard,
            route = "dashboard"
        ),
        // ... more items
    )

    EduVerseNavigationBar(
        items = navigationItems,
        selectedRoute = currentRoute,
        onItemClick = { route -> /* handle navigation */ }
    )
}
```

#### Top App Bars

Three variants available:

```kotlin
// Standard Top App Bar
EduVerseTopAppBar(
    title = "My Screen",
    navigationIcon = { /* back button */ },
    actions = { /* action buttons */ },
    scrollBehavior = scrollBehavior
)

// Large Top App Bar (for main screens)
EduVerseLargeTopAppBar(
    title = "Dashboard",
    actions = { /* action buttons */ },
    scrollBehavior = scrollBehavior
)

// Medium Top App Bar (for secondary screens)
EduVerseMediumTopAppBar(
    title = "Details",
    navigationIcon = { /* back button */ },
    scrollBehavior = scrollBehavior
)
```

### Animated Components

#### AnimatedXPBar

Displays user progress with smooth animations:

```kotlin
AnimatedXPBar(
    currentXP = progress.xp % 100,
    xpForNextLevel = 100,
    level = progress.level,
    modifier = Modifier.fillMaxWidth()
)
```

**Features:**

- Spring physics animation
- Pulsing effect when near completion (>90%)
- Gradient fill with shimmer effect
- Rotating XP icon with glow
- Real-time counter updates

#### AnimatedStreakCounter

Shows learning streak with fire animation:

```kotlin
AnimatedStreakCounter(
    streakDays = streakCount,
    modifier = Modifier.fillMaxWidth()
)
```

**Features:**

- Pulsing fire emoji
- Glowing container effect
- Dynamic motivational messages
- Smooth counter transitions
- Achievement milestone indicators

#### BadgeUnlockAnimation

Full-screen dialog for badge unlocks:

```kotlin
if (newBadges.isNotEmpty()) {
    BadgeUnlockAnimation(
        badges = newBadges,
        onDismiss = { viewModel.clearNewBadges() }
    )
}
```

**Features:**

- Spring-based scale animation
- Rotating badge icon
- Multiple badge support
- Automatic dismiss after viewing

### Screen Layout Pattern

Standard screen layout using Scaffold:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyScreen() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    
    Scaffold(
        topBar = {
            EduVerseLargeTopAppBar(
                title = "My Screen",
                actions = { /* actions */ },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            EduVerseNavigationBar(
                items = navigationItems,
                selectedRoute = currentRoute,
                onItemClick = { /* handle click */ }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Screen content
        }
    }
}
```

## Theming Best Practices

### 1. Use Theme Colors

Always use colors from the theme instead of hardcoded values:

```kotlin
// ✅ Good
color = MaterialTheme.colorScheme.primary

// ❌ Bad
color = Color(0xFF1976D2)
```

### 2. Use Theme Typography

Use typography styles from the theme:

```kotlin
// ✅ Good
Text(
    "Title",
    style = MaterialTheme.typography.headlineMedium,
    fontWeight = FontWeight.Bold
)

// ❌ Bad
Text(
    "Title",
    fontSize = 28.sp,
    fontWeight = FontWeight.Bold
)
```

### 3. Use Theme Shapes

Apply shapes from the theme system:

```kotlin
// ✅ Good
shape = MaterialTheme.shapes.medium

// ❌ Bad
shape = RoundedCornerShape(12.dp)
```

### 4. Proper Elevation

Use Material 3 elevation system:

```kotlin
ElevatedCard(
    elevation = CardDefaults.elevatedCardElevation(
        defaultElevation = 4.dp
    )
)
```

### 5. Edge-to-Edge Support

The app uses transparent system bars for edge-to-edge UI. Always use Scaffold padding:

```kotlin
Scaffold { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding) // ✅ Essential for edge-to-edge
    ) {
        // Content
    }
}
```

## Dark Mode Support

### Theme Detection

The app automatically follows system theme preference:

```kotlin
EduVerseTheme(
    darkTheme = isSystemInDarkTheme(),
    content = { /* app content */ }
)
```

### Manual Theme Toggle (Optional)

To implement manual theme toggling:

```kotlin
// In your ViewModel or state holder
var isDarkTheme by remember { mutableStateOf(false) }

// In your theme
EduVerseTheme(
    darkTheme = isDarkTheme,
    content = { /* app content */ }
)

// Toggle button
IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
    Icon(
        if (isDarkTheme) Icons.Default.LightMode 
        else Icons.Default.DarkMode,
        contentDescription = "Toggle theme"
    )
}
```

## Animation Guidelines

### Performance

All animations use:

- Hardware acceleration (graphicsLayer)
- Proper animation specs (spring, tween)
- LaunchedEffect for coroutine-based animations
- rememberInfiniteTransition for continuous animations

### Standard Durations

```kotlin
Fast: 150-200ms (micro-interactions)
Standard: 300-400ms (screen transitions)
Slow: 500-800ms (emphasis animations)
Continuous: 1000-2000ms (ambient animations)
```

### Animation Specs

```kotlin
// Spring (for natural motion)
spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

// Tween (for controlled motion)
tween(
    durationMillis = 300,
    easing = FastOutSlowInEasing
)

// Infinite (for continuous effects)
infiniteRepeatable(
    animation = tween(1000),
    repeatMode = RepeatMode.Reverse
)
```

## Icon System

### Material Icons Extended

The app uses Material Icons Extended library:

```kotlin
// Filled icons for selected states
Icons.Filled.Dashboard

// Outlined icons for unselected states  
Icons.Default.Dashboard

// Additional icon categories
Icons.Filled.School
Icons.Default.Quiz
Icons.Filled.TrendingUp
```

### Custom Icons

For custom icons, use vector drawables in `res/drawable/` and load with `painterResource()`.

## Accessibility

### Color Contrast

All color combinations meet WCAG AA standards:

- Minimum contrast ratio: 4.5:1 for text
- Minimum contrast ratio: 3:1 for UI components

### Touch Targets

Minimum touch target size: 48dp × 48dp (Material Design guideline)

### Content Descriptions

Always provide content descriptions for icons:

```kotlin
Icon(
    Icons.Default.Settings,
    contentDescription = "Settings" // ✅ Accessible
)
```

## Testing

### Preview Support

Use Compose previews to test both themes:

```kotlin
@Preview(name = "Light Mode")
@Preview(
    name = "Dark Mode",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun MyScreenPreview() {
    EduVerseTheme {
        MyScreen()
    }
}
```

## File Structure

```
ui/
├── theme/
│   ├── Color.kt          # Color definitions
│   ├── Type.kt           # Typography system
│   ├── Shape.kt          # Shape system
│   └── Theme.kt          # Main theme composable
├── components/
│   ├── NavigationComponents.kt  # Navigation UI
│   └── AnimatedComponents.kt    # Animated widgets
├── student/
│   └── StudentDashboardScreen.kt
├── teacher/
│   └── TeacherDashboardScreen.kt
└── auth/
    └── LoginScreen.kt
```

## Migration Checklist

When updating existing screens:

- [ ] Replace custom TopAppBar with EduVerseTopAppBar
- [ ] Add NavigationBar for bottom navigation
- [ ] Use Scaffold for screen layout
- [ ] Apply edge-to-edge padding properly
- [ ] Replace hardcoded colors with theme colors
- [ ] Use theme typography styles
- [ ] Add proper content descriptions
- [ ] Test both light and dark modes
- [ ] Verify animations are smooth
- [ ] Check accessibility contrast

## Resources

- [Material 3 Design](https://m3.material.io/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design Color Tool](https://material.io/resources/color/)
- [Compose Animation Documentation](https://developer.android.com/jetpack/compose/animation)

## Support

For questions or issues with the theming system, refer to:

- Project documentation in `/docs`
- Architecture guide in `ARCHITECTURE.md`
- Code comments in theme files
