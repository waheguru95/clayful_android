package com.health.clayful.ui.passwordResetSuccess

import android.content.Intent
import android.os.Bundle
import com.health.clayful.R
import com.health.clayful.base.BaseActivity
import com.health.clayful.databinding.ActivityResetPasswordSuccessBinding
import com.health.clayful.ui.home.HomeActivity
import com.health.clayful.ui.login.LoginActivity
import com.health.clayful.utils.startIntentActivity

class ResetPasswordSuccessActivity<T> : BaseActivity<ActivityResetPasswordSuccessBinding>() {

    override fun createBinding(): ActivityResetPasswordSuccessBinding {
        return ActivityResetPasswordSuccessBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        changeTopBarColor(resources.getColor(R.color.white))
        changeStatusBarIconColorToBlack(binding.root)

        binding.backToLogin.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val i = Intent(this, LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

}