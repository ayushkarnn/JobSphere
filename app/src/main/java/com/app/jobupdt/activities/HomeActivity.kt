package com.app.jobupdt.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.app.jobupdt.R
import com.app.jobupdt.databinding.ActivityHomeBinding
import com.app.jobupdt.fragments.AppliedFragment
import com.app.jobupdt.fragments.HomeFragment
import com.app.jobupdt.fragments.ProfileFragment
import com.app.jobupdt.fragments.SavedFragment
import com.app.jobupdt.utills.KeyboardVisibilityUtil
import com.google.firebase.installations.FirebaseInstallations
import me.ibrahimsn.lib.SmoothBottomBar

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var bottomBar: SmoothBottomBar
    private var lastClickTime: Long = 0
    private var DEBOUNCE_DELAY: Long = 500

    private var homeFragment: HomeFragment? = null
    private var savedFragment: SavedFragment? = null
    private var appliedFragment: AppliedFragment? = null
    private var profileFragment: ProfileFragment? = null

    private val keyboardVisibilityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isVisible = intent?.getBooleanExtra(KeyboardVisibilityUtil.KEYBOARD_VISIBLE, false) ?: false
            bottomBar.isVisible = !isVisible
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomBar = binding.bottomBar

        replaceFragment(HomeFragment())
        binding.bottomBar.setOnItemSelectedListener { i: Int ->
            when (i) {
                0 -> replaceFragment(HomeFragment())
                1 -> replaceFragment(SavedFragment())
                2 -> replaceFragment(AppliedFragment())
                3 -> replaceFragment(ProfileFragment())
            }
            true
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            keyboardVisibilityReceiver,
            IntentFilter(KeyboardVisibilityUtil.KEYBOARD_VISIBILITY_ACTION)
        )

        KeyboardVisibilityUtil.setupKeyboardVisibilityListener(this)

        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get the Firebase Installation ID
                val installationId = task.result
                // Log the Installation ID
                Log.d("FirebaseInstallationID", "Installation ID: $installationId")
            } else {
                Log.e("FirebaseInstallationID", "Unable to get Installation ID", task.exception)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(keyboardVisibilityReceiver)
    }

    fun addScrollListenerToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var scrollDist = 0
            private var isVisible = true

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (scrollDist > 20 && isVisible) {

                    bottomBar.isVisible = false
                    isVisible = false
                    scrollDist = 0
                } else if (scrollDist < -5 && !isVisible) {
                    bottomBar.isVisible = true
                    isVisible = true
                    scrollDist = 0
                }

                if ((isVisible && dy > 0) || (!isVisible && dy < 0)) {
                    scrollDist += dy
                }
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > DEBOUNCE_DELAY) {
            val fragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()

            when (fragment) {
                is HomeFragment -> {
                    if (homeFragment == null) {
                        homeFragment = HomeFragment()
                    }
                    transaction.replace(R.id.container, homeFragment!!)
                }
                is SavedFragment -> {
                    if (savedFragment == null) {
                        savedFragment = SavedFragment()
                    }
                    transaction.replace(R.id.container, savedFragment!!)
                }
                is AppliedFragment -> {
                    if (appliedFragment == null) {
                        appliedFragment = AppliedFragment()
                    }
                    transaction.replace(R.id.container, appliedFragment!!)
                }
                is ProfileFragment -> {
                    if (profileFragment == null) {
                        profileFragment = ProfileFragment()
                    }
                    transaction.replace(R.id.container, profileFragment!!)
                }
            }

            transaction.addToBackStack(null)
                .setReorderingAllowed(true)
                .commit()
        }
    }
}
