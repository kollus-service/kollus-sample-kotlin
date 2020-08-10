package kollus.test.media.utils

import android.annotation.TargetApi
import android.os.Build
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class JwtUtil {
    private val TAG: String = JwtUtil::class.simpleName!!

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Throws(NoSuchAlgorithmException::class, InvalidKeyException::class)
    fun createJwt(headerJson: String, payloadJson: String, secretKey: String): String {
        val header = Base64.encodeToString(
            headerJson.toByteArray(StandardCharsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
        val payload = Base64.encodeToString(
            payloadJson.toByteArray(StandardCharsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
        val content = String.format("%s.%s", header, payload)
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        val signatureBytes = mac.doFinal(content.toByteArray(StandardCharsets.UTF_8))
        val signature = Base64.encodeToString(signatureBytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        LogUtil.d(TAG, String.format("%s.%s", content, signature))
        return String.format("%s.%s", content, signature)
    }

    @Throws(InvalidKeyException::class, NoSuchAlgorithmException::class)
    fun createJwt(payloadJson: String, secretKey: String): String {
        val headerJson = "{\"alg\": \"HS256\",\"typ\": \"JWT\"}"
        return createJwt(headerJson, payloadJson, secretKey)
    }


    @Throws(Exception::class)
    fun splitJwt(jwt: String): Array<String> {
        var parts = jwt.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size == 2 && jwt.endsWith(".")) {
            parts = arrayOf(parts[0], parts[1], "")
        }
        if (parts.size != 3) {
            throw Exception(String.format("The token was expected to have 3 parts, but got %s.", parts.size))
        }
        return parts
    }

    @Throws(Exception::class)
    fun decodeJwt(jwt: String): Array<String> {
        val parts = splitJwt(jwt)
        val headerJson = String(Base64.decode(parts[0], Base64.URL_SAFE))
        val payloadJson = String(Base64.decode(parts[0], Base64.URL_SAFE))
        val signature = parts[2]

        return arrayOf(headerJson, payloadJson, signature)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Throws(Exception::class)
    fun verify(secretKey: String, jwt: String): Boolean {
        val parts = splitJwt(jwt)
        val contentBytes = String.format("%s.%s", parts[0], parts[1]).toByteArray(StandardCharsets.UTF_8)
        val signatureBytes = Base64.decode(parts[2], Base64.URL_SAFE)

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secretKey.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        val newSignatureBytes = mac.doFinal(contentBytes)

        return MessageDigest.isEqual(newSignatureBytes, signatureBytes)
    }
}