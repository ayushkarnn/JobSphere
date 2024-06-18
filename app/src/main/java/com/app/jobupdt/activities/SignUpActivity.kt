package com.app.jobupdt.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.jobupdt.R
import com.app.jobupdt.databinding.ActivitySignUpBinding
import com.app.jobupdt.utills.CommonMethods.Companion.closeKeyboard
import com.app.jobupdt.utills.Constants
import com.app.jobupdt.utills.PreferenceManager
import com.app.jobupdt.utills.Res
import com.app.jobupdt.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var authviewModel: AuthViewModel


    private lateinit var onBackPressedCallback: OnBackPressedCallback;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _init()


        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                val options = ActivityOptions.makeCustomAnimation(this@SignUpActivity, R.anim.slide_in, R.anim.slide_out)
                startActivity(intent, options.toBundle())
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }

    private fun _init(){
        preferenceManager = PreferenceManager(this)
        authviewModel = ViewModelProvider(this)[AuthViewModel::class.java]


        binding.alreadyAccount.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in, R.anim.slide_out)
            startActivity(intent, options.toBundle())
        }

        binding.buttonSignUp.setOnClickListener {
            if (isValidSignUpDetails()) {
                signUp()
            }
        }
    }


    private fun signUp() {
        closeKeyboard(this)
        val name = binding.inputName.text.toString()
        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val fcmtoken = fcmToken()
                when (val result = authviewModel.signUp(name, email, password, fcmtoken)) {
                    is Res.Success -> {
//                        loading(false)
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                        preferenceManager.putString(Constants.KEY_USER_EMAIL,email)
                        preferenceManager.putString(
                            Constants.KEY_USER_USER_ID,
                            FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        )
                        preferenceManager.putBoolean(Constants.KEY_IS_EMAIL_VERIFIED,false)
                        preferenceManager.putString(Constants.KEY_USER_NAME, name)
                        preferenceManager.putString(Constants.KEY_DEST_FOR_VERIFICATION,"SIGNUP");
                        showToast("Successfully Registered")
                        startActivity(Intent(this@SignUpActivity, EmailVerifiedActivity::class.java))
                        finish()
                    }
                    is Res.Error -> {
//                        loading(false)
                        showToast(result.message)
                    }
                }
            } catch (e: FCMTokenRetrievalException) {
//                loading(false)
//                showToast("Error in retrieving FCM token: ${e.message}")
            }
        }

    }


    private fun isValidSignUpDetails(): Boolean {
        return when {
            binding.inputName.text.toString().trim().isEmpty() -> {
                showToast("Enter Name")
                false
            }
            binding.inputEmail.text.toString().trim().isEmpty() -> {
                showToast("Enter Email")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches() -> {
                showToast("Enter Valid Email")
                false
            }
            binding.inputPassword.text.toString().trim().isEmpty() -> {
                showToast("Enter Password")
                false
            }
            binding.inputConfirmPassword.text.toString().trim().isEmpty() -> {
                showToast("Confirm your Password")
                false
            }
            binding.inputPassword.text.toString() != binding.inputConfirmPassword.text.toString() -> {
                showToast("Password And Confirm Password Must Be Same")
                false
            }
            else -> true
        }
    }

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private suspend fun fcmToken(): String {
        return suspendCoroutine { continuation ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (!token.isNullOrEmpty()) {
                        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token)
                        continuation.resume(token)
                    } else {
                        continuation.resumeWithException(FCMTokenRetrievalException("FCM token is null or empty"))
                    }
                } else {
                    continuation.resumeWithException(FCMTokenRetrievalException("Failed to retrieve FCM token"))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onBackPressedCallback.remove()
    }

}

class FCMTokenRetrievalException(message: String) : Exception(message)
