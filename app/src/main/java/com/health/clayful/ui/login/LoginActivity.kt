package com.health.clayful.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewbinding.ViewBinding
import com.health.clayful.BuildConfig
import com.health.clayful.R
import com.health.clayful.base.BaseActivity
import com.health.clayful.databinding.ActivityLoginBinding
import com.health.clayful.model.UserModel
import com.health.clayful.network.ApiConstants
import com.health.clayful.ui.home.HomeActivity
import com.health.clayful.utils.Constants
import com.health.clayful.utils.PrefData
import com.health.clayful.utils.ViewModelFactory
import com.health.clayful.viewModel.AuthViewModel
import com.heapanalytics.android.Heap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class LoginActivity<T>  : BaseActivity<ActivityLoginBinding>(), TextWatcher {

    private var authViewModel : AuthViewModel ?= null
    private lateinit var splashScreen: SplashScreen

    override fun createBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        initiateViewModel()
        viewModelObserver()

        initiateShakeAnimation()
        prefData = PrefData(this)
        changeTopBarColor(resources.getColor(R.color.white))
        changeStatusBarIconColorToBlack(binding.root)

        handleNewIntent(intent)

        binding.emailEt.onFocusChangeListener = MyFocusChangeListener(binding.emailEt)
        binding.passwordEt.onFocusChangeListener = MyFocusChangeListener(binding.passwordEt)
        binding.emailEt.addTextChangedListener(this)
        binding.passwordEt.addTextChangedListener(this)

    }

    private fun viewModelObserver() {

        authViewModel?.isProgressShowing?.observe(this, Observer {
            if(it) {
                binding.loginProgressBtn?.showView()
            }
            else {
                binding.loginProgressBtn?.hideView()
            }
        })

        authViewModel?.fieldError?.observe(this, Observer { errorType ->
            if(errorType != null) {
                when (errorType) {
                    Constants.EMAIL_OR_PASSWORD_EMPTY -> {
                        binding.emailFieldCantBeEmpty.showView()
                        binding.passwordFieldCantBeEmpty.showView()
                        binding.emailEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.passwordEt.background = resources.getDrawable(R.drawable.field_error_bg)

                        binding.emailEt.startAnimation(shake)
                        binding.passwordEt.startAnimation(shake)
                    }
                    Constants.EMAIL_EMPTY_FIELD_ERROR -> {
                        binding.emailFieldCantBeEmpty.showView()
                        binding.emailEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.emailEt.startAnimation(shake)
                    }
                    Constants.PASSWORD_EMPTY_FIELD_ERROR -> {
                        binding.passwordFieldCantBeEmpty.showView()
                        binding.passwordEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.passwordEt.startAnimation(shake)
                    }
                    Constants.INVALID_EMAIL_ERROR -> {
                        binding.emailFieldCantBeEmpty.text = getString(R.string.invalid_email)
                        binding.emailFieldCantBeEmpty.showView()
                        binding.emailEt.background = resources.getDrawable(R.drawable.field_error_bg)
                        binding.emailEt.startAnimation(shake)
                    }
                }
                authViewModel?.fieldError?.value = null
            }
        })

        authViewModel?.userModel?.observe(this, Observer {
            if(it != null) {
                if(it.data?.member?.customFields?.userId == null) {
                    showAlertDialog("User id is missing.")
                }
                else {
                    userId = it.data?.member?.customFields?.userId
                    saveDataToPref(it)
                    authViewModel?.identifyPerson(it.data?.member?.customFields?.userId)
                    initializeUserOnHeap(it)

                    val i = Intent(this, HomeActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    this.startActivity(i)
                }
                authViewModel?.userModel?.value = null
            }
        })
    }

    private fun initializeUserOnHeap(it: UserModel) {
        try {

            val userProperties : HashMap<String, String> = HashMap()
            userProperties["Name"] = it.data?.member?.auth?.email.toString()
            userProperties["Email"] = it.data?.member?.auth?.email.toString()
            userProperties["AccountID"] = it.data?.member?.customFields?.accountId.toString()
            userProperties["UserID"] = it.data?.member?.customFields?.userId.toString()
            userProperties["AccountName"] = it.data?.member?.customFields?.accountName.toString()
            userProperties["ParentAccountName"] = it.data?.member?.customFields?.parentAccountName.toString()
            userProperties["AppName"] = "Clayful"

            Heap.identify(it.data?.member?.customFields?.userId.toString())
            Heap.addUserProperties(userProperties)

        }
        catch (ex : Exception){
            Log.e("heapError", "${ex.message}")
        }
    }

    private fun initiateViewModel() {
        authViewModel = ViewModelProviders.of(this, ViewModelFactory(this as LoginActivity<ViewBinding>)).get(AuthViewModel::class.java)
        binding.model = authViewModel
    }

    inner class MyFocusChangeListener(private val editText: AppCompatEditText): View.OnFocusChangeListener {
        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (editText.id == binding.emailEt.id && hasFocus ) {
                if(!binding.emailFieldCantBeEmpty.isVisible) {
                    binding.emailEt.background = resources.getDrawable(R.drawable.edit_text_on_focus_bg)
                }
                if( !binding.passwordFieldCantBeEmpty.isVisible) {
                    binding.passwordEt.background = resources.getDrawable(R.drawable.edit_text_bg)
                }
            } else if (editText.id == binding.passwordEt.id && hasFocus) {

                Handler().postDelayed({
                    binding.scollView.smoothScrollTo(0,400)
                },400)

                if(!binding.passwordFieldCantBeEmpty.isVisible) {
                    binding.passwordEt.background = resources.getDrawable(R.drawable.edit_text_on_focus_bg)
                }
                if(!binding.emailFieldCantBeEmpty.isVisible) {
                    binding.emailEt.background = resources.getDrawable(R.drawable.edit_text_bg)
                }
            }
        }
    }

    override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(p0: Editable) {
        if(binding.emailEt.hasFocus()) {
            binding.emailEt.background = resources.getDrawable(R.drawable.edit_text_on_focus_bg)
            binding.emailFieldCantBeEmpty.hideView()
        }
        else if(binding.passwordEt.hasFocus()) {
            binding.passwordEt.background = resources.getDrawable(R.drawable.edit_text_on_focus_bg)
            binding.passwordFieldCantBeEmpty.hideView()
        }
    }

    private fun handleNewIntent(intent: Intent?) {
        val message = intent?.extras?.getString("dialog_message")
        val hasError = intent?.extras?.getBoolean("has_error")

        if(hasError != null && hasError) {
            showAlertDialog(message.toString())
        }

        //LoginFragment().changeFragment(binding.authContainer.id,this, false)
    }

    private fun saveDataToPref(model: UserModel) {
        CoroutineScope(Dispatchers.IO).launch {
            ApiConstants.authToken = model.data?.tokens?.accessToken.toString()
            ApiConstants.userId = model.data?.member?.customFields?.userId.toString()
            prefData.saveToken(model.data?.tokens?.accessToken.toString())
            prefData.saveUserId(model.data?.member?.customFields?.userId.toString())
            prefData.saveUserLoginStatus(true)
            prefData.saveUserLoginData(model)
        }
    }

    /*private fun useCustomExitAnimation() {
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashScreenView = splashScreenViewProvider.view
            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView,
                View.TRANSLATION_Y,
                0f,
                -splashScreenView.height.toFloat(),
            )
            slideUp.interpolator = BounceInterpolator()
            slideUp.duration = 1000L
            slideUp.doOnEnd {
                splashScreenViewProvider.remove()
            }
            slideUp.start()
        }
    }

    *//**
     * Keep splash screen on-screen indefinitely. This is useful if you're using a custom Activity
     * for routing.
     *//*
    private fun keepSplashScreenIndefinitely() {
        splashScreen.setKeepOnScreenCondition { true }
    }

    *//**
     * Keep splash screen on-screen for longer period. This is useful if you need to load data when
     * splash screen is appearing.
     *//*
    private fun keepSplashScreenFor5Seconds() {
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                Thread.sleep(5000)
                content.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }
*/

}