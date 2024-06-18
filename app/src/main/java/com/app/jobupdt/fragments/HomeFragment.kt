package com.app.jobupdt.fragments

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.jobupdt.R
import com.app.jobupdt.activities.HomeActivity
import com.app.jobupdt.adapter.JobAdapter
import com.app.jobupdt.databinding.FragmentHomeBinding
import com.app.jobupdt.datamodels.Job
import com.app.jobupdt.fcm.FcmNotificationSender
import com.app.jobupdt.utills.CommonMethods.Companion.getCurrentDate
import com.app.jobupdt.utills.Constants
import com.app.jobupdt.utills.PreferenceManager
import com.app.jobupdt.utills.Res
import com.app.jobupdt.viewmodel.JobViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

class HomeFragment : Fragment(), JobAdapter.OnClickListener {


    private lateinit var binding: FragmentHomeBinding
    private lateinit var jobAdapter: JobAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var jobViewModel: JobViewModel

    private lateinit var jobList: MutableList<Job>
    private var originalJobList = mutableListOf<Job>()


    private val TAG = "HomeFragmentDebug"

    private val messaging = FirebaseMessaging.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkNotificationPermissions(requireContext())
        preferenceManager = PreferenceManager(requireContext())
        jobViewModel = ViewModelProvider(this)[JobViewModel::class.java]
        jobList = mutableListOf()
        jobAdapter = JobAdapter(jobList, this@HomeFragment)
        binding.jobRecyclerView.apply {
            adapter = jobAdapter
            layoutManager = LinearLayoutManager(context)
        }




        if (preferenceManager.getString(Constants.KEY_USER_ROLE) == "Admin") {
            binding.addData.visibility = View.VISIBLE
            binding.sendNotif.visibility = View.VISIBLE
            binding.addData.setOnClickListener {
                openDialog()
            }
            binding.sendNotif.setOnClickListener {
                openNotificationDialog()
            }
        }



        binding.searchEt.clearFocus()
        (activity as? HomeActivity)?.addScrollListenerToRecyclerView(binding.jobRecyclerView)

        fetchData()

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            showDialog()
        }

        search()


    }

    private fun search() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }


    private fun filter(sequence: CharSequence?) {
        val temp = mutableListOf<Job>()

        if (!sequence.isNullOrBlank()) {
            val searchText = sequence.toString().lowercase(Locale.ROOT)

            for (job in originalJobList) {
                if (job.companyName.lowercase(Locale.ROOT).contains(searchText) ||
                    job.jobTitle.lowercase(Locale.ROOT).contains(searchText) ||
                    job.eligibility.lowercase(Locale.ROOT).contains(searchText) ||
                    job.salaryRange.lowercase(Locale.ROOT).contains(searchText)
                ) {
                    temp.add(job)
                }
            }
        } else {
            temp.addAll(originalJobList)
        }

        jobList.clear()
        jobList.addAll(temp)
        jobAdapter.submitList(jobList)
    }




    private fun openDialog() {
        val dialogView = layoutInflater.inflate(R.layout.add_job_data_dialog, null)
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setTitle("Add Job Data")
        val alertDialog = dialogBuilder.create()
        alertDialog.show()

        val companyNameEditText = dialogView.findViewById<EditText>(R.id.textViewCompanyName)
        val jobTitleEditText = dialogView.findViewById<EditText>(R.id.textViewJobTitle)
        val jobLocationEditText = dialogView.findViewById<EditText>(R.id.textViewJobLocation)
        val datePostedEditText = dialogView.findViewById<EditText>(R.id.textViewDatePosted)
        val applyLinkEditText = dialogView.findViewById<EditText>(R.id.textViewApplyLink)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.textViewDescription)
        val eligibilityEditText = dialogView.findViewById<EditText>(R.id.textViewEligibility)
        val salaryRangeEditText = dialogView.findViewById<EditText>(R.id.textViewSalaryRange)
        val acceptingResponseEditText = dialogView.findViewById<EditText>(R.id.textViewAcceptingResponse)

        val submitButton = dialogView.findViewById<Button>(R.id.submitButton)
        datePostedEditText.setText(getCurrentDate())

        datePostedEditText.isEnabled = false
        datePostedEditText.isFocusable = false
        datePostedEditText.isFocusableInTouchMode = false
        submitButton.setOnClickListener {
            val companyName = companyNameEditText.text.toString()
            val jobTitle = jobTitleEditText.text.toString()
            val jobLocation = jobLocationEditText.text.toString()
            val datePosted = datePostedEditText.text.toString()
            val applyLink = applyLinkEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val eligibility = eligibilityEditText.text.toString()
            val salaryRange = salaryRangeEditText.text.toString()
            val acceptingResponse = acceptingResponseEditText.text.toString()

            val randomUUID = UUID.randomUUID().toString()

            val job = Job(
                uUIDJob = randomUUID,
                companyName = companyName,
                jobTitle = jobTitle,
                jobLocation = jobLocation,
                datePosted = datePosted,
                applyLink = applyLink,
                description = description,
                eligibility = eligibility,
                salaryRange = salaryRange,
                companyImage = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTVYS7KEXYFAwqdRCW81e4DSR_nSLYSFStx1Q&s",
                acceptingResponse = acceptingResponse.isNotEmpty()
            )

            lifecycleScope.launch(Dispatchers.IO) {
                jobViewModel.uploadData(job, randomUUID)
                val title = "$companyName is hiring for $jobTitle! Apply Fast 💼"
                val body = "Job Location: $jobLocation 📍\nEligibility: $eligibility ✅"

                val fcmNotificationSender = FcmNotificationSender(title, body, requireContext(),"jobs_update")
                fcmNotificationSender.sendNotification()
            }
            alertDialog.dismiss()
        }
    }


    private fun openNotificationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.notification_sender, null)
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setTitle("Send Notification To Applied Users")
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
        var selectedUuid =""
        val companyNameEditText = dialogView.findViewById<EditText>(R.id.textViewCompanyName2)
        val jobTitleEditText = dialogView.findViewById<EditText>(R.id.textViewJobTitle2)
        val uuidSpinner: Spinner = dialogView.findViewById(R.id.uidSpinner)
        val submitButton = dialogView.findViewById<Button>(R.id.submitButton2)

        lifecycleScope.launch {
            val jobs = jobViewModel.fetchJobs()
            Log.d(TAG, "Jobs: $jobs")
            val jobNames = jobs.map { it.second }
            val uuids = jobs.map { it.first }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, jobNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            uuidSpinner.adapter = adapter

            uuidSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedUuid = uuids[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle nothing selected if needed
                }
            }
        }

        submitButton.setOnClickListener {
            val companyName = companyNameEditText.text.toString()
            val jobTitle = jobTitleEditText.text.toString()
            val fcmNotificationSender = FcmNotificationSender(companyName, jobTitle, requireContext(),selectedUuid)
            fcmNotificationSender.sendNotification()
            alertDialog.dismiss()
        }
    }


    private fun fetchData() {
        lifecycleScope.launch(Dispatchers.Main) {
            val result = jobViewModel.fetchData()
            if (result is Res.Success) {
                Log.d(TAG, "Fetch data success: ${result.data}")
                jobList.clear()
                jobList.addAll(result.data)
                originalJobList.addAll(result.data)
                jobAdapter.submitList(result.data)
                jobAdapter.notifyDataSetChanged()
                binding.progessHome.visibility = View.GONE
            } else if (result is Res.Error) {
                Log.e(TAG, "Fetch data error: ${result.message}")
                binding.progessHome.visibility = View.GONE
            }
        }
    }

    override fun onSaveClick(position: Int) {
        binding.progessHome.visibility = View.VISIBLE
        val job = jobAdapter.getItem(position)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = preferenceManager.getString(Constants.KEY_USER_USER_ID)
                if (userId != null) {
                    val result = jobViewModel.saveJob(userId, job.uUIDJob)
                    activity?.runOnUiThread {
                        if (result is Res.Success) {
                            Toast.makeText(requireContext(), "Job ${job.jobTitle} - ${job.companyName} is saved", Toast.LENGTH_SHORT).show()
                            jobAdapter.notifyItemChanged(position)
                            binding.progessHome.visibility = View.GONE
                        } else if (result is Res.Error) {
                            Toast.makeText(requireContext(), "Error: ${result.message}", Toast.LENGTH_SHORT).show()
                            binding.progessHome.visibility = View.GONE
                        }
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Failed to save job: User ID is null", Toast.LENGTH_SHORT).show()
                    }
                    binding.progessHome.visibility = View.GONE
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error Occurred While Saving Job ${job.jobTitle}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                binding.progessHome.visibility = View.GONE
            }
        }
    }

    private fun showDialog() {
        val dialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.dialog_layout_logout, null)
        dialog.setContentView(view)

        val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
        val dialogDescription = view.findViewById<TextView>(R.id.dialogDesc)
        dialogTitle.text = "Exit?"
        dialogDescription.text = "Are you sure you want to exit?"

        view.findViewById<ImageView>(R.id.dialogImgview).setImageResource(R.drawable.ic_close_circle)

        view.findViewById<View>(R.id.clickedYes).setOnClickListener {
            requireActivity().finish()
        }

        view.findViewById<View>(R.id.clickedNo).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.clickedClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    @SuppressLint("NewApi")
    private fun checkNotificationPermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val isEnabled = notificationManager.areNotificationsEnabled()
            if (!isEnabled) {
                showNotificationPermissionDialog(context)
                return false
            }
        } else {
            val areEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            if (!areEnabled) {
                showNotificationPermissionDialog(context)
                return false
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotificationPermissionDialog(context: Context) {
        val dialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.dialog_layout_logout, null)
        dialog.setContentView(view)

        val dialogTitle = view.findViewById<TextView>(R.id.dialogTitle)
        val dialogDescription = view.findViewById<TextView>(R.id.dialogDesc)
        dialogTitle.text = "Enable Permission?"
        dialogDescription.text = "Please Enable Notification Permission To Receive Job Updates Regularly?"

        view.findViewById<View>(R.id.clickedYes).setOnClickListener {
            enableNotification(context)
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.clickedNo).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<View>(R.id.clickedClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun enableNotification(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        context.startActivity(intent)
    }

    override fun onApplyClick(position: Int) {
        try {
            val job = jobAdapter.getItem(position)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job.applyLink))
            startActivity(intent)

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val userId = preferenceManager.getString(Constants.KEY_USER_USER_ID)
                    if (userId != null) {
                        val result = jobViewModel.saveJob(userId, job.uUIDJob,true)
                        activity?.runOnUiThread {
                            if (result is Res.Success) {
                                messaging.subscribeToTopic(job.uUIDJob)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Subscribed to topic ${job.uUIDJob}")
                                    }
                                    .addOnFailureListener{ e->
                                        Log.d(TAG, "Failed to subscribe to topic ${job.uUIDJob}")
                                    }
//                                Toast.makeText(requireContext(), "Job ${job.jobTitle} - ${job.companyName} is applied", Toast.LENGTH_SHORT).show()
                                jobAdapter.notifyItemChanged(position)
                                binding.progessHome.visibility = View.GONE
                            } else if (result is Res.Error) {
//                                Toast.makeText(requireContext(), "Error: ${result.message}", Toast.LENGTH_SHORT).show()
                                binding.progessHome.visibility = View.GONE
                            }
                        }
                    } else {
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Failed to save job: User ID is null", Toast.LENGTH_SHORT).show()
                        }
                        binding.progessHome.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Error Occurred While Saving Job ${job.jobTitle}: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    binding.progessHome.visibility = View.GONE
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error applying for job", e)
            Toast.makeText(requireContext(), "Failed to apply: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onShareClick(position: Int) {
        if (position < jobAdapter.itemCount) {
            val currJob = jobAdapter.getItem(position)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Job Details")
            shareIntent.putExtra(
                Intent.EXTRA_TEXT, "Company Name: ${currJob.companyName}\n" +
                        "Position: ${currJob.jobTitle}\n" +
                        "Eligibility: ${currJob.eligibility}\n" +
                        "Apply link: ${currJob.applyLink}"
            )
            try {
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            } catch (e: Exception) {
                Log.e(TAG, "Error sharing job", e)
                Toast.makeText(requireContext(), "Failed to share job: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Invalid job position", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        binding.searchEt.text.clear()
        originalJobList.clear()
    }

}