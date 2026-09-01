package com.example.util

import android.graphics.Bitmap
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer

object QrCodeDecoder {
    /**
     * Decodes QR text from a Bitmap safely and thread-safe.
     */
    fun decodeBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            val reader = MultiFormatReader().apply {
                val hints = mapOf(
                    DecodeHintType.POSSIBLE_FORMATS to listOf(com.google.zxing.BarcodeFormat.QR_CODE),
                    DecodeHintType.TRY_HARDER to true
                )
                setHints(hints)
            }
            val result = reader.decodeWithState(binaryBitmap)
            reader.reset()
            result.text
        } catch (e: Exception) {
            null
        }
    }
}
