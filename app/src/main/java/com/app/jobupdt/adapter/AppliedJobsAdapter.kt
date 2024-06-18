package com.app.jobupdt.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.jobupdt.R
import com.app.jobupdt.datamodels.Job
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView

class AppliedJobsAdapter(
    private var savedJobs: List<Pair<Job, String>>
): RecyclerView.Adapter<AppliedJobsAdapter.AppliedJobsViewHolder>() {

    private lateinit var context: Context

    class AppliedJobsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val savedImageView: RoundedImageView = itemView.findViewById(R.id.savedImageView)
        val applyLinear: LinearLayout = itemView.findViewById(R.id.applyLinear)
        val wholeTitle: TextView = itemView.findViewById(R.id.wholeTitle)
        val appliedOn: TextView = itemView.findViewById(R.id.appliedOn)
        val unSaveImageView:LinearLayout = itemView.findViewById(R.id.unSaveImageView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppliedJobsViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.saved_jobs_layout, parent, false)
        return AppliedJobsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return savedJobs.size
    }

    fun getItem(position: Int): Pair<Job, String> {
        return savedJobs[position]
    }

    override fun onBindViewHolder(holder: AppliedJobsViewHolder, position: Int) {
        val (job, savedDate) = getItem(position)

        holder.wholeTitle.text = "${job.jobTitle} - ${job.companyName}"
        holder.appliedOn.text = "Applied on: $savedDate"

        Glide.with(context)
            .load(job.companyImage)
            .into(holder.savedImageView)

        holder.applyLinear.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(job.applyLink))
            context.startActivity(browserIntent)
        }
        holder.unSaveImageView.visibility = View.GONE


    }

}
