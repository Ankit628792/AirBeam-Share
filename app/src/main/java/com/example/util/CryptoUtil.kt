package com.example.util

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val KEY_SIZE = 256
    private const val GCM_TAG_LENGTH = 128
    private const val IV_SIZE_BYTES = 12
    private const val PBKDF2_ITERATIONS = 10000

    // Fixed salt for device-to-device optical transmission derived from passphrase
    private val DEFAULT_SALT = "AirBeamOpticalAirGapSalt2026".toByteArray(Charsets.UTF_8)

    fun deriveKey(passphrase: String): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passphrase.toCharArray(), DEFAULT_SALT, PBKDF2_ITERATIONS, KEY_SIZE)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

    /**
     * Encrypts plain bytes with AES-256-GCM using derived passphrase key.
     * Output format: [12-byte IV] + [Ciphertext + Tag]
     */
    fun encrypt(plainBytes: ByteArray, passphrase: String): ByteArray {
        val secretKey = deriveKey(passphrase)
        val iv = ByteArray(IV_SIZE_BYTES)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

        val ciphertext = cipher.doFinal(plainBytes)

        val output = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, output, 0, iv.size)
        System.arraycopy(ciphertext, 0, output, iv.size, ciphertext.size)
        return output
    }

    /**
     * Decrypts AES-256-GCM payload using passphrase key.
     * Throws exception if passphrase or ciphertext is invalid/corrupted.
     */
    fun decrypt(encryptedBytes: ByteArray, passphrase: String): ByteArray {
        if (encryptedBytes.size < IV_SIZE_BYTES + 16) {
            throw IllegalArgumentException("Payload too short for AES-GCM")
        }

        val secretKey = deriveKey(passphrase)
        val iv = ByteArray(IV_SIZE_BYTES)
        System.arraycopy(encryptedBytes, 0, iv, 0, IV_SIZE_BYTES)

        val ciphertextLength = encryptedBytes.size - IV_SIZE_BYTES
        val ciphertext = ByteArray(ciphertextLength)
        System.arraycopy(encryptedBytes, IV_SIZE_BYTES, ciphertext, 0, ciphertextLength)

        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

        return cipher.doFinal(ciphertext)
    }

    /**
     * Generates a random numeric passphrase for easy 6-digit optical pairing.
     */
    fun generatePairingPin(): String {
        val random = SecureRandom()
        val pin = random.nextInt(900000) + 100000
        return pin.toString()
    }
}
