package com.app.jobupdt.datamodels

data class Job(
    var uUIDJob: String = "",
    var companyName: String = "",
    var jobTitle: String = "",
    var jobLocation: String = "",
    var applyLink: String = "",
    var datePosted: String = "",
    var description: String = "",
    var eligibility: String = "",
    var salaryRange: String = "",
    var companyImage: String = "",
    var acceptingResponse: Boolean = false
) {
    constructor() : this("","", "", "", "", "", "", "", "", "", false)
}
