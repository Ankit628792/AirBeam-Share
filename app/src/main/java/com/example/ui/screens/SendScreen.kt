package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AirBeamHeroVectorArt
import com.example.ui.components.QrStreamPlayer
import com.example.ui.theme.AppTheme
import com.example.ui.theme.neumorphicShadow
import com.example.util.PresetSampleFiles
import com.example.viewmodel.AirBeamViewModel
import com.example.viewmodel.SelectedFileState
import com.example.viewmodel.TransferSpeedMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SendScreen(
    viewModel: AirBeamViewModel,
    onNavigateToReceive: () -> Unit
) {
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val sendFrames by viewModel.sendFrames.collectAsState()
    val isSimulating by viewModel.isSimulating.collectAsState()
    val speedMode by viewModel.speedMode.collectAsState()

    val isEncryptionActive by viewModel.isSenderEncryptionActive.collectAsState()
    val senderPassphrase by viewModel.senderPassphrase.collectAsState()

    var isSettingsExpanded by remember { mutableStateOf(false) }
    var isQuickNoteOpen by remember { mutableStateOf(false) }
    var quickNoteText by remember { mutableStateOf("") }

    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newFiles = mutableListOf<SelectedFileState>()
            val contentResolver = context.contentResolver
            uris.forEach { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

                    var fileName = "AirBeam_Shared_File"
                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (cursor.moveToFirst() && nameIndex >= 0) {
                            fileName = cursor.getString(nameIndex)
                        }
                    }

                    if (bytes != null && bytes.isNotEmpty()) {
                        newFiles.add(
                            SelectedFileState(
                                fileName = fileName,
                                mimeType = mimeType,
                                bytes = bytes,
                                description = "Custom file from device"
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (newFiles.isNotEmpty()) {
                viewModel.addCustomFiles(newFiles)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // ==========================================
        // 1. MINIMAL VECTOR ART HERO SECTION
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AirBeamHeroVectorArt(
                    isStreaming = isSimulating || sendFrames.isNotEmpty(),
                    size = 110.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Optical Air-Gap Beam",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = AppTheme.colors.textPrimary
                )

                Text(
                    text = "Screen to camera • 0% network radios",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.textSecondary
                )
            }
        }

        // ==========================================
        // 2. PRIMARY MINIMAL CONTENT / STREAM STATE
        // ==========================================
        if (selectedFiles.isEmpty()) {
            // State A: Empty Slate (Minimal vector action zone)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicShadow(shape = RoundedCornerShape(22.dp))
                        .clickable { filePickerLauncher.launch("*/*") },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AppTheme.colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.primary.copy(alpha = 0.12f))
                                .border(1.5.dp, AppTheme.colors.primary.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Pick Files",
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Select File to Beam",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Tap to browse PDFs, photos, videos or data",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.textMuted
                        )
                    }
                }
            }

            // Minimal Quick Preset Bar
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUICK SAMPLES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textMuted,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isQuickNoteOpen = !isQuickNoteOpen }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = AppTheme.colors.neonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isQuickNoteOpen) "Close Note" else "Text Note",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.neonCyan
                            )
                        }
                    }

                    // 3 Minimal Vector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val samples = PresetSampleFiles.getSampleFiles()
                        val sample1 = samples.firstOrNull { it.name.contains("Heavy") } ?: samples.getOrNull(0)
                        val sample2 = samples.firstOrNull { it.name.contains("Photo") } ?: samples.getOrNull(1)
                        val sample3 = samples.firstOrNull { it.name.contains("Brief") } ?: samples.getOrNull(2)

                        listOfNotNull(sample1, sample2, sample3).forEach { preset ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.selectPresetFile(preset) },
                                shape = RoundedCornerShape(12.dp),
                                color = AppTheme.colors.surfaceCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = when {
                                            preset.name.contains("Heavy") -> Icons.Default.Bolt
                                            preset.name.contains("Photo") -> Icons.Default.Image
                                            else -> Icons.Default.Description
                                        },
                                        contentDescription = null,
                                        tint = if (preset.name.contains("Heavy")) AppTheme.colors.neonCyan else AppTheme.colors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when {
                                            preset.name.contains("Heavy") -> "4.2 MB Data"
                                            preset.name.contains("Photo") -> "Photo"
                                            else -> "Doc"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.textPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Optional Expandable Quick Note Card
                    AnimatedVisibility(
                        visible = isQuickNoteOpen,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                OutlinedTextField(
                                    value = quickNoteText,
                                    onValueChange = { quickNoteText = it },
                                    placeholder = { Text("Write quick text/keys to beam...", color = AppTheme.colors.textMuted) },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AppTheme.colors.primary,
                                        unfocusedBorderColor = AppTheme.colors.border,
                                        focusedTextColor = AppTheme.colors.textPrimary,
                                        unfocusedTextColor = AppTheme.colors.textPrimary
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        if (quickNoteText.isNotBlank()) {
                                            viewModel.addCustomFiles(
                                                listOf(
                                                    SelectedFileState(
                                                        fileName = "AirBeam_Note_${System.currentTimeMillis() % 10000}.txt",
                                                        mimeType = "text/plain",
                                                        bytes = quickNoteText.toByteArray(Charsets.UTF_8),
                                                        description = "Quick Text Note"
                                                    )
                                                )
                                            )
                                            quickNoteText = ""
                                            isQuickNoteOpen = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = quickNoteText.isNotBlank(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppTheme.colors.primary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Beam This Note", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // State B: Active Payload (Minimal Header + Stream Player)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicShadow(shape = RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedFiles.size == 1) selectedFiles[0].fileName else "Batch (${selectedFiles.size} Files)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                    maxLines = 1
                                )
                                val totalBytes = selectedFiles.sumOf { it.bytes.size }
                                val formattedSize = if (totalBytes >= 1024 * 1024) {
                                    String.format("%.2f MB", totalBytes / (1024f * 1024f))
                                } else {
                                    String.format("%.1f KB", totalBytes / 1024f)
                                }
                                Text(
                                    text = "$formattedSize • ${sendFrames.size} Chunks ${if (isEncryptionActive) "• AES-256" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isEncryptionActive) AppTheme.colors.primary else AppTheme.colors.emeraldGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AppTheme.colors.primaryContainer
                                ) {
                                    Text(
                                        text = "${sendFrames.size} F",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = AppTheme.colors.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = { viewModel.clearSelectedFiles() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = AppTheme.colors.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Central QR Code Stream Player
            item {
                QrStreamPlayer(frames = sendFrames)
            }

            // Minimal Settings Pill (Collapsible: Speed Mode & Security)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { isSettingsExpanded = !isSettingsExpanded },
                    shape = RoundedCornerShape(14.dp),
                    color = AppTheme.colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Transfer Config: ${speedMode.displayName} ${if (isEncryptionActive) "• AES-256" else ""}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textPrimary
                            )
                        }

                        Icon(
                            imageVector = if (isSettingsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = AppTheme.colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expandable Settings Content
            item {
                AnimatedVisibility(
                    visible = isSettingsExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Speed Mode Selector
                            Text(
                                text = "Speed Profile",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                TransferSpeedMode.values().forEach { mode ->
                                    val isSelected = mode == speedMode
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.setSpeedMode(mode) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) AppTheme.colors.primaryContainer else AppTheme.colors.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) AppTheme.colors.primary else Color.Transparent
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = when (mode) {
                                                    TransferSpeedMode.TURBO_12_MBPS -> "⚡ 12 Mbps"
                                                    TransferSpeedMode.ULTRA_50_MBPS -> "🚀 50 Mbps"
                                                    TransferSpeedMode.OPTICAL_VISUAL -> "👁️ Visual"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.textPrimary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            // AES-256 Toggle & PIN
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isEncryptionActive) AppTheme.colors.primary else AppTheme.colors.textMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AES-256 E2E Encryption",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = AppTheme.colors.textPrimary
                                    )
                                }

                                Switch(
                                    checked = isEncryptionActive,
                                    onCheckedChange = { viewModel.toggleSenderEncryption(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AppTheme.colors.primary,
                                        uncheckedThumbColor = AppTheme.colors.textMuted,
                                        uncheckedTrackColor = AppTheme.colors.surfaceVariant
                                    )
                                )
                            }

                            if (isEncryptionActive) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = senderPassphrase,
                                        onValueChange = { viewModel.setSenderPassphrase(it) },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Encryption PIN", color = AppTheme.colors.primary) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Key,
                                                contentDescription = null,
                                                tint = AppTheme.colors.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppTheme.colors.primary,
                                            unfocusedBorderColor = AppTheme.colors.border,
                                            focusedTextColor = AppTheme.colors.textPrimary,
                                            unfocusedTextColor = AppTheme.colors.textPrimary
                                        ),
                                        singleLine = true
                                    )

                                    IconButton(
                                        onClick = { viewModel.generateNewPassphrase() },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(AppTheme.colors.primaryContainer, RoundedCornerShape(10.dp))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Regenerate PIN",
                                            tint = AppTheme.colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Minimal Loopback Simulation Button
            item {
                Button(
                    onClick = {
                        if (isSimulating) {
                            viewModel.stopBeamSimulation()
                        } else {
                            viewModel.startBeamSimulation(fps = 10)
                            onNavigateToReceive()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSimulating) AppTheme.colors.roseError.copy(alpha = 0.2f) else AppTheme.colors.primaryContainer,
                        contentColor = if (isSimulating) AppTheme.colors.roseError else AppTheme.colors.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (isSimulating) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSimulating) "Stop Loopback Simulation" else "Test Receiver (Loopback Beam)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
