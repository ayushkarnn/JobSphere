package com.app.jobupdt.utills

sealed class Res<out T> {
    data class Success<out T>(val data: T) : Res<T>()
    data class Error(val message: String) : Res<Nothing>()
}