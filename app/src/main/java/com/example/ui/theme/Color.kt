package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// DARK THEME PALETTE (Default & Primary Vibe)
// ==========================================
val DarkObsidianBackground = Color(0xFF0A0E17)
val DarkObsidianSurface = Color(0xFF111726)
val DarkObsidianCard = Color(0xFF182032)
val DarkObsidianCardElevated = Color(0xFF1E293B)
val DarkObsidianVariant = Color(0xFF243048)

val DarkCyberIndigo = Color(0xFF818CF8)
val DarkIndigoContainer = Color(0xFF312E81)
val DarkOnIndigoContainer = Color(0xFFE0E7FF)
val DarkVectorBorder = Color(0xFF2A364F)

val DarkNeonCyan = Color(0xFF22D3EE)
val DarkNeonCyanContainer = Color(0xFF0E4C5A)
val DarkEmeraldGreen = Color(0xFF10B981)
val DarkEmeraldContainer = Color(0xFF064E3B)
val DarkAmber = Color(0xFFFBBF24)
val DarkRose = Color(0xFFF87171)

val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

val DarkNeumorphicLight = Color(0x18FFFFFF)
val DarkNeumorphicDark = Color(0x80000000)

// ==========================================
// LIGHT THEME PALETTE (Soft Minimal Clean)
// ==========================================
val LightSoftBackground = Color(0xFFF1F5F9)
val LightSoftSurface = Color(0xFFFFFFFF)
val LightSoftCard = Color(0xFFFFFFFF)
val LightSoftCardElevated = Color(0xFFF8FAFC)
val LightSoftVariant = Color(0xFFE2E8F0)

val LightCyberIndigo = Color(0xFF4F46E5)
val LightIndigoContainer = Color(0xFFEEF2FF)
val LightOnIndigoContainer = Color(0xFF3730A3)
val LightVectorBorder = Color(0xFFCBD5E1)

val LightNeonCyan = Color(0xFF0891B2)
val LightNeonCyanContainer = Color(0xFFCFFAFE)
val LightEmeraldGreen = Color(0xFF059669)
val LightEmeraldContainer = Color(0xFFD1FAE5)
val LightAmber = Color(0xFFD97706)
val LightRose = Color(0xFFDC2626)

val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF94A3B8)

val LightNeumorphicLight = Color(0xFFFFFFFF)
val LightNeumorphicDark = Color(0xFFCBD5E1)

// ==========================================
// THEME COLORS DATA CLASS
// ==========================================
data class AirBeamColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceCard: Color,
    val surfaceCardElevated: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val border: Color,
    val neonCyan: Color,
    val emeraldGreen: Color,
    val amberWarning: Color,
    val roseError: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val neumorphicLightShadow: Color,
    val neumorphicDarkShadow: Color,
    val isDark: Boolean
)

val DarkAirBeamColors = AirBeamColorScheme(
    background = DarkObsidianBackground,
    surface = DarkObsidianSurface,
    surfaceCard = DarkObsidianCard,
    surfaceCardElevated = DarkObsidianCardElevated,
    surfaceVariant = DarkObsidianVariant,
    primary = DarkCyberIndigo,
    primaryContainer = DarkIndigoContainer,
    onPrimaryContainer = DarkOnIndigoContainer,
    border = DarkVectorBorder,
    neonCyan = DarkNeonCyan,
    emeraldGreen = DarkEmeraldGreen,
    amberWarning = DarkAmber,
    roseError = DarkRose,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    neumorphicLightShadow = DarkNeumorphicLight,
    neumorphicDarkShadow = DarkNeumorphicDark,
    isDark = true
)

val LightAirBeamColors = AirBeamColorScheme(
    background = LightSoftBackground,
    surface = LightSoftSurface,
    surfaceCard = LightSoftCard,
    surfaceCardElevated = LightSoftCardElevated,
    surfaceVariant = LightSoftVariant,
    primary = LightCyberIndigo,
    primaryContainer = LightIndigoContainer,
    onPrimaryContainer = LightOnIndigoContainer,
    border = LightVectorBorder,
    neonCyan = LightNeonCyan,
    emeraldGreen = LightEmeraldGreen,
    amberWarning = LightAmber,
    roseError = LightRose,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    neumorphicLightShadow = LightNeumorphicLight,
    neumorphicDarkShadow = LightNeumorphicDark,
    isDark = false
)

val LocalAirBeamColors = staticCompositionLocalOf { DarkAirBeamColors }

// Global access object for composables
object AppTheme {
    val colors: AirBeamColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAirBeamColors.current
}

// Backward-compatibility aliases (dynamically read from LocalAirBeamColors when possible or fallback)
val DarkBackground @Composable @ReadOnlyComposable get() = AppTheme.colors.background
val DarkSurface @Composable @ReadOnlyComposable get() = AppTheme.colors.surface
val DarkSurfaceVariant @Composable @ReadOnlyComposable get() = AppTheme.colors.surfaceVariant
val DarkSurfaceCard @Composable @ReadOnlyComposable get() = AppTheme.colors.surfaceCard

val NeumorphicLightShadow @Composable @ReadOnlyComposable get() = AppTheme.colors.neumorphicLightShadow
val NeumorphicDarkShadow @Composable @ReadOnlyComposable get() = AppTheme.colors.neumorphicDarkShadow

val ImmersiveLavender @Composable @ReadOnlyComposable get() = AppTheme.colors.primary
val ImmersivePurpleContainer @Composable @ReadOnlyComposable get() = AppTheme.colors.primaryContainer
val ImmersiveOnPurpleContainer @Composable @ReadOnlyComposable get() = AppTheme.colors.onPrimaryContainer
val ImmersiveBorder @Composable @ReadOnlyComposable get() = AppTheme.colors.border

val NeonCyan @Composable @ReadOnlyComposable get() = AppTheme.colors.neonCyan
val NeonCyanLight @Composable @ReadOnlyComposable get() = AppTheme.colors.neonCyan
val NeonPurple @Composable @ReadOnlyComposable get() = AppTheme.colors.primary
val EmeraldGreen @Composable @ReadOnlyComposable get() = AppTheme.colors.emeraldGreen
val AmberWarning @Composable @ReadOnlyComposable get() = AppTheme.colors.amberWarning
val RoseError @Composable @ReadOnlyComposable get() = AppTheme.colors.roseError

val TextPrimary @Composable @ReadOnlyComposable get() = AppTheme.colors.textPrimary
val TextSecondary @Composable @ReadOnlyComposable get() = AppTheme.colors.textSecondary
val TextMuted @Composable @ReadOnlyComposable get() = AppTheme.colors.textMuted

val FrameReceivedColor @Composable @ReadOnlyComposable get() = AppTheme.colors.emeraldGreen
val FrameMissingColor @Composable @ReadOnlyComposable get() = AppTheme.colors.border
val FrameCurrentColor @Composable @ReadOnlyComposable get() = AppTheme.colors.primary
