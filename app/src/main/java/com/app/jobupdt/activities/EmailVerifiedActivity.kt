package com.app.jobupdt.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.jobupdt.R
import com.app.jobupdt.databinding.ActivityEmailVerifiedBinding
import com.app.jobupdt.utills.Constants
import com.app.jobupdt.utills.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EmailVerifiedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailVerifiedBinding
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 5000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailVerifiedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        init()
    }

    private fun init() {
        val isEmailVerified = preferenceManager.getBoolean(Constants.KEY_IS_EMAIL_VERIFIED)
        val email = preferenceManager.getString(Constants.KEY_USER_EMAIL)

        val marqueeTextView = binding.marqueeTextView
        val marqueeAnimation = AnimationUtils.loadAnimation(this@EmailVerifiedActivity, R.anim.marquee_animation)
        marqueeTextView.startAnimation(marqueeAnimation)

        val window = window

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.awui_red1, theme)

        if (isEmailVerified) {
            navigateToHome()
        } else {
            binding.email = email
            val currentUser = auth.currentUser
            if (email != null && currentUser != null) {
                currentUser.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Please Verify Your Email To Proceed. Check Your Spam Folder also.", Toast.LENGTH_SHORT).show()
                            binding.textBannerEmail.text = "We just sent you an email to verify your email $email"
                        } else {
                            Toast.makeText(this, "Failed to send verification email. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            binding.changeEmail.setOnClickListener{
                if (preferenceManager.getString(Constants.KEY_DEST_FOR_VERIFICATION)=="SIGNUP"){
                    val intent = Intent(this, SignUpActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.slide_in_left,  android.R.anim.slide_out_right)
                    startActivity(intent, options.toBundle())
                    preferenceManager.clear()
                    finish()
                }else{
                    val intent = Intent(this, LoginActivity::class.java)
                    val options = ActivityOptions.makeCustomAnimation(this, android.R.anim.slide_in_left,  android.R.anim.slide_out_right)
                    startActivity(intent, options.toBundle())
                    preferenceManager.clear()
                    finish()
                }
            }
        }

        binding.sendEmailagain.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                currentUser.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Please Verify Your Email To Proceed. Check Your Spam Folder also.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to send verification email. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        startEmailVerificationCheck()
    }

    private fun startEmailVerificationCheck() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                checkEmailVerificationStatus()
                handler.postDelayed(this, checkInterval)
            }
        }, checkInterval)
    }

    private fun checkEmailVerificationStatus() {
        val currentUser = auth.currentUser
        currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (currentUser.isEmailVerified) {
                    val userUid = preferenceManager.getString(Constants.KEY_USER_USER_ID)
                    if (userUid != null) {
                        firestore.collection(Constants.KEY_COLLECTION_USER).document(userUid)
                            .update(Constants.KEY_IS_EMAIL_VERIFIED, true)
                            .addOnSuccessListener {
                                preferenceManager.putBoolean(Constants.KEY_IS_EMAIL_VERIFIED, true)
                                navigateToHome()
                            }
                            .addOnFailureListener { exception ->
                                Log.d("EmailVerifiedActivity",exception.toString())
//                                Toast.makeText(this, "Failed to reload user. Please try again.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            } else {
                Toast.makeText(this, "Failed to reload user. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this@EmailVerifiedActivity, HomeActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
