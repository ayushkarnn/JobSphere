package com.app.jobupdt.fcm

import android.content.Context
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


//to all topic subscribed user
class FcmNotificationSender(
    private val title: String,
    private val body: String,
    private val context: Context,
    private val topic:String
) {

    private val postUrl = "https://fcm.googleapis.com/v1/projects/offcampusupdatesapp/messages:send"
//    private val topic = "c36e25b4-c7ee-4569-ae84-e2cb95d6a4f9"

    fun sendNotification() {
        CoroutineScope(Dispatchers.IO).launch {
            val accessToken = AccessToken().getAccessToken()
            withContext(Dispatchers.Main) {
                accessToken?.let { token ->
                    sendNotificationWithToken(token)
                } ?: run {
                    Toast.makeText(context, "Failed to get access token", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun sendNotificationWithToken(token: String) {
        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        val mainObj = JSONObject()

        try {
            val messageObj = JSONObject()
            val notificationObj = JSONObject()

            notificationObj.put("title", title)
            notificationObj.put("body", body)

            messageObj.put("topic", topic)
            messageObj.put("notification", notificationObj)

            mainObj.put("message", messageObj)

            val request = object : JsonObjectRequest(Request.Method.POST, postUrl, mainObj,
                { response ->
                    Toast.makeText(context, "Notification sent: ${response.toString()}", Toast.LENGTH_LONG).show()
                },
                { volleyError ->
                    val statusCode = volleyError.networkResponse?.statusCode ?: -1
                    val message = volleyError.message ?: "Unknown error"
                    Toast.makeText(context, "Error sending notification: $message (Code: $statusCode)", Toast.LENGTH_LONG).show()
                }) {
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["Authorization"] = "Bearer $token"
                    return headers
                }
            }

            requestQueue.add(request)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}


///IF FCM TOKEN REQUIRED
//class FcmNotificationSender(
//    private val userFcmToken: String,
//    private val title: String,
//    private val body: String,
//    private val context: Context
//) {
//
//    private val postUrl = "https://fcm.googleapis.com/v1/projects/offcampusupdatesapp/messages:send"
//
//    fun sendNotification() {
//        CoroutineScope(Dispatchers.IO).launch {
//            val accessToken = AccessToken().getAccessToken()
//            withContext(Dispatchers.Main) {
//                accessToken?.let { token ->
//                    sendNotificationWithToken(token)
//                } ?: run {
//                    println("Failed to get access token")
//                }
//            }
//        }
//    }
//
//    private fun sendNotificationWithToken(token: String) {
//        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
//        val mainObj = JSONObject()
//
//        try {
//            val messageObj = JSONObject()
//            val notificationObj = JSONObject()
//
//            notificationObj.put("title", title)
//            notificationObj.put("body", body)
//
//            messageObj.put("token", userFcmToken)
//            messageObj.put("notification", notificationObj)
//
//            mainObj.put("message", messageObj)
//
//            val request = object : JsonObjectRequest(Request.Method.POST, postUrl, mainObj,
//                { response ->
//                    Log.d("Response", response.toString())
//                    Toast.makeText(context, "Notification sent: ${response.toString()}", Toast.LENGTH_LONG).show()
//                },
//                { volleyError ->
//                    val statusCode = volleyError.networkResponse?.statusCode ?: -1
//                    val message = volleyError.message ?: "Unknown error"
//                    Log.d("Response", "Error sending notification: $message (Code: $statusCode)")
//                    Toast.makeText(context, "Error sending notification: $message (Code: $statusCode)", Toast.LENGTH_LONG).show()
//                }) {
//                override fun getHeaders(): Map<String, String> {
//                    val headers = HashMap<String, String>()
//                    headers["content-Type"] = "application/json"
//                    headers["authorization"] = "Bearer $token"
//                    return headers
//                }
//            }
//
//            requestQueue.add(request)
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//}
