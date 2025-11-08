package com.example.eduverse.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Shape system for EduVerse
 *
 * Defines the shape hierarchy used throughout the app:
 * - Extra Small: Small chips, badges
 * - Small: Buttons, small cards
 * - Medium: Standard cards, dialogs
 * - Large: Large cards, bottom sheets
 * - Extra Large: Hero cards, featured content
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)
