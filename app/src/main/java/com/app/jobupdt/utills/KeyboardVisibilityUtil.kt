package com.app.jobupdt.utills
import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import androidx.localbroadcastmanager.content.LocalBroadcastManager

object KeyboardVisibilityUtil {

    const val KEYBOARD_VISIBILITY_ACTION = "com.app.jobupdt.KEYBOARD_VISIBILITY_ACTION"
    const val KEYBOARD_VISIBLE = "KEYBOARD_VISIBLE"

    fun setupKeyboardVisibilityListener(activity: Activity) {
        val contentView = activity.findViewById<View>(android.R.id.content)
        contentView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private var isKeyboardVisible = false

            override fun onGlobalLayout() {
                val rect = Rect()
                contentView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = contentView.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                val isVisible = keypadHeight > screenHeight * 0.15

                if (isVisible != isKeyboardVisible) {
                    isKeyboardVisible = isVisible
                    val intent = Intent(KEYBOARD_VISIBILITY_ACTION)
                    intent.putExtra(KEYBOARD_VISIBLE, isVisible)
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(intent)
                }
            }
        })
    }
}
