package com.levonty.unitalk.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Импортируем цвета из Color.kt (предполагается, что файл существует)
import com.levonty.unitalk.ui.theme.UniBlue
import com.levonty.unitalk.ui.theme.UniTeal
import com.levonty.unitalk.ui.theme.LightBackground
import com.levonty.unitalk.ui.theme.LightSurface
import com.levonty.unitalk.ui.theme.LightOnSurface
import com.levonty.unitalk.ui.theme.DarkBackground
import com.levonty.unitalk.ui.theme.DarkSurface
import com.levonty.unitalk.ui.theme.DarkOnSurface
import com.levonty.unitalk.ui.theme.Error

private val LightColorScheme = lightColorScheme(
    primary = UniBlue,
    onPrimary = Color.White,
    secondary = UniTeal,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    error = Error,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = UniBlue,
    onPrimary = Color.White,
    secondary = UniTeal,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    error = Error,
    onError = Color.White
)

@Composable
fun UniTalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}