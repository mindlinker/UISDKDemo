package com.mindlinker.mlsdk.utils

import android.text.TextUtils
import androidx.annotation.StringDef
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * SHA 工具类
 */
object SHAUtil {

    const val SHA224 = "sha-224"
    const val SHA256 = "SHA-256"
    const val SHA384 = "sha-384"
    const val SHA512 = "sha-512"

    @StringDef(SHA224, SHA256, SHA384, SHA512)
    internal annotation class SHAType

    /**
     * Sha加密
     *
     * @param string 加密字符串
     * @param type   加密类型 ：[.SHA224]，[.SHA256]，[.SHA384]，[.SHA512]
     * @return SHA加密结果字符串
     */
    @JvmStatic
    fun sha(str: String, @SHAType type: String?): String {
        var type = type
        if (TextUtils.isEmpty(str)) {
            return ""
        }
        if (TextUtils.isEmpty(type)) {
            type = SHA256
        }
        var result = StringBuffer()
        try {
            val md5 = MessageDigest.getInstance(type)
            val bytes = md5.digest(str.toByteArray())
            for (b in bytes) {
                result.append(String.format("%02x", b))
            }
            return result.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return ""
    }
}
