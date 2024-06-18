package com.app.jobupdt.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.app.jobupdt.databinding.ActivityForgotPasswordBinding
import com.app.jobupdt.utills.CommonMethods.Companion.closeKeyboard
import com.app.jobupdt.utills.CommonMethods.Companion.showToast
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.SubmitForgot.setOnClickListener {
            closeKeyboard(this@ForgotPassword)
            if (isValidDetails()) {
                val email = binding.emailEtForgot.text.toString()
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        showToast(this,"Email Sent Check Your Mail $email")
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        showToast(this,it.exception?.message.toString())
                    }
                }
            }
        }

        binding.backBTN.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


    }

    private fun isValidDetails(): Boolean {
        return when {
            binding.emailEtForgot.text.toString().trim().isEmpty() -> {
                showToast(this,"Enter Email")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(binding.emailEtForgot.text.toString()).matches() -> {
                showToast(this,"Enter Valid Email")
                false
            }
            else -> true
        }
    }



}
