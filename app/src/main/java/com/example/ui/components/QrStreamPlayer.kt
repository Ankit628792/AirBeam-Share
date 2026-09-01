package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveLavender
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.neumorphicShadow
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.delay

@Composable
fun QrStreamPlayer(
    frames: List<String>,
    modifier: Modifier = Modifier,
    initialFps: Float = 4f,
    onFrameDisplayed: (Int, Int) -> Unit = { _, _ -> }
) {
    if (frames.isEmpty()) return

    var currentFrameIndex by remember(frames) { mutableIntStateOf(0) }
    var isPlaying by remember(frames) { mutableStateOf(false) }
    var targetFps by remember { mutableFloatStateOf(initialFps) }

    // Cache QR code bitmaps for smooth high-speed animation
    val qrBitmapCache = remember(frames) {
        mutableMapOf<Int, Bitmap>()
    }

    // Auto stream animation loop
    LaunchedEffect(isPlaying, targetFps, frames) {
        while (isPlaying && frames.isNotEmpty()) {
            val delayMs = (1000f / targetFps).toLong()
            delay(delayMs)
            currentFrameIndex = (currentFrameIndex + 1) % frames.size
            onFrameDisplayed(currentFrameIndex, frames.size)
        }
    }

    // Get current frame bitmap safely
    val currentFrameString = frames.getOrNull(currentFrameIndex) ?: ""
    val currentBitmap = remember(currentFrameIndex, currentFrameString) {
        qrBitmapCache.getOrPut(currentFrameIndex) {
            QrCodeGenerator.generateQrBitmap(currentFrameString, width = 600, height = 600)
                ?: Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .neumorphicShadow(shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Frame Header Counter Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonCyan.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Chunk ${currentFrameIndex + 1} / ${frames.size}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) EmeraldGreen else NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "BEAMING" else "READY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isPlaying) EmeraldGreen else NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pure White High-Contrast QR Frame Container
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(4.dp, if (isPlaying) EmeraldGreen else NeonCyan, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = currentBitmap.asImageBitmap(),
                    contentDescription = "Optical Beam Frame ${currentFrameIndex + 1}",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timeline Progress Indicator
            LinearProgressIndicator(
                progress = { (currentFrameIndex + 1).toFloat() / frames.size },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isPlaying) EmeraldGreen else ImmersiveLavender,
                trackColor = DarkSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Primary Start/Pause Beaming Action Button
            Button(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) DarkSurfaceVariant else ImmersiveLavender,
                    contentColor = if (isPlaying) TextPrimary else Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPlaying) "Pause Beaming" else "Start Beaming",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Step & Reset Controls
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isPlaying = false
                        currentFrameIndex = if (currentFrameIndex > 0) currentFrameIndex - 1 else frames.size - 1
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous Frame",
                        tint = TextPrimary
                    )
                }

                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.iconButtonColors(containerColor = ImmersiveLavender)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = {
                        isPlaying = false
                        currentFrameIndex = (currentFrameIndex + 1) % frames.size
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next Frame",
                        tint = TextPrimary
                    )
                }

                IconButton(
                    onClick = {
                        currentFrameIndex = 0
                        isPlaying = true
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Stream",
                        tint = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FPS Rate Slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "FPS",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${targetFps.toInt()} FPS",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    modifier = Modifier.width(50.dp)
                )
                Slider(
                    value = targetFps,
                    onValueChange = { targetFps = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = Color(0xFF334155)
                    )
                )
            }
        }
    }
}
