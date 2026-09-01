package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity Vector Art Hero Illustration for AirBeam:
 * Renders an optical transceiver broadcasting photonic data packets across an air-gap
 * with geometric vector line-art, radial wavefronts, and holographic nodes.
 */
@Composable
fun AirBeamHeroVectorArt(
    modifier: Modifier = Modifier,
    isStreaming: Boolean = false,
    size: Dp = 140.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vectorArtTransition")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isStreaming) 1200 else 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    val primaryColor = AppTheme.colors.primary
    val cyanColor = AppTheme.colors.neonCyan
    val emeraldColor = AppTheme.colors.emeraldGreen
    val borderColor = AppTheme.colors.border
    val isDark = AppTheme.colors.isDark

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2.2f

            // 1. Vector Geometric Grid / Target Crosshairs
            val crosshairColor = borderColor.copy(alpha = if (isDark) 0.4f else 0.6f)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

            drawLine(
                color = crosshairColor,
                start = Offset(center.x - radius * 1.1f, center.y),
                end = Offset(center.x + radius * 1.1f, center.y),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )
            drawLine(
                color = crosshairColor,
                start = Offset(center.x, center.y - radius * 1.1f),
                end = Offset(center.x, center.y + radius * 1.1f),
                strokeWidth = 1.5f,
                pathEffect = dashEffect
            )

            // 2. Vector Concentric Optical Rings
            drawCircle(
                color = primaryColor.copy(alpha = if (isDark) 0.08f else 0.05f),
                radius = radius * pulse,
                center = center,
                style = Fill
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.35f),
                radius = radius * pulse,
                center = center,
                style = Stroke(width = 1.5f, pathEffect = dashEffect)
            )

            // Dynamic expanding wavefront
            val waveRadius = radius * (0.3f + wavePhase * 0.8f)
            val waveAlpha = (1f - wavePhase).coerceIn(0f, 1f) * 0.8f
            drawCircle(
                color = cyanColor.copy(alpha = waveAlpha),
                radius = waveRadius,
                center = center,
                style = Stroke(width = 2f)
            )

            // 3. Rotating Vector Optical Aperture / Star Polygon
            rotate(rotation, center) {
                val hexPath = Path()
                val hexRadius = radius * 0.72f
                for (i in 0 until 6) {
                    val angle = Math.toRadians((i * 60).toDouble())
                    val x = (center.x + hexRadius * cos(angle)).toFloat()
                    val y = (center.y + hexRadius * sin(angle)).toFloat()
                    if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
                }
                hexPath.close()

                drawPath(
                    path = hexPath,
                    color = primaryColor.copy(alpha = 0.4f),
                    style = Stroke(width = 2f, join = StrokeJoin.Round)
                )

                // Vector vertex points
                for (i in 0 until 6) {
                    val angle = Math.toRadians((i * 60).toDouble())
                    val x = (center.x + hexRadius * cos(angle)).toFloat()
                    val y = (center.y + hexRadius * sin(angle)).toFloat()
                    drawCircle(
                        color = cyanColor,
                        radius = 4f,
                        center = Offset(x, y)
                    )
                }
            }

            // 4. Central Optical Emitter Lens
            val coreGradient = Brush.radialGradient(
                colors = listOf(
                    if (isDark) Color.White else primaryColor,
                    cyanColor,
                    primaryColor.copy(alpha = 0.8f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * 0.4f
            )

            drawCircle(
                brush = coreGradient,
                radius = radius * 0.35f,
                center = center
            )

            // Central Core Vector Hexagon
            val innerHex = Path()
            val innerRadius = radius * 0.28f
            for (i in 0 until 6) {
                val angle = Math.toRadians((i * 60 + 30).toDouble())
                val x = (center.x + innerRadius * cos(angle)).toFloat()
                val y = (center.y + innerRadius * sin(angle)).toFloat()
                if (i == 0) innerHex.moveTo(x, y) else innerHex.lineTo(x, y)
            }
            innerHex.close()

            drawPath(
                path = innerHex,
                color = if (isDark) Color(0xFF0F172A) else Color.White,
                style = Fill
            )
            drawPath(
                path = innerHex,
                color = cyanColor,
                style = Stroke(width = 2.5f)
            )

            // Optical Beam Photon Icon / QR Vector Glyphs
            drawCircle(
                color = if (isStreaming) emeraldColor else primaryColor,
                radius = radius * 0.10f,
                center = center
            )

            // 4 Optical Ray Nodes (North, East, South, West)
            val nodeDist = radius * 0.95f
            val nodeOffsets = listOf(
                Offset(center.x, center.y - nodeDist),
                Offset(center.x + nodeDist, center.y),
                Offset(center.x, center.y + nodeDist),
                Offset(center.x - nodeDist, center.y)
            )

            nodeOffsets.forEachIndexed { index, nodeOffset ->
                drawCircle(
                    color = if (index % 2 == 0) cyanColor else emeraldColor,
                    radius = 3.5f,
                    center = nodeOffset
                )
            }
        }
    }
}

/**
 * Geometric Vector Art Badge for Zero-Network Security Shield
 */
@Composable
fun VectorShieldBadge(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    val primaryColor = AppTheme.colors.primary
    val cyanColor = AppTheme.colors.neonCyan
    val isDark = AppTheme.colors.isDark

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val shieldPath = Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.88f, h * 0.24f)
            lineTo(w * 0.88f, h * 0.56f)
            cubicTo(w * 0.88f, h * 0.82f, w * 0.5f, h * 0.95f, w * 0.5f, h * 0.95f)
            cubicTo(w * 0.5f, h * 0.95f, w * 0.12f, h * 0.82f, w * 0.12f, h * 0.56f)
            lineTo(w * 0.12f, h * 0.24f)
            close()
        }

        drawPath(
            path = shieldPath,
            color = primaryColor.copy(alpha = if (isDark) 0.15f else 0.12f),
            style = Fill
        )
        drawPath(
            path = shieldPath,
            color = primaryColor,
            style = Stroke(width = 2.5f, join = StrokeJoin.Round)
        )

        // Vector Checkmark inside shield
        val checkPath = Path().apply {
            moveTo(w * 0.32f, h * 0.52f)
            lineTo(w * 0.46f, h * 0.66f)
            lineTo(w * 0.70f, h * 0.38f)
        }
        drawPath(
            path = checkPath,
            color = cyanColor,
            style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Vector Art for Empty States (e.g., No Files / Clean Slate)
 */
@Composable
fun VectorEmptyVaultArt(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    val primaryColor = AppTheme.colors.primary
    val cyanColor = AppTheme.colors.neonCyan
    val borderColor = AppTheme.colors.border

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Isometric Document / Vault Vector
        val boxPath = Path().apply {
            moveTo(w * 0.2f, h * 0.35f)
            lineTo(w * 0.5f, h * 0.2f)
            lineTo(w * 0.8f, h * 0.35f)
            lineTo(w * 0.8f, h * 0.7f)
            lineTo(w * 0.5f, h * 0.85f)
            lineTo(w * 0.2f, h * 0.7f)
            close()
        }

        drawPath(
            path = boxPath,
            color = primaryColor.copy(alpha = 0.08f),
            style = Fill
        )
        drawPath(
            path = boxPath,
            color = primaryColor,
            style = Stroke(width = 2f, join = StrokeJoin.Round)
        )

        // Center spine
        drawLine(
            color = primaryColor.copy(alpha = 0.6f),
            start = Offset(w * 0.5f, h * 0.2f),
            end = Offset(w * 0.5f, h * 0.85f),
            strokeWidth = 1.5f
        )
        drawLine(
            color = primaryColor.copy(alpha = 0.6f),
            start = Offset(w * 0.5f, h * 0.52f),
            end = Offset(w * 0.2f, h * 0.35f),
            strokeWidth = 1.5f
        )
        drawLine(
            color = primaryColor.copy(alpha = 0.6f),
            start = Offset(w * 0.5f, h * 0.52f),
            end = Offset(w * 0.8f, h * 0.35f),
            strokeWidth = 1.5f
        )

        // Floating Optical Beam Nodes
        drawCircle(
            color = cyanColor,
            radius = 4f,
            center = Offset(w * 0.5f, h * 0.12f)
        )
        drawCircle(
            color = cyanColor.copy(alpha = 0.4f),
            radius = 8f,
            center = Offset(w * 0.5f, h * 0.12f),
            style = Stroke(width = 1.5f)
        )
    }
}
