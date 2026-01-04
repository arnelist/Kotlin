package lt.arnastamasiunas.coachbooking.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LemonYellow,
    onPrimary = GymBlack,

    secondary = LemonYellowDark,
    onSecondary = GymBlack,

    background = GymBlack,
    onBackground = TextWhite,

    surface = GymDarkGray,
    onSurface = TextWhite,

    surfaceVariant = GymGray,
    onSurfaceVariant = TextGray
)

private val LightColorScheme = lightColorScheme(
    primary = LemonYellowDark,
    onPrimary = GymBlack,

    secondary = LemonYellow,
    onSecondary = GymBlack,

    background = Color.White,
    onBackground = GymBlack,

    surface = Color(0xFFF5F5F5),
    onSurface = GymBlack
)

@Composable
fun CoachBookingTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
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
        shapes = Shapes,
        content = content
    )
}