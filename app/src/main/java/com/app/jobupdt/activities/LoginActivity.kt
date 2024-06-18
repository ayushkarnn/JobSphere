package com.app.jobupdt.activities

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.app.jobupdt.R
import com.app.jobupdt.databinding.ActivityLoginBinding
import com.app.jobupdt.utills.CommonMethods.Companion.closeKeyboard
import com.app.jobupdt.utills.Constants
import com.app.jobupdt.utills.PreferenceManager
import com.app.jobupdt.utills.Res
import com.app.jobupdt.viewmodel.AuthViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var authviewModel: AuthViewModel

    private lateinit var onBackPressedCallback: OnBackPressedCallback;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialize()
    }

    private fun initialize() {
        preferenceManager = PreferenceManager(this)
        authviewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        binding.edtEmail.requestFocus()
        if (!preferenceManager.getBoolean(Constants.IS_ON_BOARDING_COMPLETE)) {
            preferenceManager.putBoolean(Constants.IS_ON_BOARDING_COMPLETE, true)
        }

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            if (preferenceManager.getBoolean(Constants.KEY_IS_EMAIL_VERIFIED)) {
                navigateToHome()
            } else {
                navigateToEmailVerified()
            }
        }

        setupListeners()
        FirebaseApp.initializeApp(this)


        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showDialog()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setupListeners() {
        binding.tvForgotPassword.setOnClickListener {
            navigateToActivity(ForgotPassword::class.java)
        }

        binding.newUser.setOnClickListener {
            navigateToActivity(SignUpActivity::class.java)
        }

        binding.loginBtn.setOnClickListener {
            if (isValidSignInDetails()) {
                signIn()
            }
        }
    }

    private fun signIn() {
        closeKeyboard(this)
        loading(true)
        val email = binding.edtEmail.text.toString()
        val password = binding.edtPassword.text.toString()

        lifecycleScope.launch(Dispatchers.Main) {
            when (val result = authviewModel.login(email, password)) {
                is Res.Success -> handleLoginSuccess(result.data)
                is Res.Error -> {
                    loading(false)
                    showToast("Error Occurred ${result.message}")
                }
            }
        }
    }

    private fun handleLoginSuccess(userDocument: DocumentSnapshot) {
        with(preferenceManager) {
            putBoolean(Constants.KEY_IS_SIGNED_IN, true)
            putString(Constants.KEY_USER_EMAIL, binding.edtEmail.text.toString())
            putString(Constants.KEY_USER_NAME, userDocument.getString(Constants.KEY_USER_NAME))
            putString(Constants.KEY_USER_USER_ID, userDocument.id)
            putString(Constants.KEY_USER_ROLE, userDocument.getString(Constants.KEY_USER_ROLE))
        }

        val isEmailVerified = userDocument.getBoolean(Constants.KEY_IS_EMAIL_VERIFIED) ?: false
        preferenceManager.putBoolean(Constants.KEY_IS_EMAIL_VERIFIED, isEmailVerified)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { token ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        when (authviewModel.saveFCMToken(userDocument.id, token)) {
                            is Res.Success -> preferenceManager.putString(Constants.KEY_FCM_TOKEN, token)
                            is Res.Error -> { /* Handle error */ }
                        }
                    }
                }
            }
        }

        if (isEmailVerified) navigateToHome() else navigateToEmailVerified()
    }

    private fun navigateToHome() {
        loading(false)
        navigateToActivity(HomeActivity::class.java)
    }

    private fun navigateToEmailVerified() {
        loading(false)
        navigateToActivity(EmailVerifiedActivity::class.java)
        preferenceManager.putString(Constants.KEY_DEST_FOR_VERIFICATION, "LOGIN")
    }

    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in, R.anim.slide_out)
        startActivity(intent, options.toBundle())
        finish()
    }

    private fun isValidSignInDetails(): Boolean {
        return when {
            binding.edtEmail.text.toString().trim().isEmpty() -> {
                showToast("Enter Email")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(binding.edtEmail.text.toString()).matches() -> {
                showToast("Enter Valid Email")
                false
            }
            binding.edtPassword.text.toString().trim().isEmpty() -> {
                showToast("Enter Password")
                false
            }
            else -> true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun loading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
    }


    private fun showDialog() {
        val dialog = BottomSheetDialog(this@LoginActivity, R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.dialog_layout_logout, null)
        dialog.setContentView(view)

        val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
        val dialogDescription = view.findViewById<TextView>(R.id.dialogDesc)
        dialogTitle.text = "Exit?"
        dialogDescription.text = "Are you sure you want to exit?"

        view.findViewById<ImageView>(R.id.dialogImgview).setImageResource(R.drawable.ic_close_circle)

        view.findViewById<View>(R.id.clickedYes).setOnClickListener {
            finishAffinity()
        }

        view.findViewById<View>(R.id.clickedNo).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.clickedClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}
