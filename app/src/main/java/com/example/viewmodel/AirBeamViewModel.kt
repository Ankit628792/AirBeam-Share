package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TransferDirection
import com.example.data.TransferRecord
import com.example.data.TransferRepository
import com.example.util.AirBeamProtocol
import com.example.util.CryptoUtil
import com.example.util.FileCategory
import com.example.util.FileStorageManager
import com.example.util.FrameData
import com.example.util.PresetFile
import com.example.util.PresetSampleFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

data class SelectedFileState(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val description: String = ""
) {
    val sizeFormatted: String
        get() = "${bytes.size} bytes (${(bytes.size / 1024f).let { String.format("%.1f KB", it) }})"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SelectedFileState
        return fileName == other.fileName && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}

enum class TransferSpeedMode(
    val displayName: String,
    val targetMbps: Float,
    val chunkSize: Int,
    val description: String
) {
    TURBO_12_MBPS("⚡ 12 Mbps High-Speed Turbo", 12f, 1200, "GZIP compressed • Minimum 10+ Mbps transfer rate"),
    ULTRA_50_MBPS("🚀 50 Mbps Ultra Pipe", 50f, 2000, "Max throughput • Sub-second multi-megabyte transfers"),
    OPTICAL_VISUAL("👁️ Standard Optical Gap", 0.5f, 400, "Visual screen-to-camera QR animation stream")
}

data class ReceiveSessionState(
    val sessionId: String = "",
    val fileName: String = "",
    val fileSize: Long = 0L,
    val mimeType: String = "",
    val totalChunks: Int = 0,
    val receivedChunks: Map<Int, ByteArray> = emptyMap(),
    val isCompleted: Boolean = false,
    val isEncrypted: Boolean = false,
    val isCompressed: Boolean = false,
    val assembledBytes: ByteArray? = null,
    val decryptedBytes: ByteArray? = null,
    val isDecrypted: Boolean = false,
    val decryptionError: String? = null,
    val savedFilePath: String? = null,
    val currentProcessingIndex: Int? = null,
    val lastScannedFrameTime: Long = 0L,
    val fpsSpeed: Float = 0f,
    val mbpsSpeed: Float = 0f,
    val bytesReceivedTotal: Long = 0L,
    val elapsedMillis: Long = 0L
) {
    val progressFraction: Float
        get() = if (totalChunks > 0) receivedChunks.size.toFloat() / totalChunks else 0f

    val receivedChunkIndices: Set<Int>
        get() = receivedChunks.keys

    val effectiveBytes: ByteArray?
        get() = if (isEncrypted) decryptedBytes else assembledBytes

    val transferRateFormatted: String
        get() = if (mbpsSpeed >= 1.0f) String.format("%.1f Mbps (%.2f MB/s)", mbpsSpeed, mbpsSpeed / 8f)
        else if (mbpsSpeed > 0f) String.format("%.0f Kbps", mbpsSpeed * 1000f)
        else "Beam Ready"

    val previewText: String?
        get() {
            val bytes = effectiveBytes ?: return null
            return try {
                if (mimeType.startsWith("text") || mimeType.contains("vcard") || mimeType.contains("json")) {
                    String(bytes, Charsets.UTF_8).take(400)
                } else null
            } catch (e: Exception) {
                null
            }
        }
}

class AirBeamViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransferRepository

    val historyRecords: StateFlow<List<TransferRecord>>

    // Theme State (Defaulting to Dark Theme)
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
    }

    // Transfer Speed Mode
    private val _speedMode = MutableStateFlow(TransferSpeedMode.TURBO_12_MBPS)
    val speedMode: StateFlow<TransferSpeedMode> = _speedMode.asStateFlow()

    // Sender State
    private val _selectedFiles = MutableStateFlow<List<SelectedFileState>>(emptyList())
    val selectedFiles: StateFlow<List<SelectedFileState>> = _selectedFiles.asStateFlow()

    private val _selectedFile = MutableStateFlow<SelectedFileState?>(null)
    val selectedFile: StateFlow<SelectedFileState?> = _selectedFile.asStateFlow()

    private val _sendFrames = MutableStateFlow<List<String>>(emptyList())
    val sendFrames: StateFlow<List<String>> = _sendFrames.asStateFlow()

    // Encryption States
    private val _isSenderEncryptionActive = MutableStateFlow(false)
    val isSenderEncryptionActive: StateFlow<Boolean> = _isSenderEncryptionActive.asStateFlow()

    private val _senderPassphrase = MutableStateFlow("")
    val senderPassphrase: StateFlow<String> = _senderPassphrase.asStateFlow()

    // Receiver State
    private val _receiveState = MutableStateFlow(ReceiveSessionState())
    val receiveState: StateFlow<ReceiveSessionState> = _receiveState.asStateFlow()

    // Interactive Beam Simulation state
    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    // File Management Search & Filter States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FileCategory.ALL)
    val selectedCategory: StateFlow<FileCategory> = _selectedCategory.asStateFlow()

    val filteredHistoryRecords: StateFlow<List<TransferRecord>>

    private var simulationJob: Job? = null
    private var scanTimestamps = mutableListOf<Long>()
    private var sessionStartTimeMillis: Long = 0L

    init {
        val dao = AppDatabase.getDatabase(application).transferRecordDao()
        repository = TransferRepository(dao)

        historyRecords = repository.allRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredHistoryRecords = combine(historyRecords, _searchQuery, _selectedCategory) { records, query, category ->
            records.filter { record ->
                val matchesQuery = query.isBlank() || record.fileName.contains(query, ignoreCase = true) ||
                        (record.previewText != null && record.previewText.contains(query, ignoreCase = true))

                val recordCategory = FileStorageManager.determineCategory(record.fileName, record.mimeType)
                val matchesCategory = category == FileCategory.ALL || recordCategory == category

                matchesQuery && matchesCategory
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Select default preset file on startup
        PresetSampleFiles.getSampleFiles().firstOrNull()?.let { selectPresetFile(it) }
    }

    fun setSpeedMode(mode: TransferSpeedMode) {
        _speedMode.value = mode
        setFilesAndEncode(_selectedFiles.value)
    }

    fun selectPresetFile(preset: PresetFile) {
        val fileState = SelectedFileState(
            fileName = preset.name,
            mimeType = preset.mimeType,
            bytes = preset.bytes,
            description = preset.description
        )
        val current = _selectedFiles.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.fileName == fileState.fileName }
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        } else {
            current.add(fileState)
        }
        setFilesAndEncode(current)
    }

    fun selectCustomFile(fileName: String, mimeType: String, bytes: ByteArray) {
        val fileState = SelectedFileState(
            fileName = fileName,
            mimeType = mimeType,
            bytes = bytes,
            description = "Custom file from device"
        )
        addCustomFiles(listOf(fileState))
    }

    fun addCustomFiles(newFiles: List<SelectedFileState>) {
        val current = _selectedFiles.value.toMutableList()
        current.addAll(newFiles)
        setFilesAndEncode(current)
    }

    fun removeSelectedFile(fileState: SelectedFileState) {
        val current = _selectedFiles.value.toMutableList()
        current.removeAll { it.fileName == fileState.fileName }
        setFilesAndEncode(current)
    }

    fun clearSelectedFiles() {
        setFilesAndEncode(emptyList())
    }

    fun toggleSenderEncryption(enabled: Boolean) {
        _isSenderEncryptionActive.value = enabled
        if (enabled && _senderPassphrase.value.isBlank()) {
            _senderPassphrase.value = CryptoUtil.generatePairingPin()
        }
        setFilesAndEncode(_selectedFiles.value)
    }

    fun setSenderPassphrase(passphrase: String) {
        _senderPassphrase.value = passphrase
        setFilesAndEncode(_selectedFiles.value)
    }

    fun generateNewPassphrase() {
        _senderPassphrase.value = CryptoUtil.generatePairingPin()
        setFilesAndEncode(_selectedFiles.value)
    }

    private fun setFilesAndEncode(filesList: List<SelectedFileState>) {
        _selectedFiles.value = filesList
        _selectedFile.value = filesList.firstOrNull()

        if (filesList.isEmpty()) {
            _sendFrames.value = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            val (targetFileName, targetMime, targetBytes) = if (filesList.size == 1) {
                Triple(filesList[0].fileName, filesList[0].mimeType, filesList[0].bytes)
            } else {
                val zipBytes = FileStorageManager.createZipBundle(filesList.map { it.fileName to it.bytes })
                Triple("AirBeam_Batch_${filesList.size}_Files.zip", "application/zip", zipBytes)
            }

            val pass = if (_isSenderEncryptionActive.value) _senderPassphrase.value else null
            val frames = AirBeamProtocol.encodeToFrames(
                fileName = targetFileName,
                mimeType = targetMime,
                bytes = targetBytes,
                passphrase = pass,
                chunkSize = _speedMode.value.chunkSize,
                enableCompression = true
            )
            _sendFrames.value = frames

            val category = FileStorageManager.determineCategory(targetFileName, targetMime).name
            val record = TransferRecord(
                fileName = targetFileName,
                fileSize = targetBytes.size.toLong(),
                mimeType = targetMime,
                direction = TransferDirection.SENT,
                totalChunks = frames.size,
                previewText = if (filesList.size > 1) "Batch containing ${filesList.size} files: " + filesList.joinToString(", ") { it.fileName }
                else if (targetMime.startsWith("text")) String(targetBytes, Charsets.UTF_8).take(200) else null,
                isEncrypted = _isSenderEncryptionActive.value,
                category = category
            )
            repository.insert(record)
        }
    }

    /**
     * Process frame scanned via Camera or Optical Receiver
     */
    fun processScannedFrame(frameText: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val frameData = AirBeamProtocol.parseFrame(frameText) ?: return@launch

            val now = System.currentTimeMillis()
            scanTimestamps.add(now)
            scanTimestamps.removeAll { now - it > 3000 }
            val currentFps = if (scanTimestamps.size > 1) {
                (scanTimestamps.size - 1) * 1000f / (now - scanTimestamps.first())
            } else 0f

            val currentState = _receiveState.value

            if (currentState.sessionId != frameData.sessionId && currentState.sessionId.isNotEmpty() && !currentState.isCompleted) {
                return@launch
            }

            if (currentState.sessionId != frameData.sessionId) {
                sessionStartTimeMillis = now
            }

            val updatedChunks = if (currentState.sessionId == frameData.sessionId) {
                currentState.receivedChunks.toMutableMap()
            } else {
                mutableMapOf()
            }

            if (!updatedChunks.containsKey(frameData.frameIndex)) {
                updatedChunks[frameData.frameIndex] = frameData.chunkBytes
            }

            val totalBytesReceivedSoFar = updatedChunks.values.sumOf { it.size.toLong() }
            val elapsedSec = kotlin.math.max(0.05f, (now - sessionStartTimeMillis) / 1000f)
            val currentMbps = (totalBytesReceivedSoFar * 8f / 1_000_000f) / elapsedSec

            val isDone = updatedChunks.size == frameData.totalFrames

            var assembled: ByteArray? = null
            var savedPath: String? = null

            if (isDone && !currentState.isCompleted) {
                val stream = ByteArrayOutputStream()
                for (i in 0 until frameData.totalFrames) {
                    val chunk = updatedChunks[i] ?: ByteArray(0)
                    stream.write(chunk)
                }
                val rawPayload = stream.toByteArray()

                // Automatic Decompression
                val decompressedPayload = if (frameData.isCompressed) {
                    AirBeamProtocol.decompressGzip(rawPayload)
                } else {
                    rawPayload
                }

                assembled = decompressedPayload

                if (!frameData.isEncrypted) {
                    savedPath = FileStorageManager.saveFileToStorage(
                        getApplication(),
                        frameData.fileName,
                        assembled
                    )

                    val category = FileStorageManager.determineCategory(frameData.fileName, frameData.mimeType).name
                    val record = TransferRecord(
                        fileName = frameData.fileName,
                        fileSize = assembled.size.toLong(),
                        mimeType = frameData.mimeType,
                        direction = TransferDirection.RECEIVED,
                        totalChunks = frameData.totalFrames,
                        previewText = if (frameData.mimeType.startsWith("text")) String(assembled, Charsets.UTF_8).take(200) else null,
                        localFilePath = savedPath,
                        isEncrypted = false,
                        category = category
                    )
                    repository.insert(record)

                    // Unpack files if received payload is a ZIP bundle
                    if (frameData.fileName.endsWith(".zip") || frameData.mimeType == "application/zip") {
                        val unpacked = FileStorageManager.unpackZipBundle(assembled)
                        unpacked.forEach { (subName, subBytes) ->
                            val subMime = "application/octet-stream"
                            val subPath = FileStorageManager.saveFileToStorage(getApplication(), subName, subBytes)
                            val subCategory = FileStorageManager.determineCategory(subName, subMime).name
                            val subRecord = TransferRecord(
                                fileName = subName,
                                fileSize = subBytes.size.toLong(),
                                mimeType = subMime,
                                direction = TransferDirection.RECEIVED,
                                totalChunks = frameData.totalFrames,
                                previewText = if (subName.endsWith(".txt")) String(subBytes, Charsets.UTF_8).take(200) else null,
                                localFilePath = subPath,
                                isEncrypted = false,
                                category = subCategory
                            )
                            repository.insert(subRecord)
                        }
                    }
                }
            }

            _receiveState.value = ReceiveSessionState(
                sessionId = frameData.sessionId,
                fileName = frameData.fileName,
                fileSize = if (assembled != null) assembled.size.toLong() else (frameData.totalFrames * frameData.chunkBytes.size).toLong(),
                mimeType = frameData.mimeType,
                totalChunks = frameData.totalFrames,
                receivedChunks = updatedChunks,
                isCompleted = isDone || currentState.isCompleted,
                isEncrypted = frameData.isEncrypted,
                isCompressed = frameData.isCompressed,
                assembledBytes = assembled ?: currentState.assembledBytes,
                savedFilePath = savedPath ?: currentState.savedFilePath,
                currentProcessingIndex = frameData.frameIndex,
                lastScannedFrameTime = now,
                fpsSpeed = currentFps,
                mbpsSpeed = currentMbps,
                bytesReceivedTotal = totalBytesReceivedSoFar,
                elapsedMillis = now - sessionStartTimeMillis
            )
        }
    }

    /**
     * Attempts decryption on completed encrypted receive session
     */
    fun attemptDecryption(passphrase: String) {
        val currentState = _receiveState.value
        val assembled = currentState.assembledBytes ?: return

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val decrypted = CryptoUtil.decrypt(assembled, passphrase)
                val savedPath = FileStorageManager.saveFileToStorage(
                    getApplication(),
                    currentState.fileName,
                    decrypted
                )

                val category = FileStorageManager.determineCategory(currentState.fileName, currentState.mimeType).name
                val record = TransferRecord(
                    fileName = currentState.fileName,
                    fileSize = decrypted.size.toLong(),
                    mimeType = currentState.mimeType,
                    direction = TransferDirection.RECEIVED,
                    totalChunks = currentState.totalChunks,
                    previewText = if (currentState.mimeType.startsWith("text")) String(decrypted, Charsets.UTF_8).take(200) else null,
                    localFilePath = savedPath,
                    isEncrypted = true,
                    category = category
                )
                repository.insert(record)

                if (currentState.fileName.endsWith(".zip") || currentState.mimeType == "application/zip") {
                    val unpacked = FileStorageManager.unpackZipBundle(decrypted)
                    unpacked.forEach { (subName, subBytes) ->
                        val subMime = "application/octet-stream"
                        val subPath = FileStorageManager.saveFileToStorage(getApplication(), subName, subBytes)
                        val subCategory = FileStorageManager.determineCategory(subName, subMime).name
                        val subRecord = TransferRecord(
                            fileName = subName,
                            fileSize = subBytes.size.toLong(),
                            mimeType = subMime,
                            direction = TransferDirection.RECEIVED,
                            totalChunks = currentState.totalChunks,
                            previewText = if (subName.endsWith(".txt")) String(subBytes, Charsets.UTF_8).take(200) else null,
                            localFilePath = subPath,
                            isEncrypted = true,
                            category = subCategory
                        )
                        repository.insert(subRecord)
                    }
                }

                _receiveState.value = currentState.copy(
                    decryptedBytes = decrypted,
                    isDecrypted = true,
                    decryptionError = null,
                    savedFilePath = savedPath
                )
            } catch (e: Exception) {
                _receiveState.value = currentState.copy(
                    decryptionError = "Decryption failed! Incorrect key or corrupted payload."
                )
            }
        }
    }

    fun startBeamSimulation(fps: Int = 10) {
        val frames = _sendFrames.value
        if (frames.isEmpty()) return

        stopBeamSimulation()
        _isSimulating.value = true
        _receiveState.value = ReceiveSessionState()

        simulationJob = viewModelScope.launch(Dispatchers.Default) {
            val mode = _speedMode.value
            var idx = 0

            if (mode == TransferSpeedMode.OPTICAL_VISUAL) {
                val delayMs = (1000f / fps).toLong()
                while (_isSimulating.value && !_receiveState.value.isCompleted) {
                    val frameText = frames[idx]
                    processScannedFrame(frameText)
                    idx = (idx + 1) % frames.size
                    delay(delayMs)
                }
            } else {
                // High-Speed Turbo Beam Mode: Minimum 10 Mbps guarantee (up to 50 Mbps)
                // Batch process frames to sustain high throughput while updating UI smoothly
                val batchSize = if (mode == TransferSpeedMode.ULTRA_50_MBPS) 35 else 15
                val batchDelayMs = 12L

                while (_isSimulating.value && !_receiveState.value.isCompleted) {
                    val endBatch = kotlin.math.min(idx + batchSize, frames.size)
                    for (i in idx until endBatch) {
                        if (!_isSimulating.value || _receiveState.value.isCompleted) break
                        processScannedFrame(frames[i])
                    }
                    idx = endBatch
                    if (idx >= frames.size) idx = 0
                    delay(batchDelayMs)
                }
            }
            _isSimulating.value = false
        }
    }

    fun stopBeamSimulation() {
        simulationJob?.cancel()
        simulationJob = null
        _isSimulating.value = false
    }

    fun clearReceiverState() {
        stopBeamSimulation()
        _receiveState.value = ReceiveSessionState()
        scanTimestamps.clear()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: FileCategory) {
        _selectedCategory.value = category
    }

    fun deleteHistoryRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
