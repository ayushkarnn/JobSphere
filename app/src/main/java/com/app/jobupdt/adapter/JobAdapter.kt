//package com.app.jobupdt.adapter
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.RecyclerView
//import com.app.jobupdt.R
//import com.app.jobupdt.datamodels.Job
//import com.app.jobupdt.utills.CommonMethods
//import com.app.jobupdt.utills.Constants
//import com.app.jobupdt.utills.PreferenceManager
//import com.app.jobupdt.viewmodel.JobViewModel
//import com.bumptech.glide.Glide
//import com.makeramen.roundedimageview.RoundedImageView
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.concurrent.TimeUnit
//
//class JobAdapter(
//    private var listOfJobs: MutableList<Job>,
//    private val clickListener: OnClickListener
//) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {
//
//    private lateinit var context: Context
//
//    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val companyImageView: RoundedImageView = itemView.findViewById(R.id.companyImageView)
//        val companyName: TextView = itemView.findViewById(R.id.companyNameTv)
//        val jobTitle: TextView = itemView.findViewById(R.id.jobTitleTv)
//        val jobLocation: TextView = itemView.findViewById(R.id.jobLocationTv)
//        val jobEligible: TextView = itemView.findViewById(R.id.batchEligibleTv)
//        val jobSalary: TextView = itemView.findViewById(R.id.salaryRangeTv)
//        val jobIsOpen: TextView = itemView.findViewById(R.id.openingTv)
//        val datePosted: TextView = itemView.findViewById(R.id.datePostedTv)
//        val saveJob: ImageView = itemView.findViewById(R.id.SaveImageView)
//        val shareJob: ImageView = itemView.findViewById(R.id.ShareImageView)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
//        context = parent.context
//        val view = LayoutInflater.from(context).inflate(R.layout.jobs_layout, parent, false)
//        return JobViewHolder(view)
//    }
//
//    override fun getItemCount(): Int {
//        return listOfJobs.size
//    }
//
//    fun getItem(position: Int): Job {
//        return listOfJobs[position]
//    }
//
//    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
//        val currJob = getItem(position)
//
//        holder.companyName.text = currJob.companyName
//        holder.jobTitle.text = currJob.jobTitle
//        holder.jobLocation.text = currJob.jobLocation
//        holder.jobEligible.text = "Eligiblity: ${currJob.eligibility}"
//        holder.jobSalary.text = "Compensation/Stipend: ${currJob.salaryRange}"
//
//        val _opening = currJob.acceptingResponse
//        if (_opening) {
//            holder.jobIsOpen.text = "Accepting Responses"
//            holder.jobIsOpen.setTextColor(context.getColor(R.color.awui_green1))
//        } else {
//            holder.jobIsOpen.text = "Not Accepting Responses"
//            holder.jobIsOpen.setTextColor(context.getColor(R.color.awui_red1))
//        }
//
//        GlobalScope.launch {
//            val isSaved = isJobSaved(currJob.uUIDJob)
//            withContext(Dispatchers.Main) {
//                if (isSaved) {
//                    holder.saveJob.setImageResource(R.drawable.ic_saved_1)
//                } else {
//                    holder.saveJob.setImageResource(R.drawable.bookmark)
//                }
//            }
//        }
//
//        holder.datePosted.text = "Posted " + getRelativeDate(currJob.datePosted)
//
//        Glide.with(context)
//            .load(currJob.companyImage)
//            .into(holder.companyImageView)
//
//        holder.companyImageView.setOnClickListener {
//            clickListener.onApplyClick(position)
//        }
//
//        holder.shareJob.setOnClickListener {
//            clickListener.onShareClick(position)
//
//        }
//        holder.saveJob.setOnClickListener {
//            clickListener.onSaveClick(position)
//        }
//    }
//
//    private val jobDateComparator = Comparator<Job> { job1, job2 ->
//        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
//        val date1 = dateFormat.parse(job1.datePosted)
//        val date2 = dateFormat.parse(job2.datePosted)
//        date2.compareTo(date1)
//    }
//
//    fun submitList(newList: List<Job>) {
//        val sortedList = newList.sortedWith(jobDateComparator)
//        val diffCallback = JobDiffCallback(listOfJobs, sortedList)
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//        listOfJobs.clear()
//        listOfJobs.addAll(sortedList)
//        diffResult.dispatchUpdatesTo(this)
//    }
//
//
//    private class JobDiffCallback(
//        private val oldList: List<Job>,
//        private val newList: List<Job>
//    ) : DiffUtil.Callback() {
//        override fun getOldListSize(): Int = oldList.size
//        override fun getNewListSize(): Int = newList.size
//        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//            return oldList[oldItemPosition] == newList[newItemPosition]
//        }
//
//        @SuppressLint("DiffUtilEquals")
//        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//            return oldList[oldItemPosition] == newList[newItemPosition]
//        }
//    }
//
//    private fun getRelativeDate(postedDate: String): String {
//        val currentDate = CommonMethods.getCurrentDate()
//        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
//        val datePosted = dateFormat.parse(postedDate)
//        val currentDateFormatted = dateFormat.parse(currentDate)
//
//        val diff = currentDateFormatted.time - datePosted.time
//        val daysDifference = TimeUnit.MILLISECONDS.toDays(diff)
//
//        return when {
//            daysDifference == 0L -> "Today"
//            daysDifference == 1L -> "Yesterday"
//            daysDifference in 2L..6L -> "$daysDifference days ago"
//            daysDifference in 7L..13L -> "A week ago"
//            daysDifference in 30L..31L -> "A month ago"
//            else -> "More than a month ago"
//        }
//    }
//
//    interface OnClickListener {
//        fun onSaveClick(position: Int)
//        fun onApplyClick(position: Int)
//        fun onShareClick(position: Int)
//    }
//
//    private suspend fun isJobSaved(jobId: String): Boolean {
//        val preferenceManager = PreferenceManager(context)
//
//        val viewModel = JobViewModel()
//
//        val userUid = preferenceManager.getString(Constants.KEY_USER_USER_ID)!!
//        return viewModel.isJobSaved(userUid, jobId)
//    }
//
//}

package com.app.jobupdt.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.jobupdt.R
import com.app.jobupdt.datamodels.Job
import com.app.jobupdt.utills.CommonMethods
import com.app.jobupdt.utills.Constants
import com.app.jobupdt.utills.PreferenceManager
import com.app.jobupdt.viewmodel.JobViewModel
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class JobAdapter(
    private var listOfJobs: MutableList<Job>,
    private val clickListener: OnClickListener
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    private lateinit var context: Context

    class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val companyImageView: RoundedImageView = itemView.findViewById(R.id.companyImageView)
        val companyName: TextView = itemView.findViewById(R.id.companyNameTv)
        val jobTitle: TextView = itemView.findViewById(R.id.jobTitleTv)
        val jobLocation: TextView = itemView.findViewById(R.id.jobLocationTv)
        val jobEligible: TextView = itemView.findViewById(R.id.batchEligibleTv)
        val jobSalary: TextView = itemView.findViewById(R.id.salaryRangeTv)
        val jobIsOpen: TextView = itemView.findViewById(R.id.openingTv)
        val datePosted: TextView = itemView.findViewById(R.id.datePostedTv)
        val saveJob: ImageView = itemView.findViewById(R.id.SaveImageView)
        val shareJob: ImageView = itemView.findViewById(R.id.ShareImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.jobs_layout, parent, false)
        return JobViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfJobs.size
    }

    fun getItem(position: Int): Job {
        return if (position in 0 until listOfJobs.size) {
            listOfJobs[position]
        } else {
            throw IndexOutOfBoundsException("Position $position is out of bounds for the list of size ${listOfJobs.size}")
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val currJob = getItem(position)

        holder.companyName.text = currJob.companyName
        holder.jobTitle.text = currJob.jobTitle
        holder.jobLocation.text = currJob.jobLocation
        holder.jobEligible.text = "Eligiblity: ${currJob.eligibility}"
        holder.jobSalary.text = "Compensation/Stipend: ${currJob.salaryRange}"

        val _opening = currJob.acceptingResponse
        if (_opening) {
            holder.jobIsOpen.text = "Accepting Responses"
            holder.jobIsOpen.setTextColor(context.getColor(R.color.awui_green1))
        } else {
            holder.jobIsOpen.text = "Not Accepting Responses"
            holder.jobIsOpen.setTextColor(context.getColor(R.color.awui_red1))
        }

        GlobalScope.launch {
            val isSaved = isJobSaved(currJob.uUIDJob)
            withContext(Dispatchers.Main) {
                if (isSaved) {
                    holder.saveJob.setImageResource(R.drawable.ic_saved_1)
                } else {
                    holder.saveJob.setImageResource(R.drawable.bookmark)
                }
            }
        }

        holder.datePosted.text = "Posted " + getRelativeDate(currJob.datePosted)

        Glide.with(context)
            .load(currJob.companyImage)
            .into(holder.companyImageView)

        holder.companyImageView.setOnClickListener {
            clickListener.onApplyClick(position)
        }

        holder.shareJob.setOnClickListener {
            clickListener.onShareClick(position)

        }
        holder.saveJob.setOnClickListener {
            clickListener.onSaveClick(position)
        }
    }

    private val jobDateComparator = Comparator<Job> { job1, job2 ->
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date1 = dateFormat.parse(job1.datePosted)
        val date2 = dateFormat.parse(job2.datePosted)

        when {
            job1.acceptingResponse && !job2.acceptingResponse -> -1
            !job1.acceptingResponse && job2.acceptingResponse -> 1
            else -> date2.compareTo(date1)
        }
    }




    fun submitList(newList: List<Job>) {
        val sortedList = newList.sortedWith(jobDateComparator)
        val diffCallback = JobDiffCallback(listOfJobs, sortedList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        listOfJobs.clear()
        listOfJobs.addAll(sortedList)
        diffResult.dispatchUpdatesTo(this)
        notifyDataSetChanged()
    }




    private class JobDiffCallback(
        private val oldList: List<Job>,
        private val newList: List<Job>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    private fun getRelativeDate(postedDate: String): String {
        val currentDate = CommonMethods.getCurrentDate()
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val datePosted = dateFormat.parse(postedDate)
        val currentDateFormatted = dateFormat.parse(currentDate)

        val diff = currentDateFormatted.time - datePosted.time
        val daysDifference = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            daysDifference == 0L -> "Today"
            daysDifference == 1L -> "Yesterday"
            daysDifference in 2L..6L -> "$daysDifference days ago"
            daysDifference in 7L..13L -> "A week ago"
            daysDifference in 30L..31L -> "A month ago"
            else -> "More than a month ago"
        }
    }

    interface OnClickListener {
        fun onSaveClick(position: Int)
        fun onApplyClick(position: Int)
        fun onShareClick(position: Int)
    }

    private suspend fun isJobSaved(jobId: String): Boolean {
        val preferenceManager = PreferenceManager(context)

        val viewModel = JobViewModel()

        val userUid = preferenceManager.getString(Constants.KEY_USER_USER_ID)!!
        return viewModel.isJobSaved(userUid, jobId)
    }
}
