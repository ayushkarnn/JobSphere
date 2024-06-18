package com.app.jobupdt

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.app.jobupdt.activities.HomeActivity
import com.app.jobupdt.activities.LoginActivity
import com.app.jobupdt.adapter.OnboardingViewPagerAdapter2
import com.app.jobupdt.databinding.ActivityMainBinding
import com.app.jobupdt.utills.Constants
import com.app.jobupdt.utills.PreferenceManager
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var mViewPager: ViewPager2
    private lateinit var btnCreateAccount: Button
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)
        btnCreateAccount = binding.btnCreateAccount

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN) &&
            preferenceManager.getBoolean(Constants.KEY_IS_EMAIL_VERIFIED)) {
            navigateToHome()
        } else if (preferenceManager.getBoolean(Constants.IS_ON_BOARDING_COMPLETE)) {
            navigateToLogin()
        }

        btnCreateAccount.setOnClickListener {
            if (mViewPager.currentItem < mViewPager.adapter?.itemCount?.minus(1) ?: 0) {
                mViewPager.currentItem += 1
            } else {
                navigateToLogin()
            }
        }

        binding.textPolicyTerms.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ayushkarnn.tech/privacy-policy.html/"))
            startActivity(intent)
        }

        setupStatusBar()
        setupViewPager()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java),
            ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java),
            ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
        preferenceManager.putBoolean(Constants.IS_ON_BOARDING_COMPLETE, true)
        finish()
    }

    private fun setupStatusBar() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = resources.getColor(R.color.colorPrimary2, theme)
        }
    }

    private fun setupViewPager() {
        mViewPager = binding.viewPager
        mViewPager.adapter = OnboardingViewPagerAdapter2(this, this)
        TabLayoutMediator(binding.pageIndicator, mViewPager) { _, _ -> }.attach()
        mViewPager.offscreenPageLimit = 1

        mViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                btnCreateAccount.text = if (position == mViewPager.adapter?.itemCount?.minus(1)) "Finish" else "Next"
            }
        })
    }
}
