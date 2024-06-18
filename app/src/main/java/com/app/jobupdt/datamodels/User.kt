package com.app.jobupdt.datamodels

data class User(
    val name:String = "",
    val emailAddress:String = "",
    val isEmailVerified:Boolean = false,
    val role:String="",
    val fcmToken:String = "",
    val appliedJobs:List<String> = mutableListOf(),
    val savedJobs:List<String> = mutableListOf()
)
