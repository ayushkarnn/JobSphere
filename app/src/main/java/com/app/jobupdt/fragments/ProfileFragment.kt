package com.app.jobupdt.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import com.app.jobupdt.R
import com.app.jobupdt.activities.LoginActivity
import com.app.jobupdt.databinding.FragmentProfileBinding
import com.app.jobupdt.utills.Constants
import com.app.jobupdt.utills.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())

        val name = preferenceManager.getString(Constants.KEY_USER_NAME)
        val email = preferenceManager.getString(Constants.KEY_USER_EMAIL)
//        val uuid = preferenceManager.getString(Constants.KEY_USER_USER_ID)

        binding.nameTextView.text = name
        binding.emailTextView.text = email

        binding.importantInfoTextView.isSelected = true

        binding.logoutButton.setOnClickListener {
            showExitDialog()

        }

        binding.reportBugButton.setOnClickListener{
            showReportDialog()
        }

        binding.supportBtn.setOnClickListener {
            shareAppInfo()
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {

//            showDialog()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showExitDialog() {
        val dialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.dialog_layout_logout, null)
        dialog.setContentView(view)

        view.findViewById<View>(R.id.clickedYes).setOnClickListener {
            preferenceManager.clear()
            dialog.dismiss()
            val intent = Intent(requireContext(), LoginActivity::class.java);
            startActivity(intent)
            requireActivity().finish()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.clickedNo).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.clickedClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun shareAppInfo() {
        val shareText = """
            🚀 Get all off campus job updates with realtime notification 🚀
            📅 Never miss any jobs
            🔄 Regular updates 
            💾 Save jobs 
            📋 See applied jobs 
            📲 Install the app now: https://www.example.com
        """.trimIndent()

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun showReportDialog() {
        val dialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.report_dialog, null)
        dialog.setContentView(view)

        val name = preferenceManager.getString(Constants.KEY_USER_NAME)
        val email = preferenceManager.getString(Constants.KEY_USER_EMAIL)
        view.findViewById<TextView>(R.id.nameReport).text = name
        view.findViewById<TextView>(R.id.EmailReport).text = email
        val submit = view.findViewById<LinearLayout>(R.id.submitReport)
        val messageReport = view.findViewById<EditText>(R.id.messageReport)

        if (messageReport.text.toString().isEmpty() || messageReport.text.toString().length <= 10) {
            submit.isEnabled = false
            submit.visibility = View.GONE
        } else {
            submit.isEnabled = true
            submit.visibility = View.VISIBLE
        }

        messageReport.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty() || s.toString().length <= 10) {
                    submit.isEnabled = false
                    submit.visibility = View.GONE
                } else {
                    submit.isEnabled = true
                    submit.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        submit.setOnClickListener {
            val reportData = hashMapOf(
                "timestamp" to FieldValue.serverTimestamp(),
                "email" to email,
                "name" to name,
                "message" to messageReport.text.toString()
            )
            val reportId = UUID.randomUUID().toString()
            val db = FirebaseFirestore.getInstance()
            db.collection("reports")
                .document(reportId)
                .set(reportData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Report submitted successfully!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .addOnFailureListener {e->
                    Toast.makeText(requireContext(), "Failed to submit report: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }

        dialog.setCancelable(true)
        dialog.show()
    }





}

