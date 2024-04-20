package com.example.firebaseheartrate_kotlin.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.MaterialTheme

@Composable
fun FirebaseHeartrate_KotlinTheme(
    content: @Composable () -> Unit
) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primaryVariant = Color.Green,
        ),
        content = content
    )
}