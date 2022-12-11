package com.health.clayful.ui.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.health.clayful.R
import com.health.clayful.base.BaseActivity
import com.health.clayful.databinding.ActivitySplashBinding
import com.health.clayful.network.ApiConstants
import com.health.clayful.services.NotificationModel
import com.health.clayful.ui.home.HomeActivity
import com.health.clayful.ui.login.LoginActivity
import com.health.clayful.utils.Constants
import com.health.clayful.utils.PrefData
import com.health.clayful.utils.startIntentActivity
import com.heapanalytics.android.Heap
import kotlinx.coroutines.*

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun createBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val content = findViewById<View>(android.R.id.content)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            content.viewTreeObserver.addOnDrawListener { false }
        }

        Heap.setTrackingEnabled(true)
        Heap.init(applicationContext,resources.getString(R.string.app_id))

        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()

        initializeCustomerIO()
        prefData = PrefData(this)
        changeTopBarColor(resources.getColor(R.color.app_bg_color))
        changeStatusBarIconColorToWhite(binding.root)
        showCutoutsOnNotch()

        lifecycleScope.launch(Dispatchers.Main) {
            delay(3000)
            getUserLoginStatus()
        }
    }

    private fun getUserLoginStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                // read data to open shared pref or not....
                prefData.getLoginStatus()
                prefData.loginStatusFlow.asLiveData().observe(this@SplashActivity) {
                    if(it) {
                        val bundle = Bundle()
                        readUserId()
                        readToken()
                        readJwtToken()

                        val mIntent = Intent(this@SplashActivity, HomeActivity::class.java)

                        if(intent?.data?.host?.equals("health") == true) {
                            bundle.putBoolean(Constants.HAS_NOTIFICATION, true)
                            mIntent.putExtra(NotificationModel.notification_bundle, bundle)
                            mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        else {
                            mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }


                        mIntent.putExtras(bundle)
                        startActivity(mIntent)
                        finish()

                    }
                    else {
                        ApiConstants.authToken = ""
                        startIntentActivity<LoginActivity<Any?>>(true)
                    }
                }
            }
        }
    }
}