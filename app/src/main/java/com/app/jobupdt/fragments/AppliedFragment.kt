package com.app.jobupdt.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.jobupdt.activities.HomeActivity
import com.app.jobupdt.adapter.AppliedJobsAdapter
import com.app.jobupdt.databinding.FragmentAppliedBinding
import com.app.jobupdt.utills.CommonMethods.Companion.showToast
import com.app.jobupdt.utills.Constants
import com.app.jobupdt.utills.PreferenceManager
import com.app.jobupdt.utills.Res
import com.app.jobupdt.viewmodel.JobViewModel
import kotlinx.coroutines.launch


class AppliedFragment : Fragment() {

    private lateinit var binding: FragmentAppliedBinding
    private lateinit var jobViewModel: JobViewModel
    private lateinit var appliedJobsAdapter: AppliedJobsAdapter

    private lateinit var preferenceManager: PreferenceManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAppliedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {

//            showDialog()

        }

        preferenceManager = PreferenceManager(requireContext())

        jobViewModel = ViewModelProvider(this).get(JobViewModel::class.java)

        binding.AppliedJobRecyclerView.layoutManager = LinearLayoutManager(context)

        val userUid = getUserUid()
        (activity as? HomeActivity)?.addScrollListenerToRecyclerView(binding.AppliedJobRecyclerView)

        loadSavedJobs(userUid)
    }


    private fun loadSavedJobs(userUid: String) {
        lifecycleScope.launch {
            val result = jobViewModel.fetchSavedJobs(userUid,true)
            if (result is Res.Success) {
                if (result.data.isEmpty()) {
                    showToast(requireContext(), "No Applied Jobs Found")
                    binding.noDataFoundApplied.visibility = View.VISIBLE
                } else {
                    appliedJobsAdapter = AppliedJobsAdapter(result.data)
                    binding.AppliedJobRecyclerView.adapter = appliedJobsAdapter
                }
                binding.progessSaved.visibility = View.GONE
            } else {
                binding.progessSaved.visibility = View.GONE
            }
        }
    }


    private fun getUserUid(): String {
        return preferenceManager.getString(Constants.KEY_USER_USER_ID)!!
    }

    override fun onStop() {
        super.onStop()
        binding.noDataFoundApplied.visibility=View.GONE
    }

}