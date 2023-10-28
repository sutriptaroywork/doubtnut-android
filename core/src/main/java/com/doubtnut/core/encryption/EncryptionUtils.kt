package com.doubtnut.core.encryption

import com.doubtnut.core.constant.CoreConstants
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {

    fun encrypt(strToEncrypt: String): String? {
        return try {
            val cipher = Cipher.getInstance("AES/CTR/NoPadding")
            val ivParameterSpec = IvParameterSpec(CoreConstants.IV_KEY.toByteArray())
            cipher.init(
                Cipher.ENCRYPT_MODE,
                setKey(CoreConstants.AES_KEY),
                ivParameterSpec
            )
            cipher.doFinal(strToEncrypt.toByteArray(StandardCharsets.UTF_8)).toHexString()
        } catch (e: Exception) {
            null
        }
    }

    private fun setKey(key: String): SecretKeySpec? {
        var secretKey: SecretKeySpec? = null
        try {
            secretKey = SecretKeySpec(
                key.toByteArray(charset("UTF-8")),
                "AES/CTR/NoPadding"
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return secretKey
    }

}

fun ByteArray.toHexString() =
    joinToString("") { (0xFF and it.toInt()).toString(16).padStart(2, '0') }