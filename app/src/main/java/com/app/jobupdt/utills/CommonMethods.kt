package com.app.jobupdt.utills

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommonMethods {

    companion object {

        fun closeKeyboard(context: Context) {
            val activity = context as? Activity
            if (activity != null) {
                val view = activity.currentFocus
                if (view != null) {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }

        fun getCurrentDate(): String {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val currentDate = Date()
            return dateFormat.format(currentDate)
        }

        fun showToast(context: Context,s: String) {
            Toast.makeText(context, s, Toast.LENGTH_SHORT).show()
        }

    }
}
