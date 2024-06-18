package com.app.jobupdt.viewmodel

import androidx.lifecycle.ViewModel
import com.app.jobupdt.datamodels.User
import com.app.jobupdt.utills.Constants
import com.app.jobupdt.utills.Res
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private val messaging = FirebaseMessaging.getInstance()


    private val TOPIC = "jobs_update"

    suspend fun login(email: String, password: String): Res<DocumentSnapshot> {
        return try {
            withContext(Dispatchers.IO) {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()

                val userDocument = database.collection(Constants.KEY_COLLECTION_USER)
                    .document(authResult.user!!.uid)
                    .get()
                    .await()
                messaging.subscribeToTopic(TOPIC).await()

                Res.Success(userDocument)
            }
        } catch (e: Exception) {
            Res.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        fcmtoken: String
    ): Res<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("User ID is null")

            val user = User(
                name = name,
                emailAddress = email,
                isEmailVerified = false,
                role = "user",
                fcmToken = fcmtoken,
                appliedJobs = listOf(),
                savedJobs = listOf()
            )


            database.collection(Constants.KEY_COLLECTION_USER)
                .document(uid)
                .set(user)
                .await()
            messaging.subscribeToTopic(TOPIC).await()

            Res.Success(Unit)
        } catch (e: Exception) {
            Res.Error(e.message ?: "An error occurred")
        }
    }

    suspend fun saveFCMToken(uid: String, token: String): Res<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                database.collection(Constants.KEY_COLLECTION_USER)
                    .document(uid)
                    .update(Constants.KEY_FCM_TOKEN, token)
                    .await()

                Res.Success(Unit)
            }
        } catch (e: Exception) {
            Res.Error(e.message ?: "An error occurred")
        }
    }
}
