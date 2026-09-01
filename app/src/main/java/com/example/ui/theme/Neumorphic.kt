package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom Compose Modifier that renders soft top-left specular highlight and bottom-right shadow
 * tailored for both dark obsidian surfaces and light minimal surfaces.
 */
@Composable
fun Modifier.neumorphicShadow(
    shape: Shape = RoundedCornerShape(20.dp),
    lightShadowColor: Color = AppTheme.colors.neumorphicLightShadow,
    darkShadowColor: Color = AppTheme.colors.neumorphicDarkShadow,
    blurRadius: Dp = 8.dp,
    offset: Dp = 4.dp
): Modifier = this.drawBehind {
    val shadowRadiusPx = blurRadius.toPx()
    val offsetPx = offset.toPx()
    val outline = shape.createOutline(size, layoutDirection, this)

    drawIntoCanvas { canvas ->
        // Bottom-right soft dark shadow
        val darkPaint = Paint().apply {
            color = darkShadowColor
            asFrameworkPaint().apply {
                isAntiAlias = true
                setShadowLayer(shadowRadiusPx, offsetPx, offsetPx, darkShadowColor.toArgb())
            }
        }
        canvas.drawOutline(outline, darkPaint)

        // Top-left soft light specular highlight
        val lightPaint = Paint().apply {
            color = lightShadowColor
            asFrameworkPaint().apply {
                isAntiAlias = true
                setShadowLayer(shadowRadiusPx, -offsetPx, -offsetPx, lightShadowColor.toArgb())
            }
        }
        canvas.drawOutline(outline, lightPaint)
    }
}
