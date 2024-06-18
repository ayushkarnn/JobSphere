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
import com.app.jobupdt.adapter.SavedJobsAdapter
import com.app.jobupdt.databinding.FragmentSavedBinding
import com.app.jobupdt.utills.CommonMethods.Companion.showToast
import com.app.jobupdt.utills.Constants
import com.app.jobupdt.utills.PreferenceManager
import com.app.jobupdt.utills.Res
import com.app.jobupdt.viewmodel.JobViewModel
import kotlinx.coroutines.launch

class SavedFragment : Fragment(), SavedJobsAdapter.OnUnSaveClickListener {

    private lateinit var binding: FragmentSavedBinding
    private lateinit var jobViewModel: JobViewModel
    private lateinit var savedJobsAdapter: SavedJobsAdapter

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {

        }

        preferenceManager = PreferenceManager(requireContext())

        jobViewModel = ViewModelProvider(this).get(JobViewModel::class.java)

        binding.SavedJobRecyclerView.layoutManager = LinearLayoutManager(context)

        val userUid = getUserUid()
        (activity as? HomeActivity)?.addScrollListenerToRecyclerView(binding.SavedJobRecyclerView)

        loadSavedJobs(userUid)
    }

    private fun loadSavedJobs(userUid: String) {
        lifecycleScope.launch {
            val result = jobViewModel.fetchSavedJobs(userUid)
            if (result is Res.Success) {
                if (result.data.isEmpty()) {
                    showToast(requireContext(), "No saved jobs found")
                    binding.noDataFound.visibility = View.VISIBLE
                } else {
                    savedJobsAdapter = SavedJobsAdapter(result.data, this@SavedFragment)
                    binding.SavedJobRecyclerView.adapter = savedJobsAdapter
                }
                binding.progessSaved.visibility = View.GONE
            } else {
                binding.progessSaved.visibility = View.GONE
            }
        }
    }

    override fun onUnSaveClick(position: Int) {
        binding.progessSaved.visibility = View.VISIBLE
        val userUid = getUserUid()
        val savedJob = savedJobsAdapter.getItem(position)
        val jobId = savedJob.first.uUIDJob

        viewLifecycleOwner.lifecycleScope.launch {
            val result = jobViewModel.unsaveJob(userUid, jobId)
            if (result is Res.Success) {
                savedJobsAdapter.notifyItemRemoved(position)
                loadSavedJobs(userUid)
            }
            binding.progessSaved.visibility = View.GONE
        }
    }

    private fun getUserUid(): String {
        return preferenceManager.getString(Constants.KEY_USER_USER_ID)!!
    }

    override fun onStop() {
        super.onStop()
        binding.noDataFound.visibility = View.GONE
    }
}
