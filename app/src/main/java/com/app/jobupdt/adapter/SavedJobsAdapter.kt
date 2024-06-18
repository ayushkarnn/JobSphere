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

class SavedJobsAdapter(
    private var savedJobs: List<Pair<Job, String>>,
    private val onUnSaveClickListener: OnUnSaveClickListener
): RecyclerView.Adapter<SavedJobsAdapter.SavedJobsViewHolder>() {

    private lateinit var context: Context

    class SavedJobsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val savedImageView: RoundedImageView = itemView.findViewById(R.id.savedImageView)
        val unSaveImageView: LinearLayout = itemView.findViewById(R.id.unSaveImageView)
        val applyLinear: LinearLayout = itemView.findViewById(R.id.applyLinear)
        val wholeTitle: TextView = itemView.findViewById(R.id.wholeTitle)
        val appliedOn: TextView = itemView.findViewById(R.id.appliedOn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedJobsViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.saved_jobs_layout, parent, false)
        return SavedJobsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return savedJobs.size
    }

    fun getItem(position: Int): Pair<Job, String> {
        return savedJobs[position]
    }

    override fun onBindViewHolder(holder: SavedJobsViewHolder, position: Int) {
        val (job, savedDate) = getItem(position)

        holder.wholeTitle.text = "${job.jobTitle} - ${job.companyName}"
        holder.appliedOn.text = "Saved on: $savedDate"

        Glide.with(context)
            .load(job.companyImage)
            .into(holder.savedImageView)

        holder.applyLinear.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(job.applyLink))
            context.startActivity(browserIntent)
        }

        holder.unSaveImageView.setOnClickListener {
            onUnSaveClickListener.onUnSaveClick(position)
        }
    }

    interface OnUnSaveClickListener {
        fun onUnSaveClick(position: Int)
    }
}
