package com.health.clayful.ui.home

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.health.clayful.R
import com.health.clayful.base.BaseActivity
import com.health.clayful.databinding.ActivityHomeBinding
import com.health.clayful.model.UserModel
import com.health.clayful.network.ApiConstants
import com.health.clayful.services.NotificationModel
import com.health.clayful.ui.feedback.FeedbackActivity
import com.health.clayful.ui.login.LoginActivity
import com.health.clayful.utils.Constants
import com.health.clayful.utils.HomeViewModelFactory
import com.health.clayful.utils.PrefData
import com.health.clayful.viewModel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zendesk.android.Zendesk
import zendesk.android.messaging.Messaging
import zendesk.android.messaging.MessagingDelegate
import zendesk.android.messaging.UrlSource
import zendesk.messaging.android.DefaultMessagingFactory

class HomeActivity<T> : BaseActivity<ActivityHomeBinding>(), View.OnClickListener {

    private var exitCounter = 0
    private var hasNotification : Boolean = false
    private var homeViewModel : HomeViewModel?= null

    override fun createBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onNewIntent(intent)
        initHomeActivity(this as HomeActivity<ViewBinding>)
        prefData = PrefData(this)
        changeTopBarColor(resources.getColor(R.color.home_view_bg))
        changeStatusBarIconColorToBlack(binding.root)


        initiateViewModel()
        viewModelObserver()
        changeTopBarColor(resources.getColor(R.color.home_view_bg))
        Glide.with(this).load(R.drawable.loading_anim).into(binding.loadingAnim.loadingIv)

    }

    private fun initiateViewModel() {
        homeViewModel = ViewModelProviders.of(this, HomeViewModelFactory(this as HomeActivity<ViewBinding>)).get(HomeViewModel::class.java)
        if(userId != null) {

            Handler().postDelayed({
                try {
                    expandView(binding.myView,1200, binding.myBottomView.height, true)
                }
                catch (ex : Exception) {}
            },2000)

            homeViewModel?.getZendeskJwtToken(userId!!)
        }
        else {
            binding.loadingAnim.loadingRoot.hideView()
            binding.myView.hideView()
            homeViewModel?.zendeskLogin()
        }
    }

    override fun onBackPressed() {

        exitCounter++

        if (exitCounter < 2) {
            showHomeErrorSnackBar(getString(R.string.press_back_to_exit))
            Handler().postDelayed({
                exitCounter = 0
            }, 1000)
        } else if (exitCounter == 2) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val bundle = intent?.extras?.getBundle(NotificationModel.notification_bundle)

        if(bundle != null) {

            hasNotification = bundle.getBoolean(Constants.HAS_NOTIFICATION, false)

            if(hasNotification) {
                showChatScreen()
            }
            else {

                Zendesk.initialize(
                    context = applicationContext,
                    channelKey = resources.getString(R.string.zendesk_channel_key),
                    {
                        setViewListeners()
                    },
                    { error ->
                        setViewListeners()
                        Log.e("IntegrationApplication", "Initialization failed", error)
                    },
                    messagingFactory = DefaultMessagingFactory()
                )
            }
        }

        setViewListeners()

        Messaging.setDelegate(object : MessagingDelegate() {
            override fun shouldHandleUrl(url: String, urlSource: UrlSource): Boolean {
                if(url.equals(Constants.HTTPS_FEEDBACK_URL) or url.equals(Constants.HTTP_FEEDBACK_URL)) {
                    showFeedbackForm("https://n1tn06od1jc.typeform.com/to/vJGzPwIx#id=${ApiConstants.userId}")
                    return false
                }
                return true
            }
        })
    }

    private fun setViewListeners() {
        binding.startChatBtn.setOnClickListener(this)
        binding.startChatView.setOnClickListener(this)
        binding.logoutBtn.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {

            binding.startChatBtn.id, binding.startChatView.id -> {
                binding.notificationAlertImage.hideView()
                clearAllNotification()
                Zendesk.instance.messaging.showMessaging(this)
            }

            binding.logoutBtn.id -> {
                showLogoutConfirmationDialog()

            }
        }
    }

    /*override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(homeActivity).registerReceiver(broadCastReceiver, IntentFilter(Constants.BROADCAST))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(homeActivity).unregisterReceiver(broadCastReceiver)
    }*/


    private fun showChatScreen() {
        Zendesk.initialize(
            context = applicationContext,
            channelKey = resources.getString(R.string.zendesk_channel_key),
            {
                clearAllNotification()
                Zendesk.instance.messaging.showMessaging(this)
                setViewListeners()

            },
            { error ->
                setViewListeners()
                clearAllNotification()
                Log.e("IntegrationApplication", "Initialization failed", error)
            },
            messagingFactory = DefaultMessagingFactory()
        )
    }


    private fun showLogoutConfirmationDialog() {
        val dialog : Dialog = if(resources.getBoolean(R.bool.isTablet)) {
            Dialog(this,R.style.ThemeWithCornersTablet)
        } else {
            Dialog(this,R.style.ThemeWithCorners)
        }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.logout_confirm_popup)
        val okBtn = dialog.findViewById(R.id.logoutOkBtn) as MaterialButton
        val cancelBtn = dialog.findViewById(R.id.logoutCancelBtn) as MaterialButton

        cancelBtn.setOnClickListener(View.OnClickListener { dialog.dismiss() })
        okBtn.setOnClickListener(View.OnClickListener {
            clearAllNotification()
            saveLogoutDataToPref()
            lifecycleScope.launch {
                Zendesk.instance.logoutUser()
            }

            dialog.dismiss()
            val i = Intent(this, LoginActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        })
        dialog.show()
    }

    private fun saveJwtToken(jwtToken: String) {
        ApiConstants.jwtToken  = jwtToken
        CoroutineScope(Dispatchers.IO).launch {
            prefData.saveJwtToken(jwtToken)
        }
    }

    fun expandView(v: View, duration: Int, targetHeight: Int, showBounce : Boolean) {
        val prevHeight = v.height
        v.visibility = View.VISIBLE
        val valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight)
        valueAnimator.addUpdateListener { animation ->
            v.layoutParams.height = animation.animatedValue as Int
            v.requestLayout()
        }
        valueAnimator.interpolator = if(showBounce) OvershootInterpolator() else DecelerateInterpolator()
        valueAnimator.duration = duration.toLong()
        valueAnimator.start()
    }

    fun showFeedbackForm(url : String) {

        val mIntent = Intent(this, FeedbackActivity::class.java)
        val mBundle = Bundle()
        mBundle.putString(Constants.FEEDBACK_URL,url)
        mIntent.putExtras(mBundle)
        startActivity(mIntent)
    }

    private fun viewModelObserver() {

        homeViewModel?.jwtModel?.observe(this, Observer {
            saveJwtToken(it.token.toString())

            homeViewModel?.zendeskLogin()

            Handler().postDelayed({

                expandView(binding.myView,400, binding.myBottomView2.height + binding.myBottomView.height, false)

                val animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                binding.myView.startAnimation(animation)
                binding.loadingAnim.loadingRoot.startAnimation(animation)

                Handler().postDelayed({
                    binding.myView.hideView()
                    binding.loadingAnim.loadingRoot.hideView()
                }, 400)

            },2500)
        })
    }

}