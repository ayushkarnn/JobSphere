package com.app.jobupdt.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.common.collect.Lists
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

class AccessToken {

    companion object {
        private const val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"
    }

    fun getAccessToken(): String? {
        return try {
            val jsonString = """
                {
                  "type": "service_account",
                  "project_id": "offcampusupdatesapp",
                  "private_key_id": "ab04187b3d75bdfb56656510eb77b22b3ddb9a82",
                  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCwps/S01E4JbqH\n24PfwTUyptrDIz2b0/JaDDu0f+3uJY3H3kKfb+l9sXMMgWKDrMeVZ2RoTBXoh+iW\nuZ/BBD0yzb8P0e8P5JkkDHGh7CMKBxmD+nwtEv/eCVjSc+Atp8UUS2UsSjh9tHsR\nrl24ZO23Fw0TgEuv/wLWO/wHrbvSuGY9vhyTjeIFrEf7BMLES7FSKlKCeOZz4CnF\nypwNekd9dy5VH8ipjUITNXBJogmK4T7KZ+Z05e5nkOiLqgYQx5AfqTivOu1jMgjM\nEk5p9G7Y8fN3qBUfi5qH2lMAMzggndV12ZcfkXwXFzctFJFhKBIur3Gu8wbfeVMJ\nUJz4jCd/AgMBAAECggEAAMiubU0ShO0UCKQHKBtTcZOVLLptwfJkoSarQTEKAtOG\nxWMmLzWmBJVfq9O9jbFc+mOxu5xkmKjsCJzm52nv969tnAdOMg8QhDBx2WazVBbX\nmWpG6OhUUwUXc36mMdKn1C2g2ex0uOrOQbHrjHfwngRn9wfvZi40XkGFJsoYq5Up\nRQ5WaLvZvuBBgglDXyQY731gkIVLj6ASMTb0Fi5xDGXLIN5Ydx+4jCu3VO9ykOjO\n3z4XM0Yy674FSdSiasnHj2o11IxX2DvUg97ErPdVxLi4uTbXK56Eto8Ti/Vvl4QV\nRX7JSK13UODIK4lHAtF0mCjc5ZuOAOFZRmxvpyiWjQKBgQDqHEM1POygGLXVUQjd\neDIMDszWF5NIO5eh6Rt/DBBy6KJuxtAaywhi4P7udJWQe+3wAOF9QIwgRfq+/tSQ\nIbil79ivUnu63/lFWHjBbuQWnKFIe3eXHY93NmVz6OJmAh3P4IKcDA/h5aTO3Enb\nxr+HCMG5o8DffzURisnL5rk1LQKBgQDBKzK/O00iAFOncgBQKIcpXhp1zfnXnw4G\nbk0nvQSaEjQF1Ty7837VWVxpx927Z6B9dem0k8e+Wl0HDj9Tny/TmSX1B6D88dsu\nQQKp5QbSzLhj9UsLYWA6wc0QFGxd7QB3DxK3z/93/lmNY2U5G8ptxmxo/etcuWcP\n3BnuQ+iS2wKBgQCPHqITrOA364NWGokh47Ys2utswtLeaNgFOp39qGFx7jhHIrHc\nf1zsHgKbwpgg0TKf89nmGYzQuhdVpjJKvuLERwGgBPvxPbWFKFYCYwl7rVuIFTsC\nHiczyQRyvN+TXx3clLMkNNeM0ThU4iUg/7rdEYRlOe/SclD+HWvKhI2bLQKBgHcV\nfHlrw4dckDXAD/VapsiX3NQXIRkOUwUf5qzC2B1rb2rRJFnyKJS6PEByGH25yrTN\nhj/ugssUxhjdbajNkBxwY3gFNff52ddP+KZoLKaz0lC9HYBsjXhrlVF+ZbgZrZm4\nTx/GOIXu5aJGTlbAjTDTjG3Fa92D5sdeNxYuAyjVAoGAL0wZEf64KZ/g9d4Lmch2\ngPIqxm41jUXMo45RZNb8Z7LI5yCWAs+1r+YGOaKuPTd5XqWtVt9TT0GWZYDH0dAX\nvbyXAM0hQab8MnxdQd+ZYNlS6x32o6tbjX8DBKLbCu5Hst5UNmeNz8MrAnBulvZE\nvEhwdgVdPatt2cfoNUJQsEE=\n-----END PRIVATE KEY-----\n",
                  "client_email": "firebase-adminsdk-s5523@offcampusupdatesapp.iam.gserviceaccount.com",
                  "client_id": "113968172497909440242",
                  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                  "token_uri": "https://oauth2.googleapis.com/token",
                  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
                  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-s5523@offcampusupdatesapp.iam.gserviceaccount.com",
                  "universe_domain": "googleapis.com"
                }
            """.trimIndent()

            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val credential = GoogleCredentials.fromStream(stream).createScoped(Lists.newArrayList(firebaseMessagingScope))
            credential.refresh()
            credential.accessToken.tokenValue
        } catch (e: IOException) {
            null
        }
    }
}
