package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.QrStreamPlayer
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.ImmersiveBorder
import com.example.ui.theme.ImmersiveLavender
import com.example.ui.theme.ImmersivePurpleContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.neumorphicShadow
import com.example.util.PresetSampleFiles
import com.example.viewmodel.AirBeamViewModel
import com.example.viewmodel.SelectedFileState

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
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Multi-File Payload Header Card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            if (selectedFiles.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicShadow(shape = RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (selectedFiles.size == 1) selectedFiles[0].fileName else "Batch Stream (${selectedFiles.size} Files)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val totalBytes = selectedFiles.sumOf { it.bytes.size }
                                val formattedTotal = "${totalBytes} bytes (${(totalBytes / 1024f).let { String.format("%.1f KB", it) }})"
                                Text(
                                    text = "$formattedTotal • ${sendFrames.size} Chunks ${if (isEncryptionActive) "• AES-256" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isEncryptionActive) ImmersiveLavender else EmeraldGreen
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = ImmersivePurpleContainer
                                ) {
                                    Text(
                                        text = "${sendFrames.size} FRAMES",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = ImmersiveLavender,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                if (selectedFiles.size > 1) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.clearSelectedFiles() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear all",
                                            tint = Color(0xFF938F99)
                                        )
                                    }
                                }
                            }
                        }

                        // Selected file chips list
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            selectedFiles.forEach { file ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = ImmersivePurpleContainer,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveLavender)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                                    ) {
                                        Text(
                                            text = file.fileName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { viewModel.removeSelectedFile(file) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove file",
                                                tint = ImmersiveLavender,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "No files selected to beam",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF938F99)
                        )
                        TextButton(onClick = { filePickerLauncher.launch("*/*") }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Select Files", color = ImmersiveLavender, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // File Selection Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .width(130.dp)
                            .height(80.dp)
                            .neumorphicShadow(shape = RoundedCornerShape(16.dp))
                            .clickable { filePickerLauncher.launch("*/*") },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveLavender)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Choose Files",
                                tint = ImmersiveLavender,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+ Choose Files",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                items(PresetSampleFiles.getSampleFiles()) { preset ->
                    val isSelected = selectedFiles.any { it.fileName == preset.name }
                    Card(
                        modifier = Modifier
                            .width(140.dp)
                            .height(80.dp)
                            .neumorphicShadow(shape = RoundedCornerShape(16.dp))
                            .clickable { viewModel.selectPresetFile(preset) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) ImmersivePurpleContainer else DarkSurfaceCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) ImmersiveLavender else ImmersiveBorder
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (preset.mimeType.startsWith("image")) Icons.Default.Image else Icons.Default.Description,
                                    contentDescription = preset.typeName,
                                    tint = ImmersiveLavender,
                                    modifier = Modifier.size(20.dp)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Text(
                                        text = preset.typeName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Speed Accelerator Options (Minimum 10+ Mbps)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicShadow(shape = RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "⚡ 10+ Mbps",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Transfer Speed Mode",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ImmersivePurpleContainer
                        ) {
                            Text(
                                text = "GZIP ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ImmersiveLavender,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Speed Mode Pills
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.example.viewmodel.TransferSpeedMode.values().forEach { mode ->
                            val isSelected = mode == speedMode
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setSpeedMode(mode) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) ImmersiveLavender.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) ImmersiveLavender else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) ImmersiveLavender else TextPrimary
                                        )
                                        Text(
                                            text = mode.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = ImmersiveLavender,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Estimated Time Badge
                    val totalSelectedBytes = selectedFiles.sumOf { it.bytes.size }
                    if (totalSelectedBytes > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val totalMb = totalSelectedBytes * 8f / 1_000_000f
                        val estSeconds = kotlin.math.max(0.1f, totalMb / speedMode.targetMbps)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⏱️ Estimated time: ~${String.format("%.1f", estSeconds)}s for ${(totalSelectedBytes / (1024f * 1024f)).let { String.format("%.2f MB", it) }}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = EmeraldGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Compact Encryption Options
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicShadow(shape = RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "E2E Encryption",
                                tint = if (isEncryptionActive) ImmersiveLavender else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AES-256 Encryption",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Switch(
                            checked = isEncryptionActive,
                            onCheckedChange = { viewModel.toggleSenderEncryption(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ImmersiveLavender,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DarkSurfaceVariant
                            )
                        )
                    }

                    if (isEncryptionActive) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = senderPassphrase,
                                onValueChange = { viewModel.setSenderPassphrase(it) },
                                modifier = Modifier.weight(1f),
                                label = { Text("Encryption PIN", color = ImmersiveLavender) },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = ImmersiveLavender)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ImmersiveLavender,
                                    unfocusedBorderColor = ImmersiveBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )

                            IconButton(
                                onClick = { viewModel.generateNewPassphrase() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(ImmersivePurpleContainer, RoundedCornerShape(12.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "New Key",
                                    tint = ImmersiveLavender
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary Optical Stream Player
        item {
            QrStreamPlayer(frames = sendFrames)
        }

        // Single-Device Test Button
        item {
            Button(
                onClick = {
                    if (isSimulating) {
                        viewModel.stopBeamSimulation()
                    } else {
                        viewModel.startBeamSimulation(fps = 5)
                        onNavigateToReceive()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSimulating) Color(0xFFF2B8B5) else ImmersivePurpleContainer,
                    contentColor = if (isSimulating) Color.Black else ImmersiveLavender
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (isSimulating) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSimulating) "Stop Simulation" else "Test Receiver (Simulate Beam)",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

