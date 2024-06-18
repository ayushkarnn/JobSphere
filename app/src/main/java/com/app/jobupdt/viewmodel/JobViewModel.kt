package com.app.jobupdt.viewmodel

import androidx.lifecycle.ViewModel
import com.app.jobupdt.datamodels.Job
import com.app.jobupdt.datamodels.User
import com.app.jobupdt.utills.CommonMethods
import com.app.jobupdt.utills.Constants
import com.app.jobupdt.utills.Res
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class JobViewModel: ViewModel() {
    private val database = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())


    suspend fun uploadData(job: Job, uuid: String): Res<Unit> {
        return try {
            database.collection(Constants.KEY_COLLECTION_JOB)
                .document(uuid)
                .set(job)
                .await()
            Res.Success(Unit)
        } catch (e: Exception) {
            Res.Error(e.toString())
        }
    }

    suspend fun fetchData(): Res<List<Job>> {
        return try {
            val snapshot = database.collection(Constants.KEY_COLLECTION_JOB)
                .get()
                .await()
            val jobList = snapshot.documents.mapNotNull { it.toObject<Job>() }
            Res.Success(jobList)
        } catch (e: Exception) {
            Res.Error(e.toString())
        }
    }

    suspend fun fetchJobs(): List<Pair<String, String>> {
        return try {
            val snapshot = database.collection(Constants.KEY_COLLECTION_JOB).get().await()
            snapshot.documents.map { doc ->
                val uuid = doc.id
                val jobName = doc.getString("companyName") ?: ""
                uuid to jobName
            }
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun saveJob(userUid: String, jobId: String, isForApplied: Boolean = false): Res<Unit> {
        try {
            val currentDate = CommonMethods.getCurrentDate()

            // Check if the job is already saved or applied
            val isSaved = if (isForApplied) {
                isJobSaved(userUid, jobId, true) // Check if job is already applied
            } else {
                isJobSaved(userUid, jobId) // Check if job is already saved
            }

            if (isSaved) {
                return Res.Error("Job is already saved or applied")
            }

            // Retrieve user data
            val userResult = getUser(userUid)
            if (userResult is Res.Success) {
                val user = userResult.data

                // Update savedJobs or appliedJobs list based on isForApplied flag
                val updatedJobs = if (isForApplied) {
                    user.appliedJobs.toMutableList().apply {
                        add("$jobId - $currentDate")
                    }
                } else {
                    user.savedJobs.toMutableList().apply {
                        add("$jobId - $currentDate")
                    }
                }

                // Update user document in Firestore
                database.collection(Constants.KEY_COLLECTION_USER)
                    .document(userUid)
                    .update(if (isForApplied) "appliedJobs" else "savedJobs", updatedJobs)
                    .await()

                return Res.Success(Unit)
            } else {
                return Res.Error("Failed to retrieve user data")
            }
        } catch (e: Exception) {
            return Res.Error(e.toString())
        }
    }




    suspend fun getUser(userUid: String): Res<User> {
        return try {
            val snapshot = database.collection(Constants.KEY_COLLECTION_USER)
                .document(userUid)
                .get()
                .await()
            val user = snapshot.toObject<User>()
            if (user != null) {
                Res.Success(user)
            } else {
                Res.Error("User data not found")
            }
        } catch (e: Exception) {
            Res.Error(e.toString())
        }
    }

    suspend fun isJobSaved(userUid: String, jobId: String, isForApplied: Boolean = false): Boolean {
        return try {
            val userResult = getUser(userUid)
            if (userResult is Res.Success) {
                val user = userResult.data
                if (isForApplied) {
                    val appliedJobs = user.appliedJobs
                    appliedJobs.any { appliedJob -> appliedJob.startsWith(jobId) }
                } else {
                    val savedJobs = user.savedJobs
                    savedJobs.any { savedJob -> savedJob.startsWith(jobId) }
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }




    suspend fun fetchSavedJobs(userUid: String,isForApplied: Boolean = false): Res<List<Pair<Job, String>>> {
        return try {
            val userResult = getUser(userUid)
            if (userResult is Res.Success) {
                val user = userResult.data
                val savedJobs = if (isForApplied) user.appliedJobs else user.savedJobs


                val jobs = mutableListOf<Pair<Job, String>>()
                for (savedJob in savedJobs) {
                    val parts = savedJob.split(" - ")
                    val jobId = parts[0]
                    val savedDate = parts[1]

                    val jobSnapshot = database.collection(Constants.KEY_COLLECTION_JOB)
                        .document(jobId)
                        .get()
                        .await()

                    val job = jobSnapshot.toObject<Job>()
                    if (job != null) {
                        jobs.add(Pair(job, savedDate))
                    }
                }
                jobs.sortByDescending { dateFormat.parse(it.second) }

                Res.Success(jobs)
            } else {
                Res.Error("Failed to retrieve user data")
            }
        } catch (e: Exception) {
            Res.Error(e.toString())
        }
    }

    suspend fun unsaveJob(userUid: String, jobId: String): Res<Unit> {
        return try {
            val userResult = getUser(userUid)
            if (userResult is Res.Success) {
                val user = userResult.data
                val updatedSavedJobs = user.savedJobs.filterNot { it.startsWith(jobId) }

                database.collection(Constants.KEY_COLLECTION_USER)
                    .document(userUid)
                    .update("savedJobs", updatedSavedJobs)
                    .await()

                Res.Success(Unit)
            } else {
                Res.Error("Failed to retrieve user data")
            }
        } catch (e: Exception) {
            Res.Error(e.toString())
        }
    }
}
