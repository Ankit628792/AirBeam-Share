package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.FrameMissingColor
import com.example.ui.theme.FrameReceivedColor
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RealtimeProgressHUD(
    totalChunks: Int,
    receivedChunkIndices: Set<Int>,
    currentProcessingIndex: Int? = null,
    fpsSpeed: Float = 0f,
    mbpsSpeed: Float = 0f,
    isCompressed: Boolean = false,
    fileName: String = "",
    fileSizeFormatted: String = "",
    isCompleted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val receivedCount = receivedChunkIndices.size
    val progressFraction = if (totalChunks > 0) receivedCount.toFloat() / totalChunks else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = DarkSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) EmeraldGreen else NeonCyan.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (fileName.isNotBlank()) fileName else "AirBeam Transmission",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (fileSizeFormatted.isNotBlank()) {
                        Text(
                            text = fileSizeFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(isCompleted = isCompleted)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Percentage & Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${(progressFraction * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    ),
                    color = if (isCompleted) EmeraldGreen else NeonCyan
                )

                Text(
                    text = "$receivedCount / $totalChunks Chunks",
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (isCompleted) EmeraldGreen else NeonCyan,
                trackColor = FrameMissingColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Realtime Chunks Grid Visualizer
            Text(
                text = "Real-time Chunk Matrix:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF0F172A),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                maxItemsInEachRow = 16
            ) {
                val displayCount = kotlin.math.min(totalChunks, 64)
                for (i in 0 until displayCount) {
                    val isReceived = receivedChunkIndices.contains(i)
                    val isCurrent = currentProcessingIndex == i

                    val chunkColor by animateColorAsState(
                        targetValue = when {
                            isReceived -> FrameReceivedColor
                            isCurrent -> NeonCyanLight
                            else -> FrameMissingColor
                        },
                        animationSpec = tween(durationMillis = 150),
                        label = "chunk_color"
                    )

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(chunkColor)
                            .then(
                                if (isCurrent) Modifier.border(
                                    1.dp,
                                    Color.White,
                                    RoundedCornerShape(3.dp)
                                ) else Modifier
                            )
                    )
                }

                if (totalChunks > 64) {
                    Text(
                        text = "+${totalChunks - 64} more",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transmission Speed & Protocol Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val speedStr = when {
                        mbpsSpeed >= 1.0f -> String.format("⚡ %.1f Mbps (%.2f MB/s)", mbpsSpeed, mbpsSpeed / 8f)
                        mbpsSpeed > 0f -> String.format("⚡ %.0f Kbps", mbpsSpeed * 1000f)
                        fpsSpeed > 0f -> String.format("%.1f frames/s", fpsSpeed)
                        else -> "High-Speed Turbo Beam Active"
                    }
                    Text(
                        text = speedStr,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                        color = NeonCyan
                    )
                }

                Text(
                    text = if (isCompressed) "⚡ GZIP 3x • 0% Net" else "0% WiFi / 0% Bluetooth",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(isCompleted: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (isCompleted) EmeraldGreen.copy(alpha = 0.15f) else NeonCyan.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) EmeraldGreen else NeonCyan
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = if (isCompleted) EmeraldGreen else NeonCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isCompleted) "RECEIVED" else "STREAMING",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isCompleted) EmeraldGreen else NeonCyan
            )
        }
    }
}
